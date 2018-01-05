/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.gui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.apache.commons.cli.ParseException;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.semux.KernelMock;
import org.semux.config.Config;
import org.semux.consensus.SemuxSync;
import org.semux.core.Blockchain;
import org.semux.core.BlockchainImpl;
import org.semux.core.Wallet;
import org.semux.crypto.EdDSA;
import org.semux.db.LevelDB;
import org.semux.gui.model.WalletModel;
import org.semux.net.ChannelManager;
import org.semux.rules.KernelRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SemuxGUITest {

    private static final Logger logger = LoggerFactory.getLogger(SemuxGUITest.class);

    @Rule
    public KernelRule kernelRule = new KernelRule(51610, 51710);

    @Test
    public void testStart() throws ParseException {
        SemuxGUI gui = spy(new SemuxGUI());

        Mockito.doNothing().when(gui).showUnlock(any());
        Mockito.doNothing().when(gui).showWelcome(any());

        String[] args = new String[] {
                "--datadir", kernelRule.getKernel().getConfig().dataDir().getAbsolutePath(),
                "--network", "mainnet"
        };
        gui.start(args);

        assertThat(gui.getDataDir()).isEqualTo(args[1]);
        assertThat(gui.getNetwork()).isEqualTo(args[3]);
        verify(gui).showUnlock(any());

        // start without wallet
        kernelRule.getKernel().getWallet().getFile().delete();
        gui.start(args);
        verify(gui).showWelcome(any());
    }

    @Test
    public void testSetupCoinbase() throws ParseException {
        Wallet wallet = kernelRule.getKernel().getWallet();

        // setup coinbase
        SemuxGUI gui = spy(new SemuxGUI(new WalletModel(), kernelRule.getKernel()));
        Mockito.doNothing().when(gui).startKernelAndMain(any());
        Mockito.doReturn(3).when(gui).showSelectDialog(any(), any(), any());
        gui.setupCoinbase(wallet);

        // verify
        assertThat(gui.getCoinbase()).isEqualTo(3);
    }

    @Test
    public void testSetupCoinbaseEmpty() throws ParseException {
        Wallet wallet = kernelRule.getKernel().getWallet();
        wallet.setAccounts(Collections.emptyList());

        // setup coinbase
        SemuxGUI gui = spy(new SemuxGUI(new WalletModel(), kernelRule.getKernel()));
        Mockito.doNothing().when(gui).startKernelAndMain(any());
        gui.setupCoinbase(wallet);

        // verify
        assertThat(wallet.size()).isEqualTo(1);
    }

    @Test
    public void testProcessBlock() {
        KernelMock kernel = kernelRule.getKernel();

        AddressBook addressBook = mock(AddressBook.class);
        WalletModel model = new WalletModel();
        model.setAddressBook(addressBook);

        SemuxGUI gui = new SemuxGUI(model, kernel);

        // prepare kernel
        Config config = kernel.getConfig();
        Blockchain chain = new BlockchainImpl(config, new LevelDB.LevelDBFactory(kernel.getConfig().dataDir()));
        kernel.setBlockchain(chain);
        ChannelManager channelMgr = new ChannelManager(kernel);
        kernel.setChannelManager(channelMgr);
        SemuxSync syncMgr = new SemuxSync(kernel);
        kernel.setSyncManager(syncMgr);

        // process block
        gui.processBlock(kernel.getBlockchain().getGenesis());

        // assertions
        assertThat(model.getLatestBlock().getNumber()).isEqualTo(0L);
        assertThat(model.getAccounts().size()).isEqualTo(kernel.getWallet().size());
        assertThat(model.getDelegates().size()).isEqualTo(chain.getDelegateState().getDelegates().size());
        assertThat(model.getTotalAvailable()).isEqualTo(0);
        assertThat(model.getTotalLocked()).isEqualTo(0);
        assertThat(model.getAddressBook()).isEqualTo(addressBook);
        assertThat(model.getActivePeers().size()).isEqualTo(channelMgr.getActivePeers().size());
        assertThat(model.getSyncProgress()).isEqualToComparingFieldByField(syncMgr.getProgress());
    }

    @Test
    public void testGetMinVersion() {
        SemuxGUI gui = spy(new SemuxGUI());
        String v = gui.getMinVersion();
        logger.info("Min version: {}", v);

        assertThat(v).isNotNull();
    }
}
