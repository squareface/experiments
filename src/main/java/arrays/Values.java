package arrays;

/**
 * Created by Andy on 02/02/15.
 */
public interface Values<E> extends Iterable<E> {

    /*

     */
    E get(int idx);

    int size();

    E getFirst();

    E getLast();

    E[] toArray();

    BasicValues<E> asBasicValues();

}
