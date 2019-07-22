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

    public static void to_unit(double[] p) {
        double norm = norm(p);
        for (int i = 0; i < p.length; i++) {
            p[i] /= norm;
        }
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

    public static boolean pointInRectangle(double[] p, double[] min, double[] max) {
        for (int i = 0; i < p.length; i++) {
            if (p[i] < min[i] || p[i] > max[i]) {
                return false;
            }
        }
        return true;
    }

    public static double dist2(double[] p, double[] min, double[] max) {
        double sum = 0.0;
        for (int i = 0; i < p.length; i++) {
            if (p[i] < min[i]) {
                sum += (min[i] - p[i]) * (min[i] - p[i]);
            } else if (p[i] > max[i]) {
                sum += (p[i] - max[i]) * (p[i] - max[i]);
            }
        }
        return sum;
    }
}
