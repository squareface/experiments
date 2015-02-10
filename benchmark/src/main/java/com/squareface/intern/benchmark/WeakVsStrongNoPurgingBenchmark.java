package com.squareface.intern.benchmark;

import com.squareface.intern.InternDomain;
import com.squareface.intern.Interner;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;

/**
 * Created by Andy on 08/02/15.
 */
public class WeakVsStrongNoPurgingBenchmark {

    private static final int OBJECT_POPULATION_SIZE = 5000000;

    @State(Scope.Benchmark)
    public static abstract class InternState {
        Random r;
        InternDomain<TestObject> internDomain;
        // Need to populate this array and select object to intern from here
        // This ensures Strong refs are maintained to everything that is interned and prevents cleanup when using weak interners
        TestObject[] refs = new TestObject[OBJECT_POPULATION_SIZE];

        @Setup(Level.Trial)
        public void setup() {
            r = new Random();
            internDomain = getInterner().forDomain(TestObject.class);
            // Ensure we have a set of test objects with strong refs
            for (int i = 0; i < OBJECT_POPULATION_SIZE; i++) {
                refs[i] = new TestObject(i);
            }
        }

        @TearDown(Level.Trial)
        public void tearDown() {
            int size = internDomain.size();
            System.out.println("Final size=" + size);
            if (size != OBJECT_POPULATION_SIZE) {
                throw new AssertionError();
            }
        }

        abstract Interner getInterner();

    }

    @State(Scope.Benchmark)
    public static class WeakHashmapInternDomainState extends InternState {

        @Override
        Interner getInterner() {
            return Interner.weakHashmapInterner();
        }
    }

    @State(Scope.Benchmark)
    public static class WeakNonBlockingInternDomainState extends InternState {

        @Override
        Interner getInterner() {
            return Interner.weakInterner();
        }
    }

    @State(Scope.Benchmark)
    public static class StrongNonSizeLimitedInternState extends InternState {

        @Override
        Interner getInterner() {
            return Interner.strongInterner().withNonSizeLimitedDomains();
        }

    }

    @Benchmark
    public TestObject benchmarkReadAndPopulateWeakHashmapInternDomain(WeakHashmapInternDomainState state) {
        return doIntern(state);
    }

    @Benchmark
    public Object benchmarkReadAndPopulateWeakNonBlockingInternDomain(WeakNonBlockingInternDomainState state) {
        return doIntern(state);
    }

    @Benchmark
    public Object benchmarkReadAndPopulateStrongNonSizeLimitedInternDomain(StrongNonSizeLimitedInternState state) {
        return doIntern(state);
    }

    private TestObject doIntern(InternState state) {
        //As the object population size is limited this method should produce a mixture of
        //first time interning and interned object retrieval
        int idx = state.r.nextInt(OBJECT_POPULATION_SIZE);
        TestObject item = state.refs[idx];
        return state.internDomain.intern(item);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(WeakVsStrongNoPurgingBenchmark.class.getSimpleName())
                .threads(Runtime.getRuntime().availableProcessors() * 4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
