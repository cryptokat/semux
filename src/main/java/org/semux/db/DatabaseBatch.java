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

    public void add(byte[] key, byte[] value) {
        updates.add(ImmutablePair.of(key, value));
    }

    public synchronized void flush() {
        database.updateBatch(updates);
        updates.clear();
    }
}
