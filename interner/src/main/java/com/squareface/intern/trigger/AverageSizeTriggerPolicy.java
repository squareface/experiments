package com.squareface.intern.trigger;

import com.squareface.intern.InternDomain;
import org.openjdk.jol.info.GraphLayout;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Andy on 10/02/15.
 */
public abstract class AverageSizeTriggerPolicy implements ResizeTriggerPolicy {

    private static final int SAMPLE_FREQ = 500; // Sample an object size every n registrations

    private AtomicReference<Stats> stats = new AtomicReference<>(new Stats(0, 0));
    private AtomicInteger count = new AtomicInteger();

    @Override
    public void updateStats(Object item) {
        if (count.incrementAndGet() % SAMPLE_FREQ == 0) {
            doAdd(item);
        }
    }

    private void doAdd(Object item) {

        long newObjectSize = GraphLayout.parseInstance(item).totalSize();

        Stats oldStats;
        Stats newStats;
        do {

            oldStats = stats.get();
            int newSamples = oldStats.samples + 1;
            long newAverage = (newObjectSize + oldStats.samples * oldStats.averageSize) / newSamples;

            newStats = new Stats(newSamples, newAverage);

        } while (!stats.compareAndSet(oldStats, newStats));
    }

    public Stats getStats() {
        return stats.get();
    }

    protected static class Stats {

        private int samples;
        private long averageSize;

        public Stats(int samples, long averageSize) {
            this.samples = samples;
            this.averageSize = averageSize;
        }

        public long calculateDomainSize(InternDomain domain) {
            return domain.size() * averageSize;
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

}
