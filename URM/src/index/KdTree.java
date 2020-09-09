package index;

import java.util.*;

public class KdTree {

	private final DualTree dualTree;
    private final KdNode root;
    
    private final int dim;
    private final int capacity;

    public KdTree(int dim, int capacity, DualTree dualTree) {
        this.dim = dim;
        this.capacity = capacity;
        
        this.dualTree = dualTree;

        double[] min = new double[this.dim];
        double[] max = new double[this.dim];

        for (int i = 0; i < this.dim - 1; i++) {
            min[i] = 0;
            max[i] = 1;
        }
        min[this.dim - 1] = 0;
        max[this.dim - 1] = Math.sqrt(this.dim - 1);

        List<Integer> tuples = new ArrayList<>();
        for (int i = 0; i < this.dualTree.data_size; i++) {
            if (!this.dualTree.isDeleted[i]) {
                tuples.add(i);
            }
        }

        this.root = new KdNode(min, max, tuples);
    }

    boolean insert(int t_idx) {
        boolean isUpdated = false;
        if (dualTree.isDeleted[t_idx]) {
        	dualTree.isDeleted[t_idx] = false;
            isUpdated = true;
        }
        if (!isUpdated) {
            return false;
        }
        return root.insert(t_idx);
    }

    boolean delete(int t_idx) {
        boolean isUpdated = false;
        if (!dualTree.isDeleted[t_idx]) {
        	dualTree.isDeleted[t_idx] = true;
            isUpdated = true;
        }
        if (!isUpdated) {
            return false;
        }
        return root.delete(t_idx);
    }

    public RankItem exactTop(double[] u) {
        double top_score = 0.0;
        PriorityQueue<RankItem> exact_result = new PriorityQueue<>();

        KdNode best_node = root;
        while (best_node.nodeType != NodeType.LEAF) {
            if (VectorUtil.pointInRect(u, best_node.lc.lb, best_node.lc.hb))
                best_node = best_node.lc;
            else
                best_node = best_node.rc;
        }

        for (int t_idx : best_node.tuples) {
            double score = VectorUtil.inner_product(u, dualTree.data[t_idx]);
            if (score > top_score) {
                exact_result.offer(new RankItem(t_idx, score));
                if (! exact_result.isEmpty() && exact_result.size() == 1) {
                    top_score = exact_result.peek().score;
                } else if (! exact_result.isEmpty() && exact_result.size() > 1) {
                    exact_result.poll();
                    if (! exact_result.isEmpty())
                        top_score = exact_result.peek().score;
                }
            }
        }

        double min_dist = Double.MAX_VALUE;
        if (top_score > 0)
            min_dist = VectorUtil.prod2dist(dim - 1, top_score);

        PriorityQueue<RandNode> queue = new PriorityQueue<>();
        queue.offer(new RandNode(root, 0.0));
        while (!queue.isEmpty() && queue.peek().dist2 <= min_dist) {
            KdNode cur_node = queue.poll().node;
            if (cur_node.nodeType == NodeType.LEAF) {
                if (cur_node == best_node)
                    continue;
                for (int t_idx : cur_node.tuples) {
                    double score = VectorUtil.inner_product(u, dualTree.data[t_idx]);
                    if (score > top_score) {
                        exact_result.offer(new RankItem(t_idx, score));
                        if (! exact_result.isEmpty() && exact_result.size() == 1) {
                            top_score = exact_result.peek().score;
                        } else if (! exact_result.isEmpty() && exact_result.size() > 1) {
                            exact_result.poll();
                            if (! exact_result.isEmpty())
                            	top_score = exact_result.peek().score;
                        }
                    }
                    min_dist = VectorUtil.prod2dist(dim - 1, top_score);
                }
            } else {
                double l_dist2 = VectorUtil.dist2Rect(u, cur_node.lc.lb, cur_node.lc.hb);
                if (l_dist2 <= min_dist) {
                    queue.offer(new RandNode(cur_node.lc, l_dist2));
                }
                double r_dist2 = VectorUtil.dist2Rect(u, cur_node.rc.lb, cur_node.rc.hb);
                if (r_dist2 <= min_dist) {
                    queue.offer(new RandNode(cur_node.rc, r_dist2));
                }
            }
        }
        return exact_result.peek();
    }

