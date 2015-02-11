package com.squareface.intern.benchmark;

import com.squareface.intern.InternDomain;
import com.squareface.intern.NonBlockingWeakInternDomain;
import com.squareface.intern.StrongInternDomain;
import com.squareface.intern.WeakHashmapInternDomain;
import com.squareface.intern.removal.NullRemovalPolicy;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * Benchmarks populating Strong and Weak InternDomain populations up to a given population size
 * The domain must be owend and populated by a single thread otherwise the measurement would be for a combination of
 * interning and retrieval of previously interned objects
 * <p/>
 * Collection/pruning of domain entries is prevented by:
 * 1) Applying no size limitation/removal triggering to the StrongInternDomain
 * 2) Maintaining an array of (strong) references to interned objects to prevent collection of weak references in the Weak InternDomain implementations
 */
public class WeakVsStrongPopulatingBenchmark {

    private static final int INITIAL_OBJECT_POPULATION = 50000;

    public static abstract class InternState {
        TestObject[] refs;
        InternDomain<TestObject> domain;

        @Setup(Level.Iteration)
        public void setup() {
            refs = new TestObject[INITIAL_OBJECT_POPULATION];
            domain = getInternDomain();
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            int size = domain.size();
            if (size < INITIAL_OBJECT_POPULATION) {
                throw new AssertionError();
            }
        }

        abstract InternDomain<TestObject> getInternDomain();
    }

    @State(Scope.Thread)
    public static class WeakHashmapState extends InternState {

        @Override
        InternDomain<TestObject> getInternDomain() {
            return new WeakHashmapInternDomain<TestObject>();
        }
    }

    @State(Scope.Thread)
    public static class WeakNonBlockingState extends InternState {

        @Override
        InternDomain<TestObject> getInternDomain() {
            return new NonBlockingWeakInternDomain<TestObject>();
        }
    }

    @State(Scope.Thread)
    public static class StrongNonSizeLimitedState extends InternState {

        @Override
        InternDomain<TestObject> getInternDomain() {
            return new StrongInternDomain<TestObject>(new NullRemovalPolicy<TestObject>());
        }
    }

    @Benchmark
    @OperationsPerInvocation(INITIAL_OBJECT_POPULATION)
    public void benchmarkPopulatingWeakHashmapInternDomain(WeakHashmapState state) {
        for (int i = 0; i < INITIAL_OBJECT_POPULATION; i++) {
            TestObject item = new TestObject(i);
            state.refs[i] = state.domain.intern(item);
        }
    }

    @Benchmark
    @OperationsPerInvocation(INITIAL_OBJECT_POPULATION)
    public void benchmarkPopulatingWeakNonBlockingInternDomain(WeakNonBlockingState state) {
        for (int i = 0; i < INITIAL_OBJECT_POPULATION; i++) {
            TestObject item = new TestObject(i);
            state.refs[i] = state.domain.intern(item);
        }
    }

    @Benchmark
    @OperationsPerInvocation(INITIAL_OBJECT_POPULATION)
    public void benchmarkPopulatingStrongNonSizeLimitedInternDomain(StrongNonSizeLimitedState state) {
        for (int i = 0; i < INITIAL_OBJECT_POPULATION; i++) {
            TestObject item = new TestObject(i);
            state.refs[i] = state.domain.intern(item);
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(WeakVsStrongPopulatingBenchmark.class.getSimpleName())
                .threads(1)
                .warmupIterations(5)
                .warmupTime(TimeValue.seconds(2))
                .measurementIterations(10)
                .measurementTime(TimeValue.seconds(2))
                .forks(1)
                .build();

        new Runner(opt).run();
    }


}
