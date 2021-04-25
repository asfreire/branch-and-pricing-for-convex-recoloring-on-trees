package columnGeneration;

import java.util.LinkedList;

public class DynProgForPricing {
	MasterLP masterLP;
	BranchAndPricePQ branchAndPrice;
	InputData input;
	Integer bestSubtree;
	double bestRedPrice;
	LinkedList<Integer>[] children;
	Configuration config;

	@SuppressWarnings("unchecked")
	public DynProgForPricing(MasterLP masterLP, BranchAndPricePQ branchAndPrice) {
		this.masterLP = masterLP;
		this.branchAndPrice = branchAndPrice;
		this.input = branchAndPrice.input;
		config = masterLP.config;
		children = new LinkedList[input.nVertices];

		for (int u = 0; u < input.nVertices; u++) {
			children[u] = new LinkedList<Integer>();
		}
	}

	void clear(int c) {
		for (int u = 0; u < input.nVertices; u++) {
			children[u].clear();
		}

		bestSubtree = null;
		bestRedPrice = 1 +  Math.abs(masterLP.beta[c]);
	}

	Subtree solvePricing(int c) {
		masterLP.branchAndPrice.stats.nDPs++;
		clear(c);
		findMinRedPriceSubtree(input.root, c);
		double redPrice = bestRedPrice + masterLP.beta[c];

		if (redPrice < config.VIOLATION_THRESHOLD) {
			Subtree t = retrieveOptSol(c);
			t.redPrice = redPrice;
			checkRedPrice(t);
			return t;
		}

		return null;
	}
	
	void checkRedPrice(Subtree t) {
		double redPrice = masterLP.beta[t.color];
		
		for(int u : t.vertices) {
			redPrice += masterLP.alpha[u] - input.getW(u, t.color);
			ColoredVertex uc = new ColoredVertex(u, t.color);
			
			if (branchAndPrice.isFixedZero(uc)) 
				throw new RuntimeException("Pricing returned column with forbidden vertex");
			

			if (branchAndPrice.isFixedOne(uc)) 
				redPrice += masterLP.getPhi(uc);
		}
		
		if(t.edges.size() != t.vertices.size() - 1)
			throw new RuntimeException("Edge set with wrong size " + t.edges.size() + " not equals " + (t.vertices.size() - 1));
		
		for(Edge e: t.edges) {
			ColoredEdge uvc = new ColoredEdge(e, t.color);

			if (branchAndPrice.isFixedZero(uvc)) 
				throw new RuntimeException("Pricing returned column with forbidden edge");


			if (branchAndPrice.isFixedOne(uvc)) 
				redPrice += masterLP.getPhi(uvc);
		}

		if(redPrice > config.VIOLATION_THRESHOLD)
			throw new RuntimeException("Nonegative red price");
		
		if(!Util.areEqual(redPrice, t.redPrice))
			throw new RuntimeException("Wrong red price " + t.redPrice + " not equals " + redPrice);
	}

	double findMinRedPriceSubtree(int u, int c) {
		double optVal = masterLP.alpha[u] - input.getW(u, c);
		ColoredVertex uc = new ColoredVertex(u, c);
		
		if (branchAndPrice.isFixedZero(uc)) {
			// sets a big number only to ensure that it will not be the minimum red price
			// cannot return because it needs to make the recursive call
			optVal = Integer.MAX_VALUE / 2; 
		}

		if (branchAndPrice.isFixedOne(uc)) {
			optVal += masterLP.getPhi(uc);
		}
		
		for (int v : input.neighbours[u]) {
			if (input.parent[v] != u) {
				continue;
			}

			double opt_v_c = findMinRedPriceSubtree(v, c);
			ColoredEdge uvc = new ColoredEdge(u, v, c);

			// cannot 'continue' before recursive call 
			if (branchAndPrice.isFixedZero(uvc)) {
				continue;
			}

			if (branchAndPrice.isFixedOne(uvc)) {
				opt_v_c += masterLP.getPhi(uvc);
			}

			if (opt_v_c < config.VIOLATION_THRESHOLD) {
				optVal += opt_v_c;
				children[u].add(v);
			}
		}

		if (bestSubtree == null || optVal < bestRedPrice) {
			bestRedPrice = optVal;
			bestSubtree = u;
		}

		return optVal;
	}

	Subtree retrieveOptSol(int c) {
		Subtree retrivedSubtree = new Subtree(c);
		retrivedSubtree.redPrice = bestRedPrice;
		retrieveOptSolRec(bestSubtree, retrivedSubtree);
		return retrivedSubtree;
	}

	void retrieveOptSolRec(int u, Subtree retrivedSubtree) {
		retrivedSubtree.addVertex(u, input);

		for (int v : children[u]) {
			retrivedSubtree.addEdge(input.getEdge(u, v));
			retrieveOptSolRec(v, retrivedSubtree);
		}
	}
}
