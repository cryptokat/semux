/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.core.state;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.semux.db.KVDB;
import org.semux.util.ByteArray;
import org.semux.util.Bytes;

/**
 * Account state implementation.
 * 
 * <pre>
 * account DB structure:
 * 
 * [0, address] => [account_object]
 * [1, address] => [code]
 * [2, address, storage_key] = [storage_value]
 * </pre>
 */
public class AccountStateImpl implements AccountState {

    protected static final byte TYPE_ACCOUNT = 0;
    protected static final byte TYPE_CODE = 1;
    protected static final byte TYPE_STORAGE = 2;

    protected KVDB accountDB;
    protected AccountStateImpl prev;

    /**
     * All updates, or deletes if the value is null.
     */
    protected final Map<ByteArray, byte[]> updates = new ConcurrentHashMap<>();

    /**
     * Create an AcccountState that work directly on a database.
     * 
     * @param accountDB
     */
    public AccountStateImpl(KVDB accountDB) {
        this.accountDB = accountDB;
    }

    /**
     * Create an AcccountState based on a previous AccountState.
     * 
     * @param prev
     */
    public AccountStateImpl(AccountStateImpl prev) {
        this.prev = prev;
    }

    @Override
    public Account getAccount(byte[] addr) {
        ByteArray k = getKey(TYPE_ACCOUNT, addr);

        if (updates.containsKey(k)) {
            byte[] v = updates.get(k);
            return v == null ? new Account(addr, 0, 0, 0) : Account.fromBytes(addr, v);
        } else if (prev != null) {
            return prev.getAccount(addr);
        } else {
            byte[] v = accountDB.get(k.getData());
            return v == null ? new Account(addr, 0, 0, 0) : Account.fromBytes(addr, v);
        }
    }

    @Override
    public void increaseNonce(byte[] addr) {
        ByteArray k = getKey(TYPE_ACCOUNT, addr);

        Account acc = getAccount(addr);
        acc.setNonce(acc.getNonce() + 1);
        updates.put(k, acc.toBytes());
    }

    @Override
    public void adjustAvailable(byte[] addr, long delta) {
        ByteArray k = getKey(TYPE_ACCOUNT, addr);

        Account acc = getAccount(addr);
        acc.setAvailable(acc.getAvailable() + delta);
        updates.put(k, acc.toBytes());
    }

    @Override
    public void adjustLocked(byte[] addr, long delta) {
        ByteArray k = getKey(TYPE_ACCOUNT, addr);

        Account acc = getAccount(addr);
        acc.setLocked(acc.getLocked() + delta);
        updates.put(k, acc.toBytes());
    }

    @Override
    public void getCode(byte[] addr) {
        throw new UnsupportedOperationException("getCode() is not yet supported");
    }

    @Override
    public void setCode(byte[] addr, byte[] code) {
        throw new UnsupportedOperationException("setCode() is not yet supported");
    }

    @Override
    public byte[] getStorage(byte[] addr, byte[] key) {
        throw new UnsupportedOperationException("getStorage() is not yet supported");
    }

    @Override
    public void putStorage(byte[] addr, byte[] key, byte[] value) {
        throw new UnsupportedOperationException("putStorage() is not yet supported");
    }

    @Override
    public void removeStorage(byte[] addr, byte[] key) {
        throw new UnsupportedOperationException("removeStorage() is not yet yetsupported");
    }

    @Override
    public AccountState track() {
        return new AccountStateImpl(this);
    }

    @Override
    public void commit() {
        synchronized (updates) {
            if (prev == null) {
                for (Map.Entry<ByteArray, byte[]> entry : updates.entrySet()) {
                    if (entry.getValue() == null) {
                        accountDB.delete(entry.getKey().getData());
                    } else {
                        accountDB.put(entry.getKey().getData(), entry.getValue());
                    }
                }
            } else {
                for (Entry<ByteArray, byte[]> e : updates.entrySet()) {
                    prev.updates.put(e.getKey(), e.getValue());
                }
            }

            updates.clear();
        }
    }

    @Override
    public void rollback() {
        updates.clear();
    }

    protected ByteArray getKey(byte type, byte[] addr) {
        return ByteArray.of(Bytes.merge(type, addr));
    }

    protected ByteArray getStorageKey(byte[] addr, byte[] key) {
        byte[] buf = new byte[1 + addr.length + key.length];
        buf[0] = TYPE_STORAGE;
        System.arraycopy(addr, 0, buf, 1, addr.length);
        System.arraycopy(key, 0, buf, 1 + addr.length, key.length);

        return ByteArray.of(buf);
    }
}
