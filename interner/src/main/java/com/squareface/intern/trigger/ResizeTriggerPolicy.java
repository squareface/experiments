package com.squareface.intern.trigger;


import com.squareface.intern.InternDomain;


/**
 * Created by Andy on 04/02/15.
 */
public interface ResizeTriggerPolicy {

    void updateStats(Object item);

    boolean triggerResize(InternDomain<?> internDomain);

}
