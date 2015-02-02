package arrays;

import java.util.Iterator;

/**
 * Created by Andy on 02/02/15.
 */
public class OffsetValues<E> implements Values<E> {

    private E[] array;
    private int offset;

    public OffsetValues(E[] array, int offset) {
        if (offset < 0 || !(offset < array.length)) {
            throw new IllegalArgumentException("Offset must have value n where 0 <= n < array.length");
        }

        this.array = array;
        this.offset = offset;
    }

    @Override
    public E get(int idx) {
        return array[idx + offset];
    }

    @Override
    public int size() {
        return array.length - offset;
    }

    @Override
    public E getFirst() {
        return array[offset];
    }

    @Override
    public E getLast() {
        return array[array.length - 1];
    }

    @Override
    public E[] toArray() {
        E[] copy = (E[]) new Object[array.length - offset];
        System.arraycopy(array, offset, copy, 0, copy.length);
        return copy;
    }

    @Override
    public Iterator<E> iterator() {
        return new ValueIterator<>(this);
    }

    @Override
    public BasicValues<E> asBasicValues() {
        return new BasicValues<>(toArray());
    }
}
