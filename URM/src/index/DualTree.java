package index;

public class DualTree {

    public KdTree tIdx;
    public ConeTree uIdx;

    int data_size, init_size, sample_size;
    
    public boolean[] isDeleted;
    public double[][] data;
    
    public double[][] samples;
    public RankItem[] topResults;

    public DualTree(int dim, int data_size, int init_size, int sample_size, double[][] data, double[][] samples) {
        double tau = 0.99;
        int leaf_size = 5;

        this.data = data;
        this.samples = samples;

        this.data_size = data_size;
        this.init_size = init_size;
        this.sample_size = sample_size;
        
        this.isDeleted = new boolean[data_size];
        for (int i = 0; i < init_size; i++) {
            this.isDeleted[i] = false;
        }
        for (int i = init_size; i < data_size; i++) {
            this.isDeleted[i] = true;
        }
        
        this.topResults = new RankItem[sample_size];

        this.tIdx = new KdTree(dim + 1, leaf_size, this);
        this.uIdx = new ConeTree(dim + 1, tau, this);
    }

    public void insert(int t_idx) {
        boolean is_inserted = tIdx.insert(t_idx);
        if (!is_inserted) {
            System.err.println("Insert " + t_idx + " failed");
        }
        uIdx.insert(t_idx);
    }

    public void delete(int t_idx) {
        boolean is_deleted = tIdx.delete(t_idx);
        if (!is_deleted) {
            System.err.println("Delete " + t_idx + " failed");
        }
        uIdx.delete(t_idx);
    }
}
