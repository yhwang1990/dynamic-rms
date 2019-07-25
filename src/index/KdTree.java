package index;

import utils.*;

import java.util.*;

public class KdTree {

    private KdNode root;
    private int dim;
    private int capacity;

    KdTree(int dim, int capacity, double[] min_range, double[] max_range, DualTree dualTree) {
        this.dim = dim;
        this.capacity = capacity;

        List<Tuple> tuples = new ArrayList<>();
        for (int i = 0; i < dualTree.tuples.length; i++) {
            if (!dualTree.isDeleted[i]) {
                tuples.add(new Tuple(i, dualTree.tuples[i]));
            }
        }

        this.root = new KdNode(min_range, max_range, tuples);
    }

    void insert(int idx, double[] values) {
        root.insert(new Tuple(idx, values));
    }

    void delete(int idx, double[] values) {
        root.delete(new Tuple(idx, values));
    }

    TopKResult approxTopKSearch(int k, double eps, double[] u) {
        TopKResult result = new TopKResult(k, eps);

        KdNode best_node = root;
        while (best_node.nodeType != NodeType.LEAF) {
            if (VectorUtil.pointInRectangle(u, best_node.lc.lBound, best_node.lc.hBound)) {
                best_node = best_node.lc;
            } else {
                best_node = best_node.rc;
            }
        }

        for (Tuple t : best_node.listTuples) {
            double score = VectorUtil.inner_product(u, t.values);
            result.update(t.idx, score);
        }

        double min_dist = Double.MAX_VALUE;
        if (result.k_score > 0) {
            min_dist = VectorUtil.product2dist(dim - 1, (1 - eps) * result.k_score);
        }

        PriorityQueue<RandNode> queue = new PriorityQueue<>();
        queue.offer(new RandNode(root, 0.0));
        while (!queue.isEmpty() && queue.peek().dist2 <= min_dist) {
            KdNode cur_node = queue.poll().node;
            if (cur_node.nodeType == NodeType.LEAF) {
                if (cur_node == best_node)
                    continue;
                for (Tuple t : cur_node.listTuples) {
                    double score = VectorUtil.inner_product(u, t.values);
                    boolean k_score_update = result.update(t.idx, score);

                    if (k_score_update) {
                        min_dist = VectorUtil.product2dist(dim - 1, (1 - eps) * result.k_score);
                    }
                }
            } else {
                double l_dist2 = VectorUtil.dist2(u, cur_node.lc.lBound, cur_node.lc.hBound);
                if (l_dist2 <= min_dist) {
                    queue.offer(new RandNode(cur_node.lc, l_dist2));
                }
                double r_dist2 = VectorUtil.dist2(u, cur_node.rc.lBound, cur_node.rc.hBound);
                if (r_dist2 <= min_dist) {
                    queue.offer(new RandNode(cur_node.rc, r_dist2));
                }
            }
        }

        result.initResults();

        return result;
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
        double[] lBound;
        double[] hBound;
        int size;

        NodeType nodeType;
        List<Tuple> listTuples;
        KdNode lc, rc;

        KdNode(double[] lBound, double[] hBound, List<Tuple> listTuples) {
            this.nodeType = NodeType.LEAF;

            this.listTuples = listTuples;
            this.size = listTuples.size();

            this.lc = null;
            this.rc = null;

            this.lBound = lBound;
            this.hBound = hBound;

            if (this.size > 2 * capacity) {
                int split_coordinate = findSplitCoordinate(this.lBound, this.hBound);
                this.listTuples.sort(new TupleComparator(split_coordinate));
                double split_point = this.listTuples.get(this.size / 2).values[split_coordinate];

                List<Tuple> listLeftTuples = new ArrayList<>(this.listTuples.subList(0, this.size / 2));
                List<Tuple> listRightTuples = new ArrayList<>(this.listTuples.subList(this.size / 2, this.size));

                double[] leftLBound = new double[dim];
                double[] leftHBound = new double[dim];
                System.arraycopy(this.lBound, 0, leftLBound, 0, dim);
                System.arraycopy(this.hBound, 0, leftHBound, 0, dim);
                leftHBound[split_coordinate] = split_point;

                double[] rightLBound = new double[dim];
                double[] rightHBound = new double[dim];
                System.arraycopy(this.lBound, 0, rightLBound, 0, dim);
                System.arraycopy(this.hBound, 0, rightHBound, 0, dim);
                rightLBound[split_coordinate] = split_point;

                this.nodeType = NodeType.NON_LEAF;
                this.lc = new KdNode(leftLBound, leftHBound, listLeftTuples);
                this.rc = new KdNode(rightLBound, rightHBound, listRightTuples);

                this.listTuples.clear();
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

        private void insert(Tuple t) {
            size += 1;
            if (nodeType == NodeType.NON_LEAF) {
                if (VectorUtil.pointInRectangle(t.values, lc.lBound, lc.hBound)) {
                    lc.insert(t);
                } else {
                    rc.insert(t);
                }
            } else {
                listTuples.add(t);
                if (size > 2 * capacity) {
                    int split_coordinate = findSplitCoordinate(lBound, hBound);
                    listTuples.sort(new TupleComparator(split_coordinate));
                    double split_point = listTuples.get(size / 2).values[split_coordinate];

                    List<Tuple> listLeftTuples = new ArrayList<>(listTuples.subList(0, size / 2));
                    List<Tuple> listRightTuples = new ArrayList<>(listTuples.subList(size / 2, size));

                    double[] leftLBound = new double[dim];
                    double[] leftHBound = new double[dim];
                    System.arraycopy(lBound, 0, leftLBound, 0, dim);
                    System.arraycopy(hBound, 0, leftHBound, 0, dim);
                    leftHBound[split_coordinate] = split_point;

                    double[] rightLBound = new double[dim];
                    double[] rightHBound = new double[dim];
                    System.arraycopy(lBound, 0, rightLBound, 0, dim);
                    System.arraycopy(hBound, 0, rightHBound, 0, dim);
                    rightLBound[split_coordinate] = split_point;

                    nodeType = NodeType.NON_LEAF;
                    lc = new KdNode(leftLBound, leftHBound, listLeftTuples);
                    rc = new KdNode(rightLBound, rightHBound, listRightTuples);

                    listTuples.clear();
                }
            }
        }

        private void delete(Tuple t) {
            if (nodeType == NodeType.LEAF) {
                size -= 1;
                listTuples.remove(t);
            } else if (nodeType == NodeType.NON_LEAF && lc.size > capacity && rc.size > capacity) {
                size -= 1;
                if (VectorUtil.pointInRectangle(t.values, lc.lBound, lc.hBound)) {
                    lc.delete(t);
                }
                if (VectorUtil.pointInRectangle(t.values, rc.lBound, rc.hBound)) {
                    rc.delete(t);
                }
            } else {
                listTuples.clear();
                LinkedList<KdNode> queue = new LinkedList<>();
                queue.addFirst(lc);
                queue.addFirst(rc);
                while (!queue.isEmpty()) {
                    KdNode cur = queue.removeLast();
                    if (cur.nodeType == NodeType.NON_LEAF) {
                        queue.addFirst(cur.lc);
                        queue.addFirst(cur.rc);
                    } else {
                        listTuples.addAll(cur.listTuples);
                    }
                }

                listTuples.remove(t);
                size = listTuples.size();

                if (size <= 2 * capacity) {
                    nodeType = NodeType.LEAF;
                    lc = null;
                    rc = null;
                } else {
                    int split_coordinate = findSplitCoordinate(lBound, hBound);
                    listTuples.sort(new TupleComparator(split_coordinate));
                    double split_point = listTuples.get(size / 2).values[split_coordinate];

                    List<Tuple> listLeftTuples = new ArrayList<>(listTuples.subList(0, size / 2));
                    List<Tuple> listRightTuples = new ArrayList<>(listTuples.subList(size / 2, size));

                    double[] leftLBound = new double[dim];
                    double[] leftHBound = new double[dim];
                    System.arraycopy(lBound, 0, leftLBound, 0, dim);
                    System.arraycopy(hBound, 0, leftHBound, 0, dim);
                    leftHBound[split_coordinate] = split_point;

                    double[] rightLBound = new double[dim];
                    double[] rightHBound = new double[dim];
                    System.arraycopy(lBound, 0, rightLBound, 0, dim);
                    System.arraycopy(hBound, 0, rightHBound, 0, dim);
                    rightLBound[split_coordinate] = split_point;

                    nodeType = NodeType.NON_LEAF;
                    lc = new KdNode(leftLBound, leftHBound, listLeftTuples);
                    rc = new KdNode(rightLBound, rightHBound, listRightTuples);

                    listTuples.clear();
                }
            }
        }

        private void print() {
            StringBuilder b = new StringBuilder();
            b.append(nodeType).append(" ");
            b.append(size).append(" ");
            for (int i = 0; i < dim; i++) {
                b.append("[").append(lBound[i]).append(",").append(hBound[i]).append("] ");
            }
            if (nodeType == NodeType.NON_LEAF) {
                b.append("\n");
            } else {
                b.append(" ");
                for (Tuple t : listTuples) {
                    b.append(t.idx).append(",");
                }
                b.deleteCharAt(b.length() - 1).append("\n");
            }
            System.out.print(b.toString());
        }
    }

    private class Tuple {
        private int idx;
        private double[] values;

        private Tuple(int idx, double[] values) {
            this.idx = idx;
            this.values = values;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Tuple)) return false;
            Tuple tuple = (Tuple) o;
            return idx == tuple.idx;
        }

        @Override
        public int hashCode() {
            return Objects.hash(idx);
        }
    }

    private class TupleComparator implements Comparator<Tuple> {

        private int coordinate;

        private TupleComparator(int coordinate) {
            this.coordinate = coordinate;
        }

        @Override
        public int compare(Tuple t1, Tuple t2) {
            return Double.compare(t1.values[coordinate], t2.values[coordinate]);
        }
    }
}
