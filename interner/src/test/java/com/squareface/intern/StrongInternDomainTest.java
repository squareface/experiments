package com.squareface.intern;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.squareface.intern.removal.FifoRemovalPolicy;
import com.squareface.intern.removal.NullRemovalPolicy;
import com.squareface.intern.removal.RemovalPolicy;
import com.squareface.intern.removal.TriggeredRemovalPolicy;
import com.squareface.intern.trigger.NeverResizeTriggerPolicy;
import com.squareface.intern.trigger.ResizeTriggerPolicy;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StrongInternDomainTest extends BaseInternDomainTest {

    @Mock
    RemovalPolicy<InternDomainTestUtil.TestObject> mockPolicy;

    public void testInterningSingleObjectFromMultipleThreads() throws Exception {

        int iterationsPerThread = 50000;

        InternDomain<InternDomainTestUtil.TestObject> domain = new StrongInternDomain<>(mockPolicy);
        InternDomainTestUtil.runInterningSingleObjectRepeatedlyFromMultipleThreads(10, iterationsPerThread, domain);

        when(mockPolicy.shouldRemoveItems(eq(domain))).thenReturn(false);

        // Expect to only register distinct items
        verify(mockPolicy, times(1)).registerItem(any(InternDomainTestUtil.TestObject.class));
        verify(mockPolicy, times(1)).shouldRemoveItems(eq(domain));

        // Assertions
        Assert.assertEquals("Unexpected size for domain", 1, domain.size());
        PurgeableInternDomain<InternDomainTestUtil.TestObject> purgeableInternDomain = (PurgeableInternDomain<InternDomainTestUtil.TestObject>) domain;
        Assert.assertEquals("Expected zero purges", 0, purgeableInternDomain.getPurgeCount());

    }

    public void testInterningMultipleObjectsFromMultipleThreads() throws Exception {

        int iterationsPerThread = 50000;
        int objectPopulationSize = 50;

        InternDomain<InternDomainTestUtil.TestObject> domain = new StrongInternDomain<>(mockPolicy);
        InternDomainTestUtil.runInterningMultipleNewObjectsFromMultipleThreads(8, iterationsPerThread, objectPopulationSize, domain);

        when(mockPolicy.shouldRemoveItems(eq(domain))).thenReturn(false);

        // Expect to only register distinct items
        verify(mockPolicy, times(objectPopulationSize)).registerItem(any(InternDomainTestUtil.TestObject.class));
        verify(mockPolicy, times(objectPopulationSize)).shouldRemoveItems(eq(domain));

        // Assertions
        Assert.assertEquals("Unexpected size for domain", objectPopulationSize, domain.size());
        PurgeableInternDomain<InternDomainTestUtil.TestObject> purgeableInternDomain = (PurgeableInternDomain<InternDomainTestUtil.TestObject>) domain;
        Assert.assertEquals("Expected zero purges", 0, purgeableInternDomain.getPurgeCount());
    }

    @Test
    public void testExceptionThrownIfNullRemovalPolicyUsed() {
        try {
            InternDomain<InternDomainTestUtil.TestObject> domain = new StrongInternDomain<>(null);
            Assert.fail("Expected exception thrown when attempting to construct with null RemovalPolicy");
        } catch (NullPointerException e) {
            // Pass test
        }
    }

    @Test
    public void testExceptionThrownIfNullObjectOfferedForInternment() {

        InternDomain<InternDomainTestUtil.TestObject> domain = new StrongInternDomain<>(mockPolicy);
        try {
            domain.intern(null);
            Assert.fail("Expected exception thrown when attempting to intern null");
        } catch (NullPointerException e) {
            // Pass test
        }

    }

    @Test
    public void testPurging() {
        int populationSize = 500;
        double purgeProportion = 0.1;
        int purgeAmount = (int) Math.floor(populationSize * purgeProportion);

        RemovalPolicy<InternDomainTestUtil.TestObject> removalPolicy = new FifoRemovalPolicy<>(NeverResizeTriggerPolicy.INSTANCE);

        InternDomain<InternDomainTestUtil.TestObject> domain = new StrongInternDomain<>(removalPolicy, purgeProportion);
        InternDomainTestUtil.populateDomain(domain, populationSize);

        // Initial state assertions
        Assert.assertEquals("Unexpected size for domain", populationSize, domain.size());
        PurgeableInternDomain<InternDomainTestUtil.TestObject> purgeableInternDomain = (PurgeableInternDomain<InternDomainTestUtil.TestObject>) domain;
        Assert.assertEquals("Expected zero purges", 0, purgeableInternDomain.getPurgeCount());

        purgeableInternDomain.forcePurge();
        Assert.assertEquals("Expected zero purges", 1, purgeableInternDomain.getPurgeCount());
        Assert.assertEquals("Unexpected size for domain", populationSize - purgeAmount, domain.size());

    }

    @Test
    public void testOnlyOneThreadPurgesAtOnce() throws Exception {

        final int populationSize = 1000;
        double purgeProportion = 0.5;

        // Use a resize policy based on count and fill the domain to one below the threshold
        // Then the resize can be triggered by adding a distinct item outside the known population
        // Lifo removal policy means we'll always remove the resize triggering object immediately and the interning
        // threads will fill the domain back up to the threshold
        ResizeTriggerPolicy resizeTriggerPolicy = new ItemCountResizePolicy(populationSize);
        RemovalPolicy<InternDomainTestUtil.TestObject> removalPolicy = new LifoRemovalPolicy<>(resizeTriggerPolicy);

        // Create domain and make sure its populated to the threshold
        final InternDomain<InternDomainTestUtil.TestObject> domain = new StrongInternDomain<>(removalPolicy, purgeProportion);
        InternDomainTestUtil.populateDomain(domain, populationSize);

        // Kick off a bunch of threads constantly interning an identical object population within the resize threshold
        Runnable populateRunnable = new Runnable() {
            @Override
            public void run() {
                InternDomainTestUtil.populateDomain(domain, populationSize);
            }
        };
        int nThreads = 8;
        ScheduledExecutorService interningThreads = Executors.newScheduledThreadPool(nThreads, new ThreadFactoryBuilder().setNameFormat("interner-thread-%d").build());
        for (int i = 0; i < nThreads; i++) {
            interningThreads.scheduleWithFixedDelay(populateRunnable, 0, 1, TimeUnit.MILLISECONDS);
        }

        // Trigger resizing a few times. Can use the same object each time due to LIFO removal policy
        InternDomainTestUtil.TestObject triggerItem = new InternDomainTestUtil.TestObject(populationSize + 1);
        domain.intern(triggerItem);
        domain.intern(triggerItem);
        domain.intern(triggerItem);

        // Shutdown the interning threads
        interningThreads.shutdown();
        boolean terminated = interningThreads.awaitTermination(30, TimeUnit.SECONDS);
        Assert.assertTrue("Interning threads need to terminate before next step", terminated);


        // Maximum purges should be less than or equal to the number of trigger items because a single purge may have removed multiple trigger items
        PurgeableInternDomain<InternDomainTestUtil.TestObject> purgeableInternDomain = (PurgeableInternDomain<InternDomainTestUtil.TestObject>) domain;
        Assert.assertTrue("Purge count exceeded maximum expectation", purgeableInternDomain.getPurgeCount() <= 3);

    }

    @Override
    InternDomain<InternDomainTestUtil.TestObject> getInternDomain() {
        return new StrongInternDomain<>(new NullRemovalPolicy<InternDomainTestUtil.TestObject>());
    }

    public static class LifoRemovalPolicy<T> extends TriggeredRemovalPolicy<T> {

        BlockingDeque<T> deque = new LinkedBlockingDeque<>();

        public LifoRemovalPolicy(ResizeTriggerPolicy resizeTriggerPolicy) {
            super(resizeTriggerPolicy);
        }

        @Override
        public void registerItem(T item) {
            super.registerItem(item);
            deque.addFirst(item);
        }

        @Override
        public T nextKey() {
            return deque.poll();
        }

        @Override
        public Collection<T> nextKeys(int numKeys) {
            List<T> keysToRemove = new ArrayList<>(numKeys);

            T item = deque.poll();
            while (numKeys > 0 && item != null) {
                keysToRemove.add(item);
                numKeys--;
            }

            return keysToRemove;
        }
    }

    public static class ItemCountResizePolicy implements ResizeTriggerPolicy {

        private final int maxCount;

        public ItemCountResizePolicy(int maxCount) {
            this.maxCount = maxCount;
        }

        @Override
        public void updateStats(Object item) {

        }

        @Override
        public boolean triggerResize(InternDomain<?> internDomain) {
            return internDomain.size() > maxCount;
        }
    }
}