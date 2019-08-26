package structures;

import utils.OprType;
import utils.Parameter;

import java.util.*;

public class SetCover {
    public Map<Integer, HashSet<Integer>> sets;

    private DensityLevel[] levels;

    private int[] u_assign;
    private int[] u_level;

    private MinSizeRMS instance;

    private Map<Integer, SolInfo> sol;

    SetCover(MinSizeRMS inst) {
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

        int maxLevel = (int) (Math.log(Parameter.SAMPLE_SIZE) / Math.log(2)) + 1;
        this.levels = new DensityLevel[maxLevel];

        this.u_assign = new int[Parameter.SAMPLE_SIZE];
        this.u_level = new int[Parameter.SAMPLE_SIZE];

        this.sol = new HashMap<>();
        this.greedySetCover();
    }

    private void greedySetCover() {
        int cov_size = 0;

        List<RankSet> rankSetList = new ArrayList<>(sets.size());
        for (Map.Entry<Integer, HashSet<Integer>> entry : sets.entrySet()) {
            rankSetList.add(new RankSet(entry.getKey(), entry.getValue()));
        }

        while (cov_size < Parameter.SAMPLE_SIZE) {
            rankSetList.sort(new RankSetComparator());
            RankSet next = rankSetList.get(0);
            SolInfo solInfo = new SolInfo(next);
            sol.put(next.idx, solInfo);

            for (int u_idx : solInfo.cov) {
                u_assign[u_idx] = next.idx;
                u_level[u_idx] = solInfo.level_idx;
            }

            if (levels[solInfo.level_idx] == null) {
                levels[solInfo.level_idx] = new DensityLevel(solInfo.level_idx);
            }

            levels[solInfo.level_idx].tuples.add(next.idx);
            levels[solInfo.level_idx].levelCov.addAll(solInfo.cov);

            for (RankSet rs : rankSetList) {
                if (! rs.uncovered.isEmpty()) {
                    int pre_size = rs.uncovered.size();
                    rs.uncovered.removeAll(solInfo.cov);
                    int after_size = rs.uncovered.size();

                    if (!levels[solInfo.level_idx].density.containsKey(rs.idx) && pre_size > after_size) {
                        levels[solInfo.level_idx].density.put(rs.idx, pre_size - after_size);
                    } else if (pre_size > after_size) {
                        int cov = levels[solInfo.level_idx].density.get(rs.idx);
                        levels[solInfo.level_idx].density.replace(rs.idx, cov + (pre_size - after_size));
                    }
                }
            }

            cov_size += solInfo.cov.size();
        }
    }

    public void update(Operations opr) {
        if (opr.utilities.isEmpty())
            return;
        updateSets(opr);
        if (opr.oprType == OprType.ADD) {
            Set<Integer> unassigned = new HashSet<>();
            for (Operations.SetOpr setOpr : opr.oprs) {
                boolean isAssigned = delete(setOpr.t_idx, setOpr.u_idx);
                if (!isAssigned)
                    unassigned.add(setOpr.u_idx);
            }
            if (!unassigned.isEmpty()) {
                SolInfo solInfo = new SolInfo(unassigned);
                sol.put(opr.t_idx, solInfo);
                levels[solInfo.level_idx].tuples.add(opr.t_idx);
            }
        } else if (opr.oprType == OprType.DEL) {

        }
    }

    private void updateSets(Operations opr) {
        if (opr.oprType == OprType.ADD) {
            sets.put(opr.t_idx, new HashSet<>(opr.utilities));
            for (Operations.SetOpr setOpr : opr.oprs) {
                sets.get(setOpr.t_idx).remove(setOpr.u_idx);
            }
        } else if (opr.oprType == OprType.DEL) {
            sets.remove(opr.t_idx);
            for (Operations.SetOpr setOpr : opr.oprs) {
                if (!sets.containsKey(setOpr.t_idx)) {
                    sets.put(setOpr.t_idx, new HashSet<>());
                }
                sets.get(setOpr.t_idx).add(setOpr.u_idx);
            }
        }
    }

    private void insert(int t_idx, int u_idx) {

    }

    private boolean delete(int t_idx, int u_idx) {
        if (u_assign[u_idx] != t_idx) {
            int level_idx = u_level[u_idx];
            int density = levels[level_idx].density.get(t_idx);
            levels[level_idx].density.replace(t_idx, density - 1);
            return true;
        } else {
            sol.get(t_idx).cov.remove(u_idx);
            u_assign[u_idx] = -1;

            int old_level = u_level[u_idx];
            int new_level = -1, new_idx = -1;
            for (int idx : instance.dualTree.uIdx.topKResults[u_idx].results) {
                if (sol.containsKey(idx) && sol.get(idx).level_idx > new_level) {
                    new_level = sol.get(idx).level_idx;
                    new_idx = idx;
                }
            }

            if (new_level < old_level) {
                u_level[u_idx] = -1;
                levels[old_level].levelCov.remove(u_idx);
                for (int idx : instance.dualTree.uIdx.topKResults[u_idx].results) {
                    levels[old_level].density.replace(idx, levels[old_level].density.get(idx) - 1);
                }
                return false;
            } else if (new_level == old_level) {
                sol.get(new_idx).cov.add(u_idx);
                u_assign[u_idx] = new_idx;
                return true;
            } else {
                sol.get(new_idx).cov.add(u_idx);
                u_assign[u_idx] = new_idx;
                u_level[u_idx] = new_level;

                levels[old_level].levelCov.remove(u_idx);
                levels[new_level].levelCov.add(u_idx);

                for (int idx : instance.dualTree.uIdx.topKResults[u_idx].results) {
                    levels[old_level].density.replace(idx, levels[old_level].density.get(idx) - 1);
                    if (!levels[new_level].density.containsKey(idx)) {
                        levels[new_level].density.put(idx, 1);
                    } else {
                        levels[new_level].density.replace(idx, levels[new_level].density.get(idx) + 1);
                    }
                }
                return true;
            }
        }
    }

    private void move(int t_idx) {

    }

    private void stabilize() {

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
                for (int idx : levels[i].tuples) {
                    System.out.print(idx + " ");
                }
                System.out.println();
                for (Map.Entry<Integer, Integer> entry : levels[i].density.entrySet()) {
                    System.out.print(entry.getKey() + "," + entry.getValue() + " ");
                }
                System.out.println();
            }
        }
    }

    public Set<Integer> result() {
        return sol.keySet();
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
        int low, high;
        Set<Integer> tuples;
        Set<Integer> levelCov;
        Map<Integer, Integer> density;

        DensityLevel(int level_idx) {
            this.low = (int) Math.pow(2, level_idx);
            this.high = (int) (Math.pow(2, level_idx + 1) - 1);

            this.tuples = new HashSet<>();
            this.levelCov = new HashSet<>();
            this.density = new HashMap<>();
        }
    }

    private static class SolInfo {
        int level_idx;
        Set<Integer> cov;

        SolInfo(RankSet rankSet) {
            this.level_idx = (int) Math.floor(Math.log(rankSet.uncovered.size()) / Math.log(2));
            this.cov = new HashSet<>(rankSet.uncovered);
        }

        SolInfo(Set<Integer> cov) {
            this.level_idx = (int) Math.floor(Math.log(cov.size()) / Math.log(2));
            this.cov = new HashSet<>(cov);
        }
    }
}
