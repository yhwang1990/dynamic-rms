package index;

public class VectorUtil {
    public static double cosine(double[] p, double[] q) {
        assert p.length == q.length : "dimension not match";
        return inner_product(p, q) / (norm(p) * norm(q));
    }

    public static double cosine_diff(double cos1, double cos2) {
        double sin1 = Math.sqrt(1.0 - cos1 * cos1);
        double sin2 = Math.sqrt(1.0 - cos2 * cos2);
        return cos1 * cos2 + sin1 * sin2;
    }

    public static double cosine_unit(double[] p, double[] q) {
        assert p.length == q.length : "dimension not match";
        return inner_product(p, q);
    }

    public static void to_unit(double[] p) {
        double norm = norm(p);
        for (int i = 0; i < p.length; i++) {
            p[i] /= norm;
        }
    }

    public static double inner_product(double[] p, double[] q) {
        assert p.length == q.length : "dimension not match";

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

    public static double prod2dist(int dim, double product) {
        return 1.0 + dim - 2.0 * product;
    }

    public static boolean pointInRect(double[] p, double[] min, double[] max) {
        for (int i = 0; i < p.length; i++) {
            if (p[i] < min[i] || p[i] > max[i]) {
                return false;
            }
        }
        return true;
    }

    public static double dist2Rect(double[] p, double[] min, double[] max) {
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
