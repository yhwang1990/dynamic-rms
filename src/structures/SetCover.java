package structures;

import utils.Parameter;

import java.util.*;

public class SetCover {
    private static int SCALE_FACTOR = 2;

    public Map<Integer, HashSet<Integer>> sets;

    private DensityLevel[] levels;

    private int[] utilityAssign;
    private int[] utilityLevel;

    private MinSizeRMS instance;

    Map<Integer, SetInfo> sol;

    public SetCover(MinSizeRMS inst) {
        this.instance = inst;

        this.sets = new HashMap<>();
        for (int u_idx = 0; u_idx < Parameter.SAMPLE_SIZE; u_idx++) {
            for (int t_idx : this.instance.dualTree.uIdx.topKResults[u_idx].results) {
                if (! this.sets.containsKey(t_idx)) {
                    this.sets.put(t_idx, new HashSet<>());
                    this.sets.get(t_idx).add(u_idx);
                } else {
                    this.sets.get(t_idx).add(u_idx);
                }
            }
        }

        int maxLevel = (int) (Math.log(Parameter.SAMPLE_SIZE) / Math.log(SCALE_FACTOR)) + 1;
        this.levels = new DensityLevel[maxLevel];

        this.utilityAssign = new int[Parameter.SAMPLE_SIZE];
        this.utilityLevel = new int[Parameter.SAMPLE_SIZE];

        this.sol = new HashMap<>();
        this.greedySetCover();
    }

    private void greedySetCover() {
        int cov_size = 0;
        ArrayList<RankSet> rankSetList = new ArrayList<>(sets.size());
        for (Map.Entry<Integer, HashSet<Integer>> entry : sets.entrySet()) {
            rankSetList.add(new RankSet(entry.getKey(), entry.getValue()));
        }
        while (cov_size < Parameter.SAMPLE_SIZE) {
            rankSetList.sort(new RankSetComparator());
            RankSet next = rankSetList.get(0);
            SetInfo setInfo = new SetInfo(next);
            sol.put(next.idx, setInfo);

            for (int u_idx : setInfo.cov) {
                utilityAssign[u_idx] = next.idx;
                utilityLevel[u_idx] = setInfo.level_idx;
            }

            if (levels[setInfo.level_idx] == null) {
                levels[setInfo.level_idx] = new DensityLevel();
            }

            levels[setInfo.level_idx].setIdx.add(next.idx);
            levels[setInfo.level_idx].levelCov.addAll(setInfo.cov);

            for (RankSet rs : rankSetList) {
                if (! rs.uncovered.isEmpty()) {
                    int pre_size = rs.uncovered.size();
                    rs.uncovered.removeAll(setInfo.cov);
                    int after_size = rs.uncovered.size();
                    if (!levels[setInfo.level_idx].setDensity.containsKey(rs.idx) && pre_size > after_size) {
                        levels[setInfo.level_idx].setDensity.put(rs.idx, pre_size - after_size);
                    } else if (pre_size > after_size) {
                        int cov = levels[setInfo.level_idx].setDensity.get(rs.idx);
                        levels[setInfo.level_idx].setDensity.replace(rs.idx, cov + (pre_size - after_size));
                    }
                }
            }

            cov_size += setInfo.cov.size();
        }
    }

//    public void update(TupleOpr t_opr, List<SetOpr> s_oprs) {
//        for (SetOpr s_opr : s_oprs) {
//            if (s_opr.oprType == OprType.T_ADD || s_opr.oprType == OprType.S_ADD) {
//                if (! sets.containsKey(s_opr.t_idx)) {
//                    sets.put(s_opr.t_idx, new HashSet<>());
//                    sets.get(s_opr.t_idx).add(s_opr.u_idx);
//                } else {
//                    sets.get(s_opr.t_idx).add(s_opr.u_idx);
//                }
//            } else if (s_opr.oprType == OprType.T_DEL || s_opr.oprType == OprType.S_DEL) {
//                sets.get(s_opr.t_idx).remove(s_opr.u_idx);
//                if (sets.get(s_opr.t_idx).isEmpty()) {
//                    sets.remove(s_opr.t_idx);
//                }
//            }
//        }
//    }

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
        Set<Integer> levelCov;
        Map<Integer, Integer> setDensity;

        DensityLevel() {
            this.setIdx = new HashSet<>();
            this.levelCov = new HashSet<>();
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
