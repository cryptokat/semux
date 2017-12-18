/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.bench;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.semux.config.Config;
import org.semux.config.Constants;
import org.semux.config.DevNetConfig;
import org.semux.core.Block;
import org.semux.core.BlockHeader;
import org.semux.core.BlockchainImpl;
import org.semux.core.Transaction;
import org.semux.core.TransactionResult;
import org.semux.core.TransactionType;
import org.semux.crypto.EdDSA;
import org.semux.db.DBFactory;
import org.semux.rules.TemporaryDBRule;
import org.semux.util.Bytes;
import org.semux.util.MerkleUtil;
import org.semux.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockchainPerformance {

    private static final Logger logger = LoggerFactory.getLogger(BlockchainPerformance.class);

    private static final byte[] coinbase = Bytes.random(30);
    private static final byte[] prevHash = Bytes.random(32);
    private static final EdDSA key = new EdDSA();
    private static final long value = 20;
    private static final long fee = 1;
    private static final long nonce = 0;
    private static final byte[] data = Bytes.of("test");
    private static final long timestamp = System.currentTimeMillis() - 60 * 1000;

    /**
     * The benchmark tries to create a block filled with single-recipient TRANSFER transactions
     */
    private static void testLargeBlockSingleRecipient(DBFactory dbFactory) {
        logger.info("Binary-searching the maximum number of single-recipient transactions per block...");

        Instant begin = Instant.now();

        Config config = new DevNetConfig(Constants.DEFAULT_DATA_DIR);
        BlockchainImpl blockchain = new BlockchainImpl(config, dbFactory);

        int low = 1, high = config.maxBlockSize() / EdDSA.ADDRESS_LEN, numberOfTxs = (low + high) / 2;
        Block block = makeSingleRecipientBlock(numberOfTxs);
        while(high - low > 1) {
            System.out.format("low = %d, mid = %d, high = %d\n", low, numberOfTxs, high);

            if (block.size() > config.maxBlockSize()) {
                high = numberOfTxs;
            } else {
                low = numberOfTxs;
            }
            numberOfTxs = (low + high) / 2;
            block = makeSingleRecipientBlock(numberOfTxs);
        }

        blockchain.addBlock(block);
        Duration duration = Duration.between(begin, Instant.now());

        logger.info("Single-Recipient Block Size = {} bytes, {} txs, took {}\n", block.size(), numberOfTxs, TimeUtil.formatDuration(duration));
    }

    private static Block makeSingleRecipientBlock(int numberOfTxs) {
        List<Transaction> txs = new ArrayList<>();
        List<TransactionResult> results = new ArrayList<>();

        for (int i = 0;i < numberOfTxs;i++) {
            txs.add(new Transaction(TransactionType.TRANSFER, Bytes.random(EdDSA.ADDRESS_LEN), value, fee,
                    nonce + numberOfTxs, timestamp, data).sign(key));
            results.add(new TransactionResult(true));
        }

        Block block = new Block(
                new BlockHeader(1, coinbase, prevHash, timestamp, MerkleUtil.computeTransactionsRoot(txs),
                        MerkleUtil.computeResultsRoot(results), Bytes.EMPTY_HASH, Bytes.EMPTY_BYTES),
                txs, results);
        return block;
    }

    /**
     * The benchmark tries to create a block filled with multi-recipient TRANSFER transactions
     */
    private static void testLargeBlockMultiRecipients(DBFactory dbFactory) {
        logger.info("Binary-searching the maximum number of recipients per block...");

        Instant begin = Instant.now();

        Config config = new DevNetConfig(Constants.DEFAULT_DATA_DIR);
        BlockchainImpl blockchain = new BlockchainImpl(config, dbFactory);

        int low = 1, high = config.maxBlockSize() / EdDSA.ADDRESS_LEN, numberOfRecipients = (low + high) / 2;
        Block block = makeMultiRecipientBlock(numberOfRecipients);
        while(high - low > 1) {
            System.out.format("low = %d, mid = %d, high = %d\n", low, numberOfRecipients, high);

            if (block.size() > config.maxBlockSize()) {
                high = numberOfRecipients;
            } else {
                low = numberOfRecipients;
            }
            numberOfRecipients = (low + high) / 2;
            block = makeMultiRecipientBlock(numberOfRecipients);
        }

        blockchain.addBlock(block);
        Duration duration = Duration.between(begin, Instant.now());

        logger.info("Multi-Recipient Block Size = {} bytes, {} recipients, took {}\n", block.size(), numberOfRecipients, TimeUtil.formatDuration(duration));
    }

    private static Block makeMultiRecipientBlock(int numberOfRecipients) {
        Transaction tx = new Transaction(TransactionType.TRANSFER, Bytes.random(numberOfRecipients * EdDSA.ADDRESS_LEN), value, fee,
                nonce + numberOfRecipients, timestamp, data).sign(key);
        List<TransactionResult> results = Arrays.asList(new TransactionResult(true));
        List<Transaction> txs = Arrays.asList(tx);
        Block block = new Block(
                new BlockHeader(1, coinbase, prevHash, timestamp, MerkleUtil.computeTransactionsRoot(txs),
                        MerkleUtil.computeResultsRoot(results), Bytes.EMPTY_HASH, Bytes.EMPTY_BYTES),
                txs, results);
        return block;
    }

    public static void main(String[] args) throws Throwable {
        TemporaryDBRule temporaryDBFactory = new TemporaryDBRule();
        temporaryDBFactory.before();
        testLargeBlockSingleRecipient(temporaryDBFactory);
        temporaryDBFactory.after();

        temporaryDBFactory = new TemporaryDBRule();
        temporaryDBFactory.before();
        testLargeBlockMultiRecipients(temporaryDBFactory);
        temporaryDBFactory.after();
    }
}
