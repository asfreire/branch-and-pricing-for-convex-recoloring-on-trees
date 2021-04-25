package columnGenerationForPaths;

import ilog.cplex.IloCplex;

public class Configuration {
	public double VIOLATION_THRESHOLD = -0.000001;
	public int TIME_LIMIT;
	
	public static IloCplex getCplexInstance() throws Exception {
		IloCplex cplex = new IloCplex();
		cplex.setOut(null);
		cplex.setWarning(null);
		//cplex.setParam(IloCplex.Param.Threads, 1);
		cplex.setParam(IloCplex.Param.Parallel, IloCplex.ParallelMode.Deterministic);
		return cplex;
	}
	
	public Configuration(columnGeneration.Configuration config) {
		this.TIME_LIMIT = config.TIME_LIMIT;
	}
}
