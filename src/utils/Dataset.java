package utils;

import generator.TupleGenerator;
import generator.UtilityGenerator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Dataset {

    public static int DIM;

    public static boolean[] IS_DELETED;
    public static double[][] TUPLES;

    public static double[][] UTILITIES;

    public static TopKResult[] TOP_K_RESULTS;

    public static Map<Integer, HashSet<Integer>> SET_SYSTEM;

    public static void initialize(int dim, int data_size, int init_size, int sample_size) {
        DIM = dim;
        TUPLES = TupleGenerator.uniformGenerator(DIM, data_size);
        UTILITIES = UtilityGenerator.uniformGenerator(DIM, sample_size);

        IS_DELETED = new boolean[data_size];
        for (int i = 0; i < init_size; i++) {
            IS_DELETED[i] = false;
        }
        for (int i = init_size; i < data_size; i++) {
            IS_DELETED[i] = true;
        }
    }

    public static boolean insert(int idx) {
        if (IS_DELETED[idx]) {
            IS_DELETED[idx] = false;
            return true;
        } else {
            return false;
        }
    }

    public static boolean delete(int idx) {
        if (! IS_DELETED[idx]) {
            IS_DELETED[idx] = true;
            return true;
        } else {
            return false;
        }
    }

    public static void constructSetSystem() {
        SET_SYSTEM = new HashMap<>();
        for (int i = 0; i < TOP_K_RESULTS.length; i++) {
            for (int t_idx : TOP_K_RESULTS[i].results) {
                if (! SET_SYSTEM.containsKey(t_idx)) {
                    SET_SYSTEM.put(t_idx, new HashSet<>());
                    SET_SYSTEM.get(t_idx).add(i);
                } else {
                    SET_SYSTEM.get(t_idx).add(i);
                }
            }
        }
    }
}
