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

import java.util.Random;

/**
 * This benchmark is performing a combination of first time interning and returning interned objects
 * References to interned instances are written to an array to ensure that strong references exist and prevent collection
 * of the weak references in the Wweak InternDomain implementations. The StrongInternDomain is not size limited so there is no pruning
 * of previously interned objects.
 * <p/>
 * The measurements from this benchmark can only be compared across implementations at the given population level and is not an indicator
 * of how population performance scales with the size of the domain. The reason for this is that the iteration runs for a fixed time so
 * for smaller populations a lower proportion of the run time will be due to populating and throughput will appear higher.
 */
public class WeakVsStrongPopulatingReadingCombinationBenchmark {

    private static final int OBJECT_POPULATION_SIZE = 50000;

    @State(Scope.Benchmark)
    public static abstract class InternState {
        Random r;
        InternDomain<TestObject> internDomain;
        // Need to populate this array and select object to intern from here
        // This ensures Strong refs are maintained to everything that is interned and prevents cleanup when using weak interners
        TestObject[] refs;

        @Setup(Level.Iteration)
        public void setup() {
            r = new Random();
            internDomain = getInterner();
            refs = new TestObject[OBJECT_POPULATION_SIZE];
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            int size = internDomain.size();
            if (size > OBJECT_POPULATION_SIZE) {
                throw new AssertionError();
            }
        }

        abstract InternDomain<TestObject> getInterner();

    }

    @State(Scope.Benchmark)
    public static class WeakHashmapInternDomainState extends InternState {

        @Override
        InternDomain<TestObject> getInterner() {
            return new WeakHashmapInternDomain<TestObject>();
        }
    }

    @State(Scope.Benchmark)
    public static class WeakNonBlockingInternDomainState extends InternState {

        @Override
        InternDomain<TestObject> getInterner() {
            return new NonBlockingWeakInternDomain<TestObject>();
        }
    }

    @State(Scope.Benchmark)
    public static class StrongNonSizeLimitedInternState extends InternState {

        @Override
        InternDomain<TestObject> getInterner() {
            return new StrongInternDomain<TestObject>(new NullRemovalPolicy<TestObject>());
        }

    }

    @Benchmark
    public void benchmarkReadAndPopulateWeakHashmapInternDomain(WeakHashmapInternDomainState state) {
        doIntern(state);
    }

    @Benchmark
    public void benchmarkReadAndPopulateWeakNonBlockingInternDomain(WeakNonBlockingInternDomainState state) {
        doIntern(state);
    }

    @Benchmark
    public void benchmarkReadAndPopulateStrongNonSizeLimitedInternDomain(StrongNonSizeLimitedInternState state) {
        doIntern(state);
    }

    private void doIntern(InternState state) {
        //As the object population size is limited this method should produce a mixture of
        //first time interning and interned object retrieval
        int idx = state.r.nextInt(OBJECT_POPULATION_SIZE);
        TestObject item = new TestObject(idx);
        state.refs[idx] = state.internDomain.intern(item);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(WeakVsStrongPopulatingReadingCombinationBenchmark.class.getSimpleName())
                .threads(Runtime.getRuntime().availableProcessors() * 4)
                .warmupIterations(5)
                .warmupTime(TimeValue.seconds(5))
                .measurementTime(TimeValue.seconds(5))
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
