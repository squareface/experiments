package com.squareface.intern;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Andy on 10/02/15.
 */

public abstract class BaseInternDomainTest {

    @Test
    public void testIntern() throws Exception {

        InternDomain<InternDomainTestUtil.TestObject> intDomain = getInternDomain();

        InternDomainTestUtil.TestObject base = new InternDomainTestUtil.TestObject(5);
        InternDomainTestUtil.TestObject second = new InternDomainTestUtil.TestObject(5);

        InternDomainTestUtil.TestObject internedBase = intDomain.intern(base);
        // Identity equals, expect same object back
        Assert.assertTrue("InternDomain did not return same object for first intern call", base == internedBase);


        InternDomainTestUtil.TestObject internedSecond = intDomain.intern(second);
        // Another identity equals, expect InternDomain to return same instance as base
        Assert.assertTrue("InternDomain did not return previously interned instance", internedSecond == base);

    }

    @Test
    public void testInterningSubclass() {
        InternDomain<InternDomainTestUtil.TestObject> internDomain = getInternDomain();

        TestObjectSub first = new TestObjectSub(5, "underhill");
        TestObjectSub second = new TestObjectSub(5, "underhill");

        InternDomainTestUtil.TestObject firstInterned = internDomain.intern(first);
        InternDomainTestUtil.TestObject secondInterned = internDomain.intern(second);

        Assert.assertTrue("InternDomain did not return previously interned instance", firstInterned == secondInterned);
    }

    abstract InternDomain<InternDomainTestUtil.TestObject> getInternDomain();

    @Test
    public abstract void testInterningSingleObjectFromMultipleThreads() throws Exception;

    @Test
    public abstract void testInterningMultipleObjectsFromMultipleThreads() throws Exception;

    private static class TestObjectSub extends InternDomainTestUtil.TestObject {

        private String additionalInfo;

        public TestObjectSub(int currentCount, String additionalInfo) {
            super(currentCount);
            this.additionalInfo = additionalInfo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            TestObjectSub that = (TestObjectSub) o;

            if (additionalInfo != null ? !additionalInfo.equals(that.additionalInfo) : that.additionalInfo != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (additionalInfo != null ? additionalInfo.hashCode() : 0);
            return result;
        }
    }
}
