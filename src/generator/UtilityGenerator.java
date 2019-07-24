package generator;

import utils.VectorUtil;

import java.util.Random;

public class UtilityGenerator {
    private static Random rand = new Random(0);

    public static double[][] uniformGenerator(int dim, int size) {
        double[][] utilities = new double[size][dim + 1];

        for (int i = 0; i < size; i++) {
            double[] values = new double[dim + 1];
            for (int j = 0; j < dim; j++) {
                values[j] = rand.nextDouble();
            }
            values[dim] = 0;
            VectorUtil.to_unit(values);

            utilities[i] = values;
        }

        return utilities;
    }
}
