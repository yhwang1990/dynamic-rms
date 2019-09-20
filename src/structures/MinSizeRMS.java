package structures;

import utils.TupleOpr;
import java.util.Set;

public class MinSizeRMS {

    DualTree dualTree;
    SetCover setCover;
    
    public double initTime = 0.0, addTreeTime = 0.0, addCovTime = 0.0, delTreeTime = 0.0, delCovTime = 0.0;

    public MinSizeRMS(int dim, int k, double eps, int data_size, int init_size, int sample_size, double[][] data, double[][] samples) {
        long t0 = System.nanoTime();

        this.dualTree = new DualTree(dim, k, eps, data_size, init_size, sample_size, data, samples);
        this.setCover = new SetCover(sample_size, this);

        long t1 = System.nanoTime();
        this.initTime += (t1 - t0) / 1e6;
    }

    public void update(TupleOpr opr) {
        long t0 = System.nanoTime();

        Operations operations;
        if (opr.oprType > 0)
            operations = dualTree.insert(opr.t_idx);
        else
            operations = dualTree.delete(opr.t_idx);

        long t1 = System.nanoTime();

        setCover.update(operations);

        long t2 = System.nanoTime();

        if (opr.oprType > 0) {
            addTreeTime += (t1 - t0) / 1e6;
            addCovTime += (t2 - t1) / 1e6;
        } else {
            delTreeTime += (t1 - t0) / 1e6;
            delCovTime += (t2 - t1) / 1e6;
        }
    }

    public Set<Integer> result() {
        return setCover.result();
    }
}
