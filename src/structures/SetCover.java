package structures;

import java.util.*;
import java.util.stream.Collectors;

public class SetCover {
    public int r, m, maxM;
    private Map<Integer, HashSet<Integer>> setSystem;

    private final RMSInst rmsInst;
    private SetCoverInst scInst;

    SetCover(int r, int max_mr, RMSInst mrInst) {
        this.r = r;
        this.rmsInst = mrInst;
        this.maxM = max_mr;
        this.scInst = new SetCoverInst();
    }

    void update(Operations opr) {
        if (opr.utilities.isEmpty())
            return;
        Operations filterOpr = filter(opr);
        if (filterOpr.utilities.isEmpty())
            return;
        updateMapping(filterOpr);

        if (filterOpr.utilities.size() > m / 10.0) {
            scInst = new SetCoverInst(m);
        } else {
            scInst.update(filterOpr);
        }

        if (scInst.sol.size() > r) {
            do {
                scInst.delete(m - 1);
                m -= 1;
            } while (scInst.sol.size() > r);
        } else {
            while (scInst.sol.size() < r && m < maxM) {
                scInst.add(m);
                m += 1;
            }
            if (scInst.sol.size() > r) {
                scInst = new SetCoverInst(m);
                while (scInst.sol.size() > r) {
                    scInst.delete(m - 1);
                    m -= 1;
                }
            }
        }
    }

    private Operations filter(Operations opr) {
        Operations filterOpr = new Operations(opr.t_idx, opr.oprType);
        filterOpr.utilities = opr.utilities.stream().filter(u_idx -> u_idx < m).collect(Collectors.toList());
        filterOpr.oprs = opr.oprs.stream().filter(setOpr -> setOpr.u_idx < m).collect(Collectors.toList());
        return  filterOpr;
    }

    private void updateMapping(Operations opr) {
        if (opr.oprType == 1) {
            setSystem.put(opr.t_idx, new HashSet<>(opr.utilities));
            for (Operations.SetOpr setOpr : opr.oprs)
                setSystem.get(setOpr.t_idx).remove(setOpr.u_idx);
        } else if (opr.oprType == -1) {
            setSystem.remove(opr.t_idx);
            for (Operations.SetOpr setOpr : opr.oprs) {
                if (!setSystem.containsKey(setOpr.t_idx))
                    setSystem.put(setOpr.t_idx, new HashSet<>());
                setSystem.get(setOpr.t_idx).add(setOpr.u_idx);
            }
        }
    }

    public void print() {
        scInst.print();
    }

    public void validate() {
        scInst.validate();
    }

    Set<Integer> result() {
        return scInst.sol.keySet();
    }

    public void printResult() {
        System.out.print("{ ");
        for (int t_idx : scInst.sol.keySet()) {
            System.out.print(t_idx + " ");
        }
        System.out.println("}");
    }

    private class SetCoverInst {
        private final DensityLevel[] levels;

        private final int[] u_assign;
        private final int[] u_level;

        private final Map<Integer, SolInfo> sol;

        private SetCoverInst() {
            int left = r, right = maxM;
            while (right > left) {
                if (left >= right - 1) {
                    if (testSetCover(right)) {
                        m = right;
                        break;
                    } else {
                        m = left;
                        break;
                    }
                }
                int mid = left + (right - left) / 2;
                if (testSetCover(mid))
                    left = mid;
                else
                    right = mid - 1;
            }
            
            if (m == 0)
                m = left;

            setSystem = new HashMap<>();
            for (int u_idx = 0; u_idx < m; u_idx++) {
                for (int t_idx : rmsInst.dualTree.uIdx.topKResults[u_idx].results) {
                    if (!setSystem.containsKey(t_idx)) {
                        setSystem.put(t_idx, new HashSet<>());
                        setSystem.get(t_idx).add(u_idx);
                    } else {
                        setSystem.get(t_idx).add(u_idx);
                    }
                }
            }

            int maxLevel = (int) (Math.log(maxM) / Math.log(2)) + 1;
            levels = new DensityLevel[maxLevel];

            u_assign = new int[maxM];
            u_level = new int[maxM];

            for (int u_idx = m; u_idx < maxM; u_idx++) {
                u_assign[u_idx] = -1;
                u_level[u_idx] = -1;
            }

            sol = new HashMap<>();
            greedySetCover();
        }

