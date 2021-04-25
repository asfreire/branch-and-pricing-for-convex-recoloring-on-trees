package columnGenerationForPaths;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.util.LinkedList;

public class Separation {

	MasterLP masterLP;
	ColumnGeneration branchAndPrice;
	InputData input;
	Configuration config;
	IloCplex cplex;
	
	public Separation(MasterLP masterLP, ColumnGeneration branchAndPrice, IloCplex cplex) {
		this.masterLP = masterLP;
		this.branchAndPrice = branchAndPrice;
		this.input = branchAndPrice.input;
		config = masterLP.config;
		this.cplex = cplex;
	}

	@SuppressWarnings("unchecked")
	LinkedList<Column>[] getNewArrayOfLists(int n) {
		LinkedList<Column>[] list =  new LinkedList[n];
		
		for(int i = 0; i < n; i++) {
			list[i] = new LinkedList<Column>();
		}
		
		return list;
	}
	
	public Cut findCut(int c) throws IloException {
		LinkedList<Column> nonzeroCols_c = new LinkedList<Column>();
		LinkedList<Column> nonzeroCols_not_c = new LinkedList<Column>();
		
		for(Column col : masterLP.addedCols) {
			if(cplex.getValue(col.var) > 0.001) {
				if(col.path.c == c) {
					nonzeroCols_c.add(col);
				} else {
					nonzeroCols_not_c.add(col);
				}
			}
		}
		
		double maxLHS = 0;
		Path maxLHSPath = null;
		Path p = new Path(0, 0, c);
		
		for(p.s = 0; p.s < input.nVertices; p.s++) {
			for(p.t = p.s; p.t < input.nVertices; p.t++) {
				double LHS = 0;
				
				for(Column col : nonzeroCols_c) {
					if(col.path.intersects(p)) {
						LHS += cplex.getValue(col.var);
					}
				}

				for(Column col : nonzeroCols_not_c) {
					if(col.path.contains(p)) {
						LHS += cplex.getValue(col.var);
					}
				}

				if(LHS > maxLHS) {
					maxLHS = LHS;
					maxLHSPath = new Path(p);
				}
			}
		}
		
		if(maxLHS <= 1.000001) {
			return null;
		}
		
		System.out.println("Cut " + maxLHSPath + " " + maxLHS);
		return new Cut(maxLHSPath);
	}
}
