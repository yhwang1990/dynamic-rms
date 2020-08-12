package data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Random;

public class WorkloadGenerator {

    public static void main(String[] args) {
    	generateMixedWorkload(13176, "../MovieLens_mixed.txt");
    	generateMixedWorkload(21961, "../NBA_mixed.txt");
    	generateMixedWorkload(382168, "../AirQuality_mixed.txt");
    	generateMixedWorkload(581011, "../CovType_mixed.txt");
    	generateMixedWorkload(100000, "../Indep_mixed.txt");
    	generateMixedWorkload(100000, "../AntiCorr_mixed.txt");
    }

    private static void generateWorkload(int size, String path) {
        Random rand = new Random(0);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(path));
            ArrayList<Integer> toBeDeleted = new ArrayList<>();
            int[] bitmap = new int[size];
            while (toBeDeleted.size() < Math.min(size / 2, 50000)) {
                int nxt = rand.nextInt(size);
                if (bitmap[nxt] == 0) {
                    toBeDeleted.add(nxt);
                    bitmap[nxt] = 1;
                }
            }

            for (int idx : toBeDeleted) {
                bw.write(Integer.toString(idx));
                bw.write("\n");
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void generateMixedWorkload(int size, String path) {
        Random rand = new Random(0);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(path));
            int wl_size = Math.min(size, 100000);
            ArrayList<Integer> opr_idxs = new ArrayList<>();
            ArrayList<Integer> opr_types = new ArrayList<>();
            int[] bitmap = new int[size];
            int init_size = Math.max(size * 3 / 4, size - 25000);
            for (int i = 0; i < init_size; ++i) {
            	bitmap[i] = 1;
            }
            while (opr_idxs.size() < wl_size) {
            	int type;
            	if (rand.nextBoolean() == true) {
            		type = 0;
            	} else {
            		type = 1;
            	}
            	
            	if (type == 0) {
            		int nxt = rand.nextInt(size);
            		while (bitmap[nxt] == 1) {
            			nxt = rand.nextInt(size);
            		}
                    opr_idxs.add(nxt);
                    opr_types.add(0);
                    bitmap[nxt] = 1;
                        
                    System.out.println(nxt + "," + 0);
            	} else {
            		int nxt = rand.nextInt(size);
            		while (bitmap[nxt] == 0) {
            			nxt = rand.nextInt(size);
            		}
                    opr_idxs.add(nxt);
                    opr_types.add(1);
                    bitmap[nxt] = 0;
                        
                    System.out.println(nxt + "," + 1);
            	}
            }

            for (int i = 0; i < wl_size; ++i) {
                bw.write(opr_idxs.get(i) + "," + opr_types.get(i) + "\n");
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
