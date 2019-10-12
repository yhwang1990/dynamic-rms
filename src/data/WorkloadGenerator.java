package data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Random;

public class WorkloadGenerator {

    public static void main(String[] args) {
        generateWorkload();
    }

    private static void generateWorkload() {
        Random rand = new Random(0);
        int size = 13176;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("../MovieLens_wl.txt"));
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
}
