package structures;

import utils.*;

import java.util.*;

public class ConeTree {

    private DualTree dualTree;
    private ConeNode root;

    private int dim;
    private int k;
    private double eps;
    private double tau;

    private double[][] samples;
    TopKResult[] topKResults;

    ConeTree(int dim, int k, double eps, double tau, DualTree dualTree) {
        this.dim = dim;
        this.k = k;
        this.eps = eps;

        this.tau = tau;
        this.dualTree = dualTree;

        int sample_size = dualTree.sample_size;

        this.samples = dualTree.samples;

        this.topKResults = new TopKResult[sample_size];
        for (int i = 0; i < sample_size; i++) {
            this.topKResults[i] = this.dualTree.tIdx.approxTopKSearch(this.k, this.eps, this.samples[i]);
        }

        List<Integer> utilities = new ArrayList<>();
        for (int i = 0; i < sample_size; i++) {
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

    void insert(int t_idx, Operations opr) {
        LinkedList<ConeNode> queue = new LinkedList<>();
        queue.addLast(root);
        while (!queue.isEmpty()) {
            ConeNode cur_node = queue.removeFirst();
            if (cur_node.nodeType == NodeType.LEAF) {
                boolean outdated = false;
                for (int u_idx : cur_node.utilities) {
                    double score = VectorUtil.inner_product(dualTree.tIdx.data[t_idx], samples[u_idx]);
                    double old_k_score = topKResults[u_idx].k_score;
                    boolean k_updated = topKResults[u_idx].add(u_idx, t_idx, score, opr);

                    if (k_updated && cur_node.min_k_score == old_k_score)
                        outdated = true;
                }
                if (outdated) {
                    cur_node.min_k_score = Double.MAX_VALUE;
                    for (int u_idx : cur_node.utilities)
                        cur_node.min_k_score = Math.min(topKResults[u_idx].k_score, cur_node.min_k_score);
                    update_min_k_score(cur_node);
                }
            } else {
                if (max_inner_product(dualTree.tIdx.data[t_idx], cur_node.lc) >= (1 - eps) * cur_node.lc.min_k_score)
                    queue.addLast(cur_node.lc);
                if (max_inner_product(dualTree.tIdx.data[t_idx], cur_node.rc) >= (1 - eps) * cur_node.rc.min_k_score)
                    queue.addLast(cur_node.rc);
            }
        }
    }

    void delete(int t_idx, Operations opr) {
        LinkedList<ConeNode> queue = new LinkedList<>();
        queue.addLast(root);
        while (!queue.isEmpty()) {
            ConeNode cur_node = queue.removeFirst();
            if (cur_node.nodeType == NodeType.LEAF) {
                boolean outdated = false;
                for (int u_idx : cur_node.utilities) {
                    double score = VectorUtil.inner_product(dualTree.tIdx.data[t_idx], samples[u_idx]);
                    double old_k_score = topKResults[u_idx].k_score;

                    boolean k_updated = false;
                    if (score >= topKResults[u_idx].k_score) {
                        k_updated = true;
                        TopKResult newResult = dualTree.tIdx.approxTopKSearch(k, eps, samples[u_idx]);
                        opr.utilities.add(u_idx);
                        TopKResult oldResult = topKResults[u_idx];
                        for (int new_idx : newResult.results) {
                            if (!oldResult.results.contains(new_idx)) {
                                opr.oprs.add(new Operations.SetOpr(new_idx, u_idx));
                            }
                        }
                        topKResults[u_idx] = newResult;
                    } else if (score >= (1.0 - eps) * topKResults[u_idx].k_score) {
                        topKResults[u_idx].delete(u_idx, t_idx, score, opr);
                    }

                    if (k_updated && cur_node.min_k_score == old_k_score)
                        outdated = true;
                }
                if (outdated) {
                    cur_node.min_k_score = Double.MAX_VALUE;
                    for (int u_idx : cur_node.utilities)
                        cur_node.min_k_score = Math.min(topKResults[u_idx].k_score, cur_node.min_k_score);
                    update_min_k_score(cur_node);
                }
            } else {
                if (max_inner_product(dualTree.tIdx.data[t_idx], cur_node.lc) >= (1 - eps) * cur_node.lc.min_k_score)
                    queue.addLast(cur_node.lc);
                if (max_inner_product(dualTree.tIdx.data[t_idx], cur_node.rc) >= (1 - eps) * cur_node.rc.min_k_score)
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

    private void update_min_k_score(ConeNode node) {
        ConeNode par = node.par;
        while (par != null) {
            double pre_k_score = par.min_k_score;
            par.min_k_score = Math.min(par.lc.min_k_score, par.rc.min_k_score);
            if (par.min_k_score == pre_k_score) {
                break;
            } else {
                par = par.par;
            }
        }
    }

    private class ConeNode {
        private int size;
        private double[] centroid;
        private double cosine_aperture, min_k_score;

        private NodeType nodeType;
        private List<Integer> utilities;
        private ConeNode lc, rc, par;

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
                    this.centroid[i] += samples[u_idx][i];
                }
            }
            for (int i = 0; i < dim; i++) {
                this.centroid[i] /= utilities.size();
            }
            VectorUtil.to_unit(this.centroid);

            this.cosine_aperture = 1.0;
            this.min_k_score = Double.MAX_VALUE;
            for (int u_idx : utilities) {
                double cosine = VectorUtil.cosine_unit(samples[u_idx], this.centroid);
                this.cosine_aperture = Math.min(cosine, this.cosine_aperture);
                this.min_k_score = Math.min(topKResults[u_idx].k_score, min_k_score);
            }

            if (this.cosine_aperture < tau) {
                double[][] pivots = findPivots();

                List<Integer> leftUtilities = new ArrayList<>();
                List<Integer> rightUtilities = new ArrayList<>();
                for (int u_idx : utilities) {
                    double l_cosine = VectorUtil.cosine_unit(samples[u_idx], pivots[0]);
                    double r_cosine = VectorUtil.cosine_unit(samples[u_idx], pivots[1]);

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

            double[] u0 = samples[utilities.get(0)];

            double l_cosine = 1.0;
            for (int u_idx : utilities) {
                double cosine = VectorUtil.cosine_unit(samples[u_idx], u0);
                if (cosine < l_cosine) {
                    l_cosine = cosine;
                    pivots[0] = samples[u_idx];
                }
            }

            double r_cosine = 1.0;
            for (int u_idx : utilities) {
                double cosine = VectorUtil.cosine_unit(samples[u_idx], pivots[0]);
                if (cosine < r_cosine) {
                    r_cosine = cosine;
                    pivots[1] = samples[u_idx];
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
                for (int u_idx : utilities) {
                    b.append(u_idx).append(",");
                }
                b.deleteCharAt(b.length() - 1).append("\n");
            }
            System.out.print(b.toString());
        }
    }
}
