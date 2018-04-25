/**
 * Copyright (c) 2017-2018 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.core;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.semux.config.Config;
import org.semux.config.Constants;
import org.semux.config.TestnetConfig;
import org.semux.crypto.Hex;
import org.semux.db.DatabaseFactory;
import org.semux.db.LeveldbDatabase;
import org.semux.net.filter.SemuxIpFilterLoaderTest;

public class BlockchainImplMigrationTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testMigrationBlockDbVersion000() throws IOException {
        // extract a version 0 database from resource bundle
        File dbVersion0Tarball = new File(
                SemuxIpFilterLoaderTest.class.getResource("/database/database-v0-testnet-29.tgz").getFile());
        Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");
        archiver.extract(dbVersion0Tarball, temporaryFolder.getRoot());

        // load the database
        DatabaseFactory dbFactory = new LeveldbDatabase.LeveldbFactory(new File(temporaryFolder.getRoot(), "database"));
        Config config = new TestnetConfig(Constants.DEFAULT_DATA_DIR);
        BlockchainImpl blockchain = new BlockchainImpl(config, dbFactory);

        // the database should be upgraded to the latest version
        assertThat("getDatabaseVersion", blockchain.getDatabaseVersion(), equalTo(2));
        assertThat("getLatestBlockNumber", blockchain.getLatestBlockNumber(), equalTo(29L));
        for (int i = 0; i <= blockchain.getLatestBlockNumber(); i++) {
            assertThat("getBlock(" + i + ")", blockchain.getBlock(i), is(notNullValue()));
        }
    }

    @Test
    public void testMigrationBlockDbVersion001() throws IOException {
        // extract a version 0 database from resource bundle
        File dbVersion0Tarball = new File(
                SemuxIpFilterLoaderTest.class.getResource("/database/database-v1-mainnet-189.tgz").getFile());
        Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");
        archiver.extract(dbVersion0Tarball, temporaryFolder.getRoot());

        // load the database
        DatabaseFactory dbFactory = new LeveldbDatabase.LeveldbFactory(
                new File(temporaryFolder.getRoot(), "database/mainnet"));
        Config config = new TestnetConfig(Constants.DEFAULT_DATA_DIR);
        BlockchainImpl blockchain = new BlockchainImpl(config, dbFactory);

        // the database should be upgraded to the latest version
        assertThat("getDatabaseVersion", blockchain.getDatabaseVersion(), equalTo(2));
        assertThat("getLatestBlockNumber", blockchain.getLatestBlockNumber(), equalTo(189L));
        assertThat("getValidators", blockchain.getValidators(), hasSize(4));
        for (String validator : blockchain.getValidators()) {
            assertThat("getValidatorStats", blockchain.getValidatorStats(Hex.decode(validator)), notNullValue());
        }
        assertThat("getBlockNumber", blockchain.getBlockNumber(
                Hex.decode0x("0x1b8e2c795cef53c9f910fcf042fa4069fdb8e0e9e19922635bbf44e594721c5f")), equalTo(28L));
        assertThat("getTransaction",
                blockchain.getTransaction(
                        Hex.decode0x("0x4df2c57581e98a00b42b7fc09629e0e6897c55053f7094d78ee8b3e484f04f57")),
                notNullValue());
        assertThat("getAccountTransactions",
                blockchain.getTransactions(Hex.decode0x("0x440474500bc02a297323dc5e819f9d099d8908bf"), 0, 100), allOf(
                        hasSize(5),
                        contains(
                                hasProperty("hash",
                                        equalTo(Hex.decode0x(
                                                "0x79ee324cae1c909de552050c70d884f8a430839a183fb14aae85fd9d4132e7af"))),
                                hasProperty("hash",
                                        equalTo(Hex.decode0x(
                                                "0xb9b6331ed5eb35365e7c4069204fdb1f0c6fefbbf67aa74987c48ee26219dd0e"))),
                                hasProperty("hash",
                                        equalTo(Hex.decode0x(
                                                "0x15a8a59287d8d0286c70ef75da67890a3f708ce2047d9adbc8bffcc62f49bb0d"))),
                                hasProperty("hash",
                                        equalTo(Hex.decode0x(
                                                "0x0f2cf7c326c18a70acf543ce88edbbb357861b08d852742987b5062a4f6e69d5"))),
                                hasProperty("hash", equalTo(Hex.decode0x(
                                        "0x730ff134298d77031d98b89b06ac71e1c66628e1dd92357066d951066b024c86"))))));
        assertThat("getActivatedForks", blockchain.getActivatedForks(), notNullValue());
        for (int i = 0; i <= blockchain.getLatestBlockNumber(); i++) {
            if (i > 0) {
                assertThat("getCoinbaseTransaction(" + i + ")", blockchain.getCoinbaseTransaction(i), notNullValue());
            }

            Block block = blockchain.getBlock(i);
            assertThat("getBlock(" + i + ")", block, notNullValue());

            if (i == 65) {
                assertThat(block.getTransactions(), hasSize(2276));
            } else {
                assertThat(block.getTransactions(), notNullValue());
            }
        }
    }

}
