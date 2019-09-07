package structures;

public class DualTree {

    public KdTree tIdx;
    public ConeTree uIdx;

    int data_size, init_size, sample_size;

    private int leaf_size;
    private double tau;

    public DualTree(int dim, int k, double eps, int data_size, int init_size, int sample_size) {
        this.leaf_size = Math.max(5, 2 * k);
        this.tau = 0.99;

        this.data_size = data_size;
        this.init_size = init_size;
        this.sample_size = sample_size;

        this.tIdx = new KdTree(dim + 1, this.leaf_size, this);
        this.uIdx = new ConeTree(dim + 1, k, eps, this.tau, this);
    }

    public Operations insert(int t_idx) {
        Operations opr = new Operations(t_idx, 1);
        boolean is_inserted = tIdx.insert(t_idx);
        if (!is_inserted) {
            System.err.println("Insert " + t_idx + " failed");
        }
        uIdx.insert(t_idx, opr);

        return opr;
    }

    public Operations delete(int t_idx) {
        Operations opr = new Operations(t_idx, -1);
        boolean is_deleted = tIdx.delete(t_idx);
        if (!is_deleted) {
            System.err.println("Delete " + t_idx + " failed");
        }
        uIdx.delete(t_idx, opr);

        return opr;
    }
}
