package com.squareface.intern.trigger;

import com.squareface.intern.InternDomain;

/**
 * Created by Andy on 04/02/15.
 */
public class NeverResizeTriggerPolicy implements ResizeTriggerPolicy {

    public static final NeverResizeTriggerPolicy INSTANCE = new NeverResizeTriggerPolicy();

    @Override
    public void updateStats(Object item) {

    }

    @Override
    public boolean triggerResize(InternDomain<?> internDomain) {
        return false;
    }
}
