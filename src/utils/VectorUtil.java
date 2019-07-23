package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

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

    public static void bruteForceTopK(int k, double eps, Utility u, List<Tuple> tuples) {
        double k_score = 0.0;
        PriorityQueue<RankItem> exactResult = new PriorityQueue<>();
        PriorityQueue<RankItem> approxResult = new PriorityQueue<>();
        for (Tuple t : tuples) {
            double score = inner_product(u.value, t.value);
            if (score > k_score) {
                exactResult.offer(new RankItem(t.idx, score));
                if (! exactResult.isEmpty() && exactResult.size() == k) {
                    k_score = exactResult.peek().score;
                } else if (! exactResult.isEmpty() && exactResult.size() > k) {
                    RankItem deleted_item = exactResult.poll();
                    if (! exactResult.isEmpty()) {
                        k_score = exactResult.peek().score;
                    }

                    if (deleted_item.score >= (1 - eps) * k_score) {
                        approxResult.offer(deleted_item);
                    }

                    while(! approxResult.isEmpty() && approxResult.peek().score < (1 - eps) * k_score) {
                        approxResult.poll();
                    }
                }
            } else if (score >= (1 - eps) * k_score) {
                approxResult.offer(new RankItem(t.idx, score));
            }
        }
    }

    public static void bruteForceTopKTest(Utility u, List<Tuple> tuples) {
        for (Tuple t : tuples) {
            double score = inner_product(u.value, t.value);
        }
    }
}
