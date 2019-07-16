package index;

import utils.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BallTree {

    private TreeNode root;

    public BallTree(List<Tuple> listTuples) {
        this.root = new TreeNode(listTuples);
    }

    public void BFSTraverse() {
        LinkedList<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        while(!queue.isEmpty()) {
            TreeNode cur = queue.getLast();
        }
    }

    class TreeNode {
        double[] center;
        double radius;
        NodeType nodeType;
        List<Tuple> listTuples;
        TreeNode lc,rc;

        TreeNode(List<Tuple> listTuples) {
            this.nodeType = NodeType.LEAF;
            this.listTuples = listTuples;
            this.lc = null;
            this.rc = null;

            this.center = new double[Constant.DIM];
            this.radius = 0.0;

            for (Tuple t : listTuples) {
                for (int i = 0; i < Constant.DIM; i++) {
                    this.center[i] += t.value[i];
                }
            }
            for (int i = 0; i < Constant.DIM; i++) {
                this.center[i] /= listTuples.size();
            }

            for (Tuple t : listTuples) {
                double dist = VectorUtil.l2_dist(this.center, t.value);
                this.radius = Math.max(dist, this.radius);
            }

            if (this.listTuples.size() > Constant.TAU) {
                double[][] splitPoint = findSplitPoint(listTuples);
                List<Tuple> listLeftTuples = new ArrayList<>();
                List<Tuple> listRightTuples = new ArrayList<>();
                for (Tuple t: listTuples) {
                    double l_dist = VectorUtil.l2_dist(t.value, splitPoint[0]);
                    double r_dist = VectorUtil.l2_dist(t.value, splitPoint[1]);
                    if (l_dist <= r_dist) {
                        listLeftTuples.add(t);
                    } else {
                        listRightTuples.add(t);
                    }
                }

                this.nodeType = NodeType.NON_LEAF;
                this.listTuples.clear();
                this.lc = new TreeNode(listLeftTuples);
                this.rc = new TreeNode(listRightTuples);
            }
        }

        private double[][] findSplitPoint(List<Tuple> tuples) {
            double[][] splitPoint = new double[2][Constant.DIM];

            double[] p0 = tuples.get(0).value;

            double maxDistA = 0.0;
            double[] maxTupleA = null;
            for (Tuple t : tuples) {
                double dist = VectorUtil.l2_dist(p0, t.value);
                if (dist > maxDistA) {
                    maxDistA = dist;
                    maxTupleA = t.value;
                }
            }

            double maxDistB = 0.0;
            double[] maxTupleB = null;
            for (Tuple t : tuples) {
                double dist = VectorUtil.l2_dist(maxTupleA, t.value);
                if (dist > maxDistB) {
                    maxDistB = dist;
                    maxTupleB = t.value;
                }
            }

            splitPoint[0] = maxTupleA;
            splitPoint[1] = maxTupleB;

            return splitPoint;
        }
    }
}
