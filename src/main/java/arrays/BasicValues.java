package arrays;

import java.util.Iterator;

/**
 * Created by Andy on 02/02/15.
 */
public class BasicValues<E> implements Values<E> {

    private E[] array;

    public BasicValues(E[] array) {
        this.array = array;
    }

    @Override
    public E get(int idx) {
        return array[idx];
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
        return get(array.length - 1);
    }

    @Override
    public E[] toArray() {
        E[] copy = (E[]) new Object[array.length];
        System.arraycopy(array, 0, copy, 0, copy.length);
        return copy;
    }

    @Override
    public Iterator<E> iterator() {
        return new ValueIterator<>(this);
    }

    @Override
    public BasicValues<E> toBasicValues() {
        return this;
    }
}
