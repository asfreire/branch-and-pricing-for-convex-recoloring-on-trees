package columnGeneration;

import ilog.concert.IloColumn;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class MasterLP {
	IloRange[] vertexConstraints;
	IloRange[] colorConstraints;
	IloObjective obj;
	IloCplex cplex;
	IloNumVar feasibilityColumn;
	public LinkedList<Subtree> addedCols = new LinkedList<Subtree>();
	BranchAndPricePQ branchAndPrice;
	DynProgForPricing dynProgForPricing;
	//PricingForPaths pricingForPaths;
	RandomHeuristic randomHeuristic;
	InputData input;
	HashMap<ColoredEdge, Double> nonZeroImplEdgeVars = new HashMap<ColoredEdge, Double>();
	HashMap<ColoredVertex, Double> nonZeroImplVertexVars = new HashMap<ColoredVertex, Double>();
	// dual variables
	double[] alpha; // vertex constraints
	double[] beta; // color constraints
	Configuration config;
	
	//boolean switchPricer = false;
	
	SolChecker solChecker;
	HashSet<Path> addedPaths = new HashSet<Path>();

	MasterLP(BranchAndPricePQ branchAndPrice) throws Exception {
		this.branchAndPrice = branchAndPrice;
		this.config = branchAndPrice.config;
		this.input = branchAndPrice.input;
		solChecker = new SolChecker(input);
		randomHeuristic = new RandomHeuristic(input);
		dynProgForPricing = new DynProgForPricing(this, branchAndPrice);
		//pricingForPaths = new PricingForPaths(this);
		alpha = new double[input.nVertices];
		beta = new double[input.nColors];
		cplex = Configuration.getCplexInstance();
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

	void buildInitialMasterLP() throws Exception {
		// find a feasible solution and add the respective columns
		List<Subtree> sol = randomHeuristic.solveWithoutPresetColors();
		double objVal = 0;
		
		// if it is not correct, throws an exception
		for(Subtree t : sol) 
			objVal += t.w;
		solChecker.check(sol, objVal);

		for (Subtree t : sol) {
			addColumnToMasterLP(t);
		}

		// add the "feasibility column"
		addfeasibilityColumn();

		// add all subtrees with only one vertex with its respective color
		for (int u = 0; u < input.nVertices; u++) {
			Subtree t = new Subtree(1, input.color[u]);
			t.addVertex(u, input);
			addColumnToMasterLP(t);
		}

		// add all subtree with only one edge uv (using colors: c[u] and c[v])
		for (Edge e : input.edges) {
			Subtree t = new Subtree(input.color[e.getU()]);
			t.addEdge(e);
			t.addVertex(e.getU(), input);
			t.addVertex(e.getV(), input);
			addColumnToMasterLP(t);

			if (input.color[e.getU()] != input.color[e.getV()]) {
				t = new Subtree(input.color[e.getV()]);
				t.addEdge(e);
				t.addVertex(e.getU(), input);
				t.addVertex(e.getV(), input);
				addColumnToMasterLP(t);
			}
		}
	}

	void addfeasibilityColumn() throws Exception {
		IloColumn col = cplex.column(obj, 0);

		// in theory, it would be more correct to do this:
		//IloColumn col = cplex.column(obj, -infinity); 

		for (int u = 0; u < input.nVertices; u++) {
			col = col.and(cplex.column(vertexConstraints[u], 1.0));
		}

		for (int c = 0; c < input.nColors; c++) {
			col = col.and(cplex.column(colorConstraints[c], 1.0));
		}

		feasibilityColumn = cplex.numVar(col, 0, 1);
	}

	void addColumnToMasterLP(Subtree t) throws Exception {
		if(t.path == null && input.isIncreasingPath) {
			t.path = new Path(0, 0, t.color);
			t.path.s = input.nVertices;
			t.path.t = 0;
			
			for(int u : t.vertices){
				t.path.s = Math.min(u, t.path.s);
				t.path.t = Math.max(u, t.path.t);
			}
			
			addedPaths.add(t.path);
		}
		
		if (t.vertices.size() - 1 != t.edges.size()) {
			throw new RuntimeException();
		}

		IloColumn col = cplex.column(obj, t.w);

		// add in vertex constraints
		for (int u : t.vertices) {
			col = col.and(cplex.column(vertexConstraints[u], 1.0));
		}

		// add in color constraints
		col = col.and(cplex.column(colorConstraints[t.color], 1.0));

		if (branchAndPrice.started) {
			// add in fix 1 "branch constraints"
			for (ImplicitVar implVar : branchAndPrice.implEdgeVarsToFix1.values()) {
				if (branchAndPrice.isSubTreeAssociatedWithImplicitVar(t, implVar)) {
					col = col.and(cplex.column(implVar.constrFix1, 1.0));
				}
			}
			for (ImplicitVar implVar : branchAndPrice.implVertexVarsToFix1.values()) {
				if (branchAndPrice.isSubTreeAssociatedWithImplicitVar(t, implVar)) {
					col = col.and(cplex.column(implVar.constrFix1, 1.0));
				}
			}
		}

		t.var = cplex.numVar(col, 0, Double.POSITIVE_INFINITY);
		addedCols.add(t);
	}

	void solveColumnGeneration() throws Exception {
		int nItWithNoViolatedColFound = 0;
		int c = 0;

		while (nItWithNoViolatedColFound < input.nColors) {
			Subtree t = null;

			//if (switchPricer) {
				//t = pricingForPaths.solvePricing(c);
			//} else {
				t = dynProgForPricing.solvePricing(c);
			//}

			Debug.printOptPricing(t);

			c = (c + 1) % input.nColors;

			if (t != null) {
				addColumnToMasterLP(t);
				callCplexLPSolver();
				nItWithNoViolatedColFound = 0;
			} else {
				nItWithNoViolatedColFound++;
			}
		}

/*		for(c = 0; c < input.nColors; c++) {
			findMinRedPricePathBRUTO(c);
		}*/
		
		Debug.printColGenSolVal(cplex.getObjValue());
		calcImplicitVarsVals();
	}

	/*
	private void findMinRedPricePathBRUTO(int c) {
		Path p = new Path(0, 0, c);
		
		for(p.s = 0; p.s < input.nVertices; p.s++) {
			double w = 0;
			double alpha_ = 0;
			
			for(p.t = p.s; p.t < input.nVertices; p.t++) {
				w += input.getW(p.t, c);
				alpha_ += alpha[p.t];
				double redPrice = alpha_ - w + beta[c];
				
				if(redPrice < -0.0001) {
					System.out.println("Red price: " + redPrice);
					
					for(int u = p.s; u <= p.t; u++) {
						System.out.println("alpha[" + u + "] = " + alpha[u] + " w[" + u + ", " + c + "] = " + input.getW(u, c));
					}
					
					System.out.println("beta[" + c + "] = " + beta[c]);
					
					System.exit(0);
				}
			}
		}
	}
	*/
	
	double getObjVal() throws Exception {
		return cplex.getObjValue();
	}

	int getRoundedObjVal() throws Exception {
		return (int) (cplex.getObjValue() + Util.tolerance);
	}

	double getPhi(ColoredEdge e) {
		ImplicitVar var = branchAndPrice.implEdgeVarsToFix1.get(e);
		return var == null ? 0 : var.dualVarVal;
	}

	double getPhi(ColoredVertex uc) {
		ImplicitVar var = branchAndPrice.implVertexVarsToFix1.get(uc);
		return var == null ? 0 : var.dualVarVal;
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

		// fix 1 constraints
		if (branchAndPrice.started) {
			for (ImplicitVar implVar : branchAndPrice.implEdgeVarsToFix1.values()) {
				implVar.dualVarVal = cplex.getDual(implVar.constrFix1);
			}
			for (ImplicitVar implVar : branchAndPrice.implVertexVarsToFix1.values()) {
				implVar.dualVarVal = cplex.getDual(implVar.constrFix1);
			}
		}

		// fix 0 is implicitly (it does not need to be added)...
		// ---------------------------------
	}

	void calcImplicitVarsVals() throws Exception {
		nonZeroImplEdgeVars.clear();
		nonZeroImplVertexVars.clear();
		
		for (Subtree t : addedCols) {
			double tVal = Math.abs(cplex.getValue(t.var));

			if (tVal >= 0.0001) {
				for (Edge e : t.edges) {
					ColoredEdge ce = new ColoredEdge(e, t.color);
					Double varVal = nonZeroImplEdgeVars.get(ce);

					if (varVal == null) {
						nonZeroImplEdgeVars.put(ce, tVal);
					} else {
						nonZeroImplEdgeVars.put(ce, varVal + tVal);
					}
				}
				
				for (int u : t.vertices) {
					ColoredVertex cv = new ColoredVertex(u, t.color);
					Double varVal = nonZeroImplVertexVars.get(cv);

					if (varVal == null) {
						nonZeroImplVertexVars.put(cv, tVal);
					} else {
						nonZeroImplVertexVars.put(cv, varVal + tVal);
					}
				}
			}
		}
	}
}
