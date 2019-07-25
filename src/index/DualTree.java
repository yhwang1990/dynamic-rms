package index;

import generator.TupleGenerator;
import generator.UtilityGenerator;

import utils.OprType;
import utils.SetOperation;
import utils.TopKResult;

import java.util.*;

public class DualTree {

    public int dim;
    public int k;
    public double epsilon;

    public boolean[] isDeleted;
    public double[][] tuples;

    public double[][] utilities;

    public TopKResult[] topKResults;

    public Map<Integer, HashSet<Integer>> setSystem;

    public KdTree tupleIdx;
    public ConeTree utilityIdx;

    public DualTree(int dim, int k, double eps, int data_size, int init_size, int sample_size) {
        this.dim = dim;
        this.k = k;
        this.epsilon = eps;

        initializeDataset(data_size, init_size, sample_size);

        double[] min = new double[this.dim + 1];
        double[] max = new double[this.dim + 1];

        for (int i = 0; i < this.dim; i++) {
            min[i] = 0;
            max[i] = 1;
        }
        min[this.dim] = 0;
        max[this.dim] = Math.sqrt(dim);

        this.tupleIdx = new KdTree(this.dim + 1, 2, min, max, this);

        this.topKResults = new TopKResult[this.utilities.length];
        for (int i = 0; i< this.utilities.length; i++) {
            this.topKResults[i] = this.tupleIdx.approxTopKSearch(this.k, this.epsilon, this.utilities[i]);
        }

        this.utilityIdx = new ConeTree(this.dim + 1, 0.99, this);

        constructSetSystem();
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
        setSystem = new HashMap<>();
        for (int i = 0; i < topKResults.length; i++) {
            for (int t_idx : topKResults[i].results) {
                if (! setSystem.containsKey(t_idx)) {
                    setSystem.put(t_idx, new HashSet<>());
                    setSystem.get(t_idx).add(i);
                } else {
                    setSystem.get(t_idx).add(i);
                }
            }
        }
    }

    private void updateSetSystem(List<SetOperation> operations) {
        for (SetOperation opr : operations) {
            if (opr.oprType == OprType.T_ADD || opr.oprType == OprType.S_ADD) {
                if (! setSystem.containsKey(opr.t_idx)) {
                    setSystem.put(opr.t_idx, new HashSet<>());
                    setSystem.get(opr.t_idx).add(opr.u_idx);
                } else {
                    setSystem.get(opr.t_idx).add(opr.u_idx);
                }
            } else if (opr.oprType == OprType.T_DEL || opr.oprType == OprType.S_DEL) {
                setSystem.get(opr.t_idx).remove(opr.u_idx);
                if (setSystem.get(opr.t_idx).isEmpty()) {
                    setSystem.remove(opr.t_idx);
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
        tupleIdx.insert(t_idx, tuples[t_idx]);
        utilityIdx.insert(t_idx, tuples[t_idx], operations);
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

        tupleIdx.delete(t_idx, tuples[t_idx]);
        utilityIdx.delete(t_idx, tuples[t_idx], operations);
        updateSetSystem(operations);

        return operations;
    }
}
