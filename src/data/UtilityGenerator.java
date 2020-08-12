package data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class UtilityGenerator {
	public static void main(String[] args) {
		ArrayList<double[]> samples = UtilityGenerator.balancedGenerator(3, 1_100_000);
		writeToFile(3, samples);
	}

    public static double[][] gaussianGenerator(int dim, int size) {
    	Random RAND = new Random(17);
        double[][] samples = new double[size][dim];
        for (int i = 0; i < size; i++) {
        	double len = 0;
        	double[] sample = new double[dim];
        	while(len == 0) {
        	    for(int d = 0; d < dim; d++) {
        	    	double rand = RAND.nextGaussian();
        	    	sample[d] = rand;
        	    	len += rand * rand;
        	    }
        	    len = Math.sqrt(len);
        	}
        	for (int d = 0; d < dim; d++)
        		sample[d] = Math.abs(sample[d] / len);
        	samples[i] = sample;
        }
        return samples;
    }
    
    public static ArrayList<double[]> balancedGenerator(int dim, int size) {
    	Random RAND = new Random(524287);
        ArrayList<double[]> samples = new ArrayList<>();
        
        HashMap<Integer, ArrayList<double[]>> subSamples = new HashMap<>();
        int subSize = size / (1 << (dim / 2)), idx = 0;
        System.out.println(dim + "," + subSize);
		do {
			double len = 0;
			double[] sample = new double[dim];
			while (len == 0) {
				for (int d = 0; d < dim; d++) {
					double rand = RAND.nextGaussian();
					sample[d] = rand;
					len += rand * rand;
				}
				len = Math.sqrt(len);
			}
			for (int d = 0; d < dim; d++)
				sample[d] = Math.abs(sample[d] / len);

			int code = 0;
			for (int j = 0; j < dim / 2; j++) {
				if (sample[j * 2] > sample[j * 2 + 1])
					code = code * 10 + 1;
				else
					code = code * 10;
			}
			if (!subSamples.containsKey(code)) {
				ArrayList<double[]> subSample = new ArrayList<>();
				subSamples.put(code, subSample);
			}
			subSamples.get(code).add(sample);
		} while ((++idx) % 10000 != 0 || !isBalanced(subSize, subSamples));

        System.out.println(dim + "," + subSamples.size());
        System.out.println(dim + "," + idx);
        
        ArrayList<double[]> tmpSamples = new ArrayList<>();
        for (ArrayList<double[]> subSample : subSamples.values()) {
        	tmpSamples.addAll(subSample.subList(0, subSize));
        }
        Collections.shuffle(tmpSamples);
    
        for (int d = 0; d < dim; d++) {
        	double[] unitSample = new double[dim];
            for (int i = 0; i < dim; i++) {
                if (i == d)
                	unitSample[i] = 1.0;
                else
                	unitSample[i] = 0.0;
            }
            samples.add(unitSample);
        }
        double[] eqSample = new double[dim];
        for (int i = 0; i < dim; i++) {
        	eqSample[i] = 1.0 / Math.sqrt(dim);
        }
        samples.add(eqSample);
        samples.addAll(tmpSamples);
        return samples;
    }

	private static boolean isBalanced(int subSize, HashMap<Integer, ArrayList<double[]>> subSamples) {
		for (ArrayList<double[]> subSample : subSamples.values()) {
			if (subSample.size() < subSize)
				return false;
		}
		return true;
	}

	private static void writeToFile(int dim, ArrayList<double[]> samples) {
		DecimalFormat df = new DecimalFormat("0.000000");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("utils_" + dim + "d.txt"));
			for (double[] sample : samples) {
				StringBuilder sb = new StringBuilder();
				for (int d = 0; d < dim; d++)
					sb.append(df.format(sample[d])).append(" ");
				sb.deleteCharAt(sb.length() - 1).append("\n");
				bw.write(sb.toString());
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
