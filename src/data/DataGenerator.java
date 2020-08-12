package data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class DataGenerator {

    public static void main(String[] args) {
        double[][] data = generateUniform(100_000, 2);
        writeToFile("./dataset/Indep_" + 2 + "d.txt", data, 2);
    }

    public static void writeToFile(String filename, double[][] data, int dim) {
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

    public static double[][] generateUniform(int size, int dim) {
        double[][] data = new double[size][dim];
        for (int i = 0; i < size; i++) {
            TupleGenerator.generateUniform(dim, data[i]);
        }
        return data;
    }

    public static double[][] generateAntiCorr(int size, int dim) {
        double[][] data = new double[size][dim];
        for (int i = 0; i < size; i++) {
            TupleGenerator.generateAnti(dim, data[i]);
        }
        return data;
    }
}
