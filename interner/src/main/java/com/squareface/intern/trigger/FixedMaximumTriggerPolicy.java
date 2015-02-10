package com.squareface.intern.trigger;

import com.google.common.base.Preconditions;
import com.squareface.intern.InternDomain;

/**
 * Created by Andy on 04/02/15.
 */
public class FixedMaximumTriggerPolicy extends AverageSizeTriggerPolicy {

    private final long maxSize;

    public FixedMaximumTriggerPolicy(long maxSize) {
        long maxHeap = Runtime.getRuntime().maxMemory();
        double maxPossibleSize = 0.9 * maxHeap;
        Preconditions.checkArgument(maxSize < maxPossibleSize,
                String.format("maxSize must be less than 90%% of maxHeap. MaxHeap=%d => maxSize must be < %f", maxHeap, maxPossibleSize));


        this.maxSize = maxSize;
    }

    @Override
    public boolean triggerResize(InternDomain<?> internDomain) {
        Stats current = getStats();
        long totalSize = current.calculateDomainSize(internDomain);
        return totalSize > maxSize;
    }
}
