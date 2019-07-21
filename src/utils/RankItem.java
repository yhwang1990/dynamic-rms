package utils;

import java.util.Objects;

public class RankItem implements Comparable<RankItem> {
    public int idx;
    public double score;
    public RankItem(int idx, double score) {
        this.idx = idx;
        this.score = score;
    }

    @Override
    public int compareTo(RankItem other) {
        if (this.score != other.score) {
            return Double.compare(this.score, other.score);
        } else {
            return Integer.compare(this.idx, other.idx);
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
