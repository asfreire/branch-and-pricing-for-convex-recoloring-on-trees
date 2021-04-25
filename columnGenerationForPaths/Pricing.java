package columnGenerationForPaths;

import java.util.LinkedList;

public class Pricing {
	MasterLP masterLP;
	ColumnGeneration cg;
	InputData input;
	Configuration config;
	
	public Pricing(MasterLP masterLP, ColumnGeneration cg) {
		this.masterLP = masterLP;
		this.cg = cg;
		this.input = cg.input;
		config = masterLP.config;
	}
	
	Column solve(int c) {
		masterLP.cg.stats.nDPs++;
		return findMinRedPricePath(c);
	}

	private Column findMinRedPricePath(int c) {
		double minRedPrice = config.VIOLATION_THRESHOLD;
		Path minRedPricePath = null;
		LinkedList<Cut> cuts_c = new LinkedList<Cut>();
		LinkedList<Cut> cuts_not_c = new LinkedList<Cut>();
		
		for(Cut cut : masterLP.addedCuts) {
			if(cut.H.c == c) {
				cuts_c.add(cut);
			} else {
				cuts_not_c.add(cut);
			}
		}
		
		Path p = new Path(0, 0, c);
		
		for(p.s = 0; p.s < input.nVertices; p.s++) {
			double w = 0;
			double alpha = 0;
			
			for(p.t = p.s; p.t < input.nVertices; p.t++) {
				w += input.getW(p.t, c);
				alpha += masterLP.alpha[p.t];
				double phi = 0;
				
				for(Cut cut : cuts_c) {
					if(cut.H.intersects(p)) {
						phi += cut.dualVarVal;
					}
				}

				for(Cut cut : cuts_not_c) {
					if(p.contains(cut.H)) {
						phi += cut.dualVarVal;
					}
				}
				
				double redPrice = alpha - w + phi + masterLP.beta[c];
				
				if(redPrice < minRedPrice) {
					minRedPrice = redPrice;
					minRedPricePath = new Path(p);
				}
			}
		}
		
		if(minRedPricePath == null) {
			return null;
		}
		
		System.out.println("Column " + minRedPricePath + " " + minRedPrice);
		return new Column(minRedPricePath, input);
	}
}
