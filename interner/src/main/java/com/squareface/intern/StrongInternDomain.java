package com.squareface.intern;

import com.google.common.base.Preconditions;
import com.squareface.intern.removal.RemovalPolicy;
import org.cliffc.high_scale_lib.NonBlockingHashMap;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Andy on 03/02/15.
 */
public class StrongInternDomain<T> implements PurgeableInternDomain<T> {

    private static final double DEFAULT_CLEANUP = 0.1;

    private final RemovalPolicy<T> removalPolicy;
    private final double purgeProportion;

    private NonBlockingHashMap<T, T> pool = new NonBlockingHashMap<>();

    // Updates to the purgeCount are always protected by the AtomicBoolean so the variable
    // will not be updated in multiple threads
    private volatile int purgeCount = 0;
    private AtomicBoolean purging = new AtomicBoolean(false);

    public StrongInternDomain(RemovalPolicy<T> removalPolicy) {
        this(removalPolicy, DEFAULT_CLEANUP);
    }

    public StrongInternDomain(RemovalPolicy<T> removalPolicy, double purgeProportion) {
        Preconditions.checkNotNull(removalPolicy);
        Preconditions.checkArgument(purgeProportion > 0 && purgeProportion < 1, "purgeProportion must have value p where 0 < p < 1");
        this.removalPolicy = removalPolicy;
        this.purgeProportion = purgeProportion;
    }

    @Override
    public T intern(T object) {

        Preconditions.checkNotNull(object);

        T interned = pool.putIfAbsent(object, object);

        if (interned == null) {
            // New insert, add to the removalPolicy
            removalPolicy.registerItem(object);
            interned = object;

            if (removalPolicy.shouldRemoveItems(this)) {
                doPurge();
            }

        }

        return interned;

    }

    private void doPurge() {

        // Only one thread will ever be purging. If its already in progress just move on
        if (!purging.compareAndSet(false, true)) {
            return;
        }
        int numElementsToRemove = (int) Math.floor(pool.size() * purgeProportion);
        int removalCount = 0;
        for (int i = 0; i < numElementsToRemove; i++) {
            T item = removalPolicy.nextKey();
            if (item == null) {
                break;
            }
            pool.remove(item);
            removalCount++;
        }
        // Removing doesn't actually cause the NBHM to resize
        // Need to clone to regain the space
        if (removalCount > 0) {
            pool = (NonBlockingHashMap<T, T>) pool.clone();
            purgeCount++;
        }
        purging.set(false);
    }

    @Override
    public int size() {
        return pool.size();
    }

    @Override
    public int getPurgeCount() {
        return purgeCount;
    }

    @Override
    public void forcePurge() {
        doPurge();
    }
}
