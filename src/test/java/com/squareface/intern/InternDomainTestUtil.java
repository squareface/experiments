package com.squareface.intern;

import org.junit.Assert;

import java.util.ArrayList;
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

    public static void runInterningWithGCTest(int count, InternDomain<TestObject> internDomain) {
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


        TestObject current = new TestObject(0);
        TestObject baseInterned = internDomain.intern(current);
        Assert.assertEquals(baseInterned, current);
        for (int i = 0; i < count; i++) {
            // Every 5 repeats advance and intern new value
            if (i != 0 && i % 5 == 0) {
                current = new TestObject(i);
                baseInterned = internDomain.intern(current);
            }

            TestObject newObject = new TestObject(current.getCurrentCount());
            TestObject interned = internDomain.intern(newObject);
            Assert.assertTrue(baseInterned == interned);

        }

        System.out.println(internDomain.size());


    }


    public static void runInterningFromMultipleThreads(final int count, final InternDomain<TestObject> internDomain) {

        int nThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

        // Create strong reference to something
        // Ensure all threads get same reference back

        final TestObject object = new TestObject(0);
        Assert.assertEquals(object, internDomain.intern(object));

        Runnable r = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < count; i++) {
                    TestObject temp = new TestObject(0);
                    TestObject interned = internDomain.intern(temp);
                    Assert.assertEquals(object, interned);
                }
            }
        };

        for (int i = 0; i < nThreads; i++) {
            executorService.execute(r);
        }


    }

    public static void runInterningMultipleNewObjectsFromMultipleThreads(final int nObjects, final InternDomain<TestObject> internDomain) {

        int nThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);

        // In multiple threads, create known test objects
        // Assert that all arrays returned are identical
        Callable<TestObject[]> creator = new Callable<TestObject[]>() {
            @Override
            public TestObject[] call() throws Exception {
                Random random = new Random();
                TestObject[] created = new TestObject[nObjects];
                for (int i = 0; i < created.length; i++) {
                    int g = random.nextInt(30);
                    TestObject newObject = new TestObject(g);
                    created[g] = internDomain.intern(newObject);
                }
                return created;
            }
        };

        List<Future<TestObject[]>> futures = new ArrayList<>();
        for (int i = 0; i < nThreads; i++) {
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
