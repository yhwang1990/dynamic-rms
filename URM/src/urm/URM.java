package urm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import index.DualTree;
import index.RankItem;
import index.VectorUtil;

import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

public class URM {
	
	public DualTree dualTree;
	
	public int dim, r;
	public Set<Integer> result;
    
    public double initTime = 0.0, addTime = 0.0, delTime = 0.0;
    
    private List<Integer> skylines;

    public URM(int dim, int r, int data_size, int init_size, int sample_size, double[][] data, double[][] samples) {
        long t0 = System.nanoTime();
        
        this.dim = dim;
        this.r = r;
        
        this.dualTree = new DualTree(dim, data_size, init_size, sample_size, data, samples);
        
        this.result = new HashSet<>();
        this.skylines = new ArrayList<>();
        this.getSkyline();
        
        this.initialize();
        this.KMedoid();
        
        long t1 = System.nanoTime();
        
        this.initTime += (t1 - t0) / 1e6;
    }

    public void update(TupleOpr opr, boolean forceUpdate) {
    	long t1, t2;
        if (opr.oprType > 0) {
            dualTree.insert(opr.t_idx);
            boolean isSkyline = updateSkyline(opr.t_idx);

            t1 = System.nanoTime();
            if (isSkyline) {
            	int rDominated = -1;
            	for (int r_idx : result) {
            		if (dominate(dualTree.data[opr.t_idx], dualTree.data[r_idx])) {
            			rDominated = r_idx;
            			break;
            		}
            	}
            	if (rDominated >= 0) {
            		System.out.println("insert " + opr.t_idx);
            		result.remove(rDominated);
            		result.add(opr.t_idx);
            		KMedoid();
            	}
            }
            if (forceUpdate) {
            	System.out.println("insert " + opr.t_idx);
        		KMedoid();
            }
            t2 = System.nanoTime();
        } else {
            dualTree.delete(opr.t_idx);
            t1 = System.nanoTime();
            if (result.contains(opr.t_idx)) {
            	System.out.println("delete " + opr.t_idx);
            	result.remove(opr.t_idx);
            	getSkyline();
            	Map<Integer, Integer> idx2cnt = new HashMap<>();
            	for (RankItem item : dualTree.topResults) {
            		if (!idx2cnt.containsKey(item.t_idx)) {
            			idx2cnt.put(item.t_idx, 1);
            		} else {
            			idx2cnt.put(item.t_idx, idx2cnt.get(item.t_idx) + 1);
            		}
            	}
            	
            	List<RankTuple> rankTuples = new ArrayList<>();
        		for (Entry<Integer, Integer> entry : idx2cnt.entrySet()) {
        			rankTuples.add(new RankTuple(entry.getKey(), entry.getValue()));
        		}
        		
        		Collections.sort(rankTuples);
        		
        		for (int i = 0; i < rankTuples.size(); ++i) {
    				if (!result.contains(rankTuples.get(i).idx)) {
    					result.add(rankTuples.get(i).idx);
    					if (result.size() >= r)
    						break;
    				}
        		}
        		
        		KMedoid();
            }
            t2 = System.nanoTime();
        }
        
        if (opr.oprType > 0)
            addTime += (t2 - t1) / 1e6;
        else
            delTime += (t2 - t1) / 1e6;
    }
    
    private void initialize() {
    	Map<Integer, Integer> idx2cnt = new HashMap<>();
    	for (RankItem item : dualTree.topResults) {
    		if (!idx2cnt.containsKey(item.t_idx)) {
    			idx2cnt.put(item.t_idx, 1);
    		} else
    			idx2cnt.put(item.t_idx, idx2cnt.get(item.t_idx) + 1);
    	}
    	
    	List<RankTuple> rankTuples = new ArrayList<>();
    	for (Entry<Integer, Integer> entry : idx2cnt.entrySet())
    		rankTuples.add(new RankTuple(entry.getKey(), entry.getValue()));
    		
    	Collections.sort(rankTuples);
    		
    	for (int i = 0; i < r; ++i)
			result.add(rankTuples.get(i).idx);
    }
    
    private void KMedoid() {
    	int iter = 0;
    	double rr = 1.0;
    	while (true) {
    		double new_rr = 0.0;
    		Set<Integer> newResult = new HashSet<>();
    		boolean isUpdated = false;
    		for (int idx : result) {
    			IdxRR ir = maxO(idx);
    			int newIdx = ir.idx;
    			new_rr = Math.max(new_rr, ir.rr);
    			if (newIdx < 0) {
    				newIdx = idx;
    			}
    			newResult.add(newIdx);
    			if (!result.contains(newIdx)) {
    				isUpdated = true;
    			}
    		}
    		
    		++iter;
    		System.out.println("Iteration " + iter + " " + new_rr);
    		
    		if (!isUpdated || rr < new_rr || iter == 10)
    			break;
    		
    		result.clear();
    		result.addAll(newResult);
    		rr = new_rr;
    	}
    }
    
