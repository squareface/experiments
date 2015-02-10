package com.squareface.intern;

import org.junit.Test;

public class WeakHashmapInternDomainTest extends BaseInternDomainTest {

    @Override
    InternDomain<InternDomainTestUtil.TestObject> getInternDomain() {
        return new WeakHashmapInternDomain<>();
    }

    public void testInterningSingleObjectFromMultipleThreads() throws Exception {

        int iterationsPerThread = 50000;

        InternDomain<InternDomainTestUtil.TestObject> domain = new WeakHashmapInternDomain<>();
        InternDomainTestUtil.runInterningSingleObjectRepeatedlyFromMultipleThreads(10, iterationsPerThread, domain);
    }

    public void testInterningMultipleObjectsFromMultipleThreads() throws Exception {
        int iterationsPerThread = 50000;
        int objectPopulationSize = 50;

        InternDomain<InternDomainTestUtil.TestObject> domain = new WeakHashmapInternDomain<>();
        InternDomainTestUtil.runInterningMultipleNewObjectsFromMultipleThreads(8, iterationsPerThread, objectPopulationSize, domain);
    }


    @Test
    public void testInterningWithGarbageCollection() {
        InternDomainTestUtil.runInterningWithGCTest(50000, new WeakHashmapInternDomain<InternDomainTestUtil.TestObject>());
    }


}