package index;

import utils.Dataset;
import utils.NodeType;
import utils.VectorUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ConeTree {

    private ConeNode root;
    private int dim;
    private double tau;

    ConeTree(int dim, double eps) {
        this.dim = dim;
        this.tau = 1.0 - eps;

        List<Utility> utilities = new ArrayList<>();
        for (int i = 0; i < Dataset.UTILITIES.length; i++) {
            utilities.add(new Utility(i, Dataset.TOP_K_RESULTS[i].k_score, Dataset.UTILITIES[i]));
        }

        this.root = new ConeNode(null, utilities);
    }

    void BFSTraverse() {
        LinkedList<ConeNode> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            ConeNode cur = queue.removeLast();
            cur.print();
            if (cur.nodeType == NodeType.NON_LEAF) {
                queue.addFirst(cur.lc);
                queue.addFirst(cur.rc);
            }
        }
    }

    class ConeNode {
        int size;
        double[] centroid;
        double cosine_aperture, min_k_score;

        NodeType nodeType;
        List<Utility> listUtilities;
        ConeNode lc, rc, par;

        ConeNode(ConeNode par, List<Utility> listUtilities) {
            this.par = par;
            this.nodeType = NodeType.LEAF;

            this.listUtilities = listUtilities;
            this.size = listUtilities.size();

            this.lc = null;
            this.rc = null;

            this.centroid = new double[dim];

            for (Utility u : listUtilities) {
                for (int i = 0; i < dim; i++) {
                    this.centroid[i] += u.values[i];
                }
            }
            for (int i = 0; i < dim; i++) {
                this.centroid[i] /= listUtilities.size();
            }
            VectorUtil.to_unit(this.centroid);

            this.cosine_aperture = 1.0;
            this.min_k_score = Double.MAX_VALUE;
            for (Utility u : listUtilities) {
                this.cosine_aperture = Math.min(VectorUtil.cosine_unit(u.values, this.centroid), this.cosine_aperture);
                this.min_k_score = Math.min(u.k_score, min_k_score);
            }

            if (this.cosine_aperture < tau) {
                double[][] pivots = findPivots();

                List<Utility> listLeftUtilities = new ArrayList<>();
                List<Utility> listRightUtilities = new ArrayList<>();
                for (Utility u : listUtilities) {
                    double l_cosine = VectorUtil.cosine_unit(u.values, pivots[0]);
                    double r_cosine = VectorUtil.cosine_unit(u.values, pivots[1]);

                    if (l_cosine >= r_cosine) {
                        listLeftUtilities.add(u);
                    } else {
                        listRightUtilities.add(u);
                    }
                }

                this.nodeType = NodeType.NON_LEAF;

                this.lc = new ConeNode(this, listLeftUtilities);
                this.rc = new ConeNode(this, listRightUtilities);

                this.listUtilities.clear();
            }
        }

        private double[][] findPivots() {
            double[][] pivots = new double[2][dim];

            double[] u0 = listUtilities.get(0).values;

            double l_cosine = 1.0;
            for (Utility u : listUtilities) {
                double cosine = VectorUtil.cosine_unit(u.values, u0);
                if (cosine < l_cosine) {
                    l_cosine = cosine;
                    pivots[0] = u.values;
                }
            }

            double r_cosine = 1.0;
            for (Utility u : listUtilities) {
                double cosine = VectorUtil.cosine_unit(u.values, pivots[0]);
                if (cosine < r_cosine) {
                    r_cosine = cosine;
                    pivots[1] = u.values;
                }
            }

            return pivots;
        }

        private void print() {
            StringBuilder b = new StringBuilder();
            b.append(nodeType).append(" ");
            b.append(size).append(" ").append(min_k_score).append(" [");
            for (int i = 0; i < dim; i++) {
                b.append(centroid[i]).append(" ");
            }
            b.deleteCharAt(b.length() - 1).append("] ").append(cosine_aperture);
            if (nodeType == NodeType.NON_LEAF) {
                b.append("\n");
            } else {
                b.append(" ");
                for (Utility u : listUtilities) {
                    b.append(u.idx).append(",");
                }
                b.deleteCharAt(b.length() - 1).append("\n");
            }
            System.out.print(b.toString());
        }
    }

    private class Utility {
        private int idx;
        private double k_score;
        private double[] values;

        private Utility(int idx, double k_score, double[] values) {
            this.idx = idx;
            this.k_score = k_score;
            this.values = values;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Utility)) return false;
            Utility tuple = (Utility) o;
            return idx == tuple.idx;
        }

        @Override
        public int hashCode() {
            return Objects.hash(idx);
        }
    }
}