    private IdxRR maxO(int idx) {
    	List<Integer> tuple_samples = getSamples(idx);
    	if (tuple_samples.isEmpty()) {
    		Map<Integer, Integer> idx2cnt = new HashMap<>();
        	for (RankItem item : dualTree.topResults) {
        		if (!idx2cnt.containsKey(item.t_idx)) {
        			idx2cnt.put(item.t_idx, 1);
        		} else {
        			idx2cnt.put(item.t_idx, idx2cnt.get(item.t_idx) + 1);
        		}
        	}
        	
        	List<RankTuple> rankTuples = new ArrayList<>();
    		for (Entry<Integer, Integer> entry : idx2cnt.entrySet()) {
    			rankTuples.add(new RankTuple(entry.getKey(), entry.getValue()));
    		}
    		
    		Collections.sort(rankTuples);
    		
    		for (int i = 0; i < rankTuples.size(); ++i) {
				if (!result.contains(rankTuples.get(i).idx))
					return new IdxRR(rankTuples.get(i).idx, 0.0);
    		}
    		return new IdxRR(-1, 0.0);
    	} else {
    		int newIdx = -1;
    		List<RankLB> sortedLB = sortedLB(tuple_samples);
    		
    		double tau = 1.0;
    		for (RankLB rankLB : sortedLB) {
    			if (rankLB.lb > tau)
    				break;
    			boolean canBeSkipped = false;
    			double rr = 0.0;
    			for (int s_idx : skylines) {
    				if (s_idx == rankLB.idx)
    					continue;
    				double weight = linearProgram(rankLB.idx, s_idx, idx);
    				if (weight > rr)
    					rr = weight;
    				if (rr >= tau) {
    					canBeSkipped = true;
    					break;
    				}
    			}
    			if (canBeSkipped)
    				continue;
    			else {
    				tau = rr;
    				newIdx = rankLB.idx;
    			}
    		}
//    		System.out.println(idx + ":" + newIdx + "," + tau);
    		return new IdxRR(newIdx, tau);
    	}
	}
    
    private double linearProgram(int t_idx, int t2_idx, int idx) {
    	double[] objTerms = new double[dim + 1];
    	for (int i = 0; i < dim; ++i)
    		objTerms[i] = 0;
    	objTerms[dim] = 1.0;
    	LinearObjectiveFunction obj = new LinearObjectiveFunction(objTerms, 0);
    	
    	Collection<LinearConstraint> constraints = new ArrayList<>();
    	
//    	double[][] rangeTerms = new double[dim + 1][dim + 1];
//    	for (int i = 0; i < dim + 1; ++i) {
//    		for (int j = 0; j < dim + 1; ++j) {
//    			if (i == j)
//    				rangeTerms[i][j] = 1.0;
//    			else
//    				rangeTerms[i][j] = 0;
//    		}
//    	}
//    	for (int i = 0; i < dim + 1; ++i)
//    		constraints.add(new LinearConstraint(rangeTerms[i], Relationship.GEQ, 0));
    	
    	double[] normTerms = new double[dim + 1];
    	for (int i = 0; i < dim; ++i)
    		normTerms[i] = dualTree.data[t2_idx][i];
    	normTerms[dim] = 0;
    	constraints.add(new LinearConstraint(normTerms, Relationship.EQ, 1));
    	
    	double[] regretTerms = new double[dim + 1];
    	for (int i = 0; i < dim; ++i)
    		regretTerms[i] = dualTree.data[t2_idx][i] - dualTree.data[t_idx][i];
    	regretTerms[dim] = -1;
    	constraints.add(new LinearConstraint(regretTerms, Relationship.GEQ, 0));
		
    	double[][] hyperplanes = new double[result.size() - 1][dim + 1];
    	int cnt = 0;
    	for (int r_idx : result) {
    		if (r_idx == idx)
    			continue;
        	for (int i = 0; i < dim; ++i)
        		hyperplanes[cnt][i] = dualTree.data[idx][i] - dualTree.data[r_idx][i];
        	hyperplanes[cnt][dim] = 0;
        	++cnt;
    	}
    	for (int i = 0; i < result.size() - 1; ++i)
    		constraints.add(new LinearConstraint(hyperplanes[i], Relationship.GEQ, 0));
    	
    	PointValuePair solution = null;
    	try {
    		solution = new SimplexSolver().optimize(obj, new LinearConstraintSet(constraints), GoalType.MAXIMIZE, new NonNegativeConstraint(true), new MaxIter(100));
    	} catch (NoFeasibleSolutionException e) {
    		return 0;
    	} catch (UnboundedSolutionException e) {
    		return 0;
    	} catch (TooManyIterationsException e) {
    		return 0;
    	}
		
		
		if (solution != null)
			return solution.getValue();
    	
		return 0;
	}

