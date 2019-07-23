package index;

import utils.TopKResult;
import utils.Tuple;
import utils.Utility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class DualTree {

    public KdTree tupleIndex;
    public ConeTree utilityIndex;
    public TopKResult[] topKResults;
    public Map<Integer, HashSet<Integer>> setSystem;

    public DualTree(int dim, int k, double eps, List<Tuple> tuples, List<Utility> utilities) {
        double[] min_range = new double[dim + 1];
        double[] max_range = new double[dim + 1];

        for (int i = 0; i < dim; i++) {
            min_range[i] = 0;
            max_range[i] = 1;
        }
        min_range[dim] = 0;
        max_range[dim] = Math.sqrt(dim);

        this.tupleIndex = new KdTree(dim + 1, 10, min_range, max_range, tuples);

        long t1 = System.nanoTime();
        this.topKResults = new TopKResult[utilities.size()];

        for (Utility u : utilities) {
            this.topKResults[u.idx] = this.tupleIndex.approxTopKSearch(k, eps, u);
        }
        long t2 = System.nanoTime();
        System.out.println("k-d tree time = " + ((t2 - t1) / 1e9) + "s");

        this.utilityIndex = new ConeTree(dim + 1, 0.01, utilities, this.topKResults);

        this.setSystem = new HashMap<>();
        for (int i = 0; i < this.topKResults.length; i++) {
            for (int t_idx : this.topKResults[i].results) {
                if (! this.setSystem.containsKey(t_idx)) {
                    this.setSystem.put(t_idx, new HashSet<>());
                    this.setSystem.get(t_idx).add(i);
                } else {
                    this.setSystem.get(t_idx).add(i);
                }
            }
        }
    }

    public void insert(Tuple t) {}

    public void delete(Tuple t) {}
}
