package main;

import org.apache.commons.cli.*;

import structures.RMSInst;

import utils.TupleOpr;
import utils.VectorUtil;

import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.*;

public class RunRMSInst {

    public static void main(String[] args) {
        try {
            Options options = new Options();
            options.addOption(Option.builder("i")
                    .longOpt("input")
                    .hasArg(true)
                    .required(true)
                    .build());
            options.addOption(Option.builder("o")
                    .longOpt("output")
                    .hasArg(true)
                    .required(true)
                    .build());
            options.addOption(Option.builder("d")
                    .longOpt("dim")
                    .hasArg(true)
                    .required(true)
                    .build());
            options.addOption(Option.builder("e")
                    .longOpt("eps")
                    .hasArg(true)
                    .required(true)
                    .build());
            options.addOption(Option.builder("m")
                    .longOpt("maxM")
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

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd;
            try {
                cmd = parser.parse(options, args);

                String dataFile = "", resultPrefix = "";
                int k = 0, r = 0, dim = 0, pow = 0;
                double eps = 0.0;
                
                if (cmd.hasOption("i")) {
                	dataFile = cmd.getOptionValue("i");
                } else {
                    System.out.println("Please add option '-i' or '--input'");
                    System.exit(1);
                }
                
                if (cmd.hasOption("o")) {
                	resultPrefix = cmd.getOptionValue("o");
                } else {
                    System.out.println("Please add option '-o' or '--output'");
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
                
                if (cmd.hasOption("e")) {
                    eps = Double.parseDouble(cmd.getOptionValue("e"));
                } else {
                    System.out.println("Please add option '-e' or '--eps'");
                    System.exit(1);
                }
                
                if (cmd.hasOption("m")) {
                    pow = Integer.parseInt(cmd.getOptionValue("m"));
                } else {
                    System.out.println("Please add option '-m' or '--maxM'");
                    System.exit(1);
                }

                runRMS(dataFile, resultPrefix, k, r, dim, eps, pow);
            } catch (ParseException pe) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("runRMS.jar", options);
                System.exit(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runRMS(String dataFile, String resultPrefix, int k, int r, int dim, double eps, int pow) throws IOException {
    	
    	String resultPath = resultPrefix + "_result.txt";
    	String tuplePath = resultPrefix + "_tuple.txt";

        BufferedWriter wr_result, wr_time;
        wr_result = new BufferedWriter(new FileWriter(resultPath, true));
        wr_time = new BufferedWriter(new FileWriter(tuplePath, true));

        double[][] data = readDataFile(dataFile);

        int[] toBeDeleted = readWorkload(dataFile);

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

        writeHeader(wr_result, dataFile, k, r, eps, maxM);
        writeHeader(wr_time, dataFile, k, r, eps, maxM);
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
