package main;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class OutputParser {
/*
	static class OutputLine implements Comparable<OutputLine> {
		String inputFile;
		int nVertices;
		int nColors;
		String method;
		int nVars;
		int nConstraints;
		int nNonZeros;
		int optSolVal;
		double linearRelaxBound;
		int totalTime;
		int timeToSolveLP;
		int nBBnodes;
		int nDPs;
		int nCuts;
		boolean isTimeOut;
				
		OutputLine(String line) {
			String[] data = line.split("\\s+");
			int i = 0;
			inputFile = data[i++];
			nVertices = Integer.parseInt(data[i++]);
			nColors = Integer.parseInt(data[i++]);
			method = data[i++];
			nVars = Integer.parseInt(data[i++]);
			nConstraints = Integer.parseInt(data[i++]);
			nNonZeros = Integer.parseInt(data[i++]);
			optSolVal = Integer.parseInt(data[i++]);
			linearRelaxBound = Double.parseDouble(data[i++]);

			if (data[i].equals("TIME_OUT")) {
				isTimeOut = true;
				totalTime = 3600;
				i++;
			} else if (data[i].equals("TIME")) {
				isTimeOut = true;
				totalTime = 3600;
				i += 2;
			} else {
				totalTime = Integer.parseInt(data[i++]);
			}

			timeToSolveLP = Integer.parseInt(data[i++]);
			nBBnodes = Integer.parseInt(data[i++]);
			
			i++; //nDPs = Integer.parseInt(data[i++]);
			
			nCuts = Integer.parseInt(data[i++]);
		}

		@Override
		public int compareTo(OutputLine o) {
			return this.inputFile.compareTo(o.inputFile);
		}
	}

	static List<String> getFilesNamesInDir(String dirName) {
		LinkedList<String> fileNames = new LinkedList<String>();
		File dir = new File(dirName + "/");
		File[] files = dir.listFiles();

		for (File f : files) {
			fileNames.add(f.getAbsolutePath());
		}

		return fileNames;
	}

	static LinkedList<OutputLine> readLines(String arq) throws Exception {
		LinkedList<OutputLine> outputs = new LinkedList<OutputLine>(); 
		Scanner in = new Scanner(new File(arq));

		while (in.hasNext()) {
			outputs.add(new OutputLine(in.nextLine()));
		}

		in.close();
		return outputs;
	}

	static void bla() throws Exception {
		HashMap<String, List<OutputLine>> map = new HashMap<String, List<OutputLine>>();
		
		List<String> files = getFilesNamesInDir("");
		
		for(String file : files) {
			LinkedList<OutputLine> lines = readLines(file);
			
			for(OutputLine line : lines) {
				if(!map.containsKey(line.inputFile)) {
					map.put(line.inputFile, new LinkedList<OutputParser.OutputLine>());
				}
				
				map.get(line.inputFile).add(line);
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		OutputParser parser = new OutputParser();
		parser.printTablePaths();
		//parser.printTreeTables();
	}

	void printTreeTables() throws Exception {
		String[] arqs = { 
				"CHOPRA__randomUniformTree_resultados.txt", "B&P__randomUniformTree_resultados.txt",
				"CHOPRA__publishedInMathProg_resultados.txt", "B&P__publishedInMathProg_resultados.txt",
		};

		ArrayList[] outputs = leOutputs(arqs);

		for (int i = 0; i < arqs.length; i++) {
			System.out.println();
			filter(outputs[i], outputs[i + 1]);
			printTabela(outputs[i], outputs[++i], 5);
		}
	}
	void printTablePaths() throws Exception {
		String[] arqs = { 
				"CHOPRA__capa_resultados.txt",
				"B&P_LEAST_FRAC_capa_resultados.txt"
		//"CHOPRA__pathLikeIndSetRedFromRandomGraph_resultados.txt", 
		//"CHOPRA__pathLikeIndSetRedFromGeneralizedPetersen_resultados.txt",
		//"CHOPRA__pathLikeIndSetRedFromCycleComplement_resultados.txt",
		//"B&P__pathLikeIndSetRedFromRandomGraph_resultados.txt",
		//"B&P__pathLikeIndSetRedFromGeneralizedPetersen_resultados.txt",
		//"B&P__pathLikeIndSetRedFromCycleComplement_resultados.txt"	
		};
		ArrayList[] outputs = leOutputs(arqs);
		outputs[0].addAll(outputs[1]);	
		outputs[0].addAll(outputs[2]);
		outputs[3].addAll(outputs[4]);	
		outputs[3].addAll(outputs[5]);
		filter(outputs[0], outputs[3]);
		printTabela(outputs[0], outputs[3], 5);
	}
	

	void filter(ArrayList<OutputLine> chopra, ArrayList<OutputLine> branchAndPrice) {
		System.out.println(chopra.get(0).inputFile.replace("../input/convertidas/", "").replace("/*.txt", ""));

		HashMap<String, OutputLine[]> map = new HashMap<String, OutputParser.OutputLine[]>();

		for (OutputLine out : chopra) {
			OutputLine[] outs = new OutputLine[2];
			outs[0] = out;
			map.put(out.inputFile, outs);
		}

		for (OutputLine out : branchAndPrice) {
			OutputLine[] outs = map.get(out.inputFile);

			if (outs == null) {
				outs = new OutputLine[2];
				map.put(out.inputFile, outs);
			}

			outs[1] = out;
		}

		System.out.println("Chopra size: " + chopra.size());
		System.out.println("B&C size: " + branchAndPrice.size());
		for (OutputLine[] outs : map.values()) {
			if (outs[0] == null) {
				System.out.println("FALTOU NO chopra");
				System.out.println(outs[1].inputFile);
				branchAndPrice.remove(outs[1]);
			}

			if (outs[1] == null) {
				System.out.println("FALTOU NO B&C");
				System.out.println(outs[0].inputFile);
				System.out.println(outs[0].totalTime);
				chopra.remove(outs[0]);
			}
		}
		System.out.println("Chopra size: " + chopra.size());
		System.out.println("B&C size: " + branchAndPrice.size());

		Collections.sort(chopra);
		Collections.sort(branchAndPrice);
	}

	void printTabela(ArrayList<OutputLine> chopra, ArrayList<OutputLine> branchAndPrice, int tmpMin) {
		Comparator<OutputLine> comp = new Comparator<OutputParser.OutputLine>() {
			@Override
			public int compare(OutputLine o1, OutputLine o2) {
				return (o1.nVertices * o1.nColors) - (o2.nVertices * o2.nColors);
				//return (o1.totalTime) - (o2.totalTime);
			}
		};
		
		Collections.sort(chopra, comp);
		Collections.sort(branchAndPrice, comp);
		
		int nInstanciasFaceis = 0;
		int nInstanciasDificeis = 0;
		
		for (int i = 0; i < chopra.size(); i++) {
			OutputLine ch = chopra.get(i);
			OutputLine bc = branchAndPrice.get(i);

			if (!ch.isTimeOut && !bc.isTimeOut && ch.optSolVal != bc.optSolVal) {
				System.err.println("Eita pleura!");
				continue;
			}

			
			if(ch.totalTime <= tmpMin && bc.totalTime <= tmpMin) {
				nInstanciasFaceis++;
				continue;
			}
			
			double gap;

			if (ch.isTimeOut && bc.isTimeOut) {
				gap = -1;
				nInstanciasDificeis++;
				continue;
			} else {
				double opt = ch.optSolVal;
				double UB = ch.linearRelaxBound;

				if (ch.isTimeOut) {
					opt = bc.optSolVal;
					UB = bc.linearRelaxBound;
				}

				gap = UB - opt;
				gap = (100.0 * gap) / opt;
			}

			System.out.println(ch.nVertices * ch.nColors + "\t" + ch.nNonZeros + "\t" + bc.nNonZeros + "\t" + ch.timeToSolveLP + "\t" + bc.timeToSolveLP + "\t" + ch.totalTime + "\t" + bc.totalTime + "\t" + ch.nBBnodes + "\t" + bc.nBBnodes + "\t" + gap);
		}
		
		System.out.println("instancias faceis removidas: " + nInstanciasFaceis);
		System.out.println("instancias dificeis removidas: " + nInstanciasDificeis);
	}
	*/
}
