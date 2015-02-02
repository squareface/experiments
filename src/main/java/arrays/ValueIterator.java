package arrays;

import com.google.common.collect.AbstractIterator;

/**
 * Created by Andy on 02/02/15.
 */
public class ValueIterator<T> extends AbstractIterator<T> {

    private int position = 0;
    private Values<T> underlying;

    public ValueIterator(Values<T> underlying) {
        this.underlying = underlying;
    }

    @Override
    protected T computeNext() {
        if (position == underlying.size()) {
            return endOfData();
        }
        return underlying.get(position++);
    }
}
