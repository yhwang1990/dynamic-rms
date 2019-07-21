package utils;

import java.util.Objects;

public class Utility {
    public int idx;
    public double[] value;

    public Utility(int idx, double[] value) {
        this.idx = idx;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utility)) return false;
        Utility utility = (Utility) o;
        return idx == utility.idx;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idx);
    }
}
