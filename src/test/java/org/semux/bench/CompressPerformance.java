/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.bench;

import java.io.IOException;
import java.util.ArrayList;

import org.semux.config.Config;
import org.semux.config.Constants;
import org.semux.config.MainnetConfig;
import org.semux.core.Block;
import org.semux.core.Blockchain;
import org.semux.core.BlockchainImpl;
import org.semux.db.LevelDb.LevelDbFactory;
import org.semux.net.msg.consensus.BlocksMessage;
import org.xerial.snappy.Snappy;

public class CompressPerformance {

    enum Mode {
        ALL_BLOCKS, BLOCKS_WITH_TX
    };

    public static void main(String[] args) throws IOException {
        Config config = new MainnetConfig(Constants.DEFAULT_DATA_DIR);

        LevelDbFactory dbFactory = new LevelDbFactory(config.dataDir());
        Blockchain chain = new BlockchainImpl(config, dbFactory);

        for (Mode mode : Mode.values()) {
            int blocks = 0;
            int transactions = 0;
            int size = 0;
            int sizeCompressed = 0;
            long time = System.nanoTime();
            ArrayList<Block> blockList = new ArrayList<>();
            for (int i = 1; i <= chain.getLatestBlockNumber() && blocks < 64; i++) {
                Block b = chain.getBlock(i);
                if (mode == Mode.BLOCKS_WITH_TX && b.getTransactions().isEmpty()) {
                    continue;
                }

                blockList.add(b);
                blocks++;
                transactions += b.getTransactions().size();
            }
            time = System.nanoTime() - time;

            BlocksMessage blocksMessage = new BlocksMessage(blockList);
            size = blocksMessage.getEncoded().length;
            sizeCompressed = Snappy.compress(blocksMessage.getEncoded()).length;

            System.out.println("======================================");
            System.out.println(mode);
            System.out.println("======================================");
            System.out.println("# of blocks      : " + blocks);
            System.out.println("# of transactions: " + transactions);
            System.out.println("Raw size         : " + size + " bytes");
            System.out.println("Compressed size  : " + sizeCompressed + " bytes");
            System.out.println("Ratio            : " + (100.0 * sizeCompressed / size) + " %");
            System.out.println("Total time used  : " + time + " ns");
            System.out.println("Average time used: " + time / blocks + " ns");
        }
    }
}
