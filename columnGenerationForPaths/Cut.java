package columnGenerationForPaths;

import ilog.concert.IloRange;

public class Cut {
    IloRange constr;
	double dualVarVal;
	Path H;
	
	Cut(Path H) {
		this.H = H;
	}
	
	public boolean hasNonzeroCoef(Path path) {
		if(path.c == H.c) {
			return path.intersects(H);
		} else {
			return path.contains(H);
		}
	}
}
