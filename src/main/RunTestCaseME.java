package main;

import structures.MinErrorRMS;

import utils.TupleOpr;
import utils.VectorUtil;

import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.*;

public class RunTestCaseME {

	public static void main(String[] args) {
		try {
			runMinErrorRMS(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void runMinErrorRMS(String[] args) throws IOException {
		BufferedWriter wr_result = null, wr_time = null;
		wr_result = new BufferedWriter(new FileWriter(args[1], true));
		wr_time = new BufferedWriter(new FileWriter(args[2], true));

		int k = Integer.parseInt(args[3]);
		int r = Integer.parseInt(args[4]);
		int pow = Integer.parseInt(args[5]);
		double eps = Double.parseDouble(args[6]);
		
		double[][] data = readDataFile(args[0]);
		if (data == null) {
			System.err.println("error in reading dataset");
			System.exit(0);
		}

		int[] toBeDeleted = readWorkload(args[0]);
		if (toBeDeleted == null) {
			System.err.println("error in reading workload");
			System.exit(0);
		}

		int data_size = data.length, dim = data[0].length - 1;
		int init_size = data_size - toBeDeleted.length;
		
		int m = dim + (1 << pow) - 1;

		double[][] samples = readUtilFile(dim, m);
		if (samples == null) {
			System.err.println("error in reading samples");
			System.exit(0);
		}

		List<TupleOpr> workLoad = new ArrayList<>();
		for (int idx = init_size; idx < data_size; idx++)
			workLoad.add(new TupleOpr(idx, 1));
		for (int idx : toBeDeleted)
			workLoad.add(new TupleOpr(idx, -1));

		MinErrorRMS inst = new MinErrorRMS(dim, k, r, eps, data_size, init_size, m, data, samples);

		writeHeader(wr_result, args[0], k, r, eps, m);
		writeHeader(wr_time, args[0], k, r, eps, m);
		wr_time.write("init_time=" + Math.round(inst.initTime) + " inserts=" + (workLoad.size() - toBeDeleted.length) + " deletes=" + toBeDeleted.length + "\n");

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
		inst = null;
		System.gc();
			
		wr_result.close();
		wr_time.close();
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

	private static void writeResult(BufferedWriter wr, int idx, MinErrorRMS inst, double[][] data) throws IOException {
		DecimalFormat df = new DecimalFormat("0.000000");
		wr.write("index " + idx + " " + inst.result().size() + "\n");
		for (int t_idx : inst.result()) {
			for (int d = 0; d < data[t_idx].length - 1; d++)
				wr.write(df.format(data[t_idx][d]) + " ");
			wr.write("\n");
		}
	}

	private static void writeHeader(BufferedWriter wr, String filePath, int k, int r, double eps, int m) throws IOException {
		wr.write("header " + filePath + " ");
		wr.write("k=" + k + " ");
		wr.write("r=" + r + " ");
		wr.write("eps=" + eps + " ");
		wr.write("m=" + m + "\n");
	}
}
