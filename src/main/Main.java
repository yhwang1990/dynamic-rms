package main;

import structures.MinErrorRMS;
import structures.MinSizeRMS;
import utils.TupleOpr;
import utils.VectorUtil;

import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.*;

public class Main {

    public static double cTime = 0.0, itTime = 0.0, isTime = 0.0, dtTime = 0.0, dsTime = 0.0;

    public static void main(String[] args) {
        if (args.length < 2 || args.length > 4)
            System.err.println("error in args");
        else if (args.length == 2)
            runTest(args[0], args[1]);
        else if (args.length == 3)
            runMinSizeRMS(args);
        else
            runMinErrorRMS(args);
    }

    private static void runMinSizeRMS(String[] args) {
        String filePath = args[0];
        int k = Integer.parseInt(args[1]);
        double eps = Double.parseDouble(args[2]);

        String resultFile = "results/min_size_tuples_001.txt";
        String timeFile = "results/min_size_time_001.txt";

        BufferedWriter wr_result = null, wr_time = null;
        try {
            wr_result = new BufferedWriter(new FileWriter(resultFile));
            wr_time = new BufferedWriter(new FileWriter(timeFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        double[][] data = readDataFile(filePath);
        if (data == null) {
            System.err.println("error in reading data file");
            System.exit(0);
        }
        int data_size = data.length, dim = data[0].length - 1;
        int init_size = data_size / 2;

        int[] toBeDeleted = readWorkload(filePath);
        if (toBeDeleted == null) {
            System.err.println("error in reading workload file");
            System.exit(0);
        }

        int sample_size = decideSampleSize(dim);

        double[][] samples = readUtilFile(dim, sample_size);

        if (samples == null) {
            System.err.println("error in reading sample file");
            System.exit(0);
        }

        List<TupleOpr> workLoad = new ArrayList<>();
        for (int idx = init_size; idx < data_size; idx++)
            workLoad.add(new TupleOpr(idx, 1));
        for (int idx : toBeDeleted)
            workLoad.add(new TupleOpr(idx, -1));

        MinSizeRMS inst = new MinSizeRMS(dim, k, eps, data_size, init_size, sample_size, data, samples);

        writeHeader(wr_result, args);
        writeHeader(wr_time, args);

        try {
            wr_time.write("initTime=" + Math.round(cTime) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        int interval = workLoad.size() / 10;
        int output_id = 0;
        for (int opr_id = 0; opr_id < workLoad.size(); opr_id++) {
            inst.update(workLoad.get(opr_id));
            if (opr_id % interval == interval - 1) {
                writeTime(wr_time, output_id);
                writeResult(wr_result, output_id, inst);
                output_id += 1;
            }
        }

        try {
            wr_result.flush();
            wr_time.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        resetTime();

        try {
            wr_result.close();
            wr_time.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runMinErrorRMS(String[] args) {
        String filePath = args[0];
        int k = Integer.parseInt(args[1]);
        int r = Integer.parseInt(args[2]);
        double eps = Double.parseDouble(args[3]);

        String resultFile = "result/min_error_tuples_01.txt";
        String timeFile = "result/min_error_time_01.txt";

        BufferedWriter wr_result = null, wr_time = null;
        try {
            wr_result = new BufferedWriter(new FileWriter(resultFile));
            wr_time = new BufferedWriter(new FileWriter(timeFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        double[][] data = readDataFile(filePath);
        if (data == null) {
            System.err.println("error in reading data file");
            System.exit(0);
        }
        int data_size = data.length, dim = data[0].length - 1;
        int init_size = data_size / 2;

        int[] toBeDeleted = readWorkload(filePath);
        if (toBeDeleted == null) {
            System.err.println("error in reading workload file");
            System.exit(0);
        }

        int sample_size = decideSampleSize(dim);

        double[][] samples = readUtilFile(dim, sample_size);

        if (samples == null) {
            System.err.println("error in reading sample file");
            System.exit(0);
        }

        List<TupleOpr> workLoad = new ArrayList<>();
        for (int idx = init_size; idx < data_size; idx++)
            workLoad.add(new TupleOpr(idx, 1));
        for (int idx : toBeDeleted)
            workLoad.add(new TupleOpr(idx, -1));

        MinErrorRMS inst = new MinErrorRMS(dim, k, r, eps, data_size, init_size, sample_size, data, samples);

        writeHeader(wr_result, args);
        writeHeader(wr_time, args);

        try {
            wr_time.write("initTime=" + Math.round(cTime) + "\n");
            wr_time.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int interval = workLoad.size() / 10;
        int output_id = 0;
        for (int opr_id = 0; opr_id < workLoad.size(); opr_id++) {
            inst.update(workLoad.get(opr_id));
            if (opr_id % interval == interval - 1) {
                writeTime(wr_time, output_id);
                writeResult(wr_result, output_id, inst);
                output_id += 1;

                try {
                    wr_result.flush();
                    wr_time.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            wr_result.close();
            wr_time.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runTest(String dataFile, String resultFile) {
        DecimalFormat df = new DecimalFormat("0.000000");
        double[][] data = readDataFile(dataFile);
        assert data != null;
        double[][] tests = readTestFile(data[0].length - 1);
        try {
            BufferedReader br = new BufferedReader(new FileReader(resultFile));
            BufferedWriter bw = new BufferedWriter(new FileWriter("./result/min_size_results_00.txt"));
            String line;
            int k = 0;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("dataset")) {
                    bw.write(line);
                    bw.write("\n");

                    String[] tokens = line.split(" ");
                    for (String token : tokens) {
                        String paramName = token.split("=")[0];
                        String paramValue = token.split("=")[1];
                        if ("k".equals(paramName)) {
                            k = Integer.parseInt(paramValue);
                        }
                    }
                    for (int i = 0; i < 10; i++) {
                        line = br.readLine();
                        tokens = line.split(" ");
                        List<Integer> results = new ArrayList<>();
                        for (String token : tokens)
                            results.add(Integer.valueOf(token));

                        double[] topK = new double[1_000_000];
                        double[] top1 = new double[1_000_000];
                        double[] top5 = new double[1_000_000];
                        double[] top10 = new double[1_000_000];

                        readValidFile(dataFile.substring(0, dataFile.length() - 4) + "_rst_" + i + ".txt", k, topK, top1, top5, top10);

                        double[] rmsTop1 = new double[1_000_000];
                        assert tests != null;
                        computeRMSTop1(data, results, tests, rmsTop1);

                        double mrr = computeMrr(rmsTop1, topK);
                        double acc1 = computeAcc(rmsTop1, top1);
                        double acc5 = computeAcc(rmsTop1, top5);
                        double acc10 = computeAcc(rmsTop1, top10);

                        bw.write("mrr=" + df.format(mrr) + " ");
                        bw.write("acc@1=" + df.format(acc1) + " ");
                        bw.write("acc@5=" + df.format(acc5) + " ");
                        bw.write("acc@10=" + df.format(acc10) + "\n");
                    }
                }
                bw.flush();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
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

    private static double[][] readUtilFile(int dim, int sample_size) {
        try {
            String filePath = "./utility/utils_" + dim + "d.txt";
            BufferedReader br = new BufferedReader(new FileReader(filePath));

            double[][] data = new double[sample_size][dim + 1];

            String line;
            int idx = 0;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(" ");
                for (int d = 0; d < dim; d++)
                    data[idx][d] = Double.parseDouble(tokens[d].trim());
                idx++;
                if (idx == sample_size)
                    break;
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

    private static int[] readWorkload(String filePath) {
        try {
            String wlPath = filePath.substring(0, filePath.length() - 4).split("_")[0] + "_wl.txt";
            Path path = Paths.get(wlPath);
            int size = (int) Files.lines(path).count();

            BufferedReader br = new BufferedReader(new FileReader(wlPath));
            int[] workLoad = new int[size];

            String line;
            int idx = 0;
            while ((line = br.readLine()) != null)
                workLoad[idx++] = Integer.parseInt(line.trim());
            br.close();

            return workLoad;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void readValidFile(String filePath, int k, double[] topK, double[] top1, double[] top5, double[] top10) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            int idx = 0;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(" ");
                topK[idx] = Double.parseDouble(tokens[k - 1].split(":")[1]);
                top1[idx] = Double.parseDouble(tokens[0].split(":")[1]);
                top5[idx] = Double.parseDouble(tokens[4].split(":")[1]);
                top10[idx] = Double.parseDouble(tokens[9].split(":")[1]);

                idx++;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void computeRMSTop1(double[][] data, List<Integer> results, double[][] tests, double[] rmsTop1) {
        for (int i = 0; i < tests.length; i++) {
            double score = 0.0;
            for (int t_idx : results)
                score = Math.max(score, VectorUtil.inner_product(data[t_idx], tests[i]));
            rmsTop1[i] = score;
        }
    }

    private static double computeMrr(double[] rmsTop1, double[] topK) {
        double rr = 0.0;
        for (int i = 0; i < rmsTop1.length; i++) {
            if (rmsTop1[i] < topK[i]) {
                double rr_i = 1.0 - rmsTop1[i] / topK[i];
                if (rr_i > 0.01)
                    System.out.println(i + "," + rmsTop1[i] + "," + topK[i]);
                rr = Math.max(rr, rr_i);
            }
        }
        return rr;
    }

    private static double computeAcc(double[] rmsTop1, double[] topK) {
        int t_value = 0;
        for (int i = 0; i < rmsTop1.length; i++) {
            if (rmsTop1[i] >= topK[i])
                t_value += 1;
        }
        return (t_value + 0.0) / (rmsTop1.length + 0.0);
    }

    private static int decideSampleSize(int dim) {
        int size;
        if (dim <= 3)
            size = 100000;
        else if (dim <= 4)
            size = 200000;
        else if (dim <= 6)
            size = 300000;
        else if (dim <= 8)
            size = 400000;
        else
            size = 500000;
        return size;
    }

    private static void resetTime() {
        cTime = 0.0;
        itTime = 0.0;
        isTime = 0.0;
        dtTime = 0.0;
        dsTime = 0.0;
    }

    private static void writeResult(BufferedWriter wr, int idx, MinSizeRMS inst) {
        try {
            wr.write("idx=" + idx + " ");
            for (int t_idx : inst.result())
                wr.write(t_idx + " ");
            wr.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeResult(BufferedWriter wr, int idx, MinErrorRMS inst) {
        try {
            wr.write("idx=" + idx + " ");
            for (int t_idx : inst.result())
                wr.write(t_idx + " ");
            wr.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeTime(BufferedWriter wr, int idx) {
        try {
            wr.write("idx=" + idx + " ");
            wr.write(Math.round(itTime) + " ");
            wr.write(Math.round(isTime) + " ");
            wr.write(Math.round(dtTime) + " ");
            wr.write(Math.round(dsTime) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeHeader(BufferedWriter wr, String[] args) {
        try {
            if (args.length == 3) {
                wr.write("dataset=" + args[0] + " ");
                wr.write("k=" + args[1] + " ");
                wr.write("eps=" + args[2] + "\n");
            } else {
                wr.write("dataset=" + args[0] + " ");
                wr.write("k=" + args[1] + " ");
                wr.write("r=" + args[2] + " ");
                wr.write("eps=" + args[3] + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
