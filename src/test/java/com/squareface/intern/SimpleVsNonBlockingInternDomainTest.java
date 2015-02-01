package com.squareface.intern;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Andy on 01/02/15.
 */
public class SimpleVsNonBlockingInternDomainTest {

    @Test
    public void testPerformance() {
        int count = 5;
        Stats[] stats = new Stats[count];

        for (int i = 0; i < count; i++) {
            stats[i] = runSingleTiming();
        }

        for (Stats s : stats) {
            System.out.println(s);
        }

    }

    private Stats runSingleTiming() {

        int count = 50000000;

        SimpleInternDomain<InternDomainTestUtil.TestObject> simpleDomain = new SimpleInternDomain<>();
        long start = System.nanoTime();
        InternDomainTestUtil.runInterningMultipleNewObjectsFromMultipleThreads(count, simpleDomain);
        long simpleTime = System.nanoTime() - start;

        NonBlockingInternDomain<InternDomainTestUtil.TestObject> nbDomain = new NonBlockingInternDomain<>();
        start = System.nanoTime();
        InternDomainTestUtil.runInterningMultipleNewObjectsFromMultipleThreads(count, nbDomain);
        long nbTime = System.nanoTime() - start;

        Assert.assertTrue(simpleTime > nbTime);
        long diff = (simpleTime - nbTime) * 100 / simpleTime;
        System.out.println(String.format("Simple time: %d, Non-blocking time: %d. Improvement: %d %%", simpleTime, nbTime, diff));
        return new Stats(simpleTime, nbTime);
    }


    private static class Stats {

        private long simple;
        private long nonBlocking;

        public Stats(long simple, long nonBlocking) {
            this.simple = simple;
            this.nonBlocking = nonBlocking;
        }

        public long diff() {
            return simple - nonBlocking;
        }

        public long percentage() {
            return (simple - nonBlocking) * 100 / simple;
        }

        @Override
        public String toString() {
            return "Stats{" +
                    "simple=" + simple +
                    ", nonBlocking=" + nonBlocking +
                    ", diff=" + diff() +
                    ", percent=" + percentage() +
                    '}';
        }
    }

}
