package columnGeneration;

import ilog.concert.IloException;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;

public class BranchAndPricePQ {
	int globalLB;
	int globalUB;
	int nBBNodesExplored;
	BBNode rootBBNode;
	HashMap<ColoredEdge, ImplicitVar> implEdgeVarsToFix0 = new HashMap<ColoredEdge, ImplicitVar>();
	HashMap<ColoredEdge, ImplicitVar> implEdgeVarsToFix1 = new HashMap<ColoredEdge, ImplicitVar>();
	HashMap<ColoredVertex, ImplicitVar> implVertexVarsToFix0 = new HashMap<ColoredVertex, ImplicitVar>();
	HashMap<ColoredVertex, ImplicitVar> implVertexVarsToFix1 = new HashMap<ColoredVertex, ImplicitVar>();
	boolean started;
	public MasterLP masterLP;
	InputData input;
	BPStatistics stats;
	Configuration config;
	BranchingRule branchingRule;
	SolChecker solChecker;
	boolean changedPriority;
	int nIterationsWithoutIncreasingLB;
	
	PriorityQueue<BBNode> nodes = new PriorityQueue<BBNode>(new Comparator<BBNode>() {
		@Override
		public int compare(BBNode o1, BBNode o2) {
			//return o2.UB - o1.UB; // the head element is the one with greater UB
			//return o1.level - o2.level; // BFS

			double a1 = Math.abs(o1.UB - stats.linearRelaxBound) / stats.linearRelaxBound;
			double a2 = Math.abs(o2.UB - stats.linearRelaxBound) / stats.linearRelaxBound;
			double b1 = Math.abs(o1.distFromInt - (input.nVertices - 1)) / (input.nVertices - 1);
			double b2 = Math.abs(o2.distFromInt - (input.nVertices - 1)) / (input.nVertices - 1);
			double c1 = 60 * (100 * a1) + 40 * b1; 
			double c2 = 60 * (100 * a2) + 40 * b2;
			
			//System.out.println("(" + a1 + ", " + b1 + ", " + c1 + ") -- (" + a2 + ", " + b2 + ", " + c2 + ")");
			
			if(Util.areEqual(c2, c1)) {
				return 0;
			}
			
			return Util.isGreaterThan(c1, c2) ? 1 : -1;
		}
	});

	public BranchAndPricePQ(InputData input, Configuration config) {
		this.input = input;
		stats = new BPStatistics("B&P", input, config);
		TreeAlgorithms.input = input;
		this.config = config;
		solChecker = new SolChecker(input);
	}

	public void solveLP() throws Exception {
		masterLP = new MasterLP(this);
		masterLP.buildInitialMasterLP();
		masterLP.callCplexLPSolver();
		masterLP.solveColumnGeneration();
		
		for(Subtree t : masterLP.addedCols) {
			t.value = masterLP.cplex.getValue(t.var);
		}
	}
	
	boolean isGapClosed(double LB, double UB) {
		double gap = Math.abs(UB - LB) / (1e-10 + LB);
		
		if(gap < 1e-04) 
			return true;
		else
			return false;
		
		/*
		Using the same criteria as CPLEX:  
		 
		When the value  |bestbound-bestinteger|/(1e-10+|bestinteger|)
		falls below the value of this parameter (default: 1e-04), the mixed integer optimization is stopped.
		For example, to instruct CPLEX to stop as soon as it has found a feasible integer solution proved 
		to be within five percent of optimal, set the relative MIP gap tolerance to 0.05.
		*/
	}
	
