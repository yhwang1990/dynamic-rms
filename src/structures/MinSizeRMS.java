package structures;

import generators.TupleGenerator;
import generators.UtilityGenerator;

import utils.Data;

public class MinSizeRMS {

    public DualTree dualTree;
    public SetCover setCover;

    public MinSizeRMS(int dim, int k, double eps, int data_size, int init_size, int sample_size) {
        Data.DIM = dim;
        Data.K = k;
        Data.EPS = eps;
        Data.DATA_SIZE = data_size;
        Data.SAMPLE_SIZE = sample_size;

        initializeDataset(data_size, init_size, sample_size);

        this.dualTree = new DualTree();
        this.setCover = new SetCover();
    }

    private void initializeDataset(int data_size, int init_size, int sample_size) {
        Data.TUPLES = TupleGenerator.uniformGenerator(Data.DIM, data_size);
        Data.UTILITIES = UtilityGenerator.uniformGenerator(Data.DIM, sample_size);

        Data.DELETED = new boolean[Data.DATA_SIZE];
        for (int i = 0; i < init_size; i++) {
            Data.DELETED[i] = false;
        }
        for (int i = init_size; i < data_size; i++) {
            Data.DELETED[i] = true;
        }
    }
}
