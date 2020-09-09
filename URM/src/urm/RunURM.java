package urm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import index.DualTree;
import index.RankItem;
import index.VectorUtil;

public class RunURM {

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
				int r = 0, dim = 0;
				
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

				if (cmd.hasOption("r")) {
					r = Integer.parseInt(cmd.getOptionValue("r"));
				} else {
					System.out.println("Please add option '-r' or '--r'");
					System.exit(1);
				}

				run_urm(dataFile, resultPrefix, r, dim);
			} catch (ParseException pe) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("runURM.jar", options);
				System.exit(1);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void run_urm(String dataFile, String resultPrefix, int r, int dim) throws IOException {
		
		String timePath = resultPrefix + "_time.txt";
		String tuplePath = resultPrefix + "_tuple.txt";

		BufferedWriter wr_result, wr_time;
		wr_result = new BufferedWriter(new FileWriter(tuplePath, true));
		wr_time = new BufferedWriter(new FileWriter(timePath, true));

		double[][] data = readDataFile(dataFile);

		int[] toBeDeleted = readWorkload(dataFile);

		int data_size = data.length;
		int init_size = data_size - toBeDeleted.length;

		int m = testM(dim, r, data_size, init_size, data);
		
		if (m < 0) {
			wr_time.write("No valid result provided\n");
			wr_result.close();
			wr_time.close();
			System.exit(0);
		}

		double[][] samples = readUtilFile(dim, dim + (1 << m) + 1);

		List<TupleOpr> workLoad = new ArrayList<>();
		for (int idx = init_size; idx < data_size; idx++)
			workLoad.add(new TupleOpr(idx, 1));
		for (int idx : toBeDeleted)
			workLoad.add(new TupleOpr(idx, -1));

		System.out.println(r + " " + m);

		URM urw = new URM(dim, r, data_size, init_size, samples.length, data, samples);

		writeHeader(wr_result, dataFile, r, m);
		writeHeader(wr_time, dataFile, r, m);
		wr_time.write("init_time=" + Math.round(urw.initTime) + " inserts="
				+ (workLoad.size() - toBeDeleted.length) + " deletes=" + toBeDeleted.length + "\n");
		
		int interval = workLoad.size() / 10;
		int output_id = 0;
		for (int opr_id = 0; opr_id < workLoad.size(); opr_id++) {
			if (opr_id % interval == interval - 1)
				urw.update(workLoad.get(opr_id), true);
			else
				urw.update(workLoad.get(opr_id), false);
			if (opr_id % interval == interval - 1) {
				wr_time.write("idx=" + output_id + " ");
				wr_time.write(Math.round(urw.addTime) + " ");
				wr_time.write(Math.round(urw.delTime) + "\n");

				writeResult(wr_result, output_id, urw, data);
				output_id += 1;
				wr_result.flush();
				wr_time.flush();
			}
		}

		System.gc();
		wr_result.close();
		wr_time.close();
	}

	private static int testM(int dim, int r, int data_size, int init_size, double[][] data) throws IOException {
		int test_m = 10, test_r = 0;
		while (true) {
			double[][] test_samples = readUtilFile(dim, dim + (1 << test_m) + 1);
			DualTree dualTree = new DualTree(dim, data_size, init_size, test_samples.length, data, test_samples);
			Set<Integer> test_result = new HashSet<>();
			for(RankItem item : dualTree.topResults)
				test_result.add(item.t_idx);
			test_r = test_result.size();
			if (test_r >= r + 10 || test_m >= 20)
				break;
			++test_m;
		}
		
		if (test_m == 20 && test_r < r)
			return -1;
		
		return test_m;
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

	private static void writeResult(BufferedWriter wr, int idx, URM urw, double[][] data) throws IOException {
		DecimalFormat df = new DecimalFormat("0.000000");
		wr.write("index " + idx + " " + urw.result.size() + "\n");
		for (int t_idx : urw.result) {
			for (int d = 0; d < data[t_idx].length - 1; d++)
				wr.write(df.format(data[t_idx][d]) + " ");
			wr.write("\n");
		}
	}

	private static void writeHeader(BufferedWriter wr, String filePath, int r, int m)
			throws IOException {
		wr.write("dataset " + filePath + " ");
		wr.write("r=" + r + " ");
		wr.write("m=" + m + "\n");
	}

}
