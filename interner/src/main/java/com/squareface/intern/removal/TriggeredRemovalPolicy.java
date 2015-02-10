package com.squareface.intern.removal;

import com.squareface.intern.InternDomain;
import com.squareface.intern.trigger.ResizeTriggerPolicy;

/**
 * Created by Andy on 04/02/15.
 */
public abstract class TriggeredRemovalPolicy<T> implements RemovalPolicy<T> {

    private final ResizeTriggerPolicy resizeTriggerPolicy;

    public TriggeredRemovalPolicy(ResizeTriggerPolicy resizeTriggerPolicy) {
        this.resizeTriggerPolicy = resizeTriggerPolicy;
    }

    @Override
    public void registerItem(T item) {
        resizeTriggerPolicy.updateStats(item);
    }

    @Override
    public boolean shouldRemoveItems(InternDomain<T> domain) {
        return resizeTriggerPolicy.triggerResize(domain);
    }


}