	public void solve() throws Exception {
		stats.start();
		masterLP = new MasterLP(this);
		branchingRule = new BranchingRule(masterLP, this);

		// --- build and solve initial master LP ---
		masterLP.buildInitialMasterLP();
		masterLP.callCplexLPSolver();
		// -----------------------------------------

		// --- solve root node ---
		rootBBNode = new BBNode(null, false);
		masterLP.solveColumnGeneration();
		rootBBNode.UB = masterLP.getRoundedObjVal();
		rootBBNode.distFromInt = distFromInt();
		stats.timer.pause();
		stats.timeToSolveLP = stats.timer.getTempoAcumuladoEmSegundos();
		stats.timer.restart();
		stats.linearRelaxBound = masterLP.getObjVal();
		//System.out.println("Initial LP bound: " + stats.linearRelaxBound);
		rootBBNode.fracVarToBranch = branchingRule.selectFracVar();
		// -----------------------

		started = true;
		globalUB = rootBBNode.UB;
		globalLB = findPrimalBound();

		// explore B&B tree
		if (rootBBNode.fracVarToBranch != null && !isGapClosed(globalLB, globalUB)) {
			nodes.add(new BBNode(rootBBNode, false, rootBBNode.UB, rootBBNode.distFromInt)); // right child
			nodes.add(new BBNode(rootBBNode, true, rootBBNode.UB, rootBBNode.distFromInt)); // left child

			while(!nodes.isEmpty() && !isGapClosed(globalLB, globalUB)) {
				BBNode node = nodes.poll();
				exploreNode(node);
			}
		}

		stats.nVars = masterLP.cplex.getNcols();
		stats.nNonZeros = masterLP.cplex.getNNZs();
		stats.nConstraints = masterLP.cplex.getNrows();
		stats.nBBnodes = nBBNodesExplored - 1; // root node should not be counted
		stats.finalLB = globalLB;
		stats.finalUB = globalUB;
		
		//stats.optSolVal = globalUB;
		
		//if (!isGapClosed(globalLB, globalUB))
			//throw new RuntimeException("Stopped without closing the gap " + globalLB + " " + globalUB);
		stats.stop();
		stats.print();
	}

	void exploreNode(BBNode node) throws Exception {
		//System.out.println("LEVEL: " + node.level);
		Debug.printBounds(globalLB, globalUB, nBBNodesExplored);

		if (isGapClosed(globalLB, globalUB)) 
			return;
			
		// count this node for statistics
		nBBNodesExplored++;

		// verify: is time limit exceeded?
		if (nBBNodesExplored % 10 == 0) {
			// throws exception if TLE
			// change priority of the B&B if 25% of the time limit is reached
			verifyTimeLimitAndPriorityChanging(); 
		}

		fixVars(node);
		masterLP.solveColumnGeneration();
		node.UB = masterLP.getRoundedObjVal();
		node.distFromInt = distFromInt();
		
		// prune by bound?
		if (node.UB <= globalLB || isGapClosed(globalLB, node.UB)) {
			unfixVars(node);			
			node.proned = true;
			updateGlobalUB(node.parent);
			return;
		}
		
		if(!changedPriority && nIterationsWithoutIncreasingLB <= 50 && node.UB > globalLB && node.level % 2 == 1) {
			int newGlobalLB = Math.max(globalLB, findPrimalBound());
			
			if(newGlobalLB > globalLB) {
				globalLB = newGlobalLB;
				nIterationsWithoutIncreasingLB = 0;
			} else {
				nIterationsWithoutIncreasingLB++;
			}
		}
			
		
		node.fracVarToBranch = branchingRule.selectFracVar();

		// found a better primal bound (a new feasible integral solution)?
		if (node.fracVarToBranch == null && node.UB > globalLB) {
			globalLB = node.UB;
			// if it is not correct, throw an exception
			//checkCurrentSolution();
			Debug.printNewUB(globalLB);
		}

		unfixVars(node);
		// -------------------------------------------------------------------------

		// prune by integrality ?
		if (node.fracVarToBranch == null) {
			node.proned = true;
			updateGlobalUB(node.parent);
			return;
		}

		Debug.printFixedVars(implEdgeVarsToFix1, implEdgeVarsToFix0);
		Debug.printFixedVars(implVertexVarsToFix1, implVertexVarsToFix0);
		nodes.add(new BBNode(node, false, node.UB, node.distFromInt)); // right child
		nodes.add(new BBNode(node, true, node.UB, node.distFromInt)); // left child
		updateGlobalUB(node);
	}

