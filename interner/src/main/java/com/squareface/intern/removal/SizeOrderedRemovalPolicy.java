package com.squareface.intern.removal;

import com.squareface.intern.trigger.ResizeTriggerPolicy;
import org.openjdk.jol.info.GraphLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by Andy on 04/02/15.
 */
public class SizeOrderedRemovalPolicy<T> extends TriggeredRemovalPolicy<T> {

    private final BlockingQueue<T> queue = new PriorityBlockingQueue<>(11, new Comparator<T>() {
        @Override
        public int compare(T o1, T o2) {
            long o1Size = GraphLayout.parseInstance(o1).totalSize();
            long o2Size = GraphLayout.parseInstance(o2).totalSize();

            return (int) o2Size - (int) o1Size;
        }
    });

    public SizeOrderedRemovalPolicy(ResizeTriggerPolicy resizeTriggerPolicy) {
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
