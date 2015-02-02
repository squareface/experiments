package arrays;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class OffsetValuesTest {

    static final String[] TEST_ARRAY = new String[]{"abc", "def", "ghi", "jkl", "mno", "pqr"};

    public Values<String> getValues() {
        return new OffsetValues<>(TEST_ARRAY, 2);
    }

    @Test
    public void testCreationFailsIfOffsetOutsideArraySucceedsIfOneElementIncluded() {
        try {
            Values<String> v = new OffsetValues<>(TEST_ARRAY, 6);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // pass test
        }

        Values<String> v = new OffsetValues<>(TEST_ARRAY, 5);
        Assert.assertEquals(1, v.size());
        Assert.assertEquals(v.getFirst(), v.getLast());
    }

    @Test
    public void testGet() throws Exception {
        Values<String> v = getValues();
        Assert.assertEquals("jkl", v.get(1));
    }

    @Test
    public void testSize() throws Exception {
        Values<String> v = getValues();
        Assert.assertTrue(v.size() == 4);
    }

    @Test
    public void testGetFirst() throws Exception {
        Values<String> v = getValues();
        Assert.assertEquals(v.getFirst(), "ghi");
    }

    @Test
    public void testGetLast() throws Exception {
        Values<String> v = getValues();
        Assert.assertEquals(v.getLast(), "pqr");
    }

    @Test
    public void testToArray() {
        Values<String> v = getValues();
        Assert.assertArrayEquals(new String[]{"ghi", "jkl", "mno", "pqr"}, v.toArray());
    }

    @Test
    public void testIterator() throws Exception {
        Values<String> v = getValues();
        Iterator<String> it = v.iterator();
        int count = 0;
        String[] results = new String[]{"ghi", "jkl", "mno", "pqr"};
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
        Assert.assertEquals(4, basic.size());
        Assert.assertEquals("ghi", basic.getFirst());
        Assert.assertEquals("pqr", basic.getLast());
    }

}