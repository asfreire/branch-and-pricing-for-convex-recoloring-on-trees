package columnGenerationForPaths;

import java.util.LinkedList;

import columnGeneration.Subtree;

public class ColumnGeneration {
	MasterLP masterLP;
	InputData input;
	BPStatistics stats;
	Configuration config;

	public ColumnGeneration(InputData input, Configuration config) {
		this.input = input;
		stats = new BPStatistics("C&P", input, config);
		this.config = config;
	}

	public void solve(LinkedList<Subtree> initCols) throws Exception {
		stats.start();
		masterLP = new MasterLP(this);

		// --- build and solve initial master LP ---
		masterLP.buildInitialMasterLP(initCols);
		masterLP.callCplexLPSolver();
		// -----------------------------------------

		masterLP.solveLP();
		stats.stop();
		stats.timeToSolveLP = stats.timer.getTempoAcumuladoEmSegundos();
		stats.linearRelaxBound = masterLP.getObjVal();
		stats.nVars = masterLP.cplex.getNcols();
		stats.nCuts = masterLP.addedCuts.size();
		stats.nNonZeros = masterLP.cplex.getNNZs();
		stats.nConstraints = masterLP.cplex.getNrows();
		stats.optSolVal = -1;
		stats.print();
	}
}
