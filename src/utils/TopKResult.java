package utils;

import java.util.HashSet;
import java.util.PriorityQueue;

public class TopKResult {

    private int k;
    private double eps;
    private PriorityQueue<RankItem> exact_result, approximate_result;

    public double k_score;
    public HashSet<Integer> results;

    public TopKResult(int k, double eps) {
        this.k = k;
        this.eps = eps;
        this.k_score = 0.0;
        this.results = new HashSet<>();
        this.exact_result = new PriorityQueue<>();
        this.approximate_result = new PriorityQueue<>();
    }

    public boolean update(int idx, double score) {
        boolean k_score_update = false;
        if (score > k_score) {
            exact_result.offer(new RankItem(idx, score));
            k_score_update = true;
            if (! exact_result.isEmpty() && exact_result.size() == k) {
                k_score = exact_result.peek().score;
            } else if (! exact_result.isEmpty() && exact_result.size() > k) {
                RankItem deleted_item = exact_result.poll();
                if (! exact_result.isEmpty()) {
                    k_score = exact_result.peek().score;
                }

                if (deleted_item.score >= (1 - eps) * k_score) {
                    approximate_result.offer(deleted_item);
                }

                while(! approximate_result.isEmpty() && approximate_result.peek().score < (1 - eps) * k_score) {
                    approximate_result.poll();
                }
            }
        } else if (score >= (1 - eps) * k_score) {
            approximate_result.offer(new RankItem(idx, score));
        }

        return k_score_update;
    }

    public void refreshResults() {
        results.clear();

        for(RankItem item : exact_result) {
            results.add(item.idx);
        }

        for(RankItem item : approximate_result) {
            results.add(item.idx);
        }
    }

    public void print() {
        StringBuilder builder = new StringBuilder();
        builder.append("k_score = ").append(k_score).append("\n");
        for (RankItem item : exact_result) {
            builder.append(item.idx).append(",").append(item.score).append("\n");
        }
        for (RankItem item : approximate_result) {
            builder.append(item.idx).append(",").append(item.score).append("\n");
        }
        System.out.print(builder.toString());
    }
}
