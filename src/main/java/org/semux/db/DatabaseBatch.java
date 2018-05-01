/**
 * Copyright (c) 2017-2018 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.db;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class DatabaseBatch {

    private static ConcurrentHashMap<Database, DatabaseBatch> instances = new ConcurrentHashMap<>();

    private Database database;

    private ConcurrentLinkedQueue<Pair<byte[], byte[]>> updates;

    private DatabaseBatch(Database database) {
        this.database = database;
        this.updates = new ConcurrentLinkedQueue<>();
    }

    public static DatabaseBatch getBatch(Database database) {
        return instances.computeIfAbsent(database, DatabaseBatch::new);
    }

    public void put(byte[] key, byte[] value) {
        updates.add(ImmutablePair.of(key, value));
    }

    public void delete(byte[] key) {
        updates.add(ImmutablePair.of(key, null));
    }

    public synchronized void flush() {
        database.updateBatch(updates);
        updates.clear();
    }
}
