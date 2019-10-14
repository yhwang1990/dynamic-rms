package main;

import structures.MinErrorRMS;

import utils.TupleOpr;
import utils.VectorUtil;

import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.*;

public class ME_k5 {

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

		double[][] data = readDataFile(dataPath);
		if (data == null) {
			System.err.println("error in reading data file");
			System.exit(0);
		}

		int[] toBeDeleted = readWorkload(dataPath);
		if (toBeDeleted == null) {
			System.err.println("error in reading workload file");
			System.exit(0);
		}

		int data_size = data.length, dim = data[0].length - 1;
		int init_size = data_size - toBeDeleted.length;

		int max_m = dim + (1 << 20) - 1;
		double min_eps = 0.0001;

		double[][] samples = readUtilFile(dim, max_m);
		if (samples == null) {
			System.err.println("error in reading sample file");
			System.exit(0);
		}

		List<TupleOpr> workLoad = new ArrayList<>();
		for (int idx = init_size; idx < data_size; idx++)
			workLoad.add(new TupleOpr(idx, 1));
		for (int idx : toBeDeleted)
			workLoad.add(new TupleOpr(idx, -1));

		for (int k = 2; k <= 5; k++) {
			int cur_m = dim + (1 << 10) - 1;
			double cur_eps = 0.1024;

			for (int r = 5; r <= 50; r += 5) {
				if (r < dim)
					continue;

				Pair pair = new Pair(max_m, min_eps);

				if (cur_eps > min_eps || cur_m < max_m) {
					pair = getParams(dim, k, r, data_size, init_size, cur_eps, cur_m, data, samples);
					cur_m = pair.m;
					cur_eps = pair.eps;
				}

				System.out.println(r + " " + pair.m + " " + pair.eps);

				MinErrorRMS inst = new MinErrorRMS(dim, k, r, pair.eps, data_size, init_size, pair.m, data, samples);

				if (inst.result().size() <= r - 5 && pair.m == max_m && pair.eps < 0.0002) {
					inst = null;
					System.gc();
					break;
				}

				writeHeader(wr_result, dataPath, k, r, pair.eps, pair.m);
				writeHeader(wr_time, dataPath, k, r, pair.eps, pair.m);

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
		}
		wr_result.close();
		wr_time.close();
	}

	private static Pair getParams(int dim, int k, int r, int data_size, int init_size, double old_eps, int old_m,
			double[][] data, double[][] samples) {
		int max_m = Math.min((old_m - dim + 1) * 4 + (dim - 1), dim + (1 << 20) - 1);
		int m = old_m;
		double eps = old_eps;
		while (eps > 1e-4 - 1e-9) {
			while (m <= max_m) {
				MinErrorRMS test_inst = new MinErrorRMS(dim, k, r, eps, data_size, init_size, m, data, samples);
				int mr = test_inst.maxInst.mr;
				test_inst = null;

				if (mr >= m / 10 && mr <= m / 2)
					return new Pair(m, eps);
				else if (mr > m / 2)
					m = (m - dim + 1) * 2 + (dim - 1);
				else if (mr < m / 10) {
					if (m == dim + (1 << 10) - 1)
						return new Pair(m, eps);
					else
						m = (m - dim + 1) / 2 + (dim - 1);
				}
			}
			m = old_m;
			eps /= 2;
		}
		return new Pair(dim + (1 << 20) - 1, 0.0001);
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

		double[][] samples = new double[sample_size][dim + 1];

		String line;
		int idx = 0;
		while ((line = br.readLine()) != null) {
			String[] tokens = line.split(" ");
			for (int d = 0; d < dim; d++)
				samples[idx][d] = Double.parseDouble(tokens[d].trim());
			idx++;
			if (idx == sample_size)
				break;
		}
		br.close();

		for (double[] util : samples)
			util[dim] = 0.0;

		return samples;
	}

	private static int[] readWorkload(String filePath) throws IOException {
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

	private static void writeHeader(BufferedWriter wr, String filePath, int k, int r, double eps, int sample_size)
			throws IOException {
		wr.write("dataset " + filePath + " ");
		wr.write("k=" + k + " ");
		wr.write("r=" + r + " ");
		wr.write("eps=" + eps + " ");
		wr.write("m=" + sample_size + "\n");
	}

	private static class Pair {
		int m;
		double eps;

		public Pair(int m, double eps) {
			this.m = m;
			this.eps = eps;
		}
	}
}
