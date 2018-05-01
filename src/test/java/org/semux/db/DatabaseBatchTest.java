/**
 * Copyright (c) 2017-2018 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.db;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.semux.rules.TemporaryDatabaseRule;
import org.semux.util.Bytes;

public class DatabaseBatchTest {

    @Rule
    public TemporaryDatabaseRule temporaryDatabaseRule = new TemporaryDatabaseRule();

    @Test
    public void testBatch() {
        Database db = temporaryDatabaseRule.getDB(DatabaseName.BLOCK);
        DatabaseBatch batch = DatabaseBatch.getBatch(db);
        for (int i = 1; i <= 100; i++) {
            final int j = i;
            new Thread(() -> batch.put(Bytes.of(j), Bytes.of(j))).run();
            if (i % 10 == 0) {
                new Thread(batch::flush).run();
            }
        }

        for (int i = 1; i <= 100; i++) {
            assertThat(db.get(Bytes.of(i))).isNotNull().isEqualTo(Bytes.of(i));
        }
    }
}