        private SetCoverInst(int resetMr) {
            m = resetMr;

            setSystem = new HashMap<>();
            for (int u_idx = 0; u_idx < m; u_idx++) {
                for (int t_idx : rmsInst.dualTree.uIdx.topKResults[u_idx].results) {
                    if (!setSystem.containsKey(t_idx)) {
                        setSystem.put(t_idx, new HashSet<>());
                        setSystem.get(t_idx).add(u_idx);
                    } else {
                        setSystem.get(t_idx).add(u_idx);
                    }
                }
            }

            int maxLevel = (int) (Math.log(maxM) / Math.log(2)) + 1;
            levels = new DensityLevel[maxLevel];

            u_assign = new int[maxM];
            u_level = new int[maxM];

            for (int u_idx = m; u_idx < maxM; u_idx++) {
                u_assign[u_idx] = -1;
                u_level[u_idx] = -1;
            }

            sol = new HashMap<>();
            greedySetCover();
        }

        private boolean testSetCover(int test_size) {
            int cov_size = 0;

            Map<Integer, HashSet<Integer>> test_mapping = new HashMap<>();
            for (int u_idx = 0; u_idx < test_size; u_idx++) {
                for (int t_idx : rmsInst.dualTree.uIdx.topKResults[u_idx].results) {
                    if (!test_mapping.containsKey(t_idx)) {
                        test_mapping.put(t_idx, new HashSet<>());
                        test_mapping.get(t_idx).add(u_idx);
                    } else {
                        test_mapping.get(t_idx).add(u_idx);
                    }
                }
            }

            List<RankSet> rankSetList = new ArrayList<>(test_mapping.size());
            for (Map.Entry<Integer, HashSet<Integer>> entry : test_mapping.entrySet())
                rankSetList.add(new RankSet(entry.getKey(), entry.getValue()));

            Set<Integer> test_sol = new HashSet<>();
            while (cov_size < test_size) {
                rankSetList.sort(new RankSetComparator());
                RankSet next = rankSetList.get(0);
                test_sol.add(next.idx);
                Set<Integer> cov = new HashSet<>(next.uncovered);
                cov_size += cov.size();

                for (RankSet rs : rankSetList) {
                    if (!rs.uncovered.isEmpty()) {
                        rs.uncovered.removeAll(cov);
                    }
                }

                if (test_sol.size() == r && cov_size < test_size) {
                    return false;
                }
            }
            return true;
        }

