package com.squareface.intern.experimental;

import com.google.common.base.Preconditions;
import com.squareface.intern.PurgeableInternDomain;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.openjdk.jol.info.GraphLayout;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Andy on 03/02/15.
 */
public class StrongInternDomainNonOrderedCollection<T> implements PurgeableInternDomain<T> {

    private static final double CLEANUP_PROPORTION = 0.25;
    private static final int SAMPLE_FREQ = 500; // Sample an object size every n insertions

    private final long maxSize;
    private AtomicReference<Stats> stats = new AtomicReference<>(new Stats(0, 0));
    private NonBlockingHashMap<T, T> pool = new NonBlockingHashMap<>();
    private AtomicInteger purgeCount = new AtomicInteger();

    public StrongInternDomainNonOrderedCollection(long maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public T intern(T object) {

        Preconditions.checkNotNull(object);

        T interned = pool.putIfAbsent(object, object);

        if (interned == null) {
            // First time inserting
            interned = object;
            if (pool.size() % SAMPLE_FREQ == 0) {
                updateStats(object);
            }
            checkSizeAndCleanup();
        }

        return interned;

    }

    private void updateStats(T object) {


        long newObjectSize = GraphLayout.parseInstance(object).totalSize();

        Stats oldStats;
        Stats newStats;
        do {

            oldStats = stats.get();
            int newSamples = oldStats.samples + 1;
            long newAverage = (newObjectSize + oldStats.samples * oldStats.averageSize) / newSamples;

            newStats = new Stats(newSamples, newAverage);

        } while (!stats.compareAndSet(oldStats, newStats));

    }

    private void checkSizeAndCleanup() {

        long currentAv = stats.get().averageSize;
        long estimatedTotal = pool.size() * currentAv;

        if (estimatedTotal > maxSize) {
            doPurge();
        }

    }

    private void doPurge() {
        int numElementsToRemove = (int) Math.floor(pool.size() * CLEANUP_PROPORTION);
        Iterator<T> keyIterator = pool.keySet().iterator();
        for (int i = 0; i < numElementsToRemove; i++) {
            if (!keyIterator.hasNext()) {
                break;
            }
            T key = keyIterator.next();
            pool.remove(key);
        }
        // Removing doesn't actually cause the NBHM to resize
        // Need to clone to regain the space
        pool = (NonBlockingHashMap<T, T>) pool.clone();
        purgeCount.incrementAndGet();
    }

    @Override
    public int size() {
        return pool.size();
    }

    private static class Stats {

        private int samples;
        private long averageSize;

        public Stats(int samples, long averageSize) {
            this.samples = samples;
            this.averageSize = averageSize;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Stats stats = (Stats) o;

            if (averageSize != stats.averageSize) return false;
            if (samples != stats.samples) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = samples;
            result = 31 * result + (int) (averageSize ^ (averageSize >>> 32));
            return result;
        }
    }

    @Override
    public int getPurgeCount() {
        return purgeCount.get();
    }

    @Override
    public void forcePurge() {
        doPurge();
    }
}
