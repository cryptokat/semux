/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.api.transaction;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.semux.Kernel;
import org.semux.core.TransactionType;
import org.semux.crypto.EdDSA;

public class TransactionBuilderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testDelegateWithTo() {
        expectedException.expect(IllegalArgumentException.class);
        new TransactionBuilder(mock(Kernel.class), TransactionType.DELEGATE)
                .withTo(Collections.singletonList(new EdDSA().toAddressString()));
    }

    @Test
    public void testDelegateWithValue() {
        expectedException.expect(IllegalArgumentException.class);
        new TransactionBuilder(mock(Kernel.class), TransactionType.DELEGATE).withValue("10");
    }

    @Test
    public void testTransferWithoutTo() {
        expectedException.expect(IllegalArgumentException.class);
        new TransactionBuilder(mock(Kernel.class), TransactionType.TRANSFER).withTo(new ArrayList<>());
    }

    @Test
    public void testTransferWithTo() {
        new TransactionBuilder(mock(Kernel.class), TransactionType.TRANSFER)
                .withTo(Collections.singletonList(new EdDSA().toAddressString()));
    }
}