	private double distFromInt() {
		double d = 0;
		
		for(Map.Entry<ColoredEdge, Double> var: masterLP.nonZeroImplEdgeVars.entrySet()) {
			d += Math.min(var.getValue(), 1.0 - Math.abs(var.getValue()));
		}
		
		return d;
	}

	private void verifyTimeLimitAndPriorityChanging() throws Exception {
		stats.timer.pause();
		int spentTime = stats.timer.getTempoAcumuladoEmSegundos();
		stats.timer.restart();

		System.out.println("Spent time: " + spentTime);
		System.out.println("LP: " + stats.linearRelaxBound);
		System.out.println("LB: " + globalLB + " UB: " + globalUB + " GAP%: " + (((double)globalUB - globalLB)/globalLB));
		System.out.println("#BBnodes: " + nBBNodesExplored + " Priority Changed: " + changedPriority);
		
		if (spentTime > config.TIME_LIMIT) {
			//stats.saveLog("TIME OUT - NBB: " + nBBNodesExplored + " nAddedCols: " + masterLP.addedCols.size() + " input file: " + input.inputFileName);
			
			stats.nVars = masterLP.cplex.getNcols();
			stats.nNonZeros = masterLP.cplex.getNNZs();
			stats.nConstraints = masterLP.cplex.getNrows();
			stats.nBBnodes = nBBNodesExplored - 1; // root node should not be
													// counted
			stats.finalLB = globalLB;
			stats.finalUB = globalUB;
			stats.stop();
			stats.isTimeOut = true;
			stats.print();
			throw new Exception("TIME OUT");
		}
		
		// first 25% of time is focused on primal heuristic
		// the remaining 75% of the time is focused on decreasing UB 
		if(!changedPriority && (spentTime * 100) / config.TIME_LIMIT >= 25) {
			changedPriority = true;
			
			PriorityQueue<BBNode> nodesPQ = new PriorityQueue<BBNode>(new Comparator<BBNode>() {
				@Override
				public int compare(BBNode o1, BBNode o2) {
					return o2.UB - o1.UB; // greater UB first 
				}
			});
			
			nodesPQ.addAll(nodes);
			nodes.clear();
			nodes = nodesPQ;
		}
	}
	
	void addPath(Path p) throws Exception {
		if(!masterLP.addedPaths.contains(p)) {
			masterLP.addColumnToMasterLP(new Subtree(p, input));
		}
	}
	
	// solve master LP as an integer program then use the solution as a primal
	// bound
	int findPrimalBound() throws Exception {
		masterLP.cplex.setParam(IloCplex.Param.Preprocessing.Presolve, true);
		
		if(input.isIncreasingPath) {
			ArrayList<Path> nonzeroVars = new ArrayList<Path>();
			
			for (Subtree t : masterLP.addedCols) {
				if(masterLP.cplex.getValue(t.var) > 0.0001) {
					nonzeroVars.add(t.path);
				}
			}
			
			for(int i = 0; i < nonzeroVars.size(); i++) {
				Path pi = nonzeroVars.get(i);
				
				for(int j = i+1; j < nonzeroVars.size(); j++) {
					Path pj = nonzeroVars.get(j);
					
					// intersection case
					if(pi.intersects(pj) && !pi.contains(pj) && !pj.contains(pi)) {
						if(pi.s < pj.s) {
							addPath(new Path(pi.s, pj.s - 1, pi.c));
							addPath(new Path(pi.t+1, pj.t, pj.c));
						} else {
							addPath(new Path(pj.s, pi.s - 1, pj.c));
							addPath(new Path(pj.t+1, pi.t, pi.c));
						}
					}
				}
			}
		}

		// --- solve IP ---
		for (Subtree t : masterLP.addedCols) {
			t.conv = masterLP.cplex.conversion(t.var, IloNumVarType.Bool);
			masterLP.cplex.add(t.conv);
		}

		masterLP.cplex.solve();
		
		// if it is not correct, throw an exception
		checkCurrentSolution();
		
		int objVal = 0;
		if(masterLP.cplex.getStatus() == IloCplex.Status.Optimal) {
			objVal = masterLP.getRoundedObjVal();	
		} else {
			throw new RuntimeException("No primal bound was found");
		}

		// --- re-solve the LP ---
		for (Subtree t : masterLP.addedCols) {
			masterLP.cplex.remove(t.conv);
			t.conv = null;
		}

		masterLP.cplex.setParam(IloCplex.Param.Preprocessing.Presolve, false);
		masterLP.callCplexLPSolver();
		// -----------------------

		return objVal;
	}

