package structures;

import utils.*;

public class MinSizeRMS {

    public final int dim;
    public final int k;
    public final double eps;

    public DualTree dualTree;
    public SetCover setCover;

    public MinSizeRMS(int dim, int k, double eps, int data_size, int init_size, int sample_size) {
        this.dim = dim;
        this.k = k;
        this.eps = eps;
        setParameter(data_size, init_size, sample_size);
        this.dualTree = new DualTree(dim, k, eps);
        this.setCover = new SetCover(this);
    }

    private void setParameter(int data_size, int init_size, int sample_size) {
        Parameter.DATA_SIZE = data_size;
        Parameter.INIT_SIZE = init_size;
        Parameter.SAMPLE_SIZE = sample_size;
    }

    private void readDataset() {}

    private void updateDataset(TupleOpr opr) {
    }
}
