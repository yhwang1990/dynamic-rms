package data;

import java.io.*;
import java.util.*;

public class Shuffle {
    public static void main(String[] args) {
        shuffle("datasets/Airline.txt", "datasets/Airline2.txt");
    }

    private static void shuffle(String oldFile, String newFile) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
            BufferedReader br = new BufferedReader(new FileReader(oldFile));

            bw.write(br.readLine());
            bw.write("\n");
            String line;
            ArrayList<String> data = new ArrayList<>();
            while ((line = br.readLine()) != null)
                data.add(line);
            Collections.shuffle(data, new Random(0));
            for (String s : data) {
                bw.write(s);
                bw.write("\n");
            }
            br.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
