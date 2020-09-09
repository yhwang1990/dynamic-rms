package index;

import java.util.Objects;

public class RankItem implements Comparable<RankItem> {
    public int t_idx;
    public double score;

    public RankItem(int idx, double score) {
        this.t_idx = idx;
        this.score = score;
    }

    @Override
    public int compareTo(RankItem other) {
        if (this.score != other.score) {
            return Double.compare(this.score, other.score);
        } else {
            return Integer.compare(this.t_idx, other.t_idx);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RankItem))
            return false;
        RankItem item = (RankItem) o;
        return t_idx == item.t_idx;
    }

    @Override
    public int hashCode() {
        return Objects.hash(t_idx);
    }
}
