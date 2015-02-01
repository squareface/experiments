package com.squareface.intern;

import org.junit.Assert;
import org.junit.Test;

public class NonBlockingInternDomainTest {

    @Test
    public void testEqualsAndHashCodeAgreeForProposedKeyAndInternHolder() {

        Foo foo = new Foo("spam", 99, "balloons");

        NonBlockingInternDomain.InternHolder<Foo> h = new NonBlockingInternDomain.InternHolder<>(foo, null);

        NonBlockingInternDomain.ProposedKey<Foo> p = new NonBlockingInternDomain.ProposedKey<>(foo);

        Assert.assertTrue(h.equals(p));
        Assert.assertTrue(p.equals(h));

        Assert.assertTrue(h.hashCode() == p.hashCode());


    }


    private static class Foo {

        private String f;
        private int b;
        private String foobar;

        public Foo(String f, int b, String foobar) {
            this.f = f;
            this.b = b;
            this.foobar = foobar;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Foo foo = (Foo) o;

            if (b != foo.b) return false;
            if (f != null ? !f.equals(foo.f) : foo.f != null) return false;
            if (foobar != null ? !foobar.equals(foo.foobar) : foo.foobar != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = f != null ? f.hashCode() : 0;
            result = 31 * result + b;
            result = 31 * result + (foobar != null ? foobar.hashCode() : 0);
            return result;
        }
    }

    @Test
    public void testIntern() throws Exception {

        NonBlockingInternDomain<Integer> intDomain = new NonBlockingInternDomain<>();

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
        InternDomainTestUtil.runInterningWithGCTest(5000000, new NonBlockingInternDomain<InternDomainTestUtil.TestObject>());
    }

    @Test
    public void testInterningFromMultipleThreads() {
        InternDomainTestUtil.runInterningFromMultipleThreads(5000000, new NonBlockingInternDomain<InternDomainTestUtil.TestObject>());
    }

    @Test
    public void testInterningMultipleNewObjectsFromMultipleThreads() {
        long start = System.nanoTime();
        InternDomainTestUtil.runInterningMultipleNewObjectsFromMultipleThreads(50000000, new NonBlockingInternDomain<InternDomainTestUtil.TestObject>());
        long elapsed = System.nanoTime() - start;
        System.out.println("Elapsed: " + elapsed);
    }


}