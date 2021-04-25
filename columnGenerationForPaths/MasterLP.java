package columnGenerationForPaths;

import ilog.concert.IloColumn;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.LinkedList;

import columnGeneration.Subtree;

public class MasterLP {
	IloRange[] vertexConstraints;
	IloRange[] colorConstraints;
	IloObjective obj;
	IloCplex cplex;
	LinkedList<Column> addedCols = new LinkedList<Column>();
	LinkedList<Cut> addedCuts = new LinkedList<Cut>();
	ColumnGeneration cg;
	Pricing pricing;
	Separation separation;
	InputData input;
	Configuration config;

	// dual variables
	double[] alpha; // vertex constraints
	double[] beta; // color constraints

	MasterLP(ColumnGeneration cg) throws Exception {
		this.cg = cg;
		this.config = cg.config;
		this.input = cg.input;
		pricing = new Pricing(this, cg);
		alpha = new double[input.nVertices];
		beta = new double[input.nColors];
		cplex = Configuration.getCplexInstance();
		separation = new Separation(this, cg, cplex);
		cplex.setParam(IloCplex.Param.Preprocessing.Presolve, false);
		cplex.setOut(null);
		cplex.setWarning(null);
		vertexConstraints = new IloRange[input.nVertices];
		colorConstraints = new IloRange[input.nColors];

		for (int u = 0; u < input.nVertices; u++) {
			vertexConstraints[u] = cplex.addRange(Double.NEGATIVE_INFINITY, 1);
		}

		for (int c = 0; c < input.nColors; c++) {
			colorConstraints[c] = cplex.addRange(Double.NEGATIVE_INFINITY, 1);
		}

		obj = cplex.addMaximize();
	}

	void buildInitialMasterLP(LinkedList<Subtree> initCols) throws Exception {
		// "hot start" -- solution of the colgen pre-calculated
		for(Subtree S : initCols) {
			int s = input.nVertices;
			int t = 0;
			
			for(int u: S.vertices) {
				s = Math.min(u, s);
				t = Math.max(u, t);
			}
			
			Column col = new Column(new Path(s, t, S.color), input); 
			addColumnToMasterLP(col);
		}
		
		cplex.solve();
	}

	void addColumnToMasterLP(Column p) throws Exception {
		IloColumn col = cplex.column(obj, p.w);

		// add in vertex constraints
		for(int u = p.path.s; u <= p.path.t; u++) {
			col = col.and(cplex.column(vertexConstraints[u], 1.0));
		}

		// add in color constraints
		col = col.and(cplex.column(colorConstraints[p.path.c], 1.0));

		// add in cuts
		for(Cut cut : addedCuts) {
			if(cut.hasNonzeroCoef(p.path)) {
				col = col.and(cplex.column(cut.constr, 1.0));
			}
		}
		
		p.var = cplex.numVar(col, 0, Double.POSITIVE_INFINITY);
		addedCols.add(p);
	}

	void solveLP() throws Exception {
		while(true) {
			if(solveColumnGeneration()) {
				if(!solveSeparation()) {
					break;
				}
			} else {
				break;
			}
		}
	}
	
	private boolean solveSeparation() throws Exception {
		int nItWithNoCutFound = 0;
		int c = 0;
		boolean newCutsAdded = false;
		
		while (nItWithNoCutFound < input.nColors) {
			Cut cut = separation.findCut(c);
			c = (c + 1) % input.nColors;

			if (cut != null) {
				newCutsAdded = true;
				addCutToMasterLP(cut);
				callCplexLPSolver();
				nItWithNoCutFound = 0;
			} else {
				nItWithNoCutFound++;
			}
		}

		return newCutsAdded;
	}
	
	private void addCutToMasterLP(Cut cut) throws Exception {
		addedCuts.add(cut);
		IloLinearNumExpr lhs = cplex.linearNumExpr();

		for(Column col : addedCols) {
			if(cut.hasNonzeroCoef(col.path)) {
				lhs.addTerm(1, col.var);
			}
		}

		cut.constr = cplex.addLe(lhs, 1);
	}

	private boolean solveColumnGeneration() throws Exception {
		int nItWithNoViolatedColFound = 0;
		int c = 0;
		boolean newColsAdded = false;
		
		while (nItWithNoViolatedColFound < input.nColors) {
			Column p = pricing.solve(c);
			c = (c + 1) % input.nColors;

			if (p != null) {
				newColsAdded = true;
				addColumnToMasterLP(p);
				callCplexLPSolver();
				nItWithNoViolatedColFound = 0;
			} else {
				nItWithNoViolatedColFound++;
			}
		}

		return newColsAdded;
	}

	double getObjVal() throws Exception {
		return cplex.getObjValue();
	}

	void callCplexLPSolver() throws Exception {
		cplex.solve();

		if (cplex.getStatus() != IloCplex.Status.Optimal) {
			throw new Exception("Status: " + cplex.getStatus());
		}

		// --- get dual variables values ---
		// vertex constraints
		for (int u = 0; u < input.nVertices; u++) {
			alpha[u] = cplex.getDual(vertexConstraints[u]);
		}

		// color constraints
		for (int c = 0; c < input.nColors; c++) {
			beta[c] = cplex.getDual(colorConstraints[c]);
		}

		// cuts
		for(Cut cut : addedCuts) {
			cut.dualVarVal = cplex.getDual(cut.constr);
		}
		// ---------------------------------
	}
}
