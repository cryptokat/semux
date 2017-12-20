/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.map.LRUMap;
import org.semux.Kernel;
import org.semux.config.Config;
import org.semux.core.state.AccountState;
import org.semux.core.state.DelegateState;
import org.semux.net.Channel;
import org.semux.net.ChannelManager;
import org.semux.net.msg.p2p.TransactionMessage;
import org.semux.util.ArrayUtil;
import org.semux.util.ByteArray;
import org.semux.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pending manager maintains all unconfirmed transactions, either from kernel or
 * network. All transactions are evaluated and propagated to peers if success.
 * 
 * TODO: sort transaction queue by fee, and other metrics
 *
 */
public class PendingManager implements Runnable, BlockchainListener {

    private static final Logger logger = LoggerFactory.getLogger(PendingManager.class);

    private static final ThreadFactory factory = new ThreadFactory() {

        private AtomicInteger cnt = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "pending-" + cnt.getAndIncrement());
        }
    };

    private static final int QUEUE_MAX_SIZE = 64 * 1024;
    private static final int POOL_MAX_SIZE = 8 * 1024;
    private static final int DELAYED_MAX_SIZE = 16 * 1024;
    private static final int PROCESSED_MAX_SIZE = 16 * 1024;

    private Config config;

    private Blockchain chain;
    private ChannelManager channelMgr;
    private AccountState pendingAS;
    private DelegateState pendingDS;

    /**
     * Transaction queue.
     */
    private LinkedList<Transaction> queue = new LinkedList<>();

    /**
     * Transaction pool.
     */
    private Map<ByteArray, PendingTransaction> pool = new HashMap<>();
    private List<PendingTransaction> transactions = new ArrayList<>();

    // NOTE: make sure access to the LRUMap<> are synchronized.
    private Map<ByteArray, Transaction> delayed = new LRUMap<>(DELAYED_MAX_SIZE);
    private Map<ByteArray, ?> processed = new LRUMap<>(PROCESSED_MAX_SIZE);

    private ScheduledExecutorService exec;
    private ScheduledFuture<?> validateFuture;

    private volatile boolean isRunning;

    /**
     * Creates a pending manager.
     */
    public PendingManager(Kernel kernel) {
        this.config = kernel.getConfig();

        this.chain = kernel.getBlockchain();
        this.channelMgr = kernel.getChannelManager();

        this.pendingAS = chain.getAccountState().track();
        this.pendingDS = chain.getDelegateState().track();

        this.exec = Executors.newSingleThreadScheduledExecutor(factory);
    }

    /**
     * Starts this pending manager.
     */
    public synchronized void start() {
        if (!isRunning) {
            /*
             * NOTE: a rate smaller than the message queue sending rate should be used to
             * prevent message queues from hitting the NET_MAX_QUEUE_SIZE, especially when
             * the network load is heavy.
             */
            this.validateFuture = exec.scheduleAtFixedRate(this, 2, 2, TimeUnit.MILLISECONDS);

            this.chain.addListener(this);

            logger.debug("Pending manager started");
            this.isRunning = true;
        }
    }

    /**
     * Shuts down this pending manager.
     */
    public synchronized void stop() {
        if (isRunning) {
            validateFuture.cancel(true);

            logger.debug("Pending manager stopped");
            isRunning = false;
        }
    }

    /**
     * Returns whether the pending manager is running or not.
     * 
     * @return
     */
    public synchronized boolean isRunning() {
        return isRunning;
    }

    /**
     * Returns a copy of the queue, for test purpose only.
     * 
     * @return
     */
    public synchronized List<Transaction> getQueue() {
        return new ArrayList<>(queue);
    }

    /**
     * Adds a transaction to the queue, which will be validated later by the
     * background worker.
     * 
     * @param tx
     */
    public synchronized void addTransaction(Transaction tx) {
        if (queue.size() < QUEUE_MAX_SIZE) {
            queue.add(tx);
        }
    }

    /**
     * Adds a transaction to the pool.
     * 
     * @param tx
     *            a ${@link Transaction} object to be added
     * @return a ${@link ProcessTransactionResult} object
     */
    public synchronized ProcessTransactionResult addTransactionSync(Transaction tx) {
        return tx.validate() ? //
                processTransaction(tx, true) : //
                new ProcessTransactionResult(0, TransactionResult.Error.INVALID_FORMAT);
    }

    /**
     * Returns the nonce of an account based on the pending state.
     * 
     * @param address
     * @return
     */
    public synchronized long getNonce(byte[] address) {
        return pendingAS.getAccount(address).getNonce();
    }

    /**
     * Returns pending transactions and corresponding results.
     * 
     * @param limit
     * @return
     */
    public synchronized List<PendingTransaction> getPendingTransactions(int limit) {
        List<PendingTransaction> txs = new ArrayList<>();

        if (limit == -1) {
            // returns all transactions if there is no limit
            txs.addAll(transactions);
        } else {
            Iterator<PendingTransaction> it = transactions.iterator();

            int size = 0;
            while (it.hasNext()) {
                PendingTransaction tx = it.next();

                if (size + tx.transaction.size() > limit) {
                    break;
                } else {
                    txs.add(tx);
                }
            }
        }

        return txs;
    }

    /**
     * Returns a limited number of transactions in the pool.
     * 
     * @param limit
     * @return
     */
    public synchronized List<PendingTransaction> getTransactions(int limit) {
        return getPendingTransactions(limit);
    }

    /**
     * Returns all transactions in the pool.
     * 
     * @return
     */
    public synchronized List<PendingTransaction> getTransactions() {
        return getPendingTransactions(-1);
    }

    /**
     * Clear all pending transactions
     * 
     * @return
     */
    public synchronized List<PendingTransaction> clear() {
        // reset state
        pendingAS = chain.getAccountState().track();
        pendingDS = chain.getDelegateState().track();

        // clear transaction pool
        List<PendingTransaction> txs = new ArrayList<>(transactions);
        pool.clear();
        transactions.clear();

        return txs;
    }

    @Override
    public synchronized void onBlockAdded(Block block) {
        if (isRunning) {
            long t1 = System.currentTimeMillis();

            // clear transaction pool
            List<PendingTransaction> txs = clear();

            // update pending state
            long accepted = 0;
            for (PendingTransaction tx : txs) {
                accepted += processTransaction(tx.transaction, false).accepted;
            }

            long t2 = System.currentTimeMillis();
            logger.debug("Pending tx evaluation: # txs = {} / {},  time =  {} ms", accepted, txs.size(), t2 - t1);
        }
    }

    @Override
    public synchronized void run() {
        Transaction tx;

        while (pool.size() < POOL_MAX_SIZE //
                && (tx = queue.poll()) != null //
                && tx.getFee() >= config.minTransactionFee()) {
            // filter by cache
            ByteArray key = ByteArray.of(tx.getHash());
            if (processed.containsKey(key)) {
                continue;
            }

            if (tx.validate() && processTransaction(tx, true).accepted >= 1) {
                // exit after one success transaction
                return;
            }

            processed.put(key, null);
        }
    }

    /**
     * Validates the given transaction and add to pool if success.
     * 
     * @param tx
     *            transaction
     * @param relay
     *            whether to relay the transaction if success
     * @return the number of success transactions processed and the error that
     *         interrupted the process
     */
    protected ProcessTransactionResult processTransaction(Transaction tx, boolean relay) {

        // NOTE: assume transaction format is valid

        int cnt = 0;
        while (tx != null && tx.getNonce() == getNonce(tx.getFrom())) {

            // check transaction timestamp
            long now = System.currentTimeMillis();
            long twoHours = TimeUnit.HOURS.toMillis(2);
            if (tx.getTimestamp() < now - twoHours || tx.getTimestamp() > now + twoHours) {
                return new ProcessTransactionResult(cnt, TransactionResult.Error.INVALID_TIMESTAMP);
            }

            // execute transactions
            AccountState as = pendingAS.track();
            DelegateState ds = pendingDS.track();
            TransactionResult result = new TransactionExecutor(config).execute(tx, as, ds);

            if (result.isSuccess()) {
                // commit state updates
                as.commit();
                ds.commit();

                // add transaction to pool
                PendingTransaction pendingTransaction = new PendingTransaction(tx, result);
                transactions.add(pendingTransaction);
                pool.put(createKey(tx), pendingTransaction);
                cnt++;

                // relay transaction
                if (relay) {
                    List<Channel> channels = channelMgr.getActiveChannels();
                    TransactionMessage msg = new TransactionMessage(tx);
                    int[] indices = ArrayUtil.permutation(channels.size());
                    for (int i = 0; i < indices.length && i < config.netRelayRedundancy(); i++) {
                        Channel c = channels.get(indices[i]);
                        if (c.isActive()) {
                            c.getMessageQueue().sendMessage(msg);
                        }
                    }
                }
            } else {
                // exit immediately if invalid
                return new ProcessTransactionResult(cnt, result.getError());
            }

            tx = delayed.get(createKey(tx.getFrom(), getNonce(tx.getFrom())));
        }

        // add to cache
        if (tx != null && tx.getNonce() > getNonce(tx.getFrom())) {
            delayed.put(createKey(tx), tx);
        }

        return new ProcessTransactionResult(cnt);
    }

    private ByteArray createKey(Transaction tx) {
        return ByteArray.of(Bytes.merge(tx.getFrom(), Bytes.of(tx.getNonce())));
    }

    private ByteArray createKey(byte[] acc, long nonce) {
        return ByteArray.of(Bytes.merge(acc, Bytes.of(nonce)));
    }

    /**
     * This object represents a transaction and its execution result against a
     * snapshot of local state that is not yet confirmed by the network.
     */
    public static class PendingTransaction {

        public final Transaction transaction;

        public final TransactionResult transactionResult;

        private PendingTransaction(Transaction transaction, TransactionResult transactionResult) {
            this.transaction = transaction;
            this.transactionResult = transactionResult;
        }
    }

    /**
     * This object represents the number of accepted transactions and the cause of
     * rejection by ${@link PendingManager}.
     */
    public static class ProcessTransactionResult {

        public final int accepted;

        public final TransactionResult.Error error;

        public ProcessTransactionResult(int accepted, TransactionResult.Error error) {
            this.accepted = accepted;
            this.error = error;
        }

        public ProcessTransactionResult(int accepted) {
            this.accepted = accepted;
            this.error = null;
        }
    }
}
