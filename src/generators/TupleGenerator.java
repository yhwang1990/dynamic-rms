package generators;

import utils.VectorUtil;

import java.util.Random;

public class TupleGenerator {
    private static Random rand = new Random(0);

    public static double[][] uniformGenerator(int dim, int size) {
        double[][] tuples = new double[size][dim + 1];

        for (int i = 0; i < size; i++) {
            double[] values = new double[dim + 1];
            for (int j = 0; j < dim; j++) {
                values[j] = Math.round(rand.nextDouble() * 1000.0) / 1000.0;
            }
            values[dim] = Math.sqrt(dim - VectorUtil.norm2(values));
            tuples[i] = values;
        }

        return tuples;
    }
}
