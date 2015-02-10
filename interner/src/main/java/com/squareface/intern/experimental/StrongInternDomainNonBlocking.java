package com.squareface.intern.experimental;

import com.google.common.base.Preconditions;
import com.squareface.intern.PurgeableInternDomain;
import com.squareface.intern.removal.RemovalPolicy;
import org.cliffc.high_scale_lib.NonBlockingHashMap;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Andy on 03/02/15.
 */
public class StrongInternDomainNonBlocking<T> implements PurgeableInternDomain<T> {

    private static final double CLEANUP_PROPORTION = 0.25;

    private NonBlockingHashMap<T, T> pool = new NonBlockingHashMap<>();
    private RemovalPolicy<T> removalPolicy;
    private AtomicInteger itemsToRemove = new AtomicInteger();
    private AtomicBoolean resizing = new AtomicBoolean(false);
    private AtomicInteger purgeCount = new AtomicInteger();

    public StrongInternDomainNonBlocking(RemovalPolicy<T> removalPolicy) {
        Preconditions.checkNotNull(removalPolicy);
        this.removalPolicy = removalPolicy;
    }

    @Override
    public T intern(T object) {

        Preconditions.checkNotNull(object);

        T interned = pool.putIfAbsent(object, object);

        if (interned == null) {
            // New insert, add to the removalPolicy
            removalPolicy.registerItem(object);
            interned = object;
            // One thread decides we should resize after inserting an item
            if (removalPolicy.shouldRemoveItems(this)) {
                itemsToRemove.getAndAdd((int) Math.floor(pool.size() * CLEANUP_PROPORTION));
                resizing.set(true);
            }
        }

        while (itemsToRemove.get() > 0) {
            // Everyone helps do a bit of resizing


        }

        return interned;

    }

    private void checkSizeAndCleanup() {

        if (removalPolicy.shouldRemoveItems(this)) {
            int numElementsToRemove = (int) Math.floor(pool.size() * CLEANUP_PROPORTION);
            for (int i = 0; i < numElementsToRemove; i++) {
                T item = removalPolicy.nextKey();
                if (item == null) {
                    return;
                }
                pool.remove(item);
                // Removing doesn't actually cause the NBHM to resize
                // Need to clone to regain the space
                pool = (NonBlockingHashMap<T, T>) pool.clone();
                itemsToRemove.incrementAndGet();
            }
        }

    }

    @Override
    public int size() {
        return pool.size();
    }

    @Override
    public int getPurgeCount() {
        return purgeCount.get();
    }

    @Override
    public void forcePurge() {

    }
}
