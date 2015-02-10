package com.squareface.intern;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Created by Andy on 30/01/15.
 */
public class WeakHashmapInternDomain<T> implements InternDomain<T> {

    private WeakHashMap<T, WeakReference<T>> pool = new WeakHashMap<>();


    public synchronized T intern(T object) {

        T result;
        do {
            WeakReference<T> ref = pool.get(object);
            if (ref == null) {
                ref = new WeakReference<>(object);
                pool.put(object, ref);
                result = object;
            } else {
                result = ref.get();
            }
        } while (result == null);

        return result;

    }

    @Override
    public synchronized int size() {
        return pool.size();
    }

}
