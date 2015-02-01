package com.squareface.intern;

import org.junit.Assert;
import org.junit.Test;

public class SimpleInternDomainTest {

    @Test
    public void testIntern() throws Exception {

        SimpleInternDomain<Integer> intDomain = new SimpleInternDomain<>();

        Integer base = new Integer(5);
        Integer second = new Integer(5);

        Integer internedBase = intDomain.intern(base);
        // Identity equals, expect same object back
        Assert.assertTrue("InternDomain did not return same object for first intern call", base == internedBase);


        Integer internedSecond = intDomain.intern(second);
        // Another identity equals, expect InternDomain to return same instance as base
        Assert.assertTrue("InternDomain did not return previously interned instance", internedSecond == base);

    }

    @Test
    public void testInterningWithGarbageCollection() {
        InternDomainTestUtil.runInterningWithGCTest(5000000, new SimpleInternDomain<InternDomainTestUtil.TestObject>());
    }


    @Test
    public void testInterningFromMultipleThreads() {
        InternDomainTestUtil.runInterningFromMultipleThreads(5000000, new SimpleInternDomain<InternDomainTestUtil.TestObject>());
    }

    @Test
    public void testInterningMultipleNewObjectsFromMultipleThreads() {
        InternDomainTestUtil.runInterningMultipleNewObjectsFromMultipleThreads(5000000, new SimpleInternDomain<InternDomainTestUtil.TestObject>());
    }


}