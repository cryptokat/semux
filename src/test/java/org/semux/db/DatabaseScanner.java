/**
 * Copyright (c) 2017-2018 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.db;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map.Entry;

import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.semux.core.BlockchainImpl;
import org.semux.core.exception.BlockchainException;
import org.semux.crypto.Hex;
import org.semux.crypto.Key;
import org.semux.util.Bytes;

public class DatabaseScanner {
    public static void main(String[] args) throws IOException {
        Options options = new Options()
                .createIfMissing(false);

        File f = new File("dist/linux/database/mainnet/validator_stats_history");
        System.out.println(f.exists());

        try (DB db = JniDBFactory.factory.open(f, options)) {
            DBIterator itr = db.iterator();
            itr.seek(Hex.decode("db7cadb25fdcdd546fb0268524107582c3f8999c"));
            while (itr.hasNext()) {
                Entry<byte[], byte[]> entry = itr.next();

                byte[] address = new byte[20];
                System.arraycopy(entry.getKey(), 0, address, 0, 20);

                if (!Arrays.equals(address, Hex.decode("db7cadb25fdcdd546fb0268524107582c3f8999c"))) {
                    break;
                }

                byte[] heightBytes = new byte[8];
                System.arraycopy(entry.getKey(), Key.ADDRESS_LEN, heightBytes, 0, heightBytes.length);
                long height = Bytes.toLong(heightBytes);
                BlockchainImpl.ValidatorStats validatorStats = BlockchainImpl.ValidatorStats.fromBytes(entry.getValue());

                System.out.format("%s @ %d = %d / %d / %d\n", Hex.encode(address), height, validatorStats.getBlocksForged(), validatorStats.getTurnsHit(), validatorStats.getTurnsMissed());
            }
            itr.close();
        }
    }
}
