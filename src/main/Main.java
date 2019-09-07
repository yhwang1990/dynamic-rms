package main;

import structures.MinSizeRMS;
import utils.TupleOpr;
import utils.VectorUtil;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {

    public static double cTime = 0.0, itTime = 0.0, isTime = 0.0, dtTime = 0.0, dsTime = 0.0;

    public static void main(String[] args) {
        if (args.length < 3 || args.length > 4)
            System.err.println("error in args");
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
            System.err.println("error in reading dataFile");
            System.exit(0);
        }
        int data_size = data.length, dim = data[0].length - 1;
        int init_size = data_size / 2;

        int[] toBeDeleted = readWorkload(filePath);
        if (toBeDeleted == null) {
            System.err.println("error in reading workloadFile");
            System.exit(0);
        }

        for (int sample_size = 25_000; sample_size < 1_000_000; sample_size *= 2) {
            double[][] samples = readUtilFile(dim, sample_size);
            if (samples == null) {
                System.err.println("error in reading sampleFile");
                System.exit(0);
            }

            List<TupleOpr> workLoad = new ArrayList<>();
            for (int idx = init_size; idx < data_size; idx++)
                workLoad.add(new TupleOpr(idx, 1));
            for (int idx : toBeDeleted)
                workLoad.add(new TupleOpr(idx, -1));

            MinSizeRMS inst = new MinSizeRMS(dim, k, eps, data_size, init_size, sample_size, data, samples);

            writeHeader(wr_result, args, sample_size);
            writeHeader(wr_time, args, sample_size);
            try {
                wr_time.write("cTime=" + Math.round(cTime) + "\n");
            } catch(IOException e) {
                e.printStackTrace();
            }

            int interval = workLoad.size() / 10;
            for (int opr_id = 0; opr_id < workLoad.size(); opr_id++) {
                inst.update(workLoad.get(opr_id));
                if (opr_id % interval == interval - 1) {
                    writeTime(wr_time, opr_id + 1);
                    writeResult(wr_result, opr_id + 1, inst);
                }
            }

            resetTime();
        }

        double[][] samples = readUtilFile(dim, 1_000_000);
        if (samples == null) {
            System.err.println("error in reading sampleFile");
            System.exit(0);
        }

        List<TupleOpr> workLoad = new ArrayList<>();
        for (int idx = init_size; idx < data_size; idx++)
            workLoad.add(new TupleOpr(idx, 1));
        for (int idx : toBeDeleted)
            workLoad.add(new TupleOpr(idx, -1));

        MinSizeRMS inst = new MinSizeRMS(dim, k, eps, data_size, init_size, 1_000_000, data, samples);

        writeHeader(wr_result, args, 1_000_000);
        writeHeader(wr_time, args, 1_000_000);
        try {
            wr_time.write("cTime=" + Math.round(cTime) + "\n");
        } catch(IOException e) {
            e.printStackTrace();
        }

        int interval = workLoad.size() / 10;
        for (int opr_id = 0; opr_id < workLoad.size(); opr_id++) {
            inst.update(workLoad.get(opr_id));
            if (opr_id % interval == interval - 1) {
                writeTime(wr_time, opr_id + 1);
                writeResult(wr_result, opr_id + 1, inst);
            }
        }

        try {
            wr_result.flush();
            wr_result.close();

            wr_time.flush();
            wr_time.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
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

    private static void writeHeader(BufferedWriter wr, String[] args, int sample_size) {
        try {
            wr.write("dataset=" + args[0] + " ");
            wr.write("k=" + args[1] + " ");
            wr.write("eps=" + args[2] + " ");
            wr.write("sample_size=" + sample_size + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runMinErrorRMS(String[] args) {}

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
            String filePath = "./samples/utils_" + dim + "d.txt";
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

    private static int decideSampleSize(int dim) {
        int size;

        if(dim <= 3)
            size = 25000;
        else if(dim <= 4)
            size = 50000;
        else if(dim <= 6)
            size = 100000;
        else if(dim <= 8)
            size = 200000;
        else
            size = 400000;

        return size;
    }
}
