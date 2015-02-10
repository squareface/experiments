package com.squareface.intern;

import org.junit.Assert;
import org.junit.Test;

public class NonBlockingWeakInternDomainTest extends BaseInternDomainTest {

    @Override
    InternDomain<InternDomainTestUtil.TestObject> getInternDomain() {
        return new NonBlockingWeakInternDomain<>();
    }

    public void testInterningSingleObjectFromMultipleThreads() throws Exception {

        int iterationsPerThread = 50000;

        InternDomain<InternDomainTestUtil.TestObject> domain = new NonBlockingWeakInternDomain<>();
        InternDomainTestUtil.runInterningSingleObjectRepeatedlyFromMultipleThreads(10, iterationsPerThread, domain);
    }

    public void testInterningMultipleObjectsFromMultipleThreads() throws Exception {
        int iterationsPerThread = 50000;
        int objectPopulationSize = 50;

        InternDomain<InternDomainTestUtil.TestObject> domain = new NonBlockingWeakInternDomain<>();
        InternDomainTestUtil.runInterningMultipleNewObjectsFromMultipleThreads(8, iterationsPerThread, objectPopulationSize, domain);
    }


    @Test
    public void testEqualsAndHashCodeAgreeForProposedKeyAndInternHolder() {

        InternDomainTestUtil.TestObject item = new InternDomainTestUtil.TestObject(999);

        NonBlockingWeakInternDomain.InternHolder<InternDomainTestUtil.TestObject> h = new NonBlockingWeakInternDomain.InternHolder<>(item, null);

        NonBlockingWeakInternDomain.ProposedKey<InternDomainTestUtil.TestObject> p = new NonBlockingWeakInternDomain.ProposedKey<>(item);

        Assert.assertTrue(h.equals(p));
        Assert.assertTrue(p.equals(h));

        Assert.assertTrue(h.hashCode() == p.hashCode());

    }

    @Test
    public void testInterningWithGarbageCollection() {
        InternDomainTestUtil.runInterningWithGCTest(50000, new NonBlockingWeakInternDomain<InternDomainTestUtil.TestObject>());
    }


}