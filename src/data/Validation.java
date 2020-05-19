package data;

import structures.KdTree;
import structures.RankItem;
import utils.VectorUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

public class Validation {
    public static void main(String[] args) {
        runValidate("./dataset/NBA.txt");
    }

    private static void runValidate(String filePath) {
        DecimalFormat df = new DecimalFormat("0.000000");
        for (int i = 0; i < 10; i++) {
            String datasetName = filePath.substring(0, filePath.length() - 4);
            String dataPath = datasetName + "_" + i + ".txt";

            double[][] data = readDataFile(dataPath);
            assert data != null;
            int dim = data[0].length - 1;
            double[][] tests = readTestFile(dim);

            KdTree index = new KdTree(dim + 1, 10, data);
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(datasetName + "_rst_" + i + ".txt"));
                assert tests != null;
                for (double[] test : tests) {
                    List<RankItem> result = index.exactTopK(10, test);
                    Collections.sort(result);

                    StringBuilder sb = new StringBuilder();
                    for (int r = result.size() - 1; r >= 0; r--) {
                        sb.append(result.get(r).idx).append(":").append(df.format(result.get(r).score)).append(" ");
                    }
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

    private static double[][] readDataFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            int size = (int) Files.lines(path).count() - 1;

            BufferedReader br = new BufferedReader(new FileReader(filePath));

            int dim = Integer.parseInt(br.readLine());
            double[][] data = new double[size][dim + 1];

            String line;
            int idx = 0;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(" ");
                for (int d = 0; d < dim; d++)
                    data[idx][d] = Double.parseDouble(tokens[d].trim());
                idx++;
            }
            br.close();

            for (double[] tuple : data)
                tuple[dim] = Math.sqrt(dim - VectorUtil.norm2(tuple));

            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static double[][] readTestFile(int dim) {
        try {
            String filePath = "./test/test_" + dim + "d.txt";
            BufferedReader br = new BufferedReader(new FileReader(filePath));

            double[][] data = new double[1000000][dim + 1];

            String line;
            int idx = 0;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(" ");
                for (int d = 0; d < dim; d++)
                    data[idx][d] = Double.parseDouble(tokens[d].trim());
                idx++;
            }
            br.close();

            for (double[] util : data)
                util[dim] = 0.0;

            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
