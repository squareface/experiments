package com.squareface.intern;

/**
 * Created by Andy on 09/02/15.
 */
public interface PurgeableInternDomain<T> extends InternDomain<T> {

    int getPurgeCount();

    void forcePurge();

}
