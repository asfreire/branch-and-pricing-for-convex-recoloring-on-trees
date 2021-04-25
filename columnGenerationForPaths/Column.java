package columnGenerationForPaths;

import ilog.concert.IloConversion;
import ilog.concert.IloNumVar;

public class Column {
	IloNumVar var;
	IloConversion conv;
	double w;
	Path path;

	Column(Path p, InputData input) {
		this.path = p;
		
		for(int u = p.s; u <= p.t; u++) {
			w += input.getW(u, p.c);
		}
	}
}
