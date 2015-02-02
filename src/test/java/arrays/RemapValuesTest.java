package arrays;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class RemapValuesTest {

    static final String[] TEST_ARRAY = new String[]{"abc", "def", "ghi", "jkl", "mno", "pqr"};

    public Values<String> getValues() {
        return new RemapValues<>(TEST_ARRAY, new int[]{5, 2, 3, 0, 1, 4});
    }

    @Test
    public void testThrowsExceptionIfRemapNotSameSizeAsArray() {
        try {
            RemapValues<String> values = new RemapValues<>(TEST_ARRAY, new int[]{3, 7});
            Assert.fail("Expected exception due to mismatch between array and remap size");
        } catch (IllegalArgumentException e) {
            // pass test
        }
    }

    @Test
    public void testGet() throws Exception {
        Values<String> v = getValues();
        Assert.assertEquals("ghi", v.get(1));
    }

    @Test
    public void testSize() throws Exception {
        Values<String> v = getValues();
        Assert.assertTrue(v.size() == 6);
    }

    @Test
    public void testGetFirst() throws Exception {
        Values<String> v = getValues();
        Assert.assertEquals(v.getFirst(), "pqr");
    }

    @Test
    public void testGetLast() throws Exception {
        Values<String> v = getValues();
        Assert.assertEquals(v.getLast(), "mno");
    }

    @Test
    public void testToArray() {
        Values<String> v = getValues();
        Assert.assertArrayEquals(new String[]{"pqr", "ghi", "jkl", "abc", "def", "mno"}, v.toArray());
    }

    @Test
    public void testIterator() throws Exception {
        Values<String> v = getValues();
        Iterator<String> it = v.iterator();
        int count = 0;
        String[] results = new String[]{"pqr", "ghi", "jkl", "abc", "def", "mno"};
        while (it.hasNext()) {
            Assert.assertEquals(results[count], it.next());
            count++;
        }
        Assert.assertEquals(v.size(), count);
    }

    @Test
    public void testConvertsToBasicValues() {
        Values<String> v = getValues();
        Values<String> basic = v.toBasicValues();
        Assert.assertEquals(6, basic.size());
        Assert.assertEquals("pqr", basic.getFirst());
        Assert.assertEquals("mno", basic.getLast());
    }

}