package main;

import org.apache.commons.cli.*;

import structures.RMSInst;

import utils.TupleOpr;
import utils.VectorUtil;

import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.*;

public class RunRMS {

    public static void main(String[] args) {
        try {
            Options options = new Options();
            options.addOption(Option.builder("f")
                    .longOpt("dataset")
                    .hasArg(true)
                    .required(true)
                    .build());
            options.addOption(Option.builder("d")
                    .longOpt("dim")
                    .hasArg(true)
                    .required(true)
                    .build());
            options.addOption(Option.builder("k")
                    .longOpt("k")
                    .hasArg(true)
                    .required(true)
                    .build());
            options.addOption(Option.builder("r")
                    .longOpt("r")
                    .hasArg(true)
                    .required(true)
                    .build());
            options.addOption(Option.builder("s")
                    .longOpt("size")
                    .hasArg(true)
                    .required(false)
                    .build());

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd;
            try {
                cmd = parser.parse(options, args);

                String dataset = "";
                int k = 0, r = 0, size = 0, dim = 0;
                if (cmd.hasOption("f")) {
                    dataset = cmd.getOptionValue("f");
                    if ("Indep".equals(dataset) || "AntiCor".equals(dataset)) {
                        if (cmd.hasOption("s")) {
                            size = Integer.parseInt(cmd.getOptionValue("s"));
                        } else {
                            size = 100000;
                        }
                    }
                } else {
                    System.out.println("Please add option '-f' or '--dataset'");
                    System.exit(1);
                }

                if (cmd.hasOption("d")) {
                    dim = Integer.parseInt(cmd.getOptionValue("d"));
                } else {
                    System.out.println("Please add option '-d' or '--dim'");
                    System.exit(1);
                }

                if (cmd.hasOption("k")) {
                    k = Integer.parseInt(cmd.getOptionValue("k"));
                } else {
                    System.out.println("Please add option '-k' or '--k'");
                    System.exit(1);
                }

                if (cmd.hasOption("r")) {
                    r = Integer.parseInt(cmd.getOptionValue("r"));
                } else {
                    System.out.println("Please add option '-r' or '--r'");
                    System.exit(1);
                }

                runRMS(dataset, k, r, dim, size);
            } catch (ParseException pe) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("runRMS.jar", options);
                System.exit(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runRMS(String dataset, int k, int r, int dim, int size) throws IOException {
        String[][] params;

        if ("Indep".equals(dataset) || "AntiCor".equals(dataset)) {
            params = readParams("params-synthetic.csv", false);
        } else {
            params = readParams("params-real.csv", true);
        }

        int param_idx = searchParam(params, dataset, dim, k, r, size);
        if (param_idx < 0) {
            System.err.println("Parameter setting not found");
            System.exit(1);
        }

        double eps = Double.parseDouble(params[param_idx][params[param_idx].length - 2]);
        int pow = Integer.parseInt(params[param_idx][params[param_idx].length - 1]);

        String[] paths = parseFilePath(dataset, dim, k, r, size);

        BufferedWriter wr_result, wr_time;
        wr_result = new BufferedWriter(new FileWriter(paths[1], true));
        wr_time = new BufferedWriter(new FileWriter(paths[2], true));

        double[][] data = readDataFile(paths[0]);

        int[] toBeDeleted = readWorkload(paths[0]);

        int data_size = data.length;
        int init_size = data_size - toBeDeleted.length;

        int maxM = dim + (1 << pow) + 1;
        double[][] samples = readUtilFile(dim, maxM);

        List<TupleOpr> workLoad = new ArrayList<>();
        for (int idx = init_size; idx < data_size; idx++)
            workLoad.add(new TupleOpr(idx, 1));
        for (int idx : toBeDeleted)
            workLoad.add(new TupleOpr(idx, -1));

        System.out.println(r + " " + maxM + " " + eps);

        RMSInst inst = new RMSInst(dim, k, r, eps, data_size, init_size, maxM, data, samples);

        writeHeader(wr_result, paths[0], k, r, eps, maxM);
        writeHeader(wr_time, paths[0], k, r, eps, maxM);
        wr_time.write("init_time=" + Math.round(inst.initTime) + " inserts="
                + (workLoad.size() - toBeDeleted.length) + " deletes=" + toBeDeleted.length + "\n");
        int interval = workLoad.size() / 10;
        int output_id = 0;
        for (int opr_id = 0; opr_id < workLoad.size(); opr_id++) {
            inst.update(workLoad.get(opr_id));
            if (opr_id % interval == interval - 1) {
                wr_time.write("idx=" + output_id + " ");
                wr_time.write(Math.round(inst.addTreeTime) + " ");
                wr_time.write(Math.round(inst.addCovTime) + " ");
                wr_time.write(Math.round(inst.delTreeTime) + " ");
                wr_time.write(Math.round(inst.delCovTime) + "\n");

                writeResult(wr_result, output_id, inst, data);
                output_id += 1;
                wr_result.flush();
                wr_time.flush();
            }
        }

        System.gc();
        wr_result.close();
        wr_time.close();
    }

    private static String[][] readParams(String paramPath, boolean isRealData) throws IOException {
        Path path = Paths.get(paramPath);
        int size = (int) Files.lines(path).count() - 1;

        String[][] params;
        if (isRealData) {
            params = new String[size][6];
        } else {
            params = new String[size][7];
        }

        BufferedReader br = new BufferedReader(new FileReader(paramPath));
        br.readLine();

        String line;
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            String[] tokens = line.split(",");
            for (int i = 0; i < tokens.length; ++i) {
                params[cnt][i] = tokens[i].trim();
            }
            ++cnt;
        }
        br.close();

        return params;
    }

    private static int searchParam(String[][] params, String dataset, int dim, int k, int r, int size) {
        for (int i = 0; i < params.length; ++i) {
            if (size == 0 && params[i][0].equals(dataset) && Integer.parseInt(params[i][1]) == dim
                    && Integer.parseInt(params[i][2]) == k && Integer.parseInt(params[i][3]) == r)
                return i;
            else if (size > 0 && params[i][0].equals(dataset) && Integer.parseInt(params[i][1]) == dim
                    && Integer.parseInt(params[i][2]) == size && Integer.parseInt(params[i][3]) == k
                    && Integer.parseInt(params[i][4]) == r)
                return i;
        }
        return -1;
    }

    private static String[] parseFilePath(String dataset, int dim, int k, int r, int size) {
        String[] paths = new String[3];

        switch (dataset) {
            case "BB":
                paths[0] = "./dataset/NBA/NBA.txt";
                paths[1] = "./result/NBA_tuples_fdrms_k" + k + "_r" + r + ".txt";
                paths[2] = "./result/NBA_time_fdrms_k" + k + "_r" + r + ".txt";
                break;
            case "AQ":
                paths[0] = "./dataset/AirQuality/AirQuality.txt";
                paths[1] = "./result/AQ_tuples_fdrms_k" + k + "_r" + r + ".txt";
                paths[2] = "./result/AQ_time_fdrms_k" + k + "_r" + r + ".txt";
                break;
            case "CT":
                paths[0] = "./dataset/CovType/CovType.txt";
                paths[1] = "./result/CT_tuples_fdrms_k" + k + "_r" + r + ".txt";
                paths[2] = "./result/CT_time_fdrms_k" + k + "_r" + r + ".txt";
                break;
            case "Movie":
                paths[0] = "./dataset/MovieLens/MovieLens.txt";
                paths[1] = "./result/Movie_tuples_fdrms_k" + k + "_r" + r + ".txt";
                paths[2] = "./result/Movie_time_fdrms_k" + k + "_r" + r + ".txt";
                break;
            case "Indep":
                if (size == 100000) {
                    paths[0] = "./dataset/Indep/Indep_" + dim + "d.txt";
                    paths[1] = "./result/Indep_" + dim + "d_tuples_fdrms_k" + k + "_r" + r + ".txt";
                    paths[2] = "./result/Indep_" + dim + "d_time_fdrms_k" + k + "_r" + r + ".txt";
                } else {
                    paths[0] = "./dataset/Indep/Indep1M_" + size + "_" + dim + "d.txt";
                    paths[1] = "./result/Indep_" + size + "_" + dim + "d_tuples_fdrms_k" + k + "_r" + r + ".txt";
                    paths[2] = "./result/Indep_" + size + "_" + dim + "d_time_fdrms_k" + k + "_r" + r + ".txt";
                }
                break;
            case "AntiCor":
                if (size == 100000) {
                    paths[0] = "./dataset/AntiCorr/AntiCorr_" + dim + "d.txt";
                    paths[1] = "./result/AC_" + dim + "d_tuples_fdrms_k" + k + "_r" + r + ".txt";
                    paths[2] = "./result/AC_" + dim + "d_time_fdrms_k" + k + "_r" + r + ".txt";
                } else {
                    paths[0] = "./dataset/AntiCorr/AntiCorr1M_" + size + "_" + dim + "d.txt";
                    paths[1] = "./result/AC_" + size + "_" + dim + "d_tuples_fdrms_k" + k + "_r" + r + ".txt";
                    paths[2] = "./result/AC_" + size + "_" + dim + "d_time_fdrms_k" + k + "_r" + r + ".txt";
                }
                break;
            default:
                System.err.println("Path not found");
                System.exit(1);
        }

        return paths;
    }

    private static double[][] readDataFile(String filePath) throws IOException {
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
    }

    private static double[][] readUtilFile(int dim, int sample_size) throws IOException {
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
    }

    private static int[] readWorkload(String filePath) throws IOException {
        String wlPath = filePath.substring(0, filePath.length() - 4).split("_")[0] + "_wl.txt";
        if (filePath.contains("1M"))
            wlPath = filePath.substring(0, filePath.length() - 7) + "_wl.txt";

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
    }

    private static void writeResult(BufferedWriter wr, int idx, RMSInst inst, double[][] data) throws IOException {
        DecimalFormat df = new DecimalFormat("0.000000");
        wr.write("index " + idx + " " + inst.result().size() + "\n");
        for (int t_idx : inst.result()) {
            for (int d = 0; d < data[t_idx].length - 1; d++)
                wr.write(df.format(data[t_idx][d]) + " ");
            wr.write("\n");
        }
    }

    private static void writeHeader(BufferedWriter wr, String filePath, int k, int r, double eps, int m)
            throws IOException {
        wr.write("dataset " + filePath + " ");
        wr.write("k=" + k + " ");
        wr.write("r=" + r + " ");
        wr.write("eps=" + eps + " ");
        wr.write("m=" + m + "\n");
    }
}
