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
 * The idea of this benchmark is to measure the difference
 */
public class ComposedVsDirectIgnored {

    private static final int OBJECT_POPULATION = 100000;
    private static final long DOMAIN_SIZE = 50 * 1024 * 1024;


    @State(Scope.Benchmark)
    public static abstract class ReferenceAndStateHolder {

        Random rnd = new Random();
        // Maybe could do with a bigger object here but want to keep the object creation cheap
        InternDomain<TestObject> internDomain;

        @Setup
        public void setup() {
            internDomain = getInterner().forDomain(TestObject.class);
        }

        abstract Interner getInterner();


    }

    @State(Scope.Benchmark)
    public static class ComposeStrongInternerState extends ReferenceAndStateHolder {

        @Override
        public Interner getInterner() {
            return Interner.strongInterner().fifoRemoval().withFixedMaximumSize(DOMAIN_SIZE).build();
        }

    }

    @State(Scope.Benchmark)
    public static class DirectImplStrongInternerState extends ReferenceAndStateHolder {

        @Override
        public Interner getInterner() {
            return null;
        }

    }

    @Benchmark
    public TestObject benchmarkComposedStrongRefInterner(ComposeStrongInternerState state) {
        return doIntern(state);
    }

    @Benchmark
    public TestObject benchmarkDirectImplStrongRefInterner(DirectImplStrongInternerState state) {
        return doIntern(state);
    }

    private TestObject doIntern(ReferenceAndStateHolder state) {
        TestObject item = new TestObject(state.rnd.nextInt(OBJECT_POPULATION));
        return state.internDomain.intern(item);
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ComposedVsDirectIgnored.class.getSimpleName())
                .threads(Runtime.getRuntime().availableProcessors() * 4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }


}
