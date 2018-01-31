/**
 * Copyright (c) 2017-2018 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.util;

import java.lang.management.ThreadInfo;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeadlockConsoleHandler implements DeadlockHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeadlockConsoleHandler.class);

    @Override
    public void handleDeadlock(final ThreadInfo[] deadlockedThreads) {
        if (deadlockedThreads != null) {
            logger.error("Deadlock detected!");

            Map<Thread, StackTraceElement[]> stackTraceMap = Thread.getAllStackTraces();
            for (ThreadInfo threadInfo : deadlockedThreads) {

                if (threadInfo != null) {

                    for (Thread thread : Thread.getAllStackTraces().keySet()) {

                        if (thread.getId() == threadInfo.getThreadId()) {
                            logger.error(threadInfo.toString().trim());

                            for (StackTraceElement ste : thread.getStackTrace()) {
                                logger.error("\t" + ste.toString().trim());
                            }
                        }
                    }
                }
            }
        }
    }
}