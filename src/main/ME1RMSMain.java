package main;

import structures.MinErrorRMS;
import structures.MinSizeRMS;
import utils.TupleOpr;
import utils.VectorUtil;

import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.*;

public class ME1RMSMain {

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

		boolean flag = false;
		int last_sample_size = 1000;
		int k = 1;
		for (int r = 5; r <= 100; r += 5) {
			if (flag)
				break;
			if (r < dim)
				continue;

			int sample_size = calculateSampleSize(dim, k, r, data_size, init_size, last_sample_size, max_sample_size,
					data, samples);
			last_sample_size = sample_size;
			double eps = 0.0001;
			if (sample_size == 1000)
				eps = calculateEpsValue(dim, k, r, data_size, init_size, sample_size, data, samples);
			System.out.println(r + " " + sample_size + " " + eps);

			MinErrorRMS inst = new MinErrorRMS(dim, k, r, eps, data_size, init_size, sample_size, data, samples);

			writeHeader(wr_result, dataPath, k, r, eps, sample_size);
			writeHeader(wr_time, dataPath, k, r, eps, sample_size);
			wr_time.write("init_time=" + Math.round(inst.initTime) + " wl_size=" + workLoad.size() + " inserts="
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

			if (inst.result().size() <= r - 5)
				flag = true;

			inst = null;
			System.gc();
		}
		wr_result.close();
		wr_time.close();
	}

	private static double calculateEpsValue(int dim, int k, int r, int data_size, int init_size, int sample_size,
			double[][] data, double[][] samples) {
		double eps = 0.0001, max_eps = 0.5;
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
		if (dim <= 4)
			size = 200000;
		else if (dim <= 6)
			size = 300000;
		else if (dim <= 8)
			size = 400000;
		else
			size = 500000;
		return size;
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

	private static void writeHeader(BufferedWriter wr, String filePath, int k, int r, double eps, int sample_size) throws IOException {
		wr.write("header " + filePath + " ");
		wr.write("k=" + k + " ");
		wr.write("r=" + r + " ");
		wr.write("eps=" + eps + " ");
		wr.write("m=" + sample_size + "\n");
	}
}
