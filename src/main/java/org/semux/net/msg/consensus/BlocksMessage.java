/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.net.msg.consensus;

import java.util.ArrayList;
import java.util.List;

import org.semux.core.Block;
import org.semux.net.msg.Message;
import org.semux.net.msg.MessageCode;
import org.semux.util.SimpleDecoder;
import org.semux.util.SimpleEncoder;

public class BlocksMessage extends Message {

    private List<Block> blocks;

    public BlocksMessage(List<Block> blocks) {
        super(MessageCode.BLOCK, null);

        this.blocks = blocks;

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeInt(blocks.size());
        for (Block block : blocks) {
            enc.writeBytes(block.toBytesHeader());
            enc.writeBytes(block.toBytesTransactions());
            enc.writeBytes(block.toBytesResults());
            enc.writeBytes(block.toBytesVotes());
        }
        this.encoded = enc.toBytes();
    }

    public BlocksMessage(byte[] encoded) {
        super(MessageCode.BLOCK, null);

        this.encoded = encoded;
        this.blocks = new ArrayList<>();

        SimpleDecoder dec = new SimpleDecoder(encoded);
        int n = dec.readInt();
        for (int i =0 ;i < n;i ++) {
            byte[] header = dec.readBytes();
            byte[] transactions = dec.readBytes();
            byte[] results = dec.readBytes();
            byte[] votes = dec.readBytes();
            this.blocks.add(Block.fromBytes(header, transactions, results, votes));
        }
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    @Override
    public String toString() {
        return "BlocksMessage [block=" + blocks + "]";
    }
}
