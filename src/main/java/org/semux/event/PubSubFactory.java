/**
 * Copyright (c) 2017-2018 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.event;

import java.util.concurrent.ConcurrentHashMap;

public class PubSubFactory {

    private static final ConcurrentHashMap<Class<?>, PubSub> instances = new ConcurrentHashMap<>();

    private PubSubFactory() {
    }

    public static PubSub getDefault() {
        return get(PubSub.class);
    }

    public static PubSub get(Class<?> clz) {
        return instances.computeIfAbsent(clz, k -> new PubSub(k.getCanonicalName()));
    }

    public static void stopAll() {
        instances.values().forEach(PubSub::stop);
    }
}
