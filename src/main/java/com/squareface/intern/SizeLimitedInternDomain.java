package com.squareface.intern;

import com.google.common.base.Preconditions;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.openjdk.jol.info.GraphLayout;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by Andy on 03/02/15.
 */
public class SizeLimitedInternDomain<T> implements InternDomain<T> {

    private static final double CLEANUP_PROPORTION = 0.25;

    private NonBlockingHashMap<T, T> pool = new NonBlockingHashMap<>();
    private BlockingQueue<T> cleanupQueue;
    private final double maxProportion;

    public SizeLimitedInternDomain(double maxProportion, CleanupPolicy cleanupPolicy) {
        if (maxProportion <= 0 || maxProportion >= 1) {
            throw new IllegalArgumentException("maxProportion must be specified as a fraction i.e. 0 < maxProportion < 1");
        }
        this.maxProportion = maxProportion;
        this.cleanupQueue = cleanupPolicy.getQueue();
    }

    @Override
    public T intern(T object) {

        Preconditions.checkNotNull(object);

        T interned = pool.putIfAbsent(object, object);

        if (interned == null) {
            // New insert, add to the cleanupQueue
            cleanupQueue.add(object);
            interned = object;

            checkSizeAndCleanup();


        }

        return interned;

    }

    private void checkSizeAndCleanup() {
        long maxHeap = Runtime.getRuntime().maxMemory();

        if ((double) determineObjectSize() / maxHeap > maxProportion) {
            int cleanupCount = (int) Math.floor(cleanupQueue.size() * CLEANUP_PROPORTION);
            for (int i = 0; i < cleanupCount; i++) {
                T item = cleanupQueue.poll();
                if (item == null) {
                    return;
                }
                pool.remove(item);
                // Removing doesn't actually cause the NBHM to resize
                // Need to clone to regain the space
                pool = (NonBlockingHashMap<T, T>) pool.clone();
            }
        }

    }

    private long determineObjectSize() {
        return GraphLayout.parseInstance(this).totalSize();
    }


    @Override
    public int size() {
        return pool.size();
    }

    public static enum CleanupPolicy {

        SIZE_ORDER {
            @Override
            <T> BlockingQueue<T> getQueue() {
                // Can't specify
                return new PriorityBlockingQueue<>(11, new ObjectSizeComparator<>());
            }
        },
        FIFO {
            @Override
            <T> BlockingQueue<T> getQueue() {
                return new LinkedBlockingQueue<>();
            }
        };

        abstract <T> BlockingQueue<T> getQueue();

    }

}