	private List<Integer> getSamples(int idx) {
    	List<Integer> tuple_samples = new ArrayList<>();
    	for (int u_idx = 0; u_idx < dualTree.topResults.length; ++u_idx) {
    		if (dualTree.topResults[u_idx].t_idx == idx)
    			tuple_samples.add(u_idx);
    		if (tuple_samples.size() >= 10)
    			break;
    	}
    	
    	if (tuple_samples.isEmpty()) {
    		for (int u_idx = 0; u_idx < dualTree.topResults.length; ++u_idx) {
    			boolean isValid = true;
        		for (int idx2 : result) {
        			double score1 = VectorUtil.inner_product(dualTree.data[idx], dualTree.samples[u_idx]);
        			double score2 = VectorUtil.inner_product(dualTree.data[idx2], dualTree.samples[u_idx]);
        			if (score1 < score2) {
        				isValid = false;
        				break;
        			}
        		}
        		if (isValid) {
        			tuple_samples.add(u_idx);
        			if (tuple_samples.size() >= 2)
            			break;
        		}
    		}
    	}
    	return tuple_samples;
    }
    
    private List<RankLB> sortedLB(List<Integer> tuple_samples) {
    	Map<Integer,Double> mapLB = new HashMap<>();
    	for (int s_idx : skylines)
    		mapLB.put(s_idx, 0.0);
    	for (int u_idx : tuple_samples) {
    		for (int t_idx : skylines) {
    			double score = VectorUtil.inner_product(dualTree.data[t_idx], dualTree.samples[u_idx]);
    			mapLB.put(t_idx, Math.max(mapLB.get(t_idx), 1.0 - score / dualTree.topResults[u_idx].score));
    		}
    	}
    	List<RankLB> sortedLB = new ArrayList<>();
    	for (int t_idx : skylines)
    		sortedLB.add(new RankLB(t_idx, mapLB.get(t_idx)));
    	Collections.sort(sortedLB);
    	return sortedLB;
    }
    
    private void getSkyline() {
    	skylines.clear();
        for (int idx = 0; idx < dualTree.data.length; idx++) {
        	if (dualTree.isDeleted[idx])
        		continue;
        	
            boolean dominated = false;
            boolean i_dominate = false;
            List<Integer> deleted = new ArrayList<>();
            for (int s_idx : skylines) {
                if (dominate(dualTree.data[s_idx], dualTree.data[idx])) {
                    dominated = true;
                    break;
                }

                if (dominate(dualTree.data[idx], dualTree.data[s_idx])) {
                    i_dominate = true;
                    deleted.add(s_idx);
                }
            }
            assert(!(i_dominate && dominated));
            
            if (!deleted.isEmpty())
            	skylines.removeAll(deleted);

            if (!dominated)
                skylines.add(idx);
        }
    }
    
    private boolean updateSkyline(int idx) {
    	boolean dominated = false;
        boolean i_dominate = false;
        List<Integer> deleted = new ArrayList<>();
        for (int s_idx : skylines) {
            if (dominate(dualTree.data[s_idx], dualTree.data[idx])) {
                dominated = true;
                break;
            }

            if (dominate(dualTree.data[idx], dualTree.data[s_idx])) {
                i_dominate = true;
                deleted.add(s_idx);
            }
        }
        assert(!(i_dominate && dominated));
            
        if (!deleted.isEmpty())
            skylines.removeAll(deleted);

        if (!dominated) {
        	skylines.add(idx);
        	return true;
        }
        return false;
    }
    
    private boolean dominate(double[] p1, double[] p2) {
      assert(p1.length == p2.length);
      boolean at_least_one = false;
      for(int i = 0; i < dim; i++) {
        if(p1[i] < p2[i])
          return false;
        if(p1[i] > p2[i])
          at_least_one = true;
      }
      return at_least_one;
    }

	class RankTuple implements Comparable<RankTuple> {
    	int idx;
    	int cnt;
		
    	RankTuple(int idx, int cnt) {
    		this.idx = idx;
    		this.cnt = cnt;
    	}
    	
    	@Override
		public int compareTo(RankTuple o) {
			if (this.cnt > o.cnt)
				return -1;
			else if (this.cnt < o.cnt)
				return 1;
			else
				return (this.idx - o.idx);
		}
    }
	
	class RankLB implements Comparable<RankLB> {
    	int idx;
    	double lb;
		
    	RankLB(int idx, double lb) {
    		this.idx = idx;
    		this.lb = lb;
    	}
    	
    	@Override
		public int compareTo(RankLB o) {
			if (this.lb < o.lb)
				return -1;
			else if (this.lb > o.lb)
				return 1;
			else
				return (this.idx - o.idx);
		}
    }
	
	class IdxRR {
		int idx;
		double rr;
		
		IdxRR(int idx, double rr) {
    		this.idx = idx;
    		this.rr = rr;
    	}
	}
}
