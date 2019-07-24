package index;

import utils.Dataset;
import utils.SetOperation;
import utils.TopKResult;

import java.util.*;

public class DualTree {

    public KdTree tupleIndex;
    public ConeTree utilityIndex;

    public DualTree(int dim, int k, double eps) {
        double[] min_range = new double[dim + 1];
        double[] max_range = new double[dim + 1];

        for (int i = 0; i < dim; i++) {
            min_range[i] = 0;
            max_range[i] = 1;
        }
        min_range[dim] = 0;
        max_range[dim] = Math.sqrt(dim);

        this.tupleIndex = new KdTree(dim + 1, 10, min_range, max_range);

        long t1 = System.nanoTime();
        Dataset.TOP_K_RESULTS = new TopKResult[Dataset.UTILITIES.length];
        for (int i = 0; i< Dataset.UTILITIES.length; i++) {
            Dataset.TOP_K_RESULTS[i] = this.tupleIndex.approxTopKSearch(k, eps, Dataset.UTILITIES[i]);
        }
        long t2 = System.nanoTime();
        System.out.println("k-d tree time = " + ((t2 - t1) / 1e9) + "s");

        this.utilityIndex = new ConeTree(dim + 1, 0.01);

        Dataset.constructSetSystem();
    }

    public List<SetOperation> insert(int t_idx) {
        List<SetOperation> operations = new ArrayList<>();
        List<Integer> affectedUtilities = new ArrayList<>();

        tupleIndex.insert(t_idx);

        return operations;
    }

    public List<SetOperation> delete(int t_idx) {
        return null;
    }
}
