/**
 * Copyright (c) 2017-2018 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.db;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.semux.util.ClosableIterator;
import org.semux.util.FileUtil;
import org.semux.util.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.protonail.leveldb.jna.KeyValuePair;
import com.protonail.leveldb.jna.LevelDB;
import com.protonail.leveldb.jna.LevelDBCompressionType;
import com.protonail.leveldb.jna.LevelDBException;
import com.protonail.leveldb.jna.LevelDBKeyValueIterator;
import com.protonail.leveldb.jna.LevelDBOptions;
import com.protonail.leveldb.jna.LevelDBReadOptions;
import com.protonail.leveldb.jna.LevelDBWriteBatch;
import com.protonail.leveldb.jna.LevelDBWriteOptions;

public class LeveldbDatabase implements Database {

    private static final Logger logger = LoggerFactory.getLogger(LeveldbDatabase.class);

    private File file;
    private LevelDB db;
    private boolean isOpened;

    public LeveldbDatabase(File file) {
        this.file = file;

        File dir = file.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            logger.error("Failed to create directory: {}", dir);
        }

        open(createOptions());
    }

    /**
     * Creates the default options.
     *
     * @return
     */
    protected LevelDBOptions createOptions() {
        LevelDBOptions options = new LevelDBOptions();
        options.setCreateIfMissing(true);
        options.setCompressionType(LevelDBCompressionType.NoCompression);
        options.setBlockSize(4 * 1024 * 1024);
        options.setWriteBufferSize(8 * 1024 * 1024);
        // options.cacheSize(64L * 1024L * 1024L);
        options.setParanoidChecks(true);
        // options.verifyChecksums(true);
        options.setMaxOpenFiles(128);

        return options;
    }

    /**
     * Open the database.
     * 
     * @param options
     */
    protected void open(LevelDBOptions options) {
        try {
            db = new LevelDB(file.getCanonicalPath(), options);
            isOpened = true;
        } catch (IOException e) {
            if (e.getMessage().contains("Corruption")) {
                // recover
                recover(options);

                // reopen
                try {
                    db = new LevelDB(file.getCanonicalPath(), options);
                    isOpened = true;
                } catch (IOException ex) {
                    logger.error("Failed to open database", e);
                    SystemUtil.exitAsync(SystemUtil.Code.FAILED_TO_OPEN_DB);
                }
            } else {
                logger.error("Failed to open database", e);
                SystemUtil.exitAsync(SystemUtil.Code.FAILED_TO_OPEN_DB);
            }
        }
    }

    /**
     * Tries to recover the database in case of corruption.
     *
     * @param options
     */
    protected void recover(LevelDBOptions options) {
        try {
            logger.info("Database is corrupted, trying to repair...");
            LevelDB.repair(file.getCanonicalPath(), options);
            logger.info("Repair done!");
        } catch (IOException ex) {
            logger.error("Failed to repair the database", ex);
            SystemUtil.exitAsync(SystemUtil.Code.FAILED_TO_REPAIR_DB);
        }
    }

    @Override
    public byte[] get(byte[] key) {
        try (LevelDBReadOptions options = new LevelDBReadOptions()) {
            return db.get(key, options);
        }
    }

    @Override
    public void put(byte[] key, byte[] value) {
        try (LevelDBWriteOptions options = new LevelDBWriteOptions()) {
            db.put(key, value, options);
        }
    }

    @Override
    public void delete(byte[] key) {
        try (LevelDBWriteOptions options = new LevelDBWriteOptions()) {
            db.delete(key, options);
        }
    }

    @Override
    public void updateBatch(List<Pair<byte[], byte[]>> pairs) {
        try (LevelDBWriteOptions options = new LevelDBWriteOptions()) {
            try (LevelDBWriteBatch batch = new LevelDBWriteBatch()) {
                for (Pair<byte[], byte[]> p : pairs) {
                    if (p.getValue() == null) {
                        batch.delete(p.getLeft());
                    } else {
                        batch.put(p.getLeft(), p.getRight());
                    }
                }
                db.write(batch, options);
            }
        } catch (LevelDBException e) {
            logger.error("Failed to update batch", e);
            SystemUtil.exitAsync(SystemUtil.Code.FAILED_TO_WRITE_BATCH_TO_DB);
        }
    }

    @Override
    public void close() {
        if (isOpened) {
            db.close();
            isOpened = false;
        }
    }

    @Override
    public void destroy() {
        close();
        FileUtil.recursiveDelete(file);
    }

    @Override
    public Path getDataDir() {
        return file.toPath();
    }

    @Override
    public ClosableIterator<ImmutablePair<byte[], byte[]>> iterator() {
        return iterator(null);
    }

    @Override
    public ClosableIterator<ImmutablePair<byte[], byte[]>> iterator(byte[] prefix) {

        return new ClosableIterator<ImmutablePair<byte[], byte[]>>() {
            LevelDBKeyValueIterator itr;

            private ClosableIterator<ImmutablePair<byte[], byte[]>> initialize() {
                try (LevelDBReadOptions options = new LevelDBReadOptions()) {
                    itr = new LevelDBKeyValueIterator(db, options);
                    if (prefix != null) {
                        itr.seekToKey(prefix);
                    } else {
                        itr.seekToFirst();
                    }
                }
                return this;
            }

            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }

            @Override
            public ImmutablePair<byte[], byte[]> next() {
                KeyValuePair kv = itr.next();
                return ImmutablePair.of(kv.getKey(), kv.getValue());
            }

            @Override
            public void close() {
                itr.close();
            }
        }.initialize();
    }

    public static class LevelDbFactory implements DatabaseFactory {

        private EnumMap<DatabaseName, Database> databases = new EnumMap<>(DatabaseName.class);

        private File dataDir;
        private AtomicBoolean open;

        public LevelDbFactory(File dataDir) {
            this.dataDir = dataDir;
            this.open = new AtomicBoolean(false);

            open();
        }

        @Override
        public void open() {
            if (open.compareAndSet(false, true)) {
                for (DatabaseName name : DatabaseName.values()) {
                    try {
                        File file = Paths.get(dataDir.getAbsolutePath(), name.toString().toLowerCase()).normalize().toFile();
                        Files.createDirectories(file.toPath());
                        databases.put(name, new LeveldbDatabase(file.getCanonicalFile()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public Database getDB(DatabaseName name) {
            open();
            return databases.get(name);
        }

        @Override
        public void close() {
            if (open.compareAndSet(true, false)) {
                for (Database db : databases.values()) {
                    db.close();
                }
            }
        }

        @Override
        public Path getDataDir() {
            return dataDir.toPath();
        }
    }
}
