package structures;

import generators.TupleGenerator;
import generators.UtilityGenerator;

import utils.OprType;
import utils.SetOperation;
import utils.TopKResult;

import java.util.*;

public class DualTree {

    public int dim;
    int k;
    double eps;

    boolean[] isDeleted;
    public double[][] tuples;

    public double[][] utilities;

    public TopKResult[] results;

    public Map<Integer, HashSet<Integer>> sets;

    public KdTree tIdx;
    public ConeTree uIdx;

    public SetCover setCover;

    public DualTree(int dim, int k, double eps, int data_size, int init_size, int sample_size) {
        this.dim = dim;
        this.k = k;
        this.eps = eps;

        initializeDataset(data_size, init_size, sample_size);

        double[] min = new double[this.dim + 1];
        double[] max = new double[this.dim + 1];

        for (int i = 0; i < this.dim; i++) {
            min[i] = 0;
            max[i] = 1;
        }
        min[this.dim] = 0;
        max[this.dim] = Math.sqrt(dim);

        this.tIdx = new KdTree(this.dim + 1, 2, min, max, this);

        this.results = new TopKResult[this.utilities.length];
        for (int i = 0; i< this.utilities.length; i++) {
            this.results[i] = this.tIdx.approxTopKSearch(this.k, this.eps, this.utilities[i]);
        }
        this.uIdx = new ConeTree(this.dim + 1, 0.99, this);
        constructSetSystem();

        this.setCover = new SetCover(this);
        this.setCover.constructSetCover();
    }

    private void initializeDataset(int data_size, int init_size, int sample_size) {
        tuples = TupleGenerator.uniformGenerator(dim, data_size);
        utilities = UtilityGenerator.uniformGenerator(dim, sample_size);

        isDeleted = new boolean[data_size];
        for (int i = 0; i < init_size; i++) {
            isDeleted[i] = false;
        }
        for (int i = init_size; i < data_size; i++) {
            isDeleted[i] = true;
        }
    }

    private void constructSetSystem() {
        sets = new HashMap<>();
        for (int i = 0; i < results.length; i++) {
            for (int t_idx : results[i].results) {
                if (! sets.containsKey(t_idx)) {
                    sets.put(t_idx, new HashSet<>());
                    sets.get(t_idx).add(i);
                } else {
                    sets.get(t_idx).add(i);
                }
            }
        }
    }

    private void updateSetSystem(List<SetOperation> operations) {
        for (SetOperation opr : operations) {
            if (opr.oprType == OprType.T_ADD || opr.oprType == OprType.S_ADD) {
                if (! sets.containsKey(opr.t_idx)) {
                    sets.put(opr.t_idx, new HashSet<>());
                    sets.get(opr.t_idx).add(opr.u_idx);
                } else {
                    sets.get(opr.t_idx).add(opr.u_idx);
                }
            } else if (opr.oprType == OprType.T_DEL || opr.oprType == OprType.S_DEL) {
                sets.get(opr.t_idx).remove(opr.u_idx);
                if (sets.get(opr.t_idx).isEmpty()) {
                    sets.remove(opr.t_idx);
                }
            }
        }
    }

    public List<SetOperation> insert(int t_idx) {
        boolean isUpdated = false;
        if (isDeleted[t_idx]) {
            isDeleted[t_idx] = false;
            isUpdated = true;
        }

        if (! isUpdated) {
            return null;
        }

        List<SetOperation> operations = new ArrayList<>();
        boolean is_inserted = tIdx.insert(t_idx, tuples[t_idx]);
        if (!is_inserted) {
            System.err.println("Insert " + t_idx + " failed");
        }
        uIdx.insert(t_idx, tuples[t_idx], operations);
        updateSetSystem(operations);

        return operations;
    }

    public List<SetOperation> delete(int t_idx) {
        boolean isUpdated = false;
        if (! isDeleted[t_idx]) {
            isDeleted[t_idx] = true;
            isUpdated = true;
        }

        if (! isUpdated) {
            return null;
        }

        List<SetOperation> operations = new ArrayList<>();

        boolean is_deleted = tIdx.delete(t_idx, tuples[t_idx]);
        if (!is_deleted) {
            System.err.println("Delete " + t_idx + " failed");
        }
        uIdx.delete(t_idx, tuples[t_idx], operations);
        updateSetSystem(operations);

        return operations;
    }
}
