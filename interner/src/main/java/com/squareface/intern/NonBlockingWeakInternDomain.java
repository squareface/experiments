package com.squareface.intern;

import com.google.common.base.Preconditions;
import org.cliffc.high_scale_lib.NonBlockingHashMap;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * Created by Andy on 31/01/15.
 */
public class NonBlockingWeakInternDomain<T> implements InternDomain<T> {

    private NonBlockingHashMap<InternHolder<T>, InternHolder<T>> pool = new NonBlockingHashMap<>();
    private ReferenceQueue<T> queue = new ReferenceQueue<>();

    static class ProposedKey<K> {

        private K o;

        public ProposedKey(K o) {
            this.o = o;
        }

        K getUnderlying() {
            return o;
        }

        @Override
        public boolean equals(Object o1) {
            if (!(o1 instanceof InternHolder || o1 instanceof ProposedKey))
                return false;


            K r1 = getUnderlying();
            Object r2;
            if (o1.getClass() == ProposedKey.class) {
                r2 = ((ProposedKey) o1).getUnderlying();
            } else {
                r2 = ((InternHolder<?>) o1).get();
            }

            if (r1 == r2 || (r1 != null && r1.equals(r2))) {
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return o != null ? o.hashCode() : 0;
        }
    }

    static class InternHolder<K> extends WeakReference<K> {

        private int hash;

        public InternHolder(K referent, ReferenceQueue<K> queue) {
            super(referent, queue);
            hash = referent != null ? referent.hashCode() : 0;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof InternHolder || o instanceof ProposedKey))
                return false;


            K r1 = get();
            Object r2;
            if (o.getClass() == ProposedKey.class) {
                r2 = ((ProposedKey) o).getUnderlying();
            } else {
                r2 = ((InternHolder<?>) o).get();
            }

            if (r1 == r2 || (r1 != null && r1.equals(r2))) {
                return true;
            }
            return false;

        }

        @Override
        public int hashCode() {
            return hash;
        }
    }


    public T intern(T object) {

        Preconditions.checkNotNull(object);

        // Test existence in the map using a key that doesn't register
        // with the ReferenceQueue
        ProposedKey<T> proposed = new ProposedKey<>(object);
        T value;
        do {

            // Cheap test using a non weak reference based key
            InternHolder<T> existing = pool.get(proposed);
            if (existing != null) {
                value = existing.get();
            } else {

                InternHolder<T> realKey = new InternHolder<>(object, queue);
                existing = pool.putIfAbsent(realKey, realKey);

                if (existing != null) {
                    // Someone else beat us to it, just take the interned value
                    value = existing.get();
                } else {

                    // Success! First time interned, do some cleanup
                    Reference<? extends T> ref = queue.poll();
                    while (ref != null) {
                        InternHolder<T> emptyHolder = (InternHolder<T>) ref;
                        pool.remove(emptyHolder);

                        ref = queue.poll();
                    }

                    value = object;
                }

            }

            // if value is null the entry has already been collected and we need to re-insert
        } while (value == null);

        return value;

    }

    public int size() {
        return pool.size();
    }

}
