package columnGeneration;

import ilog.cplex.IloCplex;
import columnGeneration.BranchingRule.Rule;

public class Configuration {
	public Rule branchingRule;
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
	
	public Configuration(Rule branchingRule) {
		this.branchingRule = branchingRule;
	}
}
