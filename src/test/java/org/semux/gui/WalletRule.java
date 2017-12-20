/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.gui;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.junit.rules.ExternalResource;
import org.mockito.Mockito;
import org.semux.core.Unit;
import org.semux.core.state.Account;
import org.semux.crypto.EdDSA;
import org.semux.gui.model.WalletAccount;
import org.semux.gui.model.WalletModel;

public class WalletRule extends ExternalResource {

    public final EdDSA key;

    public Account account;

    public WalletAccount walletAccount;

    public WalletModel walletModel;

    final int availableSEM;

    final int lockedSEM;

    public WalletRule(int availableSEM, int lockedSEM) {
        this.key = new EdDSA();
        this.availableSEM = availableSEM;
        this.lockedSEM = lockedSEM;
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        account = new Account(
                key.toAddress(),
                availableSEM * Unit.SEM,
                lockedSEM * Unit.SEM,
                RandomUtils.nextInt(1, 100));

        walletAccount = new WalletAccount(key, account);
        List<WalletAccount> accountList = Collections.singletonList(walletAccount);
        walletModel = mock(WalletModel.class);
        when(walletModel.getAccounts()).thenReturn(accountList);
    }

    @Override
    protected void after() {
        super.after();
        Mockito.reset(walletModel);
    }
}