	private void checkCurrentSolution() throws UnknownObjectException, IloException {
		LinkedList<Subtree> sol = new LinkedList<Subtree>();
		
		for (Subtree t : masterLP.addedCols) {
			if(Util.isOne(masterLP.cplex.getValue(t.var)))  
				sol.add(t);
			else if(!Util.isZero(masterLP.cplex.getValue(t.var)))
				throw new RuntimeException("Should be integer solution but it is fractional " + masterLP.cplex.getValue(t.var));
		}
		solChecker.check(sol, masterLP.cplex.getObjValue());
	}

	void unfixVars(BBNode node) throws Exception {
		while(node != rootBBNode) {
			if(node.parent.left == node) {
				unfixZero(node);
			} else {
				unfixOne(node);
			}
			
			node = node.parent;
		}
		
		implEdgeVarsToFix0.clear();
		implEdgeVarsToFix1.clear();
		implVertexVarsToFix0.clear();
		implVertexVarsToFix1.clear();
		//masterLP.callCplexLPSolver();
	}

	void fixVars(BBNode node) throws Exception {
		while(node != rootBBNode) {
			if(node.parent.left == node) {
				fixZero(node);
			} else {
				fixOne(node);
			}
			
			node = node.parent;
		}
		
		masterLP.callCplexLPSolver();
	}

	void fixOne(BBNode node) throws Exception {
		// what's the implicit variable to fix ?
		BBNode parent = node.parent;
		ImplicitVar implicitVarToFix = parent.fracVarToBranch;

		// create (and add to model) fix 1 constraint
		implicitVarToFix.constrFix1 = masterLP.cplex.addRange(1.0, Double.POSITIVE_INFINITY);

		// save the implicit var in list of FIX 1 implicit vars
		if(implicitVarToFix.isEdgeFixed())
			implEdgeVarsToFix1.put(implicitVarToFix.cEdge, implicitVarToFix);
		else
			implVertexVarsToFix1.put(implicitVarToFix.cVertex, implicitVarToFix);
		
		// search for added cols which contain (u,v,c) then include it into
		// respective fix 1 constraint
		for (Subtree t : masterLP.addedCols) {
			if (isSubTreeAssociatedWithImplicitVar(t, implicitVarToFix)) {
				masterLP.cplex.setLinearCoef(implicitVarToFix.constrFix1, 1, t.var);
			}
		}

		// ensure feasibility ????
		//masterLP.cplex.setLinearCoef(implicitVarToFix.constrFix1, 1, masterLP.feasibilityColumn);
	}

	void unfixOne(BBNode node) throws Exception {
		// what's the implicit variable to unfix ?
		BBNode parent = node.parent;
		ImplicitVar implicitVarToUnfix = parent.fracVarToBranch;

		// remove fix 1 constraint from model
		masterLP.cplex.remove(implicitVarToUnfix.constrFix1);

		// free fix 1 constraint from memory
		implicitVarToUnfix.constrFix1.clearExpr();
		implicitVarToUnfix.constrFix1 = null;
	}

