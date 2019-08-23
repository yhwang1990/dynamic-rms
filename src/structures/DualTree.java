package structures;

import utils.Parameter;
import utils.SetOperation;

import java.util.*;

public class DualTree {

    public KdTree tIdx;
    public ConeTree uIdx;

    public DualTree(int dim, int k, double eps) {
        this.tIdx = new KdTree(dim + 1, Parameter.LEAF_SIZE);
        this.uIdx = new ConeTree(dim + 1, k, eps, Parameter.TAU, this);
    }

    public List<SetOperation> insert(int t_idx) {
        List<SetOperation> operations = new ArrayList<>();
        boolean is_inserted = tIdx.insert(t_idx);
        if (!is_inserted) {
            System.err.println("Insert " + t_idx + " failed");
        }
        uIdx.insert(t_idx, operations);

        return operations;
    }

    public List<SetOperation> delete(int t_idx) {
        List<SetOperation> operations = new ArrayList<>();
        boolean is_deleted = tIdx.delete(t_idx);
        if (!is_deleted) {
            System.err.println("Delete " + t_idx + " failed");
        }
        uIdx.delete(t_idx, operations);

        return operations;
    }
}
