package com.squareface.intern.benchmark;

/**
 * Created by Andy on 08/02/15.
 */
class TestObject {

    private int id;

    public TestObject(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestObject that = (TestObject) o;

        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "TestObject{" +
                "id=" + id +
                '}';
    }
}
