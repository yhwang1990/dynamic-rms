package generators;

import utils.VectorUtil;

import java.util.Random;

public class UtilityGenerator {
    private static Random RAND = new Random(97);

    public static double[][] uniformGenerator(int dim, int size) {
        double[][] utilities = new double[size][dim + 1];

        for (int i = 0; i < size; i++) {
            double[] values = new double[dim + 1];
            for (int j = 0; j < dim; j++) {
                values[j] = RAND.nextDouble();
            }
            values[dim] = 0;
            VectorUtil.to_unit(values);

            utilities[i] = values;
        }

        return utilities;
    }
}
