package index;

import utils.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class KdTree {

    private KdNode root;
    private int dim;
    private int capacity;

    public KdTree(int dim, int capacity, double[] min_range, double[] max_range) {
        this.dim = dim;
        this.capacity = capacity;
        this.root = new KdNode(min_range, max_range, new ArrayList<>());
    }

    public KdTree(int dim, int capacity, double[] min_range, double[] max_range, List<Tuple> data) {
        this.dim = dim;
        this.capacity = capacity;
        this.root = new KdNode(min_range, max_range, new ArrayList<>(data));
    }

    public void insert(Tuple t) {
        System.out.println("Insert " + t.idx);
        root.insert(t);
    }

    public void delete(Tuple t) {
        System.out.println("Delete " + t.idx);
        root.delete(t);
    }

    public TopKResult top_k(Utility u) {
        TopKResult result = new TopKResult();
        return result;
    }

    public TopKResult approx_top_k(Utility u, double eps) {
        return null;
    }

    public void BFSTraverse() {
        LinkedList<KdNode> queue = new LinkedList<>();
        queue.add(root);
        while(!queue.isEmpty()) {
            KdNode cur = queue.removeLast();
            cur.print();
            if(cur.nodeType == NodeType.NON_LEAF) {
                queue.addFirst(cur.lc);
                queue.addFirst(cur.rc);
            }
        }
    }

    class KdNode {
        double[] lBound;
        double[] hBound;
        int size;

        NodeType nodeType;
        List<Tuple> listTuples;
        KdNode lc,rc;

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
                this.listTuples.sort(new CoordinateComparator(split_coordinate));
                double split_point = this.listTuples.get(this.size / 2).value[split_coordinate];

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
                if (VectorUtil.pointInRectangle(t.value, lc.lBound, lc.hBound)) {
                    lc.insert(t);
                } else {
                    rc.insert(t);
                }
            } else {
                listTuples.add(t);
                if (size > 2 * capacity) {
                    int split_coordinate = findSplitCoordinate(lBound, hBound);
                    listTuples.sort(new CoordinateComparator(split_coordinate));
                    double split_point = listTuples.get(size / 2).value[split_coordinate];

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
                if (VectorUtil.pointInRectangle(t.value, lc.lBound, lc.hBound)) {
                    lc.delete(t);
                }
                if (VectorUtil.pointInRectangle(t.value, rc.lBound, rc.hBound)) {
                    rc.delete(t);
                }
            } else {
                listTuples.clear();
                LinkedList<KdNode> queue = new LinkedList<>();
                queue.addFirst(lc);
                queue.addFirst(rc);
                while(!queue.isEmpty()) {
                    KdNode cur = queue.removeLast();
                    if(cur.nodeType == NodeType.NON_LEAF) {
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
                    listTuples.sort(new CoordinateComparator(split_coordinate));
                    double split_point = listTuples.get(size / 2).value[split_coordinate];

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
            if(nodeType == NodeType.NON_LEAF){
                b.append("\n");
            } else {
                b.append(" ");
                for(Tuple t : listTuples) {
                    b.append(t.idx).append(",");
                }
                b.deleteCharAt(b.length() - 1).append("\n");
            }

            System.out.print(b.toString());
        }
    }
}
