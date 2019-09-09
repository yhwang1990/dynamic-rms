package data;

import generators.UtilityGenerator;

import java.io.*;
import java.text.DecimalFormat;

public class TestGenerator {

    public static void main(String[] args) {
        for (int d = 3; d <= 10; d++) {
            double[][] data = UtilityGenerator.uniformGenerator(d, 1_000_000);
            writeToFile(d, data);
        }
    }

    private static void writeToFile(int dim, double[][] data) {
        DecimalFormat df = new DecimalFormat("0.000000");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("test_" + dim + "d.txt"));
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
}
