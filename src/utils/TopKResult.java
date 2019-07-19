package utils;

import java.util.Objects;
import java.util.PriorityQueue;

public class TopKResult {

    public double k_dist2;
    public PriorityQueue<RankItem> exact_result;
    public PriorityQueue<RankItem> approximate_result;

    public TopKResult() {
        this.k_dist2 = Double.MAX_VALUE;
        this.exact_result = new PriorityQueue<>();
        this.approximate_result = new PriorityQueue<>();
    }
}
