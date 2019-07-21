package utils;

import java.util.HashSet;
import java.util.PriorityQueue;

public class TopKResult {

    public double k_score;
    public HashSet<Integer> results;
    public PriorityQueue<RankItem> exact_result;
    public PriorityQueue<RankItem> approximate_result;

    public TopKResult() {
        this.k_score = 0.0;
        this.results = new HashSet<>();
        this.exact_result = new PriorityQueue<>();
        this.approximate_result = new PriorityQueue<>();
    }
}
