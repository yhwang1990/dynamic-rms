package utils;

import java.util.Objects;

public class RankItem implements Comparable<RankItem> {
    public int idx;
    public double dist2;
    public RankItem(int idx, double dist2) {
        this.idx = idx;
        this.dist2 = dist2;
    }

    @Override
    public int compareTo(RankItem other) {
        if (this.dist2 > other.dist2) {
            return -1;
        } else if (this.dist2 < other.dist2) {
            return 1;
        } else {
            return (this.idx - other.idx);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RankItem)) return false;
        RankItem item = (RankItem) o;
        return idx == item.idx;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idx);
    }
}
