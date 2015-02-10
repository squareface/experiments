package com.squareface.intern.removal;

import com.squareface.intern.InternDomain;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by Andy on 10/02/15.
 */
public class NullRemovalPolicy<T> implements RemovalPolicy<T> {

    @Override
    public boolean shouldRemoveItems(InternDomain<T> domain) {
        return false;
    }

    @Override
    public void registerItem(T item) {

    }

    @Override
    public T nextKey() {
        return null;
    }

    @Override
    public Collection<T> nextKeys(int numKeys) {
        return Collections.emptyList();
    }
}
