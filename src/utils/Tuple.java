package utils;

import java.util.Objects;

public class Tuple {
    public int idx;
    public double[] value;

    public Tuple (int idx, double[] value) {
        this.idx = idx;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple tuple = (Tuple) o;
        return idx == tuple.idx;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idx);
    }
}
