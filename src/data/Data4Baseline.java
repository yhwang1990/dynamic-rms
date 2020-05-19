package data;

import utils.TupleOpr;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Data4Baseline {
    public static void main(String[] args) {
    	generateData4Baseline("../AirQuality.txt");
    }

    private static void generateData4Baseline(String dataPath) {
        DecimalFormat df = new DecimalFormat("0.000000");
        try {
            Path path = Paths.get(dataPath);
            int data_size = (int) Files.lines(path).count() - 1;

            BufferedReader br = new BufferedReader(new FileReader(dataPath));

            int dim = Integer.parseInt(br.readLine());
            double[][] data = new double[data_size][dim];

            String line;
            int idx = 0;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(" ");
                for (int d = 0; d < dim; d++)
                    data[idx][d] = Double.parseDouble(tokens[d].trim());
                idx++;
            }
            br.close();

            String datasetName = dataPath.substring(0, dataPath.length() - 4);

            String wlPath = datasetName.split("_")[0] + "_wl.txt";
            path = Paths.get(wlPath);
            int wl_size = (int) Files.lines(path).count();

            br = new BufferedReader(new FileReader(wlPath));
            int[] wl = new int[wl_size];

            idx = 0;
            while ((line = br.readLine()) != null)
                wl[idx++] = Integer.parseInt(line.trim());
            br.close();

            int init_size = data.length - wl.length;
            List<TupleOpr> workLoad = new ArrayList<>();
            for (int i = init_size; i < data.length; i++)
                workLoad.add(new TupleOpr(i, 1));
            for (int i : wl)
                workLoad.add(new TupleOpr(i, -1));

            boolean[] isDeleted;
            isDeleted = new boolean[data.length];
            for (int i = 0; i < init_size; i++) {
                isDeleted[i] = false;
            }
            for (int i = init_size; i < data.length; i++) {
                isDeleted[i] = true;
            }

            int interval = workLoad.size() / 10;
            int data_idx = 0;
            for (int opr_id = 0; opr_id < workLoad.size(); opr_id++) {
                TupleOpr opr = workLoad.get(opr_id);
                if (opr.oprType > 0)
                    isDeleted[opr.t_idx] = false;
                else if (opr.oprType < 0)
                    isDeleted[opr.t_idx] = true;

                if (opr_id % interval == interval - 1) {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(datasetName + "_" + data_idx + ".txt"));
                    bw.write(Integer.toString(dim));
                    bw.write("\n");
                    for (int i = 0; i < data.length; i++) {
                        if (!isDeleted[i]) {
                            StringBuilder sb = new StringBuilder();
                            for (int d = 0; d < dim; d++)
                                sb.append(df.format(data[i][d])).append(" ");
                            sb.deleteCharAt(sb.length() - 1).append("\n");
                            bw.write(sb.toString());
                        }
                    }
                    bw.flush();
                    bw.close();
                    data_idx += 1;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
