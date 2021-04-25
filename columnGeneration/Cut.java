package columnGeneration;

import ilog.concert.IloRange;

public class Cut {
	ColoredEdge cEdge;
    IloRange constr;
    
    Cut(ColoredEdge cEdge) {
    	this.cEdge = cEdge;
    }

    Cut(ColoredEdge cEdge, IloRange constr) {
    	this(cEdge);
        this.constr = constr;
    }
}
