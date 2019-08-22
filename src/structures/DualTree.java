package structures;

import utils.Data;
import utils.SetOperation;
import utils.TopKResult;

import java.util.*;

public class DualTree {

    KdTree tIdx;
    ConeTree uIdx;

    public DualTree() {
        double[] min = new double[Data.DIM + 1];
        double[] max = new double[Data.DIM + 1];

        for (int i = 0; i < Data.DIM; i++) {
            min[i] = 0;
            max[i] = 1;
        }
        min[Data.DIM] = 0;
        max[Data.DIM] = Math.sqrt(Data.DIM);

        this.tIdx = new KdTree(Data.DIM + 1, Data.LEAF_SIZE, min, max);

        Data.RESULTS = new TopKResult[Data.SAMPLE_SIZE];
        for (int i = 0; i< Data.SAMPLE_SIZE; i++) {
            Data.RESULTS[i] = this.tIdx.approxTopKSearch(Data.K, Data.EPS, Data.UTILITIES[i]);
        }
        this.uIdx = new ConeTree(Data.DIM + 1, Data.TAU, this);
    }

    public List<SetOperation> insert(int t_idx) {
        boolean isUpdated = false;
        if (Data.DELETED[t_idx]) {
            Data.DELETED[t_idx] = false;
            isUpdated = true;
        }

        if (! isUpdated) {
            return null;
        }

        List<SetOperation> operations = new ArrayList<>();
        boolean is_inserted = tIdx.insert(t_idx, Data.TUPLES[t_idx]);
        if (!is_inserted) {
            System.err.println("Insert " + t_idx + " failed");
        }
        uIdx.insert(t_idx, Data.TUPLES[t_idx], operations);

        return operations;
    }

    public List<SetOperation> delete(int t_idx) {
        boolean isUpdated = false;
        if (! Data.DELETED[t_idx]) {
            Data.DELETED[t_idx] = true;
            isUpdated = true;
        }

        if (! isUpdated) {
            return null;
        }

        List<SetOperation> operations = new ArrayList<>();

        boolean is_deleted = tIdx.delete(t_idx, Data.TUPLES[t_idx]);
        if (!is_deleted) {
            System.err.println("Delete " + t_idx + " failed");
        }
        uIdx.delete(t_idx, Data.TUPLES[t_idx], operations);

        return operations;
    }
}
