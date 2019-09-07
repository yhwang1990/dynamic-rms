package structures;

import utils.*;

public class MinSizeRMS {

    public int dim;
    public int k;
    public double eps;

    public DualTree dualTree;
    public SetCover setCover;

    public int data_size, init_size, sample_size;

    public MinSizeRMS(int dim, int k, double eps, int data_size, int init_size, int sample_size) {
        this.dim = dim;
        this.k = k;
        this.eps = eps;

        this.data_size = data_size;
        this.init_size = init_size;
        this.sample_size = sample_size;

        this.dualTree = new DualTree(dim, k, eps, data_size, init_size, sample_size);
        this.setCover = new SetCover(this);
    }

    public void update(TupleOpr opr) {
        Operations operations;
        if (opr.oprType > 0)
            operations = dualTree.insert(opr.t_idx);
        else
            operations = dualTree.delete(opr.t_idx);

        operations.print();
        setCover.update(operations);
    }
}
