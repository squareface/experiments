package com.squareface.intern;


import com.google.common.base.Preconditions;
import com.squareface.intern.removal.FifoRemovalPolicy;
import com.squareface.intern.removal.NullRemovalPolicy;
import com.squareface.intern.removal.RemovalPolicy;
import com.squareface.intern.removal.SizeOrderedRemovalPolicy;
import com.squareface.intern.trigger.FixedMaximumTriggerPolicy;
import com.squareface.intern.trigger.HeapProportionTriggerPolicy;
import com.squareface.intern.trigger.NeverResizeTriggerPolicy;
import com.squareface.intern.trigger.ResizeTriggerPolicy;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Andy on 01/02/15.
 */
public class Interner {

    private DomainCreator domainCreator;
    private ConcurrentHashMap<Class<?>, InternDomain<?>> DOMAINS = new ConcurrentHashMap<>();

    private Interner(DomainCreator domainCreator) {
        this.domainCreator = domainCreator;
    }


    public <T> InternDomain<T> forDomain(Class<T> clazz) {

        // Quick check avoids unnecessary object creation
        InternDomain<T> domain = (InternDomain<T>) DOMAINS.get(clazz);
        if (domain == null) {
            InternDomain<T> potentialDomain = domainCreator.createDomain(clazz);
            domain = (InternDomain<T>) DOMAINS.putIfAbsent(clazz, potentialDomain);
            if (domain == null) {
                domain = potentialDomain;
            }
        }
        return domain;

    }

    public static Interner weakInterner() {
        return new Interner(new WeakRefsDomainCreator());
    }

    public static Interner weakHashmapInterner() {
        return new Interner(new WeakHashmapDomainCreator());
    }

    public static StrongInternerBuilder strongInterner() {
        return new StrongInternerBuilder();
    }


    public static class StrongInternerBuilder {

        private RemovalPolicyCreator removalPolicyCreator;
        private ResizeTriggerPolicy resizeTriggerPolicy;

        public Interner withNonSizeLimitedDomains() {
            Preconditions.checkState(removalPolicyCreator == null && resizeTriggerPolicy == null,
                    "Cannot use removal policy or size limit policy with non-limited domains");
            return new Interner(new SizeLimitedDomainCreator(RemovalPolicyCreator.NONE, NeverResizeTriggerPolicy.INSTANCE));
        }

        public StrongInternerBuilder sizeOrderedRemoval() {
            Preconditions.checkState(removalPolicyCreator == null, "Removal policy already set for this builder");
            this.removalPolicyCreator = RemovalPolicyCreator.SIZE_ORDERED;
            return this;
        }

        public StrongInternerBuilder fifoRemoval() {
            Preconditions.checkState(removalPolicyCreator == null, "Removal policy already set for this builder");
            this.removalPolicyCreator = RemovalPolicyCreator.FIFO;
            return this;
        }

        public StrongInternerBuilder withFixedMaximumSize(long maxSizeBytes) {
            Preconditions.checkState(resizeTriggerPolicy == null, "Size limit policy already set for this builder");
            resizeTriggerPolicy = new FixedMaximumTriggerPolicy(maxSizeBytes);
            return this;
        }

        public StrongInternerBuilder withMaxHeapProportion(double maxHeapProportion) {
            Preconditions.checkState(resizeTriggerPolicy == null, "Size limit policy already set for this builder");
            resizeTriggerPolicy = new HeapProportionTriggerPolicy(maxHeapProportion);
            return this;
        }

        public Interner build() {
            Preconditions.checkNotNull(removalPolicyCreator);
            Preconditions.checkNotNull(resizeTriggerPolicy);
            return new Interner(new SizeLimitedDomainCreator(removalPolicyCreator, resizeTriggerPolicy));
        }

    }

    private static class WeakRefsDomainCreator implements DomainCreator {

        @Override
        public <T> InternDomain<T> createDomain(Class<T> clazz) {
            return new NonBlockingWeakInternDomain<>();
        }
    }

    private static class WeakHashmapDomainCreator implements DomainCreator {

        @Override
        public <T> InternDomain<T> createDomain(Class<T> clazz) {
            return new WeakHashmapInternDomain<>();
        }
    }

    private static class SizeLimitedDomainCreator implements DomainCreator {

        private RemovalPolicyCreator removalPolicyCreator;
        private ResizeTriggerPolicy resizeTriggerPolicy;

        private SizeLimitedDomainCreator(RemovalPolicyCreator removalPolicyCreator, ResizeTriggerPolicy resizeTriggerPolicy) {
            this.removalPolicyCreator = removalPolicyCreator;
            this.resizeTriggerPolicy = resizeTriggerPolicy;
        }

        @Override
        public <E> InternDomain<E> createDomain(Class<E> clazz) {
            return new StrongInternDomain<>(removalPolicyCreator.createPolicy(clazz, resizeTriggerPolicy));
        }
    }

    private enum RemovalPolicyCreator {

        SIZE_ORDERED {
            @Override
            <T> RemovalPolicy<T> createPolicy(Class<T> clazz, ResizeTriggerPolicy resizeTriggerPolicy) {
                return new SizeOrderedRemovalPolicy<>(resizeTriggerPolicy);
            }
        },
        FIFO {
            @Override
            <T> RemovalPolicy<T> createPolicy(Class<T> clazz, ResizeTriggerPolicy resizeTriggerPolicy) {
                return new FifoRemovalPolicy<>(resizeTriggerPolicy);
            }
        },
        NONE {
            @Override
            <T> RemovalPolicy<T> createPolicy(Class<T> clazz, ResizeTriggerPolicy resizeTriggerPolicy) {
                return new NullRemovalPolicy<>();
            }
        };


        abstract <T> RemovalPolicy<T> createPolicy(Class<T> clazz, ResizeTriggerPolicy resizeTriggerPolicy);


    }

    private interface DomainCreator {

        <T> InternDomain<T> createDomain(Class<T> clazz);

    }


}
