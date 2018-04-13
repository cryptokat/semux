package org.semux.db;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.semux.crypto.Hex;
import org.semux.util.Bytes;
import org.semux.util.ClosableIterator;

public class LeveldbComparatorTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testValidatorStatsHistoryReversedComparator() {
        LeveldbDatabase db = new LeveldbDatabase(
                temporaryFolder.getRoot(),
                defaults -> defaults.comparator(new LeveldbConfigurators.ValidatorStatsHistoryReversedComparator())
        );

        final byte[] prefix = Hex.decode("ad68942a95fdd56594aa5cf862b358790e37834c");
        List<byte[]> keys = Stream.of(112556L, 243832L, 251780L, 277546L, 346662L, 448128L, 458189L, 623068L, 662201L, 959788L)
                .map(d -> Bytes.merge(prefix, Bytes.of(d)))
                .collect(Collectors.toList());
        Collections.shuffle(keys); // fuzz

        for (byte[] key : keys) {
            db.put(key, RandomUtils.nextBytes(1));
        }

        ClosableIterator<Map.Entry<byte[], byte[]>> it = db.iterator(prefix);
        try {
            ArrayList<Long> sortedKeys = new ArrayList<>();
            while (it.hasNext()) {
                byte[] key = it.next().getKey();
                byte[] prefixBytes = new byte[20];
                System.arraycopy(key, 0, prefixBytes, 0, prefix.length);
                byte[] keyBytes = new byte[8];
                System.arraycopy(key, prefix.length, keyBytes, 0, keyBytes.length);
                sortedKeys.add(Bytes.toLong(keyBytes));
            }
            assertEquals(
                    Arrays.asList(959788L, 662201L, 623068L, 458189L, 448128L, 346662L, 277546L, 251780L, 243832L, 112556L),
                    sortedKeys
            );
        } finally {
            it.close();
        }

        db.close();
    }
}
