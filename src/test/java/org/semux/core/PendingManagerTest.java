/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.core;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.semux.KernelMock;
import org.semux.core.state.AccountState;
import org.semux.crypto.EdDSA;
import org.semux.net.ChannelManager;
import org.semux.rules.TemporaryDBRule;
import org.semux.util.ArrayUtil;
import org.semux.util.Bytes;

public class PendingManagerTest {

    private static KernelMock kernel;
    private static PendingManager pendingMgr;

    private static AccountState accountState;

    private static EdDSA key = new EdDSA();
    private static TransactionType type = TransactionType.TRANSFER;
    private static byte[] from = key.toAddress();
    private static byte[] to = new EdDSA().toAddress();
    private static long value = 1 * Unit.MILLI_SEM;
    private static long fee;

    @ClassRule
    public static TemporaryDBRule temporaryDBFactory = new TemporaryDBRule();

    @BeforeClass
    public static void setup() {
        kernel = new KernelMock();

        kernel.setBlockchain(new BlockchainImpl(kernel.getConfig(), temporaryDBFactory));
        kernel.setChannelManager(new ChannelManager(kernel));

        accountState = kernel.getBlockchain().getAccountState();
        accountState.adjustAvailable(from, 10000 * Unit.SEM);

        fee = kernel.getConfig().minTransactionFee();
    }

    @Before
    public void start() {
        pendingMgr = new PendingManager(kernel);
        pendingMgr.start();
    }

    @Test
    public void testGetTransaction() throws InterruptedException {
        long now = System.currentTimeMillis();
        long nonce = accountState.getAccount(from).getNonce();

        Transaction tx = new Transaction(type, to, value, fee, nonce, now, Bytes.EMPTY_BYTES).sign(key);
        pendingMgr.addTransaction(tx);

        Thread.sleep(100);
        assertEquals(1, pendingMgr.getTransactions().size());
    }

    @Test
    public void testAddTransaction() throws InterruptedException {
        long now = System.currentTimeMillis();
        long nonce = accountState.getAccount(from).getNonce();

        Transaction tx = new Transaction(type, to, value, fee, nonce, now, Bytes.EMPTY_BYTES).sign(key);
        pendingMgr.addTransaction(tx);
        Transaction tx2 = new Transaction(type, to, value, fee, nonce + 128, now, Bytes.EMPTY_BYTES).sign(key);
        pendingMgr.addTransaction(tx2);

        Thread.sleep(100);
        assertEquals(1, pendingMgr.getTransactions().size());
    }

    /**
     * Pending transactions should be sorted by:
     * <ul>
     * <li>fee in in descending order</li>
     * <li>timestamp in in ascending order</li>
     * </ul>
     */
    @Test
    public void testSortedTransactions() {
        long now = System.currentTimeMillis();
        long nonce = accountState.getAccount(from).getNonce();

        List<Transaction> txs = Arrays.asList(
                new Transaction(type, to, value, fee + 3, nonce, now, Bytes.of("3")).sign(key),
                new Transaction(type, to, value, fee + 4, nonce + 1, now + 1, Bytes.of("4_1")).sign(key),
                new Transaction(type, to, value, fee + 4, nonce + 2, now, Bytes.of("4")).sign(key),
                new Transaction(type, to, value, fee + 5, nonce + 3, now, Bytes.of("5")).sign(key),
                new Transaction(type, to, value, fee + 2, nonce + 4, now, Bytes.of("2")).sign(key),
                new Transaction(type, to, value, fee + 1, nonce + 5, now, Bytes.of("1")).sign(key));
        for (Transaction tx : txs) {
            pendingMgr.addTransaction(tx);
        }

        await().until(() -> pendingMgr.getTransactions().size() == 6);

        List<Transaction> sortedTxs = pendingMgr.getTransactions();
        assertEquals("5", new String(sortedTxs.get(0).getData()));
        assertEquals("4", new String(sortedTxs.get(1).getData()));
        assertEquals("4_1", new String(sortedTxs.get(2).getData()));
        assertEquals("3", new String(sortedTxs.get(3).getData()));
        assertEquals("2", new String(sortedTxs.get(4).getData()));
        assertEquals("1", new String(sortedTxs.get(5).getData()));
    }

    @Test
    public void testNonceJump() throws InterruptedException {
        long now = System.currentTimeMillis();
        long nonce = accountState.getAccount(from).getNonce();

        Transaction tx3 = new Transaction(type, to, value, fee, nonce + 2, now, Bytes.EMPTY_BYTES).sign(key);
        pendingMgr.addTransaction(tx3);
        Transaction tx2 = new Transaction(type, to, value, fee, nonce + 1, now, Bytes.EMPTY_BYTES).sign(key);
        pendingMgr.addTransaction(tx2);

        Thread.sleep(100);
        assertEquals(0, pendingMgr.getTransactions().size());

        Transaction tx = new Transaction(type, to, value, fee, nonce, now, Bytes.EMPTY_BYTES).sign(key);
        pendingMgr.addTransaction(tx);

        Thread.sleep(100);
        assertEquals(3, pendingMgr.getTransactions().size());
    }

    @Test
    public void testHighVolumeTransaction() throws InterruptedException {
        long now = System.currentTimeMillis();
        long nonce = accountState.getAccount(from).getNonce();

        int[] perm = ArrayUtil.permutation(5000);
        for (int p : perm) {
            Transaction tx = new Transaction(type, to, value, fee, nonce + p, now, Bytes.EMPTY_BYTES).sign(key);
            pendingMgr.addTransaction(tx);
        }

        Thread.sleep(8000);
        assertEquals(perm.length, pendingMgr.getTransactions().size());
    }

    @Test
    public void testNewBlock() throws InterruptedException {
        long now = System.currentTimeMillis();
        long nonce = accountState.getAccount(from).getNonce();

        Transaction tx = new Transaction(type, to, value, fee, nonce, now, Bytes.EMPTY_BYTES).sign(key);
        pendingMgr.addTransaction(tx);
        Transaction tx2 = new Transaction(type, to, value, fee, nonce + 1, now, Bytes.EMPTY_BYTES).sign(key);
        // pendingMgr.addTransaction(tx3);

        Thread.sleep(100);
        assertEquals(1, pendingMgr.getTransactions().size());

        long number = 1;
        byte[] coinbase = Bytes.random(20);
        byte[] prevHash = Bytes.random(20);
        long timestamp = System.currentTimeMillis();
        byte[] transactionsRoot = Bytes.random(32);
        byte[] resultsRoot = Bytes.random(32);
        byte[] stateRoot = Bytes.random(32);
        byte[] data = {};
        List<Transaction> transactions = Arrays.asList(tx, tx2);
        List<TransactionResult> results = Arrays.asList(new TransactionResult(true), new TransactionResult(true));
        BlockHeader header = new BlockHeader(number, coinbase, prevHash, timestamp, transactionsRoot, resultsRoot,
                stateRoot, data);
        Block block = new Block(header, transactions, results);
        kernel.getBlockchain().getAccountState().increaseNonce(from);
        kernel.getBlockchain().getAccountState().increaseNonce(from);
        pendingMgr.onBlockAdded(block);

        Transaction tx3 = new Transaction(type, to, value, fee, nonce + 2, now, Bytes.EMPTY_BYTES).sign(key);
        pendingMgr.addTransaction(tx3);

        Thread.sleep(100);
        assertArrayEquals(tx3.getHash(), pendingMgr.getTransactions().get(0).getHash());
    }

    @After
    public void stop() {
        pendingMgr.stop();
    }
}
