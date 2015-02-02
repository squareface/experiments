package arrays;

import java.util.Iterator;

/**
 * Created by Andy on 02/02/15.
 */
public class RemapValues<E> implements Values<E> {

    private E[] array;
    private int[] remap;

    public RemapValues(E[] array, int[] remap) {
        if (array.length != remap.length) {
            throw new IllegalArgumentException("Array and remap sizes must be identical");
        }

        this.array = array;
        this.remap = remap;
    }

    @Override
    public E get(int idx) {
        int realIdx = remap[idx];
        return array[realIdx];
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public E getFirst() {
        return get(0);
    }

    @Override
    public E getLast() {
        return get(remap.length - 1);
    }

    @Override
    public E[] toArray() {
        E[] copy = (E[]) new Object[array.length];
        for (int i = 0; i < array.length; i++) {
            int realIdx = remap[i];
            copy[i] = array[realIdx];
        }

        return copy;
    }

    @Override
    public Iterator<E> iterator() {
        return new ValueIterator<>(this);
    }

    @Override
    public BasicValues<E> toBasicValues() {
        return new BasicValues<>(toArray());
    }
}
