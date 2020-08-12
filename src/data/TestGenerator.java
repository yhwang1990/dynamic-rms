package data;

import java.io.*;
import java.text.*;

public class TestGenerator {

    public static void main(String[] args) {
        for (int d = 2; d <= 3; d++) {
            double[][] samples = UtilityGenerator.gaussianGenerator(d, 1_000_000);
            writeToFile(d, samples);
        }
    	// double[][] samples = UtilityGenerator.gaussianGenerator(12, 1_000_000);
        // writeToFile(12, samples);
    }

    private static void writeToFile(int dim, double[][] samples) {
        DecimalFormat df = new DecimalFormat("0.000000");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("test_" + dim + "d.txt"));
            for (double[] sample : samples) {
                StringBuilder sb = new StringBuilder();
                for (int d = 0; d < dim; d++)
                    sb.append(df.format(sample[d])).append(" ");
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
