import generators.TupleGenerator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class DataGenerator {

    public static void main(String[] args) {
        double[][] data = generateIndep10D(1_000_000);
        for (int dim = 3; dim <= 10; dim++) {
            writeToFile("indep_" + dim + "d.txt", data, dim);
        }
    }

    private static void writeToFile(String filename, double[][] data, int dim) {
        DecimalFormat df = new DecimalFormat("0.000000");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
            bw.write(Integer.toString(dim));
            bw.write("\n");
            for (double[] datum : data) {
                StringBuilder sb = new StringBuilder();
                for (int d = 0; d < dim; d++)
                    sb.append(df.format(datum[d])).append(" ");
                sb.deleteCharAt(sb.length() - 1).append("\n");
                bw.write(sb.toString());
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double[][] generateIndep10D(int size) {
        double[][] data = new double[size][10];
        for (int i = 0; i < size; i++) {
            TupleGenerator.generateIndep(10, data[i]);
        }
        return data;
    }

    private static double[][] generateCorr10D(int size) {
        double[][] data = new double[size][10];
        for (int i = 0; i < size; i++) {
            TupleGenerator.generateCorr(10, data[i]);
        }
        return data;
    }

    private static double[][] generateAnti10D(int size) {
        double[][] data = new double[size][10];
        for (int i = 0; i < size; i++) {
            TupleGenerator.generateAnti(10, data[i]);
        }
        return data;
    }
}
