package structures;

import java.util.*;

public class TopKResult {

    int k;
    double eps;
    PriorityQueue<RankItem> exact_result, approximate_result;

    double k_score;
    HashSet<Integer> results;

    TopKResult(int k, double eps) {
        this.k = k;
        this.eps = eps;
        this.k_score = 0.0;

        this.results = new HashSet<>();
        this.exact_result = new PriorityQueue<>();
        this.approximate_result = new PriorityQueue<>();
    }

    boolean update(int idx, double score) {
        boolean k_updated = false;
        if (score > k_score) {
            exact_result.offer(new RankItem(idx, score));
            k_updated = true;
            if (exact_result.size() == k) {
                k_score = exact_result.peek().score;
            } else if (exact_result.size() > k) {
                RankItem deleted_item = exact_result.poll();
                k_score = exact_result.peek().score;

                if (deleted_item.score > (1 - eps) * k_score + 1e-9)
                    approximate_result.offer(deleted_item);

                while(!approximate_result.isEmpty() && approximate_result.peek().score < (1 - eps) * k_score + 1e-9)
                    approximate_result.poll();
            }
        } else if (score > (1 - eps) * k_score + 1e-9) {
            approximate_result.offer(new RankItem(idx, score));
        }

        return k_updated;
    }

    boolean add(int u_idx, int t_idx, double score, Operations opr) {
        boolean k_updated = false;
        if (score > k_score) {
            exact_result.offer(new RankItem(t_idx, score));
            results.add(t_idx);
            opr.utilities.add(u_idx);
            k_updated = true;

            if (exact_result.size() > k) {
                RankItem deleted_item = exact_result.poll();
                k_score = exact_result.peek().score;

                if (deleted_item.score > (1 - eps) * k_score + 1e-9) {
                    approximate_result.offer(deleted_item);
                } else {
                    results.remove(deleted_item.idx);
                    opr.oprs.add(new Operations.SetOpr(deleted_item.idx, u_idx));
                }

                while(! approximate_result.isEmpty() && approximate_result.peek().score < (1 - eps) * k_score + 1e-9) {
                    RankItem obsolete_item = approximate_result.poll();
                    results.remove(obsolete_item.idx);
                    opr.oprs.add(new Operations.SetOpr(obsolete_item.idx, u_idx));
                }
            }
        } else if (score > (1 - eps) * k_score + 1e-9) {
            approximate_result.offer(new RankItem(t_idx, score));
            results.add(t_idx);
            opr.utilities.add(u_idx);
        }

        return k_updated;
    }

    void initResults() {
        results.clear();
        for(RankItem item : exact_result)
            results.add(item.idx);
        for(RankItem item : approximate_result)
            results.add(item.idx);
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
