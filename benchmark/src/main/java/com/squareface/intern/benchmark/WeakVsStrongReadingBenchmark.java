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
 * This benchmark measures retrieving previously interned objects from the Strong, WeakHashmap and NonBlockingWeak InternDomain
 * implementation. The interned object population size can be adjusted to measure how retrieval scales with the size of the domain.
 * <p/>
 * To measure retrieval of previously interned objects, this benchmark pre-populates the intern domain and holds references
 * to the interned objects in an array. This prevents collection of the references from the weak interndomain implementations.
 * The StrongInternDomain is not size limited so no objects will ever be pruned. The benchmark generates and interns a new object which is
 * guaranteed to be equal to one of the interned object population.
 */
public class WeakVsStrongReadingBenchmark {

    private static final int OBJECT_POPULATION_SIZE = 1000000;


    public static abstract class InternState {
        Random r;
        InternDomain<TestObject> internDomain;
        // Need to populate this array and select object to intern from here
        // This ensures Strong refs are maintained to everything that is interned and prevents cleanup when using weak interners
        TestObject[] refs = new TestObject[OBJECT_POPULATION_SIZE];

        @Setup(Level.Trial)
        public void setup() {
            r = new Random();
            internDomain = getInterner();
            // Ensure we have a set of test objects with strong refs
            for (int i = 0; i < OBJECT_POPULATION_SIZE; i++) {
                refs[i] = internDomain.intern(new TestObject(i));
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
    public TestObject benchmarkReadingWeakHashmapInternDomain(WeakHashmapInternDomainState state) {
        return doInterning(state);
    }

    @Benchmark
    public TestObject benchmarkReadingWeakNonBlockingInternDomain(WeakNonBlockingInternDomainState state) {
        return doInterning(state);
    }

    @Benchmark
    public TestObject benchmarkReadingStrongNonSizeLimitedInternDomain(StrongNonSizeLimitedInternState state) {
        return doInterning(state);
    }

    public TestObject doInterning(InternState state) {
        int idx = state.r.nextInt(OBJECT_POPULATION_SIZE);
        TestObject item = new TestObject(idx);
        return state.internDomain.intern(item);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(WeakVsStrongReadingBenchmark.class.getSimpleName())
                .threads(Runtime.getRuntime().availableProcessors() * 4)
                .warmupIterations(5)
                .warmupTime(TimeValue.seconds(2))
                .measurementTime(TimeValue.seconds(2))
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}
