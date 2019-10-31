package main;

import structures.MinErrorRMS;
import utils.TupleOpr;
import utils.VectorUtil;

import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.*;

public class ME_n_AC {

	public static void main(String[] args) {
		try {
			runMinErrorRMS(args[0], args[1], args[2]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void runMinErrorRMS(String dataPath, String tuplePath, String timePath) throws IOException {
		BufferedWriter wr_result = null, wr_time = null;
		wr_result = new BufferedWriter(new FileWriter(tuplePath, true));
		wr_time = new BufferedWriter(new FileWriter(timePath, true));

		int k = 1, r = 50;
		for (int data_size = 100_000; data_size <= 1_000_000; data_size += 100_000) {
			double[][] data = readDataFile(dataPath, data_size);
			if (data == null) {
				System.err.println("error in reading data file");
				System.exit(0);
			}

			int[] toBeDeleted = readWorkload(dataPath, data_size);
			if (toBeDeleted == null) {
				System.err.println("error in reading workload file");
				System.exit(0);
			}

			int dim = data[0].length - 1;
			int init_size = data_size - toBeDeleted.length;

			int fix_pow = 16;
			double[][] samples = readUtilFile(dim, calcM(fix_pow, dim));
			if (samples == null) {
				System.err.println("error in reading sample file");
				System.exit(0);
			}

			List<TupleOpr> workLoad = new ArrayList<>();
			for (int idx = init_size; idx < data_size; idx++)
				workLoad.add(new TupleOpr(idx, 1));
			for (int idx : toBeDeleted)
				workLoad.add(new TupleOpr(idx, -1));

			double init_eps = 0.05;
			Pair pair = getParams(dim, k, r, data_size, init_size, init_eps, fix_pow, data, samples);

			System.out.println(r + " " + pair.pow + " " + pair.eps);

			MinErrorRMS inst = new MinErrorRMS(dim, k, r, pair.eps, data_size, init_size, calcM(pair.pow, dim), data,
					samples);

			writeHeader(wr_result, dataPath, k, r, pair.eps, calcM(pair.pow, dim), data_size);
			writeHeader(wr_time, dataPath, k, r, pair.eps, calcM(pair.pow, dim), data_size);
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
			inst = null;
			System.gc();
		}
		wr_result.close();
		wr_time.close();
	}

	private static double[][] readDataFile(String filePath, int data_size) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));

			int dim = Integer.parseInt(br.readLine());
			double[][] data = new double[data_size][dim + 1];

			String line;
			int idx = 0;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(" ");
				for (int d = 0; d < dim; d++)
					data[idx][d] = Double.parseDouble(tokens[d].trim());
				idx++;
				if (idx == data_size)
					break;
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

	private static int[] readWorkload(String filePath, int data_size) {
		try {
			String wlPath = filePath.substring(0, filePath.length() - 4).split("_")[0] + "_" + data_size + "_wl.txt";
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

	private static void writeHeader(BufferedWriter wr, String filePath, int k, int r, double eps, int m, int n)
			throws IOException {
		wr.write("dataset " + filePath + " ");
		wr.write("k=" + k + " ");
		wr.write("r=" + r + " ");
		wr.write("eps=" + eps + " ");
		wr.write("m=" + m + " ");
		wr.write("n=" + n + "\n");
	}

	private static Pair getParams(int dim, int k, int r, int data_size, int init_size, double old_eps, int fix_pow,
			double[][] data, double[][] samples) {
		double eps = old_eps;
		while (eps > 0.01) {
			int fix_m = calcM(fix_pow, dim);
			MinErrorRMS test_inst = new MinErrorRMS(dim, k, r, eps, data_size, init_size, fix_m, data, samples);
			int mr = test_inst.maxInst.mr;
			test_inst = null;

			if (mr < fix_m / 1.1)
				return new Pair(fix_pow, eps);
			else {
				eps -= 0.01;
			}
		}
		return new Pair(fix_pow, 0.01);
	}

	private static class Pair {
		int pow;
		double eps;

		public Pair(int pow, double eps) {
			this.pow = pow;
			this.eps = eps;
		}
	}

	private static int calcM(int pow, int dim) {
		return (1 << pow) + dim + 1;
	}
}