        private void greedySetCover() {
            int cov_size = 0;

            List<RankSet> rankSetList = new ArrayList<>(setSystem.size());
            for (Map.Entry<Integer, HashSet<Integer>> entry : setSystem.entrySet())
                rankSetList.add(new RankSet(entry.getKey(), entry.getValue()));

            while (cov_size < m) {
                rankSetList.sort(new RankSetComparator());
                RankSet next = rankSetList.get(0);
                SolInfo solInfo = new SolInfo(next);
                sol.put(next.idx, solInfo);

                for (int u_idx : solInfo.cov) {
                    u_assign[u_idx] = next.idx;
                    u_level[u_idx] = solInfo.level_idx;
                }

                if (levels[solInfo.level_idx] == null)
                    levels[solInfo.level_idx] = new DensityLevel(solInfo.level_idx);

                levels[solInfo.level_idx].tuples.add(next.idx);

                for (RankSet rs : rankSetList) {
                    if (!rs.uncovered.isEmpty()) {
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

        private void update(Operations opr) {
            Set<Integer> toBeMoved = new HashSet<>();
            if (opr.oprType == 1) {
                for (Operations.SetOpr setOpr : opr.oprs) {
                    int lv = u_level[setOpr.u_idx];
                    int old_density = levels[lv].density.get(setOpr.t_idx);
                    levels[lv].density.replace(setOpr.t_idx, old_density - 1);
                }

                Set<Integer> unAssigned = new HashSet<>();
                for (Operations.SetOpr setOpr : opr.oprs) {
                    delete(setOpr.t_idx, setOpr.u_idx, opr.t_idx, toBeMoved, unAssigned);
                }
                if (!unAssigned.isEmpty()) {
                    SolInfo solInfo = new SolInfo(unAssigned);
                    sol.put(opr.t_idx, solInfo);

                    if (levels[solInfo.level_idx] == null) {
                        levels[solInfo.level_idx] = new DensityLevel(solInfo.level_idx);
                    }

                    levels[solInfo.level_idx].tuples.add(opr.t_idx);

                    for (int u_idx : unAssigned) {
                        u_assign[u_idx] = opr.t_idx;

                        int old_level = u_level[u_idx];
                        u_level[u_idx] = solInfo.level_idx;
                        int new_level = u_level[u_idx];

                        if (new_level != old_level) {
                            for (int idx : rmsInst.dualTree.uIdx.topKResults[u_idx].results) {
                                if (idx != opr.t_idx) {
                                    levels[old_level].density.replace(idx, levels[old_level].density.get(idx) - 1);

                                    if (!levels[new_level].density.containsKey(idx)) {
                                        levels[new_level].density.put(idx, 1);
                                    } else {
                                        levels[new_level].density.replace(idx, levels[new_level].density.get(idx) + 1);
                                    }
                                }
                            }
                        }
                    }
                }
                for (int u_idx : opr.utilities) {
                    int lv = u_level[u_idx];
                    if (!levels[lv].density.containsKey(opr.t_idx)) {
                        levels[lv].density.put(opr.t_idx, 1);
                    } else {
                        levels[lv].density.replace(opr.t_idx, levels[lv].density.get(opr.t_idx) + 1);
                    }
                }
            } else if (opr.oprType == -1) {
                for (Operations.SetOpr setOpr : opr.oprs) {
                    int lv = u_level[setOpr.u_idx];
                    if (!levels[lv].density.containsKey(setOpr.t_idx)) {
                        levels[lv].density.put(setOpr.t_idx, 1);
                    } else {
                        levels[lv].density.replace(setOpr.t_idx, levels[lv].density.get(setOpr.t_idx) + 1);
                    }
                }

                for (DensityLevel level : levels) {
                    if (level != null) {
                        level.tuples.remove(opr.t_idx);
                        level.density.remove(opr.t_idx);
                    }
                }

                if (sol.containsKey(opr.t_idx))
                    delete(opr.t_idx, toBeMoved);
            }

            for (int t_idx : toBeMoved) {
                move(t_idx);
            }
            stabilize();
        }

        private void delete(int t_idx, int u_idx, int opr_idx, Set<Integer> toBeMoved, Set<Integer> unAssigned) {
            int old_level = u_level[u_idx];
            int s, l;
            if (u_assign[u_idx] == t_idx) {
                sol.get(t_idx).cov.remove(u_idx);

                s = sol.get(t_idx).cov.size();
                l = sol.get(t_idx).level_idx;
                if (s < levels[l].low) {
                    toBeMoved.add(t_idx);
                }

                int new_level = -1, new_idx = -1;
                for (int idx : rmsInst.dualTree.uIdx.topKResults[u_idx].results) {
                    if (sol.containsKey(idx) && sol.get(idx).level_idx > new_level) {
                        new_level = sol.get(idx).level_idx;
                        new_idx = idx;
                    }
                }

                if (new_level < 0) {
                    unAssigned.add(u_idx);
                } else if (new_level == old_level) {
                    sol.get(new_idx).cov.add(u_idx);
                    u_assign[u_idx] = new_idx;

                    s = sol.get(new_idx).cov.size();
                    l = sol.get(new_idx).level_idx;
                    if (s > levels[l].high) {
                        toBeMoved.add(new_idx);
                    }
                } else {
                    sol.get(new_idx).cov.add(u_idx);
                    u_assign[u_idx] = new_idx;
                    u_level[u_idx] = new_level;

                    s = sol.get(new_idx).cov.size();
                    l = sol.get(new_idx).level_idx;
                    if (s > levels[l].high) {
                        toBeMoved.add(new_idx);
                    }

                    for (int idx : rmsInst.dualTree.uIdx.topKResults[u_idx].results) {
                        if (idx != opr_idx) {
                            levels[old_level].density.replace(idx, levels[old_level].density.get(idx) - 1);

                            if (!levels[new_level].density.containsKey(idx))
                                levels[new_level].density.put(idx, 1);
                            else
                                levels[new_level].density.replace(idx, levels[new_level].density.get(idx) + 1);
                        }
                    }
                }
            }
        }

        private void delete(int opr_idx, Set<Integer> toBeMoved) {
            SolInfo solInfo = sol.remove(opr_idx);

            int s, l;
            int old_level = solInfo.level_idx;
            for (int u_idx : solInfo.cov) {
                int new_level = -1, new_idx = -1, cand_size = 0;
                for (int idx : rmsInst.dualTree.uIdx.topKResults[u_idx].results) {
                    if (sol.containsKey(idx) && sol.get(idx).level_idx > new_level) {
                        new_level = sol.get(idx).level_idx;
                        new_idx = idx;
                    } else if (new_level == -1 && setSystem.get(idx).size() > cand_size) {
                        cand_size = setSystem.get(idx).size();
                        new_idx = idx;
                    }
                }

                if (new_level >= 0) {
                    if (new_level == old_level) {
                        sol.get(new_idx).cov.add(u_idx);
                        u_assign[u_idx] = new_idx;

                        s = sol.get(new_idx).cov.size();
                        l = sol.get(new_idx).level_idx;
                        if (s > levels[l].high) {
                            toBeMoved.add(new_idx);
                        }
                    } else {
                        sol.get(new_idx).cov.add(u_idx);
                        u_assign[u_idx] = new_idx;
                        u_level[u_idx] = new_level;

                        s = sol.get(new_idx).cov.size();
                        l = sol.get(new_idx).level_idx;
                        if (s > levels[l].high) {
                            toBeMoved.add(new_idx);
                        }

                        for (int idx : rmsInst.dualTree.uIdx.topKResults[u_idx].results) {
                            levels[old_level].density.replace(idx, levels[old_level].density.get(idx) - 1);

                            if (!levels[new_level].density.containsKey(idx))
                                levels[new_level].density.put(idx, 1);
                            else
                                levels[new_level].density.replace(idx, levels[new_level].density.get(idx) + 1);
                        }
                    }
                } else {
                    SolInfo newSolInfo = new SolInfo(u_idx);
                    sol.put(new_idx, newSolInfo);

                    if (levels[newSolInfo.level_idx] == null) {
                        levels[newSolInfo.level_idx] = new DensityLevel(newSolInfo.level_idx);
                    }

                    levels[newSolInfo.level_idx].tuples.add(new_idx);

                    u_assign[u_idx] = new_idx;
                    u_level[u_idx] = newSolInfo.level_idx;
                    new_level = u_level[u_idx];

                    if (new_level != old_level) {
                        for (int idx : rmsInst.dualTree.uIdx.topKResults[u_idx].results) {
                            levels[old_level].density.replace(idx, levels[old_level].density.get(idx) - 1);

                            if (!levels[new_level].density.containsKey(idx)) {
                                levels[new_level].density.put(idx, 1);
                            } else {
                                levels[new_level].density.replace(idx, levels[new_level].density.get(idx) + 1);
                            }
                        }
                    }
                }
            }
        }

        private void delete(int u_idx) {
            int t_idx = u_assign[u_idx];
            int lv = u_level[u_idx];

            u_assign[u_idx] = -1;
            u_level[u_idx] = -1;

            for (int idx : rmsInst.dualTree.uIdx.topKResults[u_idx].results) {
            	setSystem.get(idx).remove(u_idx);
                levels[lv].density.replace(idx, levels[lv].density.get(idx) - 1);
            }
            sol.get(t_idx).cov.remove(u_idx);

            int s = sol.get(t_idx).cov.size(), l = sol.get(t_idx).level_idx;
            if (s < levels[l].low) {
                move(t_idx);
                stabilize();
            }
        }

        private void add(int u_idx) {
            int new_level = -1;
            int new_idx = -1;

            for (int idx : rmsInst.dualTree.uIdx.topKResults[u_idx].results) {
                if (sol.containsKey(idx) && sol.get(idx).level_idx > new_level) {
                    new_level = sol.get(idx).level_idx;
                    new_idx = idx;
                }

                if (!setSystem.containsKey(idx)) {
                    setSystem.put(idx, new HashSet<>());
                    setSystem.get(idx).add(u_idx);
                } else {
                    setSystem.get(idx).add(u_idx);
                }
            }

            if (new_level >= 0) {
                for (int idx : rmsInst.dualTree.uIdx.topKResults[u_idx].results) {
                    if (!levels[new_level].density.containsKey(idx)) {
                        levels[new_level].density.put(idx, 1);
                    } else {
                        levels[new_level].density.replace(idx, levels[new_level].density.get(idx) + 1);
                    }
                }

                u_assign[u_idx] = new_idx;
                u_level[u_idx] = new_level;

                sol.get(new_idx).cov.add(u_idx);

                int s = sol.get(new_idx).cov.size(), l = sol.get(new_idx).level_idx;
                if (s > levels[l].high) {
                    move(new_idx);
                    stabilize();
                }
            } else {
                int cand_size = 0;
                for (int idx : rmsInst.dualTree.uIdx.topKResults[u_idx].results) {
                    if (setSystem.get(idx).size() > cand_size) {
                        cand_size = setSystem.get(idx).size();
                        new_idx = idx;
                    }
                }

                SolInfo newSolInfo = new SolInfo(u_idx);
                sol.put(new_idx, newSolInfo);

                new_level = newSolInfo.level_idx;

                if (levels[new_level] == null) {
                    levels[new_level] = new DensityLevel(new_level);
                }

                levels[new_level].tuples.add(new_idx);

                u_assign[u_idx] = new_idx;
                u_level[u_idx] = new_level;

                for (int idx : rmsInst.dualTree.uIdx.topKResults[u_idx].results) {
                    if (!levels[new_level].density.containsKey(idx)) {
                        levels[new_level].density.put(idx, 1);
                    } else {
                        levels[new_level].density.replace(idx, levels[new_level].density.get(idx) + 1);
                    }
                }
            }
        }

        private void move(int t_idx) {
            if (sol.get(t_idx).cov.isEmpty()) {
                SolInfo solInfo = sol.remove(t_idx);
                levels[solInfo.level_idx].tuples.remove(t_idx);
                return;
            }

            int old_level = sol.get(t_idx).level_idx;
            int new_level = (int) Math.floor(Math.log(sol.get(t_idx).cov.size()) / Math.log(2));

            if (old_level == new_level)
                return;

            sol.get(t_idx).level_idx = new_level;

            if (levels[new_level] == null) {
                levels[new_level] = new DensityLevel(new_level);
            }

            levels[old_level].tuples.remove(t_idx);
            levels[new_level].tuples.add(t_idx);

            for (int u_idx : sol.get(t_idx).cov) {
                u_level[u_idx] = new_level;

                for (int idx : rmsInst.dualTree.uIdx.topKResults[u_idx].results) {
                    levels[old_level].density.replace(idx, levels[old_level].density.get(idx) - 1);

                    if (!levels[new_level].density.containsKey(idx)) {
                        levels[new_level].density.put(idx, 1);
                    } else {
                        levels[new_level].density.replace(idx, levels[new_level].density.get(idx) + 1);
                    }
                }
            }
        }

        private void stabilize() {
            int unstable_level = levels.length - 1;
            while (unstable_level >= 0) {
                int unstable_idx = -1;
                for (int level_idx = unstable_level; level_idx >= 0; level_idx--) {
                    if (levels[level_idx] == null)
                        continue;
                    for (Map.Entry<Integer, Integer> entry : levels[level_idx].density.entrySet()) {
                        if (entry.getValue() > levels[level_idx].high) {
                            unstable_level = level_idx;
                            unstable_idx = entry.getKey();
                            break;
                        }
                    }
                    if (unstable_idx >= 0) {
                        break;
                    }
                }

                if (unstable_idx == -1) {
                    unstable_level = -1;
                } else {
                    Set<Integer> add_cov = new HashSet<>();
                    for (int t_idx : levels[unstable_level].tuples) {
                        if (t_idx == unstable_idx)
                            continue;
                        for (int u_idx : sol.get(t_idx).cov) {
                            if (setSystem.get(unstable_idx).contains(u_idx)) {
                                add_cov.add(u_idx);
                            }
                        }
                    }

                    Set<Integer> canMove = new HashSet<>();

                    if (!sol.containsKey(unstable_idx)) {
                        SolInfo solInfo = new SolInfo(add_cov);
                        sol.put(unstable_idx, solInfo);

                        if (levels[solInfo.level_idx] == null) {
                            levels[solInfo.level_idx] = new DensityLevel(solInfo.level_idx);
                        }

                        levels[solInfo.level_idx].tuples.add(unstable_idx);

                        for (int u_idx : add_cov) {
                            int old_idx = u_assign[u_idx];
                            int old_level = u_level[u_idx];

                            sol.get(old_idx).cov.remove(u_idx);

                            u_assign[u_idx] = unstable_idx;
                            u_level[u_idx] = solInfo.level_idx;

                            int new_level = solInfo.level_idx;

                            int s = sol.get(old_idx).cov.size(), l = sol.get(old_idx).level_idx;
                            if (s < levels[l].low) {
                                canMove.add(old_idx);
                            }

                            for (int idx : rmsInst.dualTree.uIdx.topKResults[u_idx].results) {
                                levels[old_level].density.replace(idx, levels[old_level].density.get(idx) - 1);

                                if (!levels[new_level].density.containsKey(idx)) {
                                    levels[new_level].density.put(idx, 1);
                                } else {
                                    levels[new_level].density.replace(idx, levels[new_level].density.get(idx) + 1);
                                }
                            }
                        }
                    } else {
                        int new_level = sol.get(unstable_idx).level_idx;

                        for (int u_idx : add_cov) {
                            sol.get(unstable_idx).cov.add(u_idx);

                            int old_idx = u_assign[u_idx];
                            int old_level = u_level[u_idx];

                            sol.get(old_idx).cov.remove(u_idx);

                            u_assign[u_idx] = unstable_idx;
                            u_level[u_idx] = new_level;

                            int s = sol.get(old_idx).cov.size(), l = sol.get(old_idx).level_idx;
                            if (s < levels[l].low) {
                                canMove.add(old_idx);
                            }

                            if (new_level == old_level)
                                continue;

                            for (int idx : rmsInst.dualTree.uIdx.topKResults[u_idx].results) {
                                levels[old_level].density.replace(idx, levels[old_level].density.get(idx) - 1);

                                if (!levels[new_level].density.containsKey(idx)) {
                                    levels[new_level].density.put(idx, 1);
                                } else {
                                    levels[new_level].density.replace(idx, levels[new_level].density.get(idx) + 1);
                                }
                            }
                        }

                        int s = sol.get(unstable_idx).cov.size(), l = sol.get(unstable_idx).level_idx;
                        if (s > levels[l].high)
                            canMove.add(unstable_idx);
                    }
                    for (int t_idx : canMove) {
                        move(t_idx);
                    }
                    unstable_level = sol.get(unstable_idx).level_idx;
                }
            }
        }

        private void print() {
            System.out.println("Solution:");
            for (int t_idx : sol.keySet()) {
                System.out.println(t_idx + "," + sol.get(t_idx).level_idx + "," + sol.get(t_idx).cov.size());
            }
            System.out.println();

            System.out.println("Density Levels:");
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
            System.out.println();
        }

        private void validate() {
            int total_cov = 0;
            for (int t_idx : sol.keySet()) {
                total_cov += sol.get(t_idx).cov.size();
                int level_idx = sol.get(t_idx).level_idx;
                assert levels[level_idx].tuples.contains(t_idx) : "Error in level.tuples";
                for (int u_idx : sol.get(t_idx).cov) {
                    assert u_assign[u_idx] == t_idx : "Error in u_assign";
                    assert u_level[u_idx] == level_idx : "Error in u_level";
                }
            }
            assert total_cov == m : "Error in sol.cov";

            for (DensityLevel level : levels) {
                if (level != null && level.tuples.isEmpty()) {
                    for (int t_idx : level.density.keySet()) {
                        assert level.density.get(t_idx) == 0 : "Error in level.density";
                    }
                } else if (level != null) {
                    Set<Integer> levelCov = new HashSet<>();
                    for (int t_idx : level.tuples) {
                        levelCov.addAll(sol.get(t_idx).cov);
                    }
                    for (int t_idx : level.density.keySet()) {
                        Set<Integer> intersect = new HashSet<>(levelCov);
                        intersect.retainAll(setSystem.get(t_idx));
                        assert level.density.get(t_idx) == intersect.size() : "Error in level.density";
                    }
                }
            }

            System.out.println("No error found");
        }

        private class RankSet {
            private final int idx;
            private final int set_size;
            private final Set<Integer> uncovered;

            private RankSet(int idx, Set<Integer> set) {
                this.idx = idx;
                this.set_size = set.size();
                this.uncovered = new HashSet<>(set);
            }
        }

        private class RankSetComparator implements Comparator<RankSet> {

            @Override
            public int compare(RankSet rs1, RankSet rs2) {
                if (rs1.uncovered.size() > rs2.uncovered.size())
                    return -1;
                else if (rs1.uncovered.size() < rs2.uncovered.size())
                    return 1;
                else
                    return (rs2.set_size - rs1.set_size);
            }
        }

        private class DensityLevel {
            private final int low;
            private final int high;
            private final Set<Integer> tuples;
            private final Map<Integer, Integer> density;

            private DensityLevel(int level_idx) {
                this.low = (int) Math.pow(2, level_idx);
                this.high = (int) (Math.pow(2, level_idx + 1) - 1);

                this.tuples = new HashSet<>();
                this.density = new HashMap<>();
            }
        }

        private class SolInfo {
            private int level_idx;
            private final Set<Integer> cov;

            private SolInfo(RankSet rankSet) {
                this.level_idx = (int) Math.floor(Math.log(rankSet.uncovered.size()) / Math.log(2));
                this.cov = new HashSet<>(rankSet.uncovered);
            }

            private SolInfo(Set<Integer> cov) {
                this.level_idx = (int) Math.floor(Math.log(cov.size()) / Math.log(2));
                this.cov = new HashSet<>(cov);
            }

            private SolInfo(int u_idx) {
                this.level_idx = 0;
                this.cov = new HashSet<>();
                this.cov.add(u_idx);
            }
        }
    }
}
