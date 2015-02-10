package com.squareface.intern;

import com.google.common.base.Preconditions;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Andy on 01/02/15.
 */
public class InternDomainTestUtil {

    public static void runInterningWithGCTest(int populationSize, InternDomain<TestObject> internDomain) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ie) {
                        // ignore and loop
                    }
                    Runtime.getRuntime().gc();
                }
            }
        });
        t.start();


        for (int i = 0; i < populationSize; i++) {
            TestObject current = new TestObject(i);
            TestObject baseInterned = internDomain.intern(current);
            Assert.assertTrue(baseInterned == current);
            for (int j = 0; j < 5; j++) {
                TestObject newObject = new TestObject(current.getCurrentCount());
                TestObject interned = internDomain.intern(newObject);
                Assert.assertTrue(baseInterned == interned);
            }


        }

        Assert.assertTrue(internDomain.size() < populationSize);


    }


    public static void runInterningSingleObjectRepeatedlyFromMultipleThreads(final int threadCount,
                                                                             final int iterationsPerThread,
                                                                             final InternDomain<TestObject> internDomain) {

        Preconditions.checkArgument(threadCount > 1, "Test must be run with multiple threads");
        Preconditions.checkArgument(iterationsPerThread > 1, "iterationsPerThread must be > 1");

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        // Create strong reference to something
        // Ensure all threads get same reference back
        final TestObject object = new TestObject(0);
        Assert.assertEquals(object, internDomain.intern(object));

        Runnable r = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < iterationsPerThread; i++) {
                    TestObject temp = new TestObject(0);
                    TestObject interned = internDomain.intern(temp);
                    Assert.assertEquals(object, interned);
                }
            }
        };

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(r);
        }


    }

    public static void runInterningMultipleNewObjectsFromMultipleThreads(int threadCount,
                                                                         final int iterationsPerThread,
                                                                         final int objectPopulationSize,
                                                                         final InternDomain<TestObject> internDomain) {

        Preconditions.checkArgument(threadCount > 1, "Test must be run with multiple threads");
        Preconditions.checkArgument(iterationsPerThread >= 10 * objectPopulationSize,
                "iterationsPerThread must be a significant multiple of objectPopulationSize - at least 10x");

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // In multiple threads, create known test objects
        // Assert that all arrays returned are identical i.e. all threads actually end up with the same interned objects
        Callable<TestObject[]> creator = new Callable<TestObject[]>() {
            @Override
            public TestObject[] call() throws Exception {
                Random random = new Random();
                TestObject[] created = new TestObject[objectPopulationSize];
                for (int i = 0; i < iterationsPerThread; i++) {
                    int g = random.nextInt(objectPopulationSize);
                    TestObject newObject = new TestObject(g);
                    // Presence of these Strong refs will prevent refs being removed in a weak domain
                    created[g] = internDomain.intern(newObject);
                }
                return created;
            }
        };

        List<Future<TestObject[]>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Future<TestObject[]> f = executor.submit(creator);
            futures.add(f);
        }

        TestObject[] baseLine = null;
        for (Future<TestObject[]> result : futures) {
            try {
                if (baseLine == null) {
                    baseLine = result.get();
                } else {
                    Assert.assertArrayEquals(baseLine, result.get());
                }
            } catch (Exception e) {
                Assert.fail();
            }
        }

    }

    public static void populateDomainWithVariableSizeObjects(InternDomain<VariableSizeTestObject> internDomain, int populationSize) {

        for (int i = 0; i < populationSize; i++) {
            VariableSizeTestObject item = new VariableSizeTestObject(i);
            internDomain.intern(item);
        }

    }

    public static void populateDomain(InternDomain<TestObject> internDomain, int populationSize) {

        for (int i = 0; i < populationSize; i++) {
            TestObject item = new TestObject(i);
            internDomain.intern(item);
        }

    }

    static class VariableSizeTestObject extends TestObject {

        private TestObject[] packing;

        public VariableSizeTestObject(int currentCount) {
            super(currentCount);
            packing = new TestObject[currentCount];
            for (int i = 0; i < currentCount; i++) {
                // Use new String to ensure
                packing[i] = new TestObject(i);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            VariableSizeTestObject that = (VariableSizeTestObject) o;

            if (!Arrays.equals(packing, that.packing)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (packing != null ? Arrays.hashCode(packing) : 0);
            return result;
        }
    }

    static class TestObject {
        private int currentCount;

        public TestObject(int currentCount) {
            this.currentCount = currentCount;
        }

        public int getCurrentCount() {
            return currentCount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestObject that = (TestObject) o;

            if (currentCount != that.currentCount) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return currentCount;
        }
    }
}
