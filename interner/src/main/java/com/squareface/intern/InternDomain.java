package com.squareface.intern;

/**
 * Created by Andy on 01/02/15.
 */
public interface InternDomain<T> {

    T intern(T object);

    int size();

}
