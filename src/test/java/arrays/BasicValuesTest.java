package arrays;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

/**
 * Created by Andy on 02/02/15.
 */
public class BasicValuesTest {

    static final String[] TEST_ARRAY = new String[]{"abc", "def", "ghi", "jkl", "mno", "pqr"};

    public Values<String> getValues() {
        return new BasicValues<>(TEST_ARRAY);
    }

    @Test
    public void testGet() throws Exception {
        Values<String> v = getValues();
        Assert.assertEquals("def", v.get(1));
    }

    @Test
    public void testSize() throws Exception {
        Values<String> v = getValues();
        Assert.assertTrue(v.size() == 6);
    }

    @Test
    public void testGetFirst() throws Exception {
        Values<String> v = getValues();
        Assert.assertEquals(v.getFirst(), "abc");
    }

    @Test
    public void testGetLast() throws Exception {
        Values<String> v = getValues();
        Assert.assertEquals(v.getLast(), "pqr");
    }

    @Test
    public void testToArray() {
        Values<String> v = getValues();
        Assert.assertArrayEquals(TEST_ARRAY, v.toArray());
    }

    @Test
    public void testIterator() throws Exception {
        Values<String> v = getValues();
        Iterator<String> it = v.iterator();
        int count = 0;
        while (it.hasNext()) {
            Assert.assertEquals(TEST_ARRAY[count], it.next());
            count++;
        }
        Assert.assertEquals(v.size(), count);
    }

    @Test
    public void testConvertsToBasicValues() {
        Values<String> v = getValues();
        Values<String> basic = v.asBasicValues();
        Assert.assertEquals(6, basic.size());
        Assert.assertEquals("abc", basic.getFirst());
        Assert.assertEquals("pqr", basic.getLast());
    }

}
