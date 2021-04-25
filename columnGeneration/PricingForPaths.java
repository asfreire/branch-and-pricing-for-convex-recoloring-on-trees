package columnGeneration;


public class PricingForPaths {
/*
	InputData input;
	MasterLP masterLP;
	double[] dualVarVal;
	Configuration config;
	BranchAndPrice branchAndPrice;

	public PricingForPaths(MasterLP masterLP) {
		this.masterLP = masterLP;
		config = masterLP.config;
		input = masterLP.input;
		branchAndPrice = masterLP.branchAndPrice;
		dualVarVal = new double[input.nColors];
	}

	double getCost(int u, int c) {
		return masterLP.alpha[u] - input.getW(u, c);
	}


	Subtree solvePricing(int c) {
		masterLP.branchAndPrice.stats.nDPs++;
				
		// best segment
		double bestRedPrice = 0;
		Path bestPath = null;
		
		// current segment
		int s = 0;
		double redPrice = 0;
		
		for (int t = 0; t < input.nVertices; t++) {
			redPrice += getCost(t, c);
			
			if(s < t) {
				ColoredEdge ce = new ColoredEdge(input.getEdge(t - 1, t), c);

				if (branchAndPrice.isFixedZero(ce)) {
					s = t;
					t--; // "t" will be incremented inside the "for" statement, thus s=t will hold in the next iteration   
					redPrice = 0;
					continue;
				}
				
				if (branchAndPrice.isFixedOne(ce)) {
					redPrice += masterLP.getPhi(ce);
				}
			}
			
			
			Path path = new Path(s, t, c);
			
			// should we keep "stretching" this segment in the next iteration?
			if(redPrice < -Util.tolerance) {
				if (redPrice < bestRedPrice) {
					bestRedPrice = redPrice;
					bestPath = path;
				}
			} else {
				if(s < t) {
					redPrice = 0;
					s = t;
					t--; // "t" will be incremented inside the "for" statement, thus s=t will hold in the next iteration
				}
			}
		}
		
		bestRedPrice += masterLP.beta[c];

		if (bestRedPrice < config.VIOLATION_THRESHOLD) {
			return new Subtree(bestPath, bestRedPrice, input);
		} else {
			return null;
		}
	}
	*/
}
