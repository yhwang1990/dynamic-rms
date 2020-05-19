package data;

import java.util.Random;

import utils.VectorUtil;

public class TupleGenerator {
    private static final Random RAND = new Random(0);

    public static double[][] uniformGenerator(int dim, int size) {
        double[][] tuples = new double[size][dim + 1];

        for (int i = 0; i < size; i++) {
            double[] values = new double[dim + 1];
            for (int j = 0; j < dim; j++) {
                values[j] = Math.round(RAND.nextDouble() * 100000.0) / 100000.0;
            }
            values[dim] = Math.sqrt(dim - VectorUtil.norm2(values));
            tuples[i] = values;
        }

        return tuples;
    }

    private static double randEqual(double min, double max) {
        double x = RAND.nextDouble();
        return x * (max - min) + min;
    }

    private static double randPeak(double min, double max, int dim) {
        double sum = 0.0;
        for (int d = 0; d < dim; d++)
            sum += randEqual(0, 1);
        sum /= dim;
        return sum * (max - min) + min;
    }

    private static double randNormal(double med, double var) {
        return randPeak(med - var, med + var, 12);
    }

    private static boolean isNotValid(int dim, double[] x) {
        for (int d = 0; d < dim; d++) {
            if (x[d] < 0.0 || x[d] > 1.0)
                return true;
        }
        return false;
    }


    public static void generateUniform(int dim, double[] values) {
        for (int i = 0; i < dim; ++i)
            values[i] = randEqual(0, 1);
    }

    public static void generateAnti(int dim, double[] values) {
        int i;
        double[] x = new double[dim];

        do {
            double v = randNormal(0.5, 0.25);
            double l = v <= 0.5 ? v : 1.0 - v;

            for (i = 0; i < dim; ++i)
                x[i] = v;

            for (i = 0; i < dim; ++i) {
                double h = randEqual(-l, l);
                x[i] += h;
                x[(i + 1) % dim] -= h;
            }
        } while (isNotValid(dim, x));

        for (i = 0; i < dim; ++i)
            values[i] = x[i];
    }
}
