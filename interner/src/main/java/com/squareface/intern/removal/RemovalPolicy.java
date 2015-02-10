package com.squareface.intern.removal;

import com.squareface.intern.InternDomain;

import java.util.Collection;


public interface RemovalPolicy<T> {

    boolean shouldRemoveItems(InternDomain<T> domain);

    void registerItem(T item);

    T nextKey();

    Collection<T> nextKeys(int numKeys);

}
