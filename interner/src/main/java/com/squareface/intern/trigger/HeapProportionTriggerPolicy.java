package com.squareface.intern.trigger;

import com.squareface.intern.InternDomain;

/**
 * Created by Andy on 04/02/15.
 */
public class HeapProportionTriggerPolicy extends AverageSizeTriggerPolicy {

    private final double maxHeapProportion;

    public HeapProportionTriggerPolicy(double maxHeapProportion) {
        this.maxHeapProportion = maxHeapProportion;
    }

    @Override
    public boolean triggerResize(InternDomain<?> internDomain) {
        Stats current = getStats();
        long totalSize = current.calculateDomainSize(internDomain);

        long maxHeap = Runtime.getRuntime().maxMemory();
        return (double) totalSize / maxHeap > maxHeapProportion;
    }

}
