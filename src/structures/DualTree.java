package structures;

import utils.OprType;
import utils.Parameter;

public class DualTree {

    public KdTree tIdx;
    public ConeTree uIdx;

    public DualTree(int dim, int k, double eps) {
        this.tIdx = new KdTree(dim + 1, Parameter.LEAF_SIZE);
        this.uIdx = new ConeTree(dim + 1, k, eps, Parameter.TAU, this);
    }

    public Operations insert(int t_idx) {
        Operations opr = new Operations(t_idx, OprType.T_ADD);
        boolean is_inserted = tIdx.insert(t_idx);
        if (!is_inserted) {
            System.err.println("Insert " + t_idx + " failed");
        }
        uIdx.insert(t_idx, opr);

        return opr;
    }

    public Operations delete(int t_idx) {
        Operations opr = new Operations(t_idx, OprType.T_DEL);
        boolean is_deleted = tIdx.delete(t_idx);
        if (!is_deleted) {
            System.err.println("Delete " + t_idx + " failed");
        }
        uIdx.delete(t_idx, opr);

        return opr;
    }
}
