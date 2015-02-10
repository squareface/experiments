package com.squareface.intern.removal;

import com.squareface.intern.trigger.ResizeTriggerPolicy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Andy on 04/02/15.
 */
public class FifoRemovalPolicy<T> extends TriggeredRemovalPolicy<T> {

    private final BlockingQueue<T> queue = new LinkedBlockingQueue<>();


    public FifoRemovalPolicy(ResizeTriggerPolicy resizeTriggerPolicy) {
        super(resizeTriggerPolicy);
    }

    @Override
    public void registerItem(T item) {
        super.registerItem(item);
        queue.add(item);
    }

    @Override
    public T nextKey() {
        return queue.poll();
    }

    @Override
    public Collection<T> nextKeys(int numKeys) {
        List<T> keysToRemove = new ArrayList<>(numKeys);

        T item = queue.poll();
        while (numKeys > 0 && item != null) {
            keysToRemove.add(item);
            numKeys--;
        }

        return keysToRemove;
    }
}
