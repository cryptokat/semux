package org.semux.db;

import java.util.function.Function;

import org.bouncycastle.util.Arrays;
import org.iq80.leveldb.DBComparator;
import org.iq80.leveldb.Options;
import org.semux.crypto.Hex;
import org.semux.crypto.Key;
import org.semux.util.Bytes;

class LeveldbConfigurators {

    public static class ValidatorStatsHistoryReversedComparator implements DBComparator {
        public int compare(byte[] key1, byte[] key2) {
            if (key1.length != key2.length) {
                System.out.format("%s %s\n", Hex.encode(key1), Hex.encode(key2));
                return Integer.compare(key1.length, key2.length);
            }

            // compare address
            byte[] addressBytes1 = new byte[Key.ADDRESS_LEN];
            System.arraycopy(key1, 0, addressBytes1, 0, Key.ADDRESS_LEN);
            byte[] addressBytes2 = new byte[20];
            System.arraycopy(key2, 0, addressBytes2, 0, Key.ADDRESS_LEN);
            int addressCmp = Arrays.compareUnsigned(addressBytes2, addressBytes1);
            if (addressCmp != 0) {
                return addressCmp;
            }

            // compare height
            byte[] heightBytes1 = new byte[8];
            System.arraycopy(key1, Key.ADDRESS_LEN, heightBytes1, 0, 8);
            byte[] heightBytes2 = new byte[8];
            System.arraycopy(key2, Key.ADDRESS_LEN, heightBytes2, 0, 8);

            return Long.compare(Bytes.toLong(heightBytes2), Bytes.toLong(heightBytes1));
        }

        public String name() {
            return "VALIDATOR_STATS_HISTORY_REVERSED_COMPARATOR";
        }

        public byte[] findShortestSeparator(byte[] start, byte[] limit) {
            return start;
        }

        public byte[] findShortSuccessor(byte[] key) {
            return key;
        }
    };

    private LeveldbConfigurators() { }

    static Function<Options, Options> getConfigurator(DatabaseName name) {
        switch (name) {
        case VALIDATOR_STATS_HISTORY:
            return defaults -> defaults.comparator(new ValidatorStatsHistoryReversedComparator());
        default:
            return null;
        }
    }
}
