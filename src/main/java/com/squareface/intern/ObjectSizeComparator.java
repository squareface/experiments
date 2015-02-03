package com.squareface.intern;

import org.openjdk.jol.info.GraphLayout;

import java.util.Comparator;

/**
 * Created by Andy on 03/02/15.
 */
public class ObjectSizeComparator<E> implements Comparator<E> {

    @Override
    public int compare(E o1, E o2) {

        long o1Size = GraphLayout.parseInstance(o1).totalSize();
        long o2Size = GraphLayout.parseInstance(o2).totalSize();

        return (int) o2Size - (int) o1Size;
    }
}
