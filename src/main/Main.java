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

	public static double InitTime = 0.0, AddTreeTime = 0.0, AddSetTime = 0.0, DelTreeTime = 0.0, DelSetTime = 0.0;

	public static void main(String[] args) {
		if (args.length < 2)
			System.err.println("error in args");
		else if ("ms".equals(args[0])) {
			try {
				runMinSizeRMS(args[1]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if ("me".equals(args[0])) {
			try {
				runMinErrorRMS(args[1]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void runMinSizeRMS(String filePath) throws IOException {
		String resultFile = "result/tuples_contRMS_ms.txt";
		String timeFile = "result/time_contRMS_ms.txt";

		BufferedWriter wr_result = null, wr_time = null;
		wr_result = new BufferedWriter(new FileWriter(resultFile, true));
		wr_time = new BufferedWriter(new FileWriter(timeFile, true));

		double[][] data = readDataFile(filePath);

		if (data == null) {
			System.err.println("error in reading dataset");
			System.exit(0);
		}

		int data_size = data.length, dim = data[0].length - 1;
		int init_size = data_size / 2;

		int[] toBeDeleted = readWorkload(filePath);
		if (toBeDeleted == null) {
			System.err.println("error in reading workload");
			System.exit(0);
		}

		int max_sample_size = decideSampleSize(dim);
		double[][] samples = readUtilFile(dim, max_sample_size);

		if (samples == null) {
			System.err.println("error in reading samples");
			System.exit(0);
		}

		List<TupleOpr> workLoad = new ArrayList<>();
		for (int idx = init_size; idx < data_size; idx++)
			workLoad.add(new TupleOpr(idx, 1));
		for (int idx : toBeDeleted)
			workLoad.add(new TupleOpr(idx, -1));

		for (int k = 1; k <= 1; k++) {
			for (int scale = 1; scale <= 10; scale++) {
				double eps = scale * 0.001;
				int sample_size = (int) (max_sample_size * 0.001 / eps);
				MinSizeRMS inst = new MinSizeRMS(dim, k, eps, data_size, init_size, sample_size, data, samples);
				writeHeader(wr_result, filePath, k, eps);
				writeHeader(wr_time, filePath, k, eps);
				wr_time.write("init_time=" + Math.round(InitTime) + " wl_size=" + workLoad.size() + " inserts="
						+ (workLoad.size() - toBeDeleted.length) + " deletes=" + toBeDeleted.length + "\n");

				int interval = workLoad.size() / 10;
				int output_id = 0;
				for (int opr_id = 0; opr_id < workLoad.size(); opr_id++) {
					inst.update(workLoad.get(opr_id));
					if (opr_id % interval == interval - 1) {
						writeTime(wr_time, output_id, inst.result().size());
						writeResult(wr_result, output_id, inst, data);
						output_id += 1;

						wr_result.flush();
						wr_time.flush();
					}
				}
				resetTime();

				inst = null;
				System.gc();
			}

			for (int scale = 2; scale <= 10; scale++) {
				double eps = scale * 0.01;
				int sample_size = (int) (max_sample_size * 0.001 / eps);
				MinSizeRMS inst = new MinSizeRMS(dim, k, eps, data_size, init_size, sample_size, data, samples);
				writeHeader(wr_result, filePath, k, eps);
				writeHeader(wr_time, filePath, k, eps);
				wr_time.write("init_time=" + Math.round(InitTime) + " wl_size=" + workLoad.size() + " inserts="
						+ (workLoad.size() - toBeDeleted.length) + " deletes=" + toBeDeleted.length + "\n");

				int interval = workLoad.size() / 10;
				int output_id = 0;
				for (int opr_id = 0; opr_id < workLoad.size(); opr_id++) {
					inst.update(workLoad.get(opr_id));
					if (opr_id % interval == interval - 1) {
						writeTime(wr_time, output_id, inst.result().size());
						writeResult(wr_result, output_id, inst, data);
						output_id += 1;

						wr_result.flush();
						wr_time.flush();
					}
				}
				resetTime();

				inst = null;
				System.gc();
			}
		}
		wr_result.close();
		wr_time.close();
	}

	private static void runMinErrorRMS(String filePath) throws IOException {
		String resultFile = "result/tuples_contRMS_me.txt";
		String timeFile = "result/time_contRMS_me.txt";

		BufferedWriter wr_result = null, wr_time = null;
		wr_result = new BufferedWriter(new FileWriter(resultFile, true));
		wr_time = new BufferedWriter(new FileWriter(timeFile, true));

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

		int max_sample_size = decideSampleSize(dim);
		double[][] samples = readUtilFile(dim, max_sample_size);

		if (samples == null) {
			System.err.println("error in reading sample file");
			System.exit(0);
		}

		List<TupleOpr> workLoad = new ArrayList<>();
		for (int idx = init_size; idx < data_size; idx++)
			workLoad.add(new TupleOpr(idx, 1));
		for (int idx : toBeDeleted)
			workLoad.add(new TupleOpr(idx, -1));

		
		for (int k = 1; k <= 1; k++) {
			boolean flag = false;
			int last_sample_size = 1000;
			for (int r = 5; r <= 100; r += 5) {
				if (flag)
					break;
				if (r < dim)
					continue;

				int sample_size = calculateSampleSize(dim, k, r, data_size, init_size, last_sample_size,
						max_sample_size, data, samples);
				last_sample_size = sample_size;
				double eps = 0.0001;
				if (sample_size == 1000)
					eps = calculateEpsValue(dim, k, r, data_size, init_size, sample_size, data, samples);
				System.out.println(r + " " + sample_size + " " + eps);

				MinErrorRMS inst = new MinErrorRMS(dim, k, r, eps, data_size, init_size, sample_size, data, samples);

				writeHeader(wr_result, filePath, k, r, eps);
				writeHeader(wr_time, filePath, k, r, eps);
				wr_time.write("init_time=" + Math.round(InitTime) + " wl_size=" + workLoad.size() + " inserts="
						+ (workLoad.size() - toBeDeleted.length) + " deletes=" + toBeDeleted.length + "\n");

				int interval = workLoad.size() / 10;
				int output_id = 0;
				for (int opr_id = 0; opr_id < workLoad.size(); opr_id++) {
					inst.update(workLoad.get(opr_id));
					if (opr_id % interval == interval - 1) {
						writeTime(wr_time, output_id);
						writeResult(wr_result, output_id, inst, data);
						output_id += 1;
						wr_result.flush();
						wr_time.flush();
					}
				}
				resetTime();

				if (inst.result().size() <= r - 10)
					flag = true;

				inst = null;
				System.gc();
			}
		}
		wr_result.close();
		wr_time.close();
	}

	/*
	 * private static void runTest(String dataFile, String resultFile) {
	 * DecimalFormat df = new DecimalFormat("0.000000"); double[][] data =
	 * readDataFile(dataFile);
	 * 
	 * if (data == null) { System.err.println("error in reading data file");
	 * System.exit(0); }
	 * 
	 * double[][] tests = readTestFile(data[0].length - 1);
	 * 
	 * try { BufferedReader br = new BufferedReader(new FileReader(resultFile));
	 * BufferedWriter bw = new BufferedWriter(new
	 * FileWriter("./result/min_size_results_00.txt")); String line; int k = 0;
	 * while ((line = br.readLine()) != null) { if (line.startsWith("dataset")) {
	 * bw.write(line); bw.write("\n");
	 * 
	 * String[] tokens = line.split(" "); for (String token : tokens) { String
	 * paramName = token.split("=")[0]; String paramValue = token.split("=")[1]; if
	 * ("k".equals(paramName)) { k = Integer.parseInt(paramValue); } } for (int i =
	 * 0; i < 10; i++) { line = br.readLine(); tokens = line.split(" ");
	 * List<Integer> results = new ArrayList<>(); for (String token : tokens)
	 * results.add(Integer.valueOf(token));
	 * 
	 * double[] topK = new double[1_000_000]; double[] top1 = new double[1_000_000];
	 * double[] top5 = new double[1_000_000]; double[] top10 = new
	 * double[1_000_000];
	 * 
	 * readValidFile(dataFile.substring(0, dataFile.length() - 4) + "_rst_" + i +
	 * ".txt", k, topK, top1, top5, top10);
	 * 
	 * double[] rmsTop1 = new double[1_000_000];
	 * 
	 * computeRMSTop1(data, results, tests, rmsTop1);
	 * 
	 * double mrr = computeMrr(rmsTop1, topK); double acc1 = computeAcc(rmsTop1,
	 * top1); double acc5 = computeAcc(rmsTop1, top5); double acc10 =
	 * computeAcc(rmsTop1, top10);
	 * 
	 * bw.write("mrr=" + df.format(mrr) + " "); bw.write("acc@1=" + df.format(acc1)
	 * + " "); bw.write("acc@5=" + df.format(acc5) + " "); bw.write("acc@10=" +
	 * df.format(acc10) + "\n"); } } bw.flush(); } br.close(); bw.close(); } catch
	 * (IOException e) { e.printStackTrace(); } }
	 */

	private static double calculateEpsValue(int dim, int k, int r, int data_size, int init_size, int sample_size,
			double[][] data, double[][] samples) {
		double eps = 0.0001, max_eps = 0.11;
		while (eps < max_eps) {
			MinErrorRMS test_inst = new MinErrorRMS(dim, k, r, eps, data_size, init_size, sample_size, data, samples);
			int mr = test_inst.maxInst.mr;
			test_inst = null;

			if (mr <= sample_size / 10) {
				eps *= 2;
			} else
				return eps;
		}
		return 0.1;
	}

	private static int calculateSampleSize(int dim, int k, int r, int data_size, int init_size, int sample_size,
			int max_sample_size, double[][] data, double[][] samples) {
		while (sample_size < max_sample_size) {
			MinSizeRMS test_inst = new MinSizeRMS(dim, k, 0.0001, data_size, init_size, sample_size, data, samples);
			int test_size = test_inst.result().size();
			test_inst = null;

			if (test_size >= r + 4)
				return sample_size;
			else
				sample_size *= 2;
		}
		return max_sample_size;
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

	/*
	 * private static double[][] readTestFile(int dim) { try { String filePath =
	 * "./test/test_" + dim + "d.txt"; BufferedReader br = new BufferedReader(new
	 * FileReader(filePath));
	 * 
	 * double[][] data = new double[1000000][dim + 1];
	 * 
	 * String line; int idx = 0; while ((line = br.readLine()) != null) { String[]
	 * tokens = line.split(" "); for (int d = 0; d < dim; d++) data[idx][d] =
	 * Double.parseDouble(tokens[d].trim()); idx++; } br.close();
	 * 
	 * for (double[] util : data) util[dim] = 0.0;
	 * 
	 * return data; } catch (IOException e) { e.printStackTrace(); return null; } }
	 */

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

	/*
	 * private static void readValidFile(String filePath, int k, double[] topK,
	 * double[] top1, double[] top5, double[] top10) { try { BufferedReader br = new
	 * BufferedReader(new FileReader(filePath)); String line; int idx = 0; while
	 * ((line = br.readLine()) != null) { String[] tokens = line.split(" ");
	 * topK[idx] = Double.parseDouble(tokens[k - 1].split(":")[1]); top1[idx] =
	 * Double.parseDouble(tokens[0].split(":")[1]); top5[idx] =
	 * Double.parseDouble(tokens[4].split(":")[1]); top10[idx] =
	 * Double.parseDouble(tokens[9].split(":")[1]);
	 * 
	 * idx++; } br.close(); } catch (IOException e) { e.printStackTrace(); } }
	 */

	/*
	 * private static void computeRMSTop1(double[][] data, List<Integer> results,
	 * double[][] tests, double[] rmsTop1) { for (int i = 0; i < tests.length; i++)
	 * { double score = 0.0; for (int t_idx : results) score = Math.max(score,
	 * VectorUtil.inner_product(data[t_idx], tests[i])); rmsTop1[i] = score; } }
	 */

	/*
	 * private static double computeMrr(double[] rmsTop1, double[] topK) { double rr
	 * = 0.0; for (int i = 0; i < rmsTop1.length; i++) { if (rmsTop1[i] < topK[i]) {
	 * double rr_i = 1.0 - rmsTop1[i] / topK[i]; if (rr_i > 0.01)
	 * System.out.println(i + "," + rmsTop1[i] + "," + topK[i]); rr = Math.max(rr,
	 * rr_i); } } return rr; }
	 */

	/*
	 * private static double computeAcc(double[] rmsTop1, double[] topK) { int
	 * t_value = 0; for (int i = 0; i < rmsTop1.length; i++) { if (rmsTop1[i] >=
	 * topK[i]) t_value += 1; } return (t_value + 0.0) / (rmsTop1.length + 0.0); }
	 */

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

	private static void writeResult(BufferedWriter wr, int idx, MinSizeRMS inst, double[][] data) {
		DecimalFormat df = new DecimalFormat("0.000000");
		try {
			wr.write("idx=" + idx + "\n");
			for (int t_idx : inst.result()) {
				for (int d = 0; d < data[t_idx].length - 1; d++) {
					wr.write(df.format(data[t_idx][d]) + " ");
				}
				wr.write("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeResult(BufferedWriter wr, int idx, MinErrorRMS inst, double[][] data) {
		DecimalFormat df = new DecimalFormat("0.000000");
		try {
			wr.write("idx=" + idx + "\n");
			for (int t_idx : inst.result()) {
				for (int d = 0; d < data[t_idx].length - 1; d++) {
					wr.write(df.format(data[t_idx][d]) + " ");
				}
				wr.write("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeTime(BufferedWriter wr, int idx, int size) {
		try {
			wr.write("idx=" + idx + " size=" + size + " ");
			wr.write(Math.round(AddTreeTime) + " ");
			wr.write(Math.round(AddSetTime) + " ");
			wr.write(Math.round(DelTreeTime) + " ");
			wr.write(Math.round(DelSetTime) + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeTime(BufferedWriter wr, int idx) {
		try {
			wr.write("idx=" + idx + " ");
			wr.write(Math.round(AddTreeTime) + " ");
			wr.write(Math.round(AddSetTime) + " ");
			wr.write(Math.round(DelTreeTime) + " ");
			wr.write(Math.round(DelSetTime) + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeHeader(BufferedWriter wr, String filePath, int k, double eps) {
		try {
			wr.write("dataset=" + filePath + " ");
			wr.write("k=" + k + " ");
			wr.write("eps=" + eps + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeHeader(BufferedWriter wr, String filePath, int k, int r, double eps) {
		try {
			wr.write("dataset=" + filePath + " ");
			wr.write("k=" + k + " ");
			wr.write("r=" + r + " ");
			wr.write("eps=" + eps + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void resetTime() {
		InitTime = 0.0;
		AddTreeTime = 0.0;
		AddSetTime = 0.0;
		DelTreeTime = 0.0;
		DelSetTime = 0.0;
	}
}
