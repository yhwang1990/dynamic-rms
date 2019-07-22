package index;

import utils.TopKResult;
import utils.Tuple;
import utils.Utility;

import java.util.List;

public class DualTree {

    private KdTree tupleIndex;
    private ConeTree utilityIndex;
    private TopKResult[] topKResults;

    public DualTree(int dim, int k, double eps, List<Tuple> tuples, List<Utility> utilities) {
        double[] min_range = new double[dim + 1];
        double[] max_range = new double[dim + 1];

        for (int i = 0; i < dim; i++) {
            min_range[i] = 0;
            max_range[i] = 1;
        }
        min_range[dim] = 0;
        max_range[dim] = Math.sqrt(dim);

        this.tupleIndex = new KdTree(dim + 1, k, min_range, max_range, tuples);

        this.topKResults = new TopKResult[utilities.size()];

        for (Utility u : utilities) {
            this.topKResults[u.idx] = this.tupleIndex.approxTopKSearch(k, eps, u);
        }

        this.utilityIndex = new ConeTree(dim + 1, eps, utilities, this.topKResults);
    }
}
