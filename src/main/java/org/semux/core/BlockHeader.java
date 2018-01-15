/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.core;

import static org.semux.crypto.Hash.HASH_LEN;

import org.semux.crypto.Hash;
import org.semux.crypto.Hex;
import org.semux.crypto.Key;
import org.semux.util.Bytes;
import org.semux.util.SimpleDecoder;
import org.semux.util.SimpleEncoder;

public class BlockHeader {

    public static final int MAX_DATA_SIZE = 32;

    private byte[] hash;

    private long number;

    private byte[] coinbase;

    private byte[] parentHash;

    private long timestamp;

    private byte[] transactionsRoot;

    private byte[] resultsRoot;

    private byte[] stateRoot;

    private byte[] data;

    private byte[] encoded;

    /**
     * Creates an instance of block header.
     *
     * @param number
     * @param coinbase
     * @param prevHash
     * @param timestamp
     * @param transactionsRoot
     * @param resultsRoot
     * @param data
     */
    public BlockHeader(long number, byte[] coinbase, byte[] prevHash, long timestamp, byte[] transactionsRoot,
            byte[] resultsRoot, byte[] stateRoot, byte[] data) {
        this.number = number;
        this.coinbase = coinbase;
        this.parentHash = prevHash;
        this.timestamp = timestamp;
        this.transactionsRoot = transactionsRoot;
        this.resultsRoot = resultsRoot;
        this.stateRoot = stateRoot;
        this.data = data;

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeLong(number);
        enc.writeBytes(coinbase);
        enc.writeBytes(prevHash);
        enc.writeLong(timestamp);
        enc.writeBytes(transactionsRoot);
        enc.writeBytes(resultsRoot);
        enc.writeBytes(stateRoot);
        enc.writeBytes(data);
        this.encoded = enc.toBytes();
        this.hash = Hash.h256(encoded);
    }

    /**
     * Parses block header from byte arrays.
     *
     * @param hash
     * @param encoded
     */
    public BlockHeader(byte[] hash, byte[] encoded) {
        this.hash = hash;

        SimpleDecoder dec = new SimpleDecoder(encoded);
        this.number = dec.readLong();
        this.coinbase = dec.readBytes();
        this.parentHash = dec.readBytes();
        this.timestamp = dec.readLong();
        this.transactionsRoot = dec.readBytes();
        this.resultsRoot = dec.readBytes();
        this.stateRoot = dec.readBytes();
        this.data = dec.readBytes();

        this.encoded = encoded;
    }

    /**
     * Validates block header format.
     *
     * @return true if success, otherwise false
     */
    public boolean validate() {
        return hash != null && hash.length == HASH_LEN
                && number >= 0
                && coinbase != null && coinbase.length == Key.ADDRESS_LEN
                && parentHash != null && parentHash.length == HASH_LEN
                && timestamp >= 0
                && transactionsRoot != null && transactionsRoot.length == HASH_LEN
                && resultsRoot != null && resultsRoot.length == HASH_LEN
                && stateRoot != null && Bytes.equals(Bytes.EMPTY_HASH, stateRoot) // RESERVED FOR VM
                && data != null && data.length <= MAX_DATA_SIZE
                && encoded != null
                && Bytes.equals(Hash.h256(encoded), hash);
    }

    public byte[] getHash() {
        return hash;
    }

    public long getNumber() {
        return number;
    }

    public byte[] getCoinbase() {
        return coinbase;
    }

    public byte[] getParentHash() {
        return parentHash;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] getTransactionsRoot() {
        return transactionsRoot;
    }

    public byte[] getResultsRoot() {
        return resultsRoot;
    }

    public byte[] getStateRoot() {
        return stateRoot;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] toBytes() {
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeBytes(hash);
        enc.writeBytes(encoded);
        return enc.toBytes();
    }

    public static BlockHeader fromBytes(byte[] bytes) {
        SimpleDecoder dec = new SimpleDecoder(bytes);
        byte[] hash = dec.readBytes();
        byte[] encoded = dec.readBytes();

        return new BlockHeader(hash, encoded);
    }

    @Override
    public String toString() {
        return "BlockHeader [number=" + number + ", timestamp=" + timestamp + ", data=" + Hex.encode(data)
                + ", parentHash=" + Hex.encode(parentHash) + ", hash=" + Hex.encode(hash) + "]";
    }

}
