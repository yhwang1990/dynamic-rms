package utils;

public class VectorUtil {
    public static double cosine(double[] p, double[] q) {
        return inner_product(p, q) / (l2_norm(p) * l2_norm(q));
    }

    public static double cosine_unit(double[] p, double[] q) {
        return inner_product(p, q);
    }

    public static double l2_dist(double[] p, double[] q) {
        double sum = 0.0;
        for (int i = 0; i < Constant.DIM; i++) {
            sum += (p[i] - q[i]) * (p[i] - q[i]);
        }
        return Math.sqrt(sum);
    }

    public static double inner_product(double[] p, double[] q) {
        double sum = 0.0;
        for (int i = 0; i < Constant.DIM; i++) {
            sum += p[i] * q[i];
        }
        return sum;
    }

    public static double l2_norm(double[] p) {
        double sum = 0.0;
        for (int i = 0; i < Constant.DIM; i++) {
            sum += p[i] * p[i];
        }
        return Math.sqrt(sum);
    }
}
