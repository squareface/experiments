package com.squareface.intern;

import org.junit.Test;
import org.openjdk.jol.info.GraphLayout;
import org.openjdk.jol.util.VMSupport;

import java.util.Random;

public class SizeLimitedInternDomainTest {

    @Test
    public void testIntern() throws Exception {

        System.out.println(VMSupport.vmDetails());

        int count = 2000;
        InternDomain<LargeTestObject> internDomain = new SizeLimitedInternDomain<>(0.01, SizeLimitedInternDomain.CleanupPolicy.SIZE_ORDER);
        for (int i = 0; i < count; i++) {
            LargeTestObject object = new LargeTestObject(i);
            internDomain.intern(object);

            if (i % 100 == 0) {
                System.out.println("Objects created=" + i);
                System.out.println("Total memory=" + Runtime.getRuntime().totalMemory() / (1024 * 1024));
                System.out.println("Free memory=" + Runtime.getRuntime().freeMemory() / (1024 * 1024));
                System.out.println("Max memory=" + Runtime.getRuntime().maxMemory() / (1024 * 1024));
                System.out.println(GraphLayout.parseInstance(internDomain).toFootprint());
            }

        }


    }

    private static class LargeTestObject {

        private String[] strs;
        private Integer count;

        public LargeTestObject(Integer count) {
            Random random = new Random();
            this.strs = new String[random.nextInt(200)];
            for (int i = 0; i < strs.length; i++) {
                strs[i] = new String("abcdefghijklmnopqrstuvwxyz");
            }
            this.count = count;
        }

        @Override
        public boolean equals(Object o) {
            return this == o;
        }

        @Override
        public int hashCode() {
            return count != null ? count.hashCode() : 0;
        }
    }
}