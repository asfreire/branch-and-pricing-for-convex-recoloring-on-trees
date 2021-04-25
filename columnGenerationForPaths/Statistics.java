package columnGenerationForPaths;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import common.MedidorTempo;

public class Statistics {
	public String LOG_FILE = "resultados.txt";
	public int nVars;
	public int nConstraints;
	public int nNonZeros;
	public int totalTime;
	public double linearRelaxBound;
	public int nCuts;
	public int timeToSolveLP;
	public int nBBnodes;
	public int optSolVal;
	public int nVertices;
	public int nColors;
	public String method;
	public String inputFile;
	public MedidorTempo timer = new MedidorTempo();
	Configuration config;
	public boolean isTimeOut;

	public Statistics(String method, InputData input, Configuration config) {
		this.method = method;
		this.inputFile = input.inputFileName;
		this.nColors = input.nColors;
		this.nVertices = input.nVertices;
		this.config = config;
	}

	public void start() {
		timer.start();
	}

	public void stop() {
		timer.pause();
		totalTime = (int) timer.getTempoAcumuladoEmSegundos();
	}

	public void print() throws Exception {
		System.out.println("------------------------------------------------------");
		System.out.println("Method: " + method);
		System.out.println("Input File: " + inputFile);
		// System.out.println("#Vertices: " + nVertices);
		// System.out.println("#Colors: " + nColors);
		// System.out.println("#Columns: " + nVars);
		// System.out.println("#Rows: " + nConstraints);
		// System.out.println("#Nonzeros: " + nNonZeros);
		// System.out.println("Total Time: " + totalTime + " seconds");
		// System.out.println("#B&B Nodes: " + nBBnodes);
		System.out.println("Opt Solution Value: " + optSolVal);
		// System.out.println("#Cuts: " + nCuts);
		System.out.println("Linear relax bound: " + linearRelaxBound);
		// System.out.println("Time to solve only LP: " + timeToSolveLP);
		System.out.println("------------------------------------------------------");
		saveLog();
	}

	public void saveLog(String message) throws Exception {
		String prefix = "C&P";
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(prefix + LOG_FILE), true)));
		out.println(message);
		out.close();
	}

	void saveLog() throws Exception {
		String 	prefix = "C&P";
		
		if(inputFile.contains("chopra")){
			prefix = prefix + "_chopra_";
		} else if(inputFile.contains("randomUniformTree")){
			prefix = prefix + "_randomUniformTree_";
		} else if(inputFile.contains("pathLikeIndSetRedFromCycleComplement")){
			prefix = prefix + "_pathLikeIndSetRedFromCycleComplement_";
		} else if(inputFile.contains("pathLikeIndSetRedFromGeneralizedPetersen")){
			prefix = prefix + "_pathLikeIndSetRedFromGeneralizedPetersen_";
		} else if(inputFile.contains("pathLikeIndSetRedFromRandomGraph")){
			prefix = prefix + "_pathLikeIndSetRedFromRandomGraph_";
		} else if(inputFile.contains("capa")){
			prefix = prefix + "_capa_";
		}

		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(prefix + LOG_FILE), true)));
		String nDPs = " - ";

		if (this instanceof BPStatistics) {
			BPStatistics stats = (BPStatistics) this;
			nDPs = stats.nDPs + "";
		}

		// input
		out.print(inputFile + "\t");
		out.print(nVertices + "\t");
		out.print(nColors + "\t");

		// model
		out.print(method + "\t");
		out.print(nVars + "\t");
		out.print(nConstraints + "\t");
		out.print(nNonZeros + "\t");

		// solution
		out.print(optSolVal + "\t");
		out.print(String.format("%.2f", linearRelaxBound) + "\t");

		// time
		if (isTimeOut) {
			out.print("TIME_OUT\t");
		} else {
			out.print(totalTime + "\t");
		}
		out.print(timeToSolveLP + "\t");

		// other statistics
		out.print((nBBnodes == -1 ? 0 : nBBnodes) + "\t");
		out.print(nDPs + "\t");
		out.println(nCuts);

		out.close();
	}
}