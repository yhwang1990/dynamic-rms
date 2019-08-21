package structures;

import utils.SetOperation;

import java.util.*;

public class SetCover {
    private static int SCALE_FACTOR = 2;
    private static int MAX_SIZE;

    private DualTree dualTree;

    private DensityLevel[] levels;
    private ElemInfo[] elemInfo;

    Map<Integer, SetInfo> sol;

    public SetCover(DualTree dualTree) {
        this.dualTree = dualTree;
        MAX_SIZE = this.dualTree.utilities.length;

        this.sol = new HashMap<>();

        int maxLevel = (int) (Math.log(MAX_SIZE) / Math.log(SCALE_FACTOR)) + 1;
        this.levels = new DensityLevel[maxLevel];

        this.elemInfo = new ElemInfo[MAX_SIZE];
    }

    public void greedySetCover() {
        int cov_size = 0;
        ArrayList<RankSet> rankSetList = new ArrayList<>(dualTree.sets.size());
        for (Map.Entry<Integer, HashSet<Integer>> entry : dualTree.sets.entrySet()) {
            rankSetList.add(new RankSet(entry.getKey(), entry.getValue()));
        }
        while (cov_size < MAX_SIZE) {
            rankSetList.sort(new RankSetComparator());
            RankSet bestSet = rankSetList.get(0);
            SetInfo setInfo = new SetInfo(bestSet);
            sol.put(bestSet.idx, setInfo);

            for (int elem_idx : setInfo.cov) {
                elemInfo[elem_idx] = new ElemInfo(bestSet.idx);
                elemInfo[elem_idx].covSets.addAll(dualTree.results[elem_idx].results);
            }

            if (levels[setInfo.level_idx] == null) {
                levels[setInfo.level_idx] = new DensityLevel();
                levels[setInfo.level_idx].setIdx.add(bestSet.idx);
                for (RankSet rs : rankSetList) {
                    if (! rs.uncovered.isEmpty()) {
                        levels[setInfo.level_idx].setDensity.put(rs.idx, rs.uncovered.size());
                    }
                }
            } else {
                levels[setInfo.level_idx].setIdx.add(bestSet.idx);
            }

            for (RankSet rs : rankSetList) {
                rs.uncovered.removeAll(setInfo.cov);
            }

            cov_size += setInfo.cov.size();
        }
    }

    public void update(SetOperation opr) {

    }

    public void insertTuple(int t_idx) {

    }

    public void deleteTuple(int t_idx) {

    }

    public void print() {
        System.out.println("Solution Overview");
        for (int idx : sol.keySet()) {
            System.out.println(idx + " " + sol.get(idx).level_idx + " " + sol.get(idx).cov.size());
        }
        System.out.println();

        System.out.println("Density Levels");
        for (int i = 0; i < levels.length; i++) {
            if (levels[i] != null) {
                System.out.println("Level " + i);
                for (int idx : levels[i].setIdx) {
                    System.out.print(idx + " ");
                }
                System.out.println();
                for (Map.Entry<Integer, Integer> entry : levels[i].setDensity.entrySet()) {
                    System.out.print(entry.getKey() + "," + entry.getValue() + " ");
                }
                System.out.println();
            }
        }
    }

    private static class RankSet {
        int idx;
        Set<Integer> uncovered;

        RankSet(int idx, Set<Integer> set) {
            this.idx = idx;
            this.uncovered = new HashSet<>(set);
        }
    }

    private static class RankSetComparator implements Comparator<RankSet> {

        @Override
        public int compare(RankSet rs1, RankSet rs2) {
            if (rs1.uncovered.size() > rs2.uncovered.size())
                return -1;
            else if (rs1.uncovered.size() < rs2.uncovered.size())
                return 1;
            else
                return (rs1.idx - rs2.idx);
        }
    }

    private static class DensityLevel {
        Set<Integer> setIdx;
        Map<Integer, Integer> setDensity;

        DensityLevel() {
            this.setIdx = new HashSet<>();
            this.setDensity = new HashMap<>();
        }
    }

    private static class SetInfo {
        int level_idx;
        Set<Integer> cov;

        SetInfo(RankSet rankSet) {
            this.level_idx = (int) Math.floor(Math.log(rankSet.uncovered.size()) / Math.log(SCALE_FACTOR));
            this.cov = new HashSet<>(rankSet.uncovered);
        }
    }

    private static class ElemInfo {
        int covIdx;
        Set<Integer> covSets;

        ElemInfo(int covIdx) {
            this.covIdx = covIdx;
            this.covSets = new HashSet<>();
        }
    }
}
