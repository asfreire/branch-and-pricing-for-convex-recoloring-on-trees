package chopra;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import columnGeneration.Configuration;
import columnGeneration.InputData;

import common.MedidorTempo;
import common.Statistics;

public class CRP_Chopra {
	int n, k, m;
	int[] color;
	double tolerance = 0.000001;
	Edge[] edges;
	public Statistics stats;
	InputData input;
	Configuration config;

	public void leEntradaGambi(InputData in, Configuration config) {
		this.input = in;
		stats = new Statistics("Chopra", in, config);
		this.config = config;
		this.n = in.nVertices;
		this.k = in.nColors;
		this.m = n - 1;
		color = new int[n];
		edges = new Edge[m];

		for (int i = 0; i < n; i++) {
			color[i] = in.color[i];
		}

		int i = 0;
		for (columnGeneration.Edge e : in.edges) {
			edges[i] = new Edge();
			edges[i].u = e.getU();
			edges[i++].v = e.getV();
		}
	}

	class Edge {
		int u, v, index;
	}

	int getEdgeColorIndex(Edge e, int c) {
		return e.index * k + c;
	}

	int getIndexInX(int u, int c) {
		return u * k + c;
	}

	void initEdgeIndexes() {
		int indexCount = 0;

		for (int i = 0; i < m; i++) {
			Edge e = edges[i];
			e.index = indexCount++;
		}
	}

	public void solve() throws Exception {
		solveLP();
		stats.start();
		IloCplex cplex = Configuration.getCplexInstance();
		IloIntVar[] x = cplex.boolVarArray(n * k);
		IloIntVar[] y = cplex.boolVarArray(m * k);
		initEdgeIndexes();
		buildModel(cplex, x, y);
		stats.nVars = cplex.getNcols();
		stats.nNonZeros = cplex.getNNZs();
		stats.nConstraints = cplex.getNrows();
		InfoCallback infoCB = new InfoCallback();
		cplex.use(infoCB);
		cplex.setParam(IloCplex.LongParam.TimeLimit, config.TIME_LIMIT);
		cplex.solve();
		stats.nCuts = infoCB.nCuts;
		stats.nBBnodes = cplex.getNnodes();
		stats.finalLB = (int) (cplex.getObjValue() + 0.001);
		stats.finalUB = (int) (cplex.getBestObjValue() + 0.001);
		stats.stop();

		if(cplex.getStatus() != IloCplex.Status.Optimal) {
			stats.isTimeOut = true;
		}
		
		stats.print();	
		cplex.end();
	}

	public void solveLP() throws Exception {
		MedidorTempo timer = new MedidorTempo();
		timer.start();
		IloCplex cplex = Configuration.getCplexInstance();
		IloNumVar[] x = cplex.numVarArray(n * k, 0, 1);
		IloNumVar[] y = cplex.numVarArray(m * k, 0, 1);
		initEdgeIndexes();
		buildModel(cplex, x, y);
		cplex.solve();
		stats.linearRelaxBound = cplex.getObjValue();
		cplex.end();
		timer.pause();
		stats.timeToSolveLP = timer.getTempoAcumuladoEmSegundos();
	}

	private void buildModel(IloCplex cplex, IloNumVar[] x, IloNumVar[] y)
			throws IloException {
		IloLinearNumExpr expr = cplex.linearNumExpr();

		for (int c = 0; c < k; c++) {
			for (int u = 0; u < n; u++) {
				if (input.getW(u, c) > tolerance) {
					expr.addTerm(x[getIndexInX(u, c)], input.getW(u, c));
				}
			}
		}

		cplex.addMaximize(expr);

		for (int u = 0; u < n; u++) {
			expr = cplex.linearNumExpr();

			for (int c = 0; c < k; c++) {
				expr.addTerm(x[getIndexInX(u, c)], 1.0);
			}

			cplex.addLe(expr, 1);
		}

		for (int c = 0; c < k; c++) {
			expr = cplex.linearNumExpr();

			for (int u = 0; u < n; u++) {
				expr.addTerm(x[getIndexInX(u, c)], 1.0);
			}

			for (int i = 0; i < m; i++) {
				Edge e = edges[i];
				expr.addTerm(y[getEdgeColorIndex(e, c)], -1.0);
			}

			cplex.addLe(expr, 1);
		}

		for (int c = 0; c < k; c++) {
			for (Edge e : edges) {
				expr = cplex.linearNumExpr();
				expr.addTerm(x[getIndexInX(e.u, c)], -1.0);
				expr.addTerm(y[getEdgeColorIndex(e, c)], 1.0);
				cplex.addLe(expr, 0);

				expr = cplex.linearNumExpr();
				expr.addTerm(x[getIndexInX(e.v, c)], -1.0);
				expr.addTerm(y[getEdgeColorIndex(e, c)], 1.0);
				cplex.addLe(expr, 0);
			}
		}
	}
}
