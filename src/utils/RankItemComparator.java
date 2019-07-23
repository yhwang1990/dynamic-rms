package utils;

import java.util.Comparator;

public class RankItemComparator implements Comparator<RankItem> {

    private boolean is_ascend;

    public RankItemComparator(boolean is_ascend) {
        this.is_ascend = is_ascend;
    }

    @Override
    public int compare(RankItem item1, RankItem item2) {
        if (is_ascend)
            return Double.compare(item1.score, item2.score);
        else
            return - Double.compare(item1.score, item2.score);
    }
}

