package com.squareface.intern;


import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Andy on 01/02/15.
 */
public class Interner {

    private static ConcurrentHashMap<Class<?>, InternDomain<?>> DOMAINS = new ConcurrentHashMap<>();


    public static <T> InternDomain<T> getInterner(Class<T> clazz) {

        InternDomain<T> domain = (InternDomain<T>) DOMAINS.get(clazz);
        if (domain == null) {
            domain = (InternDomain<T>) DOMAINS.putIfAbsent(clazz, new NonBlockingInternDomain<T>());
        }
        return domain;

    }


}
