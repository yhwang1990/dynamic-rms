package data;

import generators.TupleGenerator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class DataGenerator {

    public static void main(String[] args) {
        for (int dim = 3; dim <= 10; dim++) {
            double[][] data = generateAntiCorr(100_000, dim);
            writeToFile("./dataset/AntiCorr_" + dim + "d.txt", data, dim);
        }
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

    public static double[][] generateIndep(int size, int dim) {
        double[][] data = new double[size][dim];
        for (int i = 0; i < size; i++) {
            TupleGenerator.generateIndep(dim, data[i]);
        }
        return data;
    }

    public static double[][] generateCorr(int size, int dim) {
        double[][] data = new double[size][dim];
        for (int i = 0; i < size; i++) {
            TupleGenerator.generateCorr(dim, data[i]);
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
