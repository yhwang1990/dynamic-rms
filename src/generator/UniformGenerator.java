package generator;

import utils.Constant;
import utils.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UniformGenerator {
    static Random rand = new Random(0);

    public static List<Tuple> uniformGenerator(int size) {
        List<Tuple> listTuples = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            double[] value = new double[Constant.DIM];
            for (int j = 0; j < Constant.DIM; j++) {
                value[j] = rand.nextDouble();
            }
            listTuples.add(new Tuple(i, value));
        }

        return listTuples;
    }
}
