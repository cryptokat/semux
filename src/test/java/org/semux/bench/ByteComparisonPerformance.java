/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.bench;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.semux.crypto.Hash;
import org.semux.util.Bytes;

@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ByteComparisonPerformance {

    public byte[] a;

    public byte[] b;

    @Setup
    public void setUp() {
        a = Hash.h256(Bytes.random(1));
        b = Hash.h256(Bytes.random(1));
    }

    @Benchmark
    public void benchmarkFastEqual() {
        Bytes.equals(a, b);
    }

    @Benchmark
    public void benchmarkArrayEqual() {
        Arrays.equals(a, b);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ByteComparisonPerformance.class.getName() + ".*")
                .forks(1)
                .warmupIterations(1)
                .measurementIterations(5)
                .shouldDoGC(true)
                .build();

        new Runner(opt).run();
    }
}