    public void BFSTraverse() {
        LinkedList<KdNode> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            KdNode cur = queue.removeLast();
            cur.print();
            if (cur.nodeType == NodeType.NON_LEAF) {
                queue.addFirst(cur.lc);
                queue.addFirst(cur.rc);
            }
        }
    }

    private class RandNode implements Comparable<RandNode> {
        KdNode node;
        double dist2;

        RandNode(KdNode node, double dist2) {
            this.node = node;
            this.dist2 = dist2;
        }

        @Override
        public int compareTo(RandNode other) {
            return Double.compare(this.dist2 - 1e-9, other.dist2);
        }
    }

    private class KdNode {
        double[] lb;
        double[] hb;
        int size;

        NodeType nodeType;
        List<Integer> tuples;
        KdNode lc, rc;

        KdNode(double[] lb, double[] hb, List<Integer> tuples) {
            this.nodeType = NodeType.LEAF;

            this.tuples = tuples;
            this.size = tuples.size();

            this.lc = null;
            this.rc = null;

            this.lb = lb;
            this.hb = hb;

            if (this.size > 2 * capacity) {
                int split_coordinate = findSplitCoordinate(this.lb, this.hb);
                this.tuples.sort(new CoordComp(split_coordinate));
                double split_point = dualTree.data[this.tuples.get(this.size / 2)][split_coordinate];

                List<Integer> leftTuples = new ArrayList<>(this.tuples.subList(0, this.size / 2));
                List<Integer> rightTuples = new ArrayList<>(this.tuples.subList(this.size / 2, this.size));

                double[] leftLBound = new double[dim];
                double[] leftHBound = new double[dim];
                System.arraycopy(this.lb, 0, leftLBound, 0, dim);
                System.arraycopy(this.hb, 0, leftHBound, 0, dim);
                leftHBound[split_coordinate] = split_point;

                double[] rightLBound = new double[dim];
                double[] rightHBound = new double[dim];
                System.arraycopy(this.lb, 0, rightLBound, 0, dim);
                System.arraycopy(this.hb, 0, rightHBound, 0, dim);
                rightLBound[split_coordinate] = split_point;

                this.nodeType = NodeType.NON_LEAF;
                this.lc = new KdNode(leftLBound, leftHBound, leftTuples);
                this.rc = new KdNode(rightLBound, rightHBound, rightTuples);

                this.tuples.clear();
            }
        }

        private int findSplitCoordinate(double[] lBound, double[] hBound) {
            int max_coordinate = -1;
            double max_range = 0.0;
            for (int i = 0; i < dim; i++) {
                if (hBound[i] - lBound[i] > max_range) {
                    max_coordinate = i;
                    max_range = hBound[i] - lBound[i];
                }
            }
            return max_coordinate;
        }

        private boolean insert(int t_idx) {
            size += 1;
            if (nodeType == NodeType.NON_LEAF) {
                if (VectorUtil.pointInRect(dualTree.data[t_idx], lc.lb, lc.hb)) {
                    return lc.insert(t_idx);
                } else {
                    return rc.insert(t_idx);
                }
            } else {
                tuples.add(t_idx);
                if (size > 2 * capacity) {
                    int split_coordinate = findSplitCoordinate(lb, hb);
                    tuples.sort(new CoordComp(split_coordinate));
                    double split_point = dualTree.data[tuples.get(size / 2)][split_coordinate];

                    List<Integer> leftTuples = new ArrayList<>(tuples.subList(0, size / 2));
                    List<Integer> rightTuples = new ArrayList<>(tuples.subList(size / 2, size));

                    double[] leftLBound = new double[dim];
                    double[] leftHBound = new double[dim];
                    System.arraycopy(lb, 0, leftLBound, 0, dim);
                    System.arraycopy(hb, 0, leftHBound, 0, dim);
                    leftHBound[split_coordinate] = split_point;

                    double[] rightLBound = new double[dim];
                    double[] rightHBound = new double[dim];
                    System.arraycopy(lb, 0, rightLBound, 0, dim);
                    System.arraycopy(hb, 0, rightHBound, 0, dim);
                    rightLBound[split_coordinate] = split_point;

                    nodeType = NodeType.NON_LEAF;
                    lc = new KdNode(leftLBound, leftHBound, leftTuples);
                    rc = new KdNode(rightLBound, rightHBound, rightTuples);

                    tuples.clear();
                }
                return true;
            }
        }

        private boolean delete(int t_idx) {
            if (nodeType == NodeType.LEAF) {
                boolean is_deleted = tuples.remove(Integer.valueOf(t_idx));
                if (is_deleted) {
                    size -= 1;
                }
                return is_deleted;
            } else if (nodeType == NodeType.NON_LEAF && lc.size > capacity && rc.size > capacity) {
                boolean left_deleted = false, right_deleted = false;
                if (VectorUtil.pointInRect(dualTree.data[t_idx], lc.lb, lc.hb)) {
                    left_deleted = lc.delete(t_idx);
                }
                if (VectorUtil.pointInRect(dualTree.data[t_idx], rc.lb, rc.hb)) {
                    right_deleted = rc.delete(t_idx);
                }
                if (left_deleted || right_deleted) {
                    size -= 1;
                    return true;
                } else {
                    return false;
                }
            } else {
                tuples.clear();
                LinkedList<KdNode> queue = new LinkedList<>();
                queue.addFirst(lc);
                queue.addFirst(rc);
                while (!queue.isEmpty()) {
                    KdNode cur = queue.removeLast();
                    if (cur.nodeType == NodeType.NON_LEAF) {
                        queue.addFirst(cur.lc);
                        queue.addFirst(cur.rc);
                    } else {
                        tuples.addAll(cur.tuples);
                    }
                }

                boolean is_deleted = tuples.remove(Integer.valueOf(t_idx));
                if (is_deleted) {
                    size -= 1;
                }

                if (size <= 2 * capacity) {
                    nodeType = NodeType.LEAF;
                    lc = null;
                    rc = null;
                } else {
                    int split_coordinate = findSplitCoordinate(lb, hb);
                    tuples.sort(new CoordComp(split_coordinate));
                    double split_point = dualTree.data[tuples.get(size / 2)][split_coordinate];

                    List<Integer> listLeftTuples = new ArrayList<>(tuples.subList(0, size / 2));
                    List<Integer> listRightTuples = new ArrayList<>(tuples.subList(size / 2, size));

                    double[] leftLBound = new double[dim];
                    double[] leftHBound = new double[dim];
                    System.arraycopy(lb, 0, leftLBound, 0, dim);
                    System.arraycopy(hb, 0, leftHBound, 0, dim);
                    leftHBound[split_coordinate] = split_point;

                    double[] rightLBound = new double[dim];
                    double[] rightHBound = new double[dim];
                    System.arraycopy(lb, 0, rightLBound, 0, dim);
                    System.arraycopy(hb, 0, rightHBound, 0, dim);
                    rightLBound[split_coordinate] = split_point;

                    nodeType = NodeType.NON_LEAF;
                    lc = new KdNode(leftLBound, leftHBound, listLeftTuples);
                    rc = new KdNode(rightLBound, rightHBound, listRightTuples);

                    tuples.clear();
                }
                return is_deleted;
            }
        }

        private void print() {
            StringBuilder b = new StringBuilder();
            b.append(nodeType).append(" ");
            b.append(size).append(" ");
            for (int i = 0; i < dim; i++) {
                b.append("[").append(lb[i]).append(",").append(hb[i]).append("] ");
            }
            if (nodeType == NodeType.NON_LEAF) {
                b.append("\n");
            } else {
                b.append(" ");
                for (int t_idx : tuples) {
                    b.append(t_idx).append(",");
                }
                b.deleteCharAt(b.length() - 1).append("\n");
            }
            System.out.print(b.toString());
        }
    }

    private class CoordComp implements Comparator<Integer> {
        private final int coord;

        private CoordComp(int coord) {
            this.coord = coord;
        }

        @Override
        public int compare(Integer t1, Integer t2) {
            return Double.compare(dualTree.data[t1][coord], dualTree.data[t2][coord]);
        }
    }
}
