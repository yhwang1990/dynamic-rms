package generator;

import utils.Tuple;
import utils.VectorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TupleGenerator {
    private static Random rand = new Random(0);

    public static List<Tuple> uniformGenerator(int dim, int size) {
        List<Tuple> listTuples = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            double[] value = new double[dim + 1];
            for (int j = 0; j < dim; j++) {
                value[j] = Math.round(rand.nextDouble() * 10.0) / 10.0;
            }
            value[dim] = Math.sqrt(dim - VectorUtil.norm2(value));
            listTuples.add(new Tuple(i, value));
        }

        return listTuples;
    }
}
