package index;

import utils.*;

import java.util.*;

public class ConeTree {

    private DualTree dualTree;
    private ConeNode root;
    private int dim;
    private double tau;

    ConeTree(int dim, double tau, DualTree dualTree) {
        this.dim = dim;
        this.tau = tau;
        this.dualTree = dualTree;

        List<Utility> utilities = new ArrayList<>();
        for (int i = 0; i < this.dualTree.utilities.length; i++) {
            utilities.add(new Utility(i, this.dualTree.topKResults[i].k_score, this.dualTree.utilities[i]));
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

    void insert(int t_idx, double[] t_values, List<SetOperation> operations) {
        LinkedList<ConeNode> queue = new LinkedList<>();
        queue.addLast(root);
        while (!queue.isEmpty()) {
            ConeNode cur_node = queue.removeFirst();
            if (cur_node.nodeType == NodeType.LEAF) {
                boolean outdated = false;
                for (Utility u : cur_node.listUtilities) {
                    double score = VectorUtil.inner_product(t_values, u.values);
                    boolean k_updated = dualTree.topKResults[u.idx].add(u.idx, t_idx, score, operations);
                    if (k_updated) {
                        if (cur_node.min_k_score == u.k_score) {
                            outdated = true;
                        }
                        u.k_score = dualTree.topKResults[u.idx].k_score;
                    }
                }
                if (outdated) {
                    cur_node.min_k_score = Double.MAX_VALUE;
                    for (Utility u : cur_node.listUtilities) {
                        cur_node.min_k_score = Math.min(u.k_score, cur_node.min_k_score);
                    }
                    update_min_k_score(cur_node);
                }
            } else {
                if (max_inner_product(t_values, cur_node.lc) >= (1 - dualTree.epsilon) * cur_node.lc.min_k_score) {
                    queue.addLast(cur_node.lc);
                }
                if (max_inner_product(t_values, cur_node.rc) >= (1 - dualTree.epsilon) * cur_node.rc.min_k_score) {
                    queue.addLast(cur_node.rc);
                }
            }
        }
    }

    void delete(int t_idx, double[] t_values, List<SetOperation> operations) {
        LinkedList<ConeNode> queue = new LinkedList<>();
        queue.addLast(root);
        while (!queue.isEmpty()) {
            ConeNode cur_node = queue.removeFirst();
            if (cur_node.nodeType == NodeType.LEAF) {
                boolean outdated = false;
                for (Utility u : cur_node.listUtilities) {
                    double score = VectorUtil.inner_product(t_values, u.values);
                    boolean k_updated = false;
                    if (score > u.k_score) {
                        k_updated = true;
                        TopKResult newResult = dualTree.tupleIdx.approxTopKSearch(dualTree.k, dualTree.epsilon, u.values);
                        operations.add(new SetOperation(OprType.T_DEL, t_idx, u.idx));
                        TopKResult oldResult = dualTree.topKResults[u.idx];
                        for (int idx : newResult.results) {
                            if (!oldResult.results.contains(idx)) {
                                operations.add(new SetOperation(OprType.S_ADD, idx, u.idx));
                            }
                        }
                        dualTree.topKResults[u.idx] = newResult;
                    } else if (score >= (1.0 - dualTree.epsilon) * u.k_score) {
                        dualTree.topKResults[u.idx].delete(u.idx, t_idx, score, operations);
                    }

                    if (k_updated) {
                        if (cur_node.min_k_score == u.k_score) {
                            outdated = true;
                        }
                        u.k_score = dualTree.topKResults[u.idx].k_score;
                    }
                }
                if (outdated) {
                    cur_node.min_k_score = Double.MAX_VALUE;
                    for (Utility u : cur_node.listUtilities) {
                        cur_node.min_k_score = Math.min(u.k_score, cur_node.min_k_score);
                    }
                    update_min_k_score(cur_node);
                }
            } else {
                if (max_inner_product(t_values, cur_node.lc) >= (1 - dualTree.epsilon) * cur_node.lc.min_k_score) {
                    queue.addLast(cur_node.lc);
                }
                if (max_inner_product(t_values, cur_node.rc) >= (1 - dualTree.epsilon) * cur_node.rc.min_k_score) {
                    queue.addLast(cur_node.rc);
                }
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
            par.min_k_score = Math.min(par.lc.min_k_score, par.rc.min_k_score);
            if (par.min_k_score < node.min_k_score) {
                break;
            } else {
                par = par.par;
            }
        }
    }

    private class ConeNode {
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
