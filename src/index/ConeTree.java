package index;

import utils.NodeType;
import utils.Utility;
import utils.VectorUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ConeTree {

    private ConeNode root;
    private int dim;
    private double tau;

    public ConeTree(int dim, double eps, List<Utility> samples) {
        this.dim = dim;
        this.tau = 1.0 -eps;
        this.root = new ConeNode(new ArrayList<>(samples));
    }

    public void BFSTraverse() {
        LinkedList<ConeNode> queue = new LinkedList<>();
        queue.add(root);
        while(!queue.isEmpty()) {
            ConeNode cur = queue.removeLast();
            cur.print();
            if(cur.nodeType == NodeType.NON_LEAF) {
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
        ConeNode lc,rc;

        ConeNode(List<Utility> listUtilities) {
            this.nodeType = NodeType.LEAF;
            this.listUtilities = listUtilities;
            this.size = listUtilities.size();
            this.min_k_score = 0.0;

            this.lc = null;
            this.rc = null;

            this.centroid = new double[dim];

            for (Utility u : listUtilities) {
                for (int i = 0; i < dim; i++) {
                    this.centroid[i] += u.value[i];
                }
            }
            for (int i = 0; i < dim; i++) {
                this.centroid[i] /= listUtilities.size();
            }
            VectorUtil.to_unit(this.centroid);


            this.cosine_aperture = 1.0;
            for(Utility u : listUtilities) {
                double cosine = VectorUtil.cosine_unit(u.value, this.centroid);
                if (cosine < this.cosine_aperture)
                    this.cosine_aperture = cosine;
            }

            if (this.cosine_aperture < tau) {
                double[][] pivots = findPivots();

                List<Utility> listLeftUtilities = new ArrayList<>();
                List<Utility> listRightUtilities = new ArrayList<>();
                for (Utility u : listUtilities) {
                    double l_cosine = VectorUtil.cosine_unit(u.value, pivots[0]);
                    double r_cosine = VectorUtil.cosine_unit(u.value, pivots[1]);

                    if(l_cosine >= r_cosine) {
                        listLeftUtilities.add(u);
                    } else {
                        listRightUtilities.add(u);
                    }
                }

                this.nodeType = NodeType.NON_LEAF;

                this.lc = new ConeNode(listLeftUtilities);
                this.rc = new ConeNode(listRightUtilities);

                this.listUtilities.clear();
            }
        }

        private double[][] findPivots() {
            double[][] pivots = new double[2][dim];

            double[] u0 = listUtilities.get(0).value;

            double l_cosine = 1.0;
            for(Utility u : listUtilities) {
                double cosine = VectorUtil.cosine_unit(u.value, u0);
                if (cosine < l_cosine) {
                    l_cosine = cosine;
                    pivots[0] = u.value;
                }
            }

            double r_cosine = 1.0;
            for(Utility u : listUtilities) {
                double cosine = VectorUtil.cosine_unit(u.value, pivots[0]);
                if (cosine < r_cosine) {
                    r_cosine = cosine;
                    pivots[1] = u.value;
                }
            }

            return pivots;
        }

        private void print() {
            StringBuilder b = new StringBuilder();
            b.append(nodeType).append(" ");
            b.append(size).append(" [");
            for (int i = 0; i < dim; i++) {
                b.append(centroid[i]).append(" ");
            }
            b.deleteCharAt(b.length() - 1).append("] ").append(cosine_aperture);
            if(nodeType == NodeType.NON_LEAF){
                b.append("\n");
            } else {
                b.append(" ");
                for(Utility u : listUtilities) {
                    b.append(u.idx).append(",");
                }
                b.deleteCharAt(b.length() - 1).append("\n");
            }
            System.out.print(b.toString());
        }
    }
}
