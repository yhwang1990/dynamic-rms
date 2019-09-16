package structures;

import main.Main;
import utils.TupleOpr;

import java.util.Set;

public class MinErrorRMS {

    DualTree dualTree;
    public MaxInst maxInst;

    public MinErrorRMS(int dim, int k, int r, double eps, int data_size, int init_size, int sample_size, double[][] data, double[][] samples) {
        long t0 = System.nanoTime();

        this.dualTree = new DualTree(dim, k, eps, data_size, init_size, sample_size, data, samples);
        this.maxInst = new MaxInst(r, sample_size, this);

        long t1 = System.nanoTime();
        Main.InitTime += (t1 - t0) / 1e6;
    }

    public void update(TupleOpr opr) {
        long t0 = System.nanoTime();

        Operations operations;
        if (opr.oprType > 0)
            operations = dualTree.insert(opr.t_idx);
        else
            operations = dualTree.delete(opr.t_idx);

        long t1 = System.nanoTime();

        maxInst.update(operations);

        long t2 = System.nanoTime();

        if (opr.oprType > 0) {
            Main.AddTreeTime += (t1 - t0) / 1e6;
            Main.AddSetTime += (t2 - t1) / 1e6;
        } else {
            Main.DelTreeTime += (t1 - t0) / 1e6;
            Main.DelSetTime += (t2 - t1) / 1e6;
        }
    }

    public Set<Integer> result() {
        return maxInst.result();
    }
}
