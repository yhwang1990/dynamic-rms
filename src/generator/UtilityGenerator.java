package generator;

import utils.Utility;
import utils.VectorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UtilityGenerator {
    private static Random rand = new Random(0);

    public static List<Utility> uniformGenerator(int dim, int size) {
        List<Utility> listUtilities = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            double[] value = new double[dim + 1];
            for (int j = 0; j < dim; j++) {
                value[j] = rand.nextDouble();
            }
            value[dim] = 0;
            VectorUtil.to_unit(value);
            listUtilities.add(new Utility(i, value));
        }

        return listUtilities;
    }
}
