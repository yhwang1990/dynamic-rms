package structures;

import utils.OprType;
import utils.TupleOpr;

public class MinErrorRMS {

    public int dim;
    public int k;
    public int r;
    public double eps;

    public DualTree dualTree;
    public MaxInst maxInst;

    public int data_size, init_size, sample_size;

    public MinErrorRMS(int dim, int k, int r, double eps, int data_size, int init_size, int sample_size) {
        this.dim = dim;
        this.k = k;
        this.r = r;
        this.eps = eps;

        this.data_size = data_size;
        this.init_size = init_size;
        this.sample_size = sample_size;

        this.dualTree = new DualTree(dim, k, eps, data_size, init_size, sample_size);
        this.maxInst = new MaxInst(r, this);
    }

    public void update(TupleOpr opr) {
        Operations operations;
        if (opr.oprType == OprType.ADD)
            operations = dualTree.insert(opr.t_idx);
        else
            operations = dualTree.delete(opr.t_idx);

//        operations.print();
        maxInst.update(operations);
    }
}
