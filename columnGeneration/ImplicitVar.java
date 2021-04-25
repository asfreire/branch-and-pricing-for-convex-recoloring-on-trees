package columnGeneration;

import java.util.Map;

import ilog.concert.IloRange;

public class ImplicitVar {
	ColoredEdge cEdge;
	ColoredVertex cVertex;
	double value;
	double dualVarVal;
	IloRange constrFix1;

	ImplicitVar(Map.Entry<ColoredEdge, Double> v) {
		this(v.getKey(), v.getValue());
	}

	ImplicitVar(ColoredEdge cEdge) {
		this.cEdge = cEdge;
	}

	ImplicitVar(ColoredEdge cEdge, double value) {
		this.cEdge = cEdge;
		this.value = value;
	}

	ImplicitVar(ColoredVertex cVertex) {
		this.cVertex = cVertex;
	}
	
	public ImplicitVar(ColoredVertex v, Double value) {
		this(v);
		this.value = value;
	}

	boolean isEdgeFixed() {
		return cEdge != null;
	}

	//ImplicitVar(ImplicitVar var) {
		//this.cVertex = var.cVertex;
		//this.cEdge = var.cEdge;
	//}
	
	@Override
	public String toString() {
		if(cVertex == null)
			return cEdge.toString() + ": " + value;
		else
			return cVertex.toString();
	}
}
