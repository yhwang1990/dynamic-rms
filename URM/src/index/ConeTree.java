package index;

import java.util.*;

public class ConeTree {

    private final DualTree dualTree;
    private final ConeNode root;

    private final int dim;
    private final double tau;

    public ConeTree(int dim, double tau, DualTree dualTree) {
        this.dim = dim;

        this.tau = tau;
        this.dualTree = dualTree;

        for (int i = 0; i < this.dualTree.sample_size; i++)
        	this.dualTree.topResults[i] = this.dualTree.tIdx.exactTop(this.dualTree.samples[i]);

        List<Integer> utilities = new ArrayList<>();
        for (int i = 0; i < this.dualTree.sample_size; i++) {
            utilities.add(i);
        }

        this.root = new ConeNode(null, utilities);
    }

    public void BFSTraverse() {
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

    void insert(int t_idx) {
        LinkedList<ConeNode> queue = new LinkedList<>();
        queue.addLast(root);
        while (!queue.isEmpty()) {
            ConeNode cur_node = queue.removeFirst();
            if (cur_node.nodeType == NodeType.LEAF) {
                boolean outdated = false;
                for (int u_idx : cur_node.utilities) {
                    double score = VectorUtil.inner_product(dualTree.data[t_idx], dualTree.samples[u_idx]);
                    double old_top_score = dualTree.topResults[u_idx].score;
                    boolean isUpdated = false;
                    if (score > old_top_score + 1e-9) {
                    	isUpdated = true;
                    	dualTree.topResults[u_idx].t_idx = t_idx;
                    	dualTree.topResults[u_idx].score = score;
                    }
                    if (isUpdated && Math.abs(cur_node.min_score - old_top_score) < 1e-9)
                        outdated = true;
                }
                if (outdated) {
                    cur_node.min_score = Double.MAX_VALUE;
                    for (int u_idx : cur_node.utilities)
                        cur_node.min_score = Math.min(dualTree.topResults[u_idx].score, cur_node.min_score);
                    update_min_score(cur_node);
                }
            } else {
                if (max_inner_product(dualTree.data[t_idx], cur_node.lc) > cur_node.lc.min_score - 1e-6)
                    queue.addLast(cur_node.lc);
                if (max_inner_product(dualTree.data[t_idx], cur_node.rc) > cur_node.rc.min_score - 1e-6)
                    queue.addLast(cur_node.rc);
            }
        }
    }

    void delete(int t_idx) {
        LinkedList<ConeNode> queue = new LinkedList<>();
        queue.addLast(root);
        while (!queue.isEmpty()) {
            ConeNode cur_node = queue.removeFirst();
            if (cur_node.nodeType == NodeType.LEAF) {
                boolean outdated = false;
                for (int u_idx : cur_node.utilities) {
                    double old_top_score = dualTree.topResults[u_idx].score;
                    boolean isUpdated = false;
                    if (dualTree.topResults[u_idx].t_idx == t_idx) {
                    	isUpdated = true;
                    	dualTree.topResults[u_idx] = dualTree.tIdx.exactTop(dualTree.samples[u_idx]);
                    }
                    if (isUpdated && Math.abs(cur_node.min_score - old_top_score) < 1e-9)
                        outdated = true;
                }
                if (outdated) {
                    cur_node.min_score = Double.MAX_VALUE;
                    for (int u_idx : cur_node.utilities)
                        cur_node.min_score = Math.min(dualTree.topResults[u_idx].score, cur_node.min_score);
                    update_min_score(cur_node);
                }
            } else {
                if (max_inner_product(dualTree.data[t_idx], cur_node.lc) > cur_node.lc.min_score - 1e-6)
                    queue.addLast(cur_node.lc);
                if (max_inner_product(dualTree.data[t_idx], cur_node.rc) > cur_node.rc.min_score - 1e-6)
                    queue.addLast(cur_node.rc);
            }
        }
    }

    private double max_inner_product(double[] t, ConeNode node) {
        double cosine = VectorUtil.cosine(t, node.centroid);
        if (cosine > node.cosine_aperture)
            return VectorUtil.norm(t);
        else
            return VectorUtil.norm(t) * VectorUtil.cosine_diff(cosine, node.cosine_aperture);
    }

    private void update_min_score(ConeNode node) {
        ConeNode par = node.par;
        while (par != null) {
            double pre_score = par.min_score;
            par.min_score = Math.min(par.lc.min_score, par.rc.min_score);
            if (Math.abs(par.min_score - pre_score) < 1e-9) {
                break;
            } else {
                par = par.par;
            }
        }
    }

    private class ConeNode {
        private final int size;
        private final double[] centroid;
        private double cosine_aperture, min_score;

        private NodeType nodeType;
        private final List<Integer> utilities;
        private ConeNode lc;
        private ConeNode rc;
        private final ConeNode par;

        private ConeNode(ConeNode par, List<Integer> utilities) {
            this.par = par;
            this.nodeType = NodeType.LEAF;

            this.utilities = utilities;
            this.size = utilities.size();

            this.lc = null;
            this.rc = null;

            this.centroid = new double[dim];

            for (int u_idx : utilities) {
                for (int i = 0; i < dim; i++) {
                    this.centroid[i] += dualTree.samples[u_idx][i];
                }
            }
            for (int i = 0; i < dim; i++) {
                this.centroid[i] /= utilities.size();
            }
            VectorUtil.to_unit(this.centroid);

            this.cosine_aperture = 1.0;
            this.min_score = Double.MAX_VALUE;
            for (int u_idx : utilities) {
                double cosine = VectorUtil.cosine_unit(dualTree.samples[u_idx], this.centroid);
                this.cosine_aperture = Math.min(cosine, this.cosine_aperture);
                this.min_score = Math.min(dualTree.topResults[u_idx].score, min_score);
            }

            if (this.cosine_aperture < tau) {
                double[][] pivots = findPivots();

                List<Integer> leftUtilities = new ArrayList<>();
                List<Integer> rightUtilities = new ArrayList<>();
                for (int u_idx : utilities) {
                    double l_cosine = VectorUtil.cosine_unit(dualTree.samples[u_idx], pivots[0]);
                    double r_cosine = VectorUtil.cosine_unit(dualTree.samples[u_idx], pivots[1]);

                    if (l_cosine >= r_cosine) {
                        leftUtilities.add(u_idx);
                    } else {
                        rightUtilities.add(u_idx);
                    }
                }

                this.nodeType = NodeType.NON_LEAF;

                this.lc = new ConeNode(this, leftUtilities);
                this.rc = new ConeNode(this, rightUtilities);

                this.utilities.clear();
            }
        }

        private double[][] findPivots() {
            double[][] pivots = new double[2][dim];

            double[] u0 = dualTree.samples[utilities.get(0)];

            double l_cosine = 1.0;
            for (int u_idx : utilities) {
                double cosine = VectorUtil.cosine_unit(dualTree.samples[u_idx], u0);
                if (cosine < l_cosine) {
                    l_cosine = cosine;
                    pivots[0] = dualTree.samples[u_idx];
                }
            }

            double r_cosine = 1.0;
            for (int u_idx : utilities) {
                double cosine = VectorUtil.cosine_unit(dualTree.samples[u_idx], pivots[0]);
                if (cosine < r_cosine) {
                    r_cosine = cosine;
                    pivots[1] = dualTree.samples[u_idx];
                }
            }

            return pivots;
        }

        private void print() {
            StringBuilder b = new StringBuilder();
            b.append(nodeType).append(" ");
            b.append(size).append(" ").append(min_score).append(" [");
            for (int i = 0; i < dim; i++) {
                b.append(centroid[i]).append(" ");
            }
            b.deleteCharAt(b.length() - 1).append("] ").append(cosine_aperture);
            if (nodeType == NodeType.NON_LEAF) {
                b.append("\n");
            } else {
                b.append(" ");
                for (int u_idx : utilities) {
                    b.append(u_idx).append(",");
                }
                b.deleteCharAt(b.length() - 1).append("\n");
            }
            System.out.print(b.toString());
        }
    }
}
