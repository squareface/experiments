package com.squareface.intern.benchmark;

import com.squareface.intern.InternDomain;
import com.squareface.intern.Interner;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;

/**
 * The idea behind this benchmark is that we only keep Strong references for a fraction of the possible object population.
 * This way we should see a level of cleanup and a mixture of first time interning and returning the previously interned object.
 * Provided the number of intern invocations is much greater than the object population we should cycle through a number of cleanups and generate
 * the same objects a few times before and after pruning.
 */
public class WeakImplementationsBenchmark {

    private static final int OBJECT_POPULATION = 5000000;
    private static final int REF_COUNT = (int) Math.floor(OBJECT_POPULATION * 0.75);


    @State(Scope.Benchmark)
    public static abstract class ReferenceAndStateHolder {

        Random rnd = new Random();
        // Maybe could do with a bigger object here but want to keep the object creation cheap
        InternDomain<TestObject> internDomain;
        TestObject[] refs = new TestObject[OBJECT_POPULATION];

        @Setup
        public void setup() {

            internDomain = getInterner().forDomain(TestObject.class);
            for (int i = 0; i < REF_COUNT; i++) {
                int idx;
                do {
                    idx = rnd.nextInt(OBJECT_POPULATION);
                } while (refs[idx] != null);
                refs[idx] = new TestObject(idx);
            }

        }

        abstract Interner getInterner();


    }

    @State(Scope.Benchmark)
    public static class WeakHashmapInternerState extends ReferenceAndStateHolder {

        @Override
        public Interner getInterner() {
            return Interner.weakHashmapInterner();
        }

    }

    @State(Scope.Benchmark)
    public static class WeakNonBlockingInternerState extends ReferenceAndStateHolder {

        @Override
        public Interner getInterner() {
            return Interner.weakInterner();
        }

    }

    @Benchmark
    public TestObject benchmarkWeakHashmapInterner(WeakHashmapInternerState state) {
        return doIntern(state);
    }

    @Benchmark
    public TestObject benchmarkWeakNonBlockingInterner(WeakNonBlockingInternerState state) {
        return doIntern(state);
    }

    private TestObject doIntern(ReferenceAndStateHolder state) {
        TestObject item = new TestObject(state.rnd.nextInt(OBJECT_POPULATION));
        return state.internDomain.intern(item);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(WeakImplementationsBenchmark.class.getSimpleName())
                .threads(Runtime.getRuntime().availableProcessors() * 4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
