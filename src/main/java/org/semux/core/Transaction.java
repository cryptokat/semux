/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.core;

import java.util.Arrays;
import java.util.concurrent.Callable;

import org.semux.crypto.EdDSA;
import org.semux.crypto.EdDSA.Signature;
import org.semux.crypto.Hash;
import org.semux.crypto.Hex;
import org.semux.util.SimpleDecoder;
import org.semux.util.SimpleEncoder;
import org.xbill.DNS.Address;

public class Transaction implements Callable<Boolean> {

    private byte[] hash;

    private TransactionType type;

    private byte[] to;

    private long value;

    private long fee;

    private long nonce;

    private long timestamp;

    private byte[] data;

    private byte[] encoded;
    private Signature signature;

    /**
     * Create a new transaction.
     * 
     * @param type
     * @param to
     * @param value
     * @param fee
     * @param nonce
     * @param timestamp
     * @param data
     */
    public Transaction(TransactionType type, byte[] to, long value, long fee, long nonce, long timestamp, byte[] data) {
        this.type = type;
        this.to = to;
        this.value = value;
        this.fee = fee;
        this.nonce = nonce;
        this.timestamp = timestamp;
        this.data = data;

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeByte(type.toByte());
        enc.writeBytes(to);
        enc.writeLong(value);
        enc.writeLong(fee);
        enc.writeLong(nonce);
        enc.writeLong(timestamp);
        enc.writeBytes(data);
        this.encoded = enc.toBytes();
        this.hash = Hash.h256(encoded);
    }

    /**
     * Create a transaction from raw bytes
     * 
     * @param hash
     * @param encoded
     * @param signature
     */
    public Transaction(byte[] hash, byte[] encoded, byte[] signature) {
        this.hash = hash;

        SimpleDecoder dec = new SimpleDecoder(encoded);
        this.type = TransactionType.of(dec.readByte());
        this.to = dec.readBytes();
        this.value = dec.readLong();
        this.fee = dec.readLong();
        this.nonce = dec.readLong();
        this.timestamp = dec.readLong();
        this.data = dec.readBytes();

        this.encoded = encoded;
        this.signature = Signature.fromBytes(signature);
    }

    /**
     * Sign this transaction.
     * 
     * @param key
     * @return
     */
    public Transaction sign(EdDSA key) {
        this.signature = key.sign(this.hash);
        return this;
    }

    /**
     * <p>
     * Validate transaction format and signature. </>
     * 
     * <p>
     * NOTE: this method does not check transaction validity over the state. Use
     * {@link TransactionExecutor} for that purpose
     * </p>
     * 
     * @return true if success, otherwise false
     */
    public boolean validate() {
        return hash != null && hash.length == 32 //
                && type != null //
                && to != null && to.length == 20 //
                && value >= 0 //
                && fee >= 0 //
                && nonce >= 0 //
                && timestamp > 0 //
                && data != null && (data.length <= 128) //
                && encoded != null //
                && signature != null //

                && Arrays.equals(Hash.h256(encoded), hash) //
                && EdDSA.verify(hash, signature);
    }

    /**
     * Returns the transaction hash.
     * 
     * @return
     */
    public byte[] getHash() {
        return hash;
    }

    /**
     * Returns the transaction type.
     * 
     * @return
     */
    public TransactionType getType() {
        return type;
    }

    /**
     * Parses the from address from signature.
     * 
     * @return an {@link Address} if the signature is success, otherwise null
     */
    public byte[] getFrom() {
        return (signature == null) ? null : signature.getAddress();
    }

    /**
     * Returns the to address.
     * 
     * @return
     */
    public byte[] getTo() {
        return to;
    }

    /**
     * Returns the value.
     * 
     * @return
     */
    public long getValue() {
        return value;
    }

    /**
     * Returns the transaction fee.
     * 
     * @return
     */
    public long getFee() {
        return fee;
    }

    /**
     * Returns the nonce.
     * 
     * @return
     */
    public long getNonce() {
        return nonce;
    }

    /**
     * Returns the timestamp.
     * 
     * @return
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the extra data.
     * 
     * @return
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Returns the signature.
     * 
     * @return
     */
    public Signature getSignature() {
        return signature;
    }

    /**
     * Converts into a byte array.
     * 
     * @return
     */
    public byte[] toBytes() {
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeBytes(hash);
        enc.writeBytes(encoded);
        enc.writeBytes(signature.toBytes());

        return enc.toBytes();
    }

    /**
     * Parses from a byte array.
     * 
     * @param bytes
     * @return
     */
    public static Transaction fromBytes(byte[] bytes) {
        SimpleDecoder dec = new SimpleDecoder(bytes);
        byte[] hash = dec.readBytes();
        byte[] encoded = dec.readBytes();
        byte[] signature = dec.readBytes();

        return new Transaction(hash, encoded, signature);
    }

    @Override
    public String toString() {
        return "Transaction [type=" + type + ", from=" + Hex.encode(getFrom()) + ", to=" + Hex.encode(to) + ", value="
                + value + ", fee=" + fee + ", nonce=" + nonce + ", timestamp=" + timestamp + ", data="
                + Hex.encode(data) + ", hash=" + Hex.encode(hash) + "]";
    }

    @Override
    public Boolean call() throws Exception {
        return validate();
    }
}
