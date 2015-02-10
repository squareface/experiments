package com.squareface.intern.benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Created by Andy on 10/02/15.
 */
public class BenchmarkSuite {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(StrongInternDomainConfigurationsBenchmark.class.getSimpleName())
                .include(WeakVsStrongNoPurgingBenchmark.class.getSimpleName())
                .include(WeakImplementationsBenchmark.class.getSimpleName())
                .threads(Runtime.getRuntime().availableProcessors() * 4)
                .result("benchmark-result")
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}
