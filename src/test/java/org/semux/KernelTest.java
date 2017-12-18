/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux;

import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.junit.Rule;
import org.junit.Test;
import org.semux.integration.KernelTestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KernelTest {

    private Logger logger = LoggerFactory.getLogger(KernelTest.class);

    @Rule
    public KernelTestRule kernelRule = new KernelTestRule(15160, 15170);

    public KernelTest() throws IOException {
    }

    @Test
    public void testStart() {
        Kernel kernel = kernelRule.getKernelMock();

        // start kernel
        Instant begin = Instant.now();
        kernel.start();
        await().until(() -> //
        kernel.isRunning() && //
                kernel.getNodeManager().isRunning() && //
                kernel.getPendingManager().isRunning() && //
                kernel.getApi().isRunning() && //
                kernel.getP2p().isRunning() && //
                kernel.getConsensus().isRunning() && //
                !kernel.getSyncManager().isRunning() //
        );
        logger.info("Kernel successfully started after {} ms", Duration.between(begin, Instant.now()).toMillis());

        // stop kernel
        begin = Instant.now();
        kernel.stop();
        await().until(() -> //
        !kernel.isRunning() && //
                !kernel.getNodeManager().isRunning() && //
                !kernel.getPendingManager().isRunning() && //
                !kernel.getApi().isRunning() && //
                !kernel.getP2p().isRunning() && //
                !kernel.getConsensus().isRunning() && //
                !kernel.getSyncManager().isRunning() //
        );
        logger.info("Kernel successfully stopped after {} ms", Duration.between(begin, Instant.now()).toMillis());
    }
}
