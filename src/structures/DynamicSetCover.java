package structures;

import java.util.*;

public class DynamicSetCover {
    private static int SCALE_FACTOR = 2;
    private static int MAX_SIZE;

    private Map<Integer, HashSet<Integer>> sets;

    private DensityLevel[] levels;
    private ElemInfo[] elemInfo;

    Map<Integer, SetInfo> sol;

    public DynamicSetCover(int size, Map<Integer, HashSet<Integer>> sets) {
        MAX_SIZE = size;
        this.sets = sets;

        this.sol = new HashMap<>();

        int maxLevel = (int) (Math.log(MAX_SIZE) / Math.log(SCALE_FACTOR)) + 1;
        this.levels = new DensityLevel[maxLevel];

        this.elemInfo = new ElemInfo[MAX_SIZE];
    }

    public void constructSetCover() {
        int cov_size = 0;
        int[] bitmap = new int[MAX_SIZE];
        while (cov_size < MAX_SIZE) {

        }
    }

    private class RankSet implements Comparable<RankSet> {
        int idx;
        Set<Integer> uncovered;

        private RankSet(int idx, Set<Integer> set) {
            this.idx = idx;
            this.uncovered = new HashSet<>(set);
        }


        @Override
        public int compareTo(RankSet rankSet) {
            if (this.uncovered.size() > rankSet.uncovered.size())
                return -1;
            else if (this.uncovered.size() < rankSet.uncovered.size())
                return 1;
            else
                return (this.idx - rankSet.idx);
        }
    }

    private class DensityLevel {
        Set<Integer> setIdx;
        Map<Integer, Integer> setDensity;
    }

    private class SetInfo {
        int level_idx;
        Set<Integer> cov;
    }

    private class ElemInfo {
        int covIdx;
        Set<Integer> covSets;
    }
}