	void fixZero(BBNode node) throws Exception {
		// add implicit var to be fixed in "implicitVarsToFix0" list
		BBNode parent = node.parent;
		ImplicitVar implicitVarToFix = parent.fracVarToBranch;
		
		if(implicitVarToFix.isEdgeFixed())
			implEdgeVarsToFix0.put(implicitVarToFix.cEdge, implicitVarToFix);
		else
			implVertexVarsToFix0.put(implicitVarToFix.cVertex, implicitVarToFix);

		// since unfix "removes all zero constraints" then we need to
		// "re-include all zero constraints", by including the recent added one
		// OBS: a column can be associated with more than one fixed zero
		// implicit variable thus we cannot remove the UB of the column without 
		// looking at all the other "fix zero constraints"
		for (ImplicitVar implVar : implEdgeVarsToFix0.values()) {
			fixZeroColsAssociatedWithImplVar(implVar);
		}
		for (ImplicitVar implVar : implVertexVarsToFix0.values()) {
			fixZeroColsAssociatedWithImplVar(implVar);
		}
	}

	void unfixZero(BBNode node) throws Exception {
		BBNode parent = node.parent;
		ImplicitVar implicitVarToUnfix = parent.fracVarToBranch;

		for (Subtree t : masterLP.addedCols) {
			if (isSubTreeAssociatedWithImplicitVar(t, implicitVarToUnfix)) {
				t.var.setUB(Double.POSITIVE_INFINITY);
			}
		}
	}
	
	void fixZeroColsAssociatedWithImplVar(ImplicitVar implVar) throws Exception {
		for (Subtree t : masterLP.addedCols) {
			if (isSubTreeAssociatedWithImplicitVar(t, implVar)) {
				t.var.setUB(0.0);
			}
		}
	}

	boolean isFixedZero(ColoredEdge uvc) {
		if(!started)
			return false;
		return implEdgeVarsToFix0.containsKey(uvc);
	}

	boolean isFixedOne(ColoredEdge uvc) {
		if(!started)
			return false;
		return implEdgeVarsToFix1.containsKey(uvc);
	}

	//boolean isImplicitVarFixed(ColoredEdge uvc) {
		//return isFixedOne(uvc) || isFixedZero(uvc);
	//}

	boolean isFixedZero(ColoredVertex uc) {
		if(!started)
			return false;
		return implVertexVarsToFix0.containsKey(uc);
	}

	boolean isFixedOne(ColoredVertex uc) {
		if(!started)
			return false;
		return implVertexVarsToFix1.containsKey(uc);
	}

	boolean isImplicitVarFixed(ColoredVertex uc) {
		return isFixedOne(uc) || isFixedZero(uc);
	}
	
	boolean isSubTreeAssociatedWithImplicitVar(Subtree t, ImplicitVar var) {
		if(var.isEdgeFixed())
			return t.color == var.cEdge.getColor() && t.containsVertex(var.cEdge.getU()) && t.containsVertex(var.cEdge.getV());
		else
			return t.color == var.cVertex.color && t.containsVertex(var.cVertex.u);
	}

	void updateGlobalUB(BBNode node) {
		if(node.proned) {
			node.UB = globalLB;
			
			if (node.parent == null) {
				globalUB = node.UB;
			} else {
				updateGlobalUB(node.parent);
			}
		}
		
		if (node.left == null || node.right == null) {
			return;
		}

		if(node.left.proned)
			node.left.UB = globalLB;

		if(node.right.proned)
			node.right.UB = globalLB;

		node.UB = node.left.UB > node.right.UB ? node.left.UB : node.right.UB;

		if (node.parent == null) {
			globalUB = node.UB;
		} else {
			updateGlobalUB(node.parent);
		}
	}
}
