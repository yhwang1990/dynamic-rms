package utils;

public class VectorUtil {
    public static double cosine(double[] p, double[] q) {
        if (p.length != q.length)
            System.err.println("dimension not equal");

        return inner_product(p, q) / (norm(p) * norm(q));
    }

    public static double cosine_unit(double[] p, double[] q) {
        if (p.length != q.length)
            System.err.println("dimension not equal");

        return inner_product(p, q);
    }

    public static double[] unit_norm(double[] p) {
        double norm = norm(p);
        double[] unit_p = new double[p.length];
        for (int i = 0; i < p.length; i++) {
            unit_p[i] = p[i] / norm;
        }
        return unit_p;
    }

    public static double dist(double[] p, double[] q) {
        if (p.length != q.length)
            System.err.println("dimension not equal");

        double sum = 0.0;
        for (int i = 0; i < p.length; i++) {
            sum += (p[i] - q[i]) * (p[i] - q[i]);
        }
        return Math.sqrt(sum);
    }

    public static double dist2(double[] p, double[] q) {
        if (p.length != q.length)
            System.err.println("dimension not equal");

        double sum = 0.0;
        for (int i = 0; i < p.length; i++) {
            sum += (p[i] - q[i]) * (p[i] - q[i]);
        }
        return sum;
    }

    public static double inner_product(double[] p, double[] q) {
        if (p.length != q.length)
            System.err.println("dimension not equal");

        double sum = 0.0;
        for (int i = 0; i < p.length; i++) {
            sum += p[i] * q[i];
        }
        return sum;
    }

    public static double norm(double[] p) {
        double sum = 0.0;
        for (double val : p) {
            sum += val * val;
        }
        return Math.sqrt(sum);
    }

    public static double norm2(double[] p) {
        double sum = 0.0;
        for (double val : p) {
            sum += val * val;
        }
        return sum;
    }

    public static double dist2product(int dim, double dist2) {
        return (1.0 + dim - dist2) / 2.0;
    }

    public static double product2dist(int dim, double product) {
        return 1.0 + dim - 2.0 * product;
    }

    public static boolean pointInRectangle(double[] p, double[] lBound, double[] hBound) {
        for (int i = 0; i < p.length; i++) {
            if (p[i] < lBound[i] || p[i] > hBound[i]) {
                return false;
            }
        }
        return true;
    }
}
