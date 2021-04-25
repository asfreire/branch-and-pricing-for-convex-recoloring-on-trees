package columnGeneration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BranchingRule {
	public enum Rule {
		MOST_FRACTIONAL, LEAST_FRACTIONAL, ADVANCED;
	}

	MasterLP masterLP;
	Configuration config;
	InputData input;
	BranchAndPricePQ branchAndPrice;

	public BranchingRule(MasterLP masterLP, BranchAndPricePQ branchAndPrice) {
		this.masterLP = masterLP;
		this.input = masterLP.input;
		this.config = masterLP.config;
		this.branchAndPrice = branchAndPrice;
	}

	List<Integer> getColorsByPriority(LinkedList<Map.Entry<ColoredEdge, Double>> fracVars) {
		int[] colorFixed = new int[input.nColors];
		double[] colorVal = new double[input.nColors];
		
		for(ColoredEdge e: branchAndPrice.implEdgeVarsToFix1.keySet()) {
			colorFixed[e.getColor()]++;
		}

		for(ColoredEdge e: branchAndPrice.implEdgeVarsToFix0.keySet()) {
			colorFixed[e.getColor()]++;
		}

		for(Map.Entry<ColoredVertex,Double> v : masterLP.nonZeroImplVertexVars.entrySet()) {
			colorVal[v.getKey().color] += v.getValue();
		}
		
		ArrayList<Integer> colorsByPriority = new ArrayList<Integer>(input.nColors);
		
		for(int i = 0; i < input.nColors; i++) {
			colorsByPriority.add(i);
		}
		
		Collections.sort(colorsByPriority, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				if(colorFixed[o1] != colorFixed[o2] ) {
					return colorFixed[o1] - colorFixed[o2];
				}
				
				if(Util.areEqual(colorVal[o1], colorVal[o2])) {
					return 0;
				}
				
				return Util.isGreaterThan(colorVal[o1], colorVal[o2]) ? 1 : -1;
			}
		});
		
		return colorsByPriority;
	}
	
	public ImplicitVar selectFracVarAdvanced() throws Exception {
		LinkedList<Map.Entry<ColoredEdge, Double>> fracVars = getFracEdgeVars();
		
		if(fracVars.size() == 0)
			return null;
		
		Collections.shuffle(fracVars);
		List<Integer> colorsByPriority = getColorsByPriority(fracVars); 
		@SuppressWarnings("unchecked")
		LinkedList<Map.Entry<ColoredEdge, Double>>[] fracVarsByColors = new LinkedList[input.nColors];
		
		for(Map.Entry<ColoredEdge, Double> var : fracVars) {
			if(fracVarsByColors[var.getKey().getColor()] == null) {
				fracVarsByColors[var.getKey().getColor()] = new LinkedList<Map.Entry<ColoredEdge,Double>>();
			}
			
			fracVarsByColors[var.getKey().getColor()].add(var); 
		}
		
		for(int c : colorsByPriority) {
			if(fracVarsByColors[c] == null)
				continue;
			
			Map.Entry<ColoredEdge, Double> var = selectMostFracEdgeVar(fracVarsByColors[c]);
			return new ImplicitVar(var.getKey(), var.getValue());
		}
		
		return null;
	}
	
	public Map.Entry<ColoredEdge, Double> selectMostFracEdgeVar(LinkedList<Map.Entry<ColoredEdge, Double>> fracVars) throws Exception {
		Map.Entry<ColoredEdge, Double> leastFrac = fracVars.getFirst();

		for (Map.Entry<ColoredEdge, Double> x_ec : fracVars) {
			if (Math.abs(0.5 - x_ec.getValue()) < Math.abs(0.5 - leastFrac.getValue())) {
				leastFrac = x_ec;
			}
		}

		return leastFrac;
	}
	
	public ImplicitVar selectMostFracVar() throws Exception {
		Map.Entry<ColoredEdge, Double> mostFracEdge = selectMostFracEdgeVar();
		Map.Entry<ColoredVertex, Double> mostFracVertex = selectMostFracVertexVar();
		
		if(mostFracEdge == null && mostFracVertex == null)
			return null;
			
		if(mostFracEdge == null)
			return new ImplicitVar(mostFracVertex.getKey(), mostFracVertex.getValue());

		if(mostFracVertex == null)
			return new ImplicitVar(mostFracEdge);
		
		if (Math.abs(0.5 - mostFracEdge.getValue()) <= Math.abs(0.5 - mostFracVertex.getValue())) {
			return new ImplicitVar(mostFracEdge);
		} else {
			return new ImplicitVar(mostFracVertex.getKey(), mostFracVertex.getValue());
		}
	}

	public Map.Entry<ColoredEdge, Double> selectMostFracEdgeVar() throws Exception {
		LinkedList<Map.Entry<ColoredEdge, Double>> fracVars = getFracEdgeVars();

		if (fracVars.size() == 0) {
			return null;
		}

		Map.Entry<ColoredEdge, Double> mostFrac = fracVars.getFirst();

		for (Map.Entry<ColoredEdge, Double> x_ec : fracVars) {
			if (Math.abs(0.5 - x_ec.getValue()) < Math.abs(0.5 - mostFrac.getValue())) {
				mostFrac = x_ec;
			}
		}

		return mostFrac;
	}

	public Map.Entry<ColoredVertex, Double> selectMostFracVertexVar() throws Exception {
		LinkedList<Map.Entry<ColoredVertex, Double>> fracVars = getFracVertexVars();

		if (fracVars.size() == 0) {
			return null;
		}

		Map.Entry<ColoredVertex, Double> mostFrac = fracVars.getFirst();

		for (Map.Entry<ColoredVertex, Double> x_uc : fracVars) {
			if (Math.abs(0.5 - x_uc.getValue()) < Math.abs(0.5 - mostFrac.getValue())) {
				mostFrac = x_uc;
			}
		}

		return mostFrac;
	}

	public ImplicitVar selectLeastFracVar() throws Exception {
		Map.Entry<ColoredEdge, Double> leastFracEdge = selectLeastFracEdgeVar();
		Map.Entry<ColoredVertex, Double> leastFracVertex = selectLeastFracVertexVar();
		
		if(leastFracEdge == null && leastFracVertex == null) {
			return selectMostFracVar();
		}
		
		if(leastFracEdge == null)
			return new ImplicitVar(leastFracVertex.getKey(), leastFracVertex.getValue());

		if(leastFracVertex == null)
			return new ImplicitVar(leastFracEdge);

		return distFromInt(leastFracEdge.getValue()) <= distFromInt(leastFracVertex.getValue()) ? 
				new ImplicitVar(leastFracEdge) : new ImplicitVar(leastFracVertex.getKey(), leastFracVertex.getValue()); 
	}
	
	double distFromInt(double val) {
		return Math.min(Math.abs(val), 1.0 - Math.abs(val));
	}
	
	// For a frac var to be considered, "dist from int" must be at least 0.1
	public Map.Entry<ColoredEdge, Double> selectLeastFracEdgeVar() throws Exception {
		LinkedList<Map.Entry<ColoredEdge, Double>> fracVars = getFracEdgeVars();

		if (fracVars.size() == 0) {
			return null;
		}
		
		Map.Entry<ColoredEdge, Double> leastFrac = fracVars.getFirst();

		for (Map.Entry<ColoredEdge, Double> x_ec : fracVars) {
			if (distFromInt(x_ec.getValue()) < distFromInt(leastFrac.getValue())) {
				leastFrac = x_ec;
			}
		}

		return leastFrac;
	}

	// For a frac var to be considered, "dist from int" must be at least 0.1
	public Map.Entry<ColoredVertex, Double> selectLeastFracVertexVar() throws Exception {
		LinkedList<Map.Entry<ColoredVertex, Double>> fracVars = getFracVertexVars();

		if (fracVars.size() == 0) {
			return null;
		}

		Map.Entry<ColoredVertex, Double> leastFrac = fracVars.getFirst();

		for (Map.Entry<ColoredVertex, Double> x_uc : fracVars) {
			if (distFromInt(x_uc.getValue()) < distFromInt(leastFrac.getValue())) {
				leastFrac = x_uc;
			}
		}

		return leastFrac;
	}

	private LinkedList<Map.Entry<ColoredEdge, Double>> getFracEdgeVars() {
		LinkedList<Map.Entry<ColoredEdge, Double>> fracVars = new LinkedList<Map.Entry<ColoredEdge, Double>>();
		Set<Map.Entry<ColoredEdge, Double>> nzVars = masterLP.nonZeroImplEdgeVars.entrySet();
		
		for (Map.Entry<ColoredEdge, Double> x_ec : nzVars) {
			if (!Util.isOne(x_ec.getValue())) {
				fracVars.add(x_ec);
			}
		}
		
		return fracVars;
	}

	private LinkedList<Map.Entry<ColoredVertex, Double>> getFracVertexVars() {
		LinkedList<Map.Entry<ColoredVertex, Double>> fracVars = new LinkedList<Map.Entry<ColoredVertex, Double>>();
		Set<Map.Entry<ColoredVertex, Double>> nzVars = masterLP.nonZeroImplVertexVars.entrySet();
		
		for (Map.Entry<ColoredVertex, Double> x_uc : nzVars) {
			if (!Util.isOne(x_uc.getValue())) {
				fracVars.add(x_uc);
			}
		}
		
		return fracVars;
	}

	
	public ImplicitVar selectFracVar() throws Exception {
		ImplicitVar var;
		
		if(config.branchingRule.equals(Rule.LEAST_FRACTIONAL)) {
			var = selectLeastFracVar();
		} else if(config.branchingRule.equals(Rule.MOST_FRACTIONAL)) {
			var = selectMostFracVar();
		} else if(config.branchingRule.equals(Rule.ADVANCED)) {
			var = selectFracVarAdvanced();
		} else { 
			// default
			var = selectFracVarAdvanced();
		}
		
		//System.out.println("Frac var val: " + (var == null ? "null" : var.value + " " + Util.isZero(var.value)) 
			//	+ " obj: " + masterLP.cplex.getObjValue() 
				//+ " LB: " + branchAndPrice.globalLB 
				//+ " UB " + branchAndPrice.globalUB);
		
		
		
		//if(var != null && Util.isZero(var.value))
			//System.out.println(masterLP.nonZeroImplEdgeVars);
		return var;
	}

}
