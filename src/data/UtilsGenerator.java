package data;

import generators.UtilityGenerator;

import java.io.*;
import java.text.*;
import java.util.ArrayList;

public class UtilsGenerator {

    public static void main(String[] args) {
        for (int d = 4; d <= 10; d++) {
        	ArrayList<double[]> samples = UtilityGenerator.balancedGenerator(d, 1_100_000);
            writeToFile(d, samples);
        }
    	ArrayList<double[]> samples = UtilityGenerator.balancedGenerator(12, 1_100_000);
        writeToFile(12, samples);
    }

    private static void writeToFile(int dim, ArrayList<double[]> samples) {
        DecimalFormat df = new DecimalFormat("0.000000");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("utils_" + dim + "d.txt"));
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
