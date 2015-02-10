package com.squareface.intern.benchmark;

import com.squareface.intern.InternDomain;
import com.squareface.intern.Interner;
import com.squareface.intern.PurgeableInternDomain;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;

/**
 * Created by Andy on 10/02/15.
 */
public class StrongInternDomainConfigurationsBenchmark {

    private static final int OBJECT_POPULATION_SIZE = 100000;

    @State(Scope.Benchmark)
    public static abstract class InternState {
        Random r;
        InternDomain<TestObject> internDomain;

        @Setup(Level.Trial)
        public void setup() {
            r = new Random();
            internDomain = getInterner().forDomain(TestObject.class);
        }

        @TearDown(Level.Trial)
        public void tearDown() {
            int size = internDomain.size();
            System.out.println("Final size=" + size);
            if (size != OBJECT_POPULATION_SIZE) {
                throw new AssertionError();
            }
            PurgeableInternDomain<TestObject> purgeable = (PurgeableInternDomain<TestObject>) internDomain;
            if (purgeable.getPurgeCount() == 0) {
                throw new AssertionError();
            }
        }

        abstract Interner getInterner();

    }

    @State(Scope.Benchmark)
    public static class StrongNonSizeLimitedInternState extends InternState {

        @Override
        Interner getInterner() {
            return Interner.strongInterner().withNonSizeLimitedDomains();
        }

    }

    @State(Scope.Benchmark)
    public static class StrongFifoFixedMaxSizeInternState extends InternState {

        @Override
        Interner getInterner() {
            return Interner.strongInterner().fifoRemoval().withFixedMaximumSize(500 * 1024 * 1024).build();
        }

    }

    @State(Scope.Benchmark)
    public static class StrongFifoMaxHeapProportionInternState extends InternState {

        @Override
        Interner getInterner() {
            return Interner.strongInterner().fifoRemoval().withMaxHeapProportion(0.6).build();
        }

    }


    @State(Scope.Benchmark)
    public static class StrongSizeOrderedFixedMaxSizeInternState extends InternState {

        @Override
        Interner getInterner() {
            return Interner.strongInterner().sizeOrderedRemoval().withFixedMaximumSize(500 * 1024 * 1024).build();
        }

    }

    @State(Scope.Benchmark)
    public static class StrongSizeOrderedMaxHeapProportionInternState extends InternState {

        @Override
        Interner getInterner() {
            return Interner.strongInterner().sizeOrderedRemoval().withMaxHeapProportion(0.6).build();
        }

    }


//    @Benchmark
//    public Object benchmarkReadAndPopulateStrongNonSizeLimitedInternDomain(StrongNonSizeLimitedInternState state) {
//        return doIntern(state);
//    }

    @Benchmark
    public Object benchmarkReadAndPopulateStrongFifoFixedMaxSizeInternDomain(StrongFifoFixedMaxSizeInternState state) {
        return doIntern(state);
    }

    @Benchmark
    public Object benchmarkReadAndPopulateStrongFifoMaxHeapProportionInternDomain(StrongFifoMaxHeapProportionInternState state) {
        return doIntern(state);
    }

    @Benchmark
    public Object benchmarkReadAndPopulateStrongSizeOrderedFixedMaxSizeInternDomain(StrongSizeOrderedFixedMaxSizeInternState state) {
        return doIntern(state);
    }

    @Benchmark
    public Object benchmarkReadAndPopulateStrongSizeOrderedMaxHeapProportionInternDomain(StrongSizeOrderedMaxHeapProportionInternState state) {
        return doIntern(state);
    }


    private TestObject doIntern(InternState state) {
        //As the object population size is limited this method should produce a mixture of
        //first time interning and interned object retrieval
        int i = state.r.nextInt(OBJECT_POPULATION_SIZE);
        TestObject item = new TestObject(i);
        return state.internDomain.intern(item);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(StrongInternDomainConfigurationsBenchmark.class.getSimpleName())
                .threads(Runtime.getRuntime().availableProcessors() * 4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
