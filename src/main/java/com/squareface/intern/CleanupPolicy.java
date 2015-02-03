package com.squareface.intern;

/**
 * Created by Andy on 03/02/15.
 * <p/>
 * Might want to push one of these into a SizeLimitedInternDomain to control the order in which keys are cleaned from
 * from the map. Haven't decided if that's worth doing. Currently different queue implementations are swapped in to determine
 * the cleanup order.
 */


public interface CleanupPolicy<T> {

    void registerItem();

    T nextKeyToClean();


}
