package main;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import chopra.CRP_Chopra;
import columnGeneration.BranchAndPricePQ;
import columnGeneration.BranchingRule;
import columnGeneration.Configuration;
import columnGeneration.InputData;

public class Main {
	static String rootPath = "../input/"; // hulk
	enum METHOD {
		BP, BCP, EXTENDED;
	}
	
	enum DATASET {
		CAPA, TREEBASE, PETERSEN, CYCLECOMPL, RANDPATH, RANDTREE;  
	}
	
	enum BRANCHING_RULE {
		MOSTR_FRAC, LEAST_FRAC, ADVANCED;
	}

	static class ARGS {
		METHOD method;
		List<DATASET> datasets;
		List<BRANCHING_RULE> branchingRules;
		int timeLimit;
		boolean isSingleInstance;
		String file;
	}

	
	// static String rootPath = "input/convertidas/"; // minha maquina

	// ------------------------------------- COMANDOS PARA COPIAR CODIGOS NO SERVIDOR, COMPILAR E RODAR --------------------------------------------
	// no meu notebook (na src do projeto):
	// --> zip -r codigos.zip .
	// --> scp codigos.zip afreire@ime.usp.br:TEMP
	//
	// no ime.usp.br (na pasta TEMP):
	// --> scp codigos.zip chegado@hulk:2021_convex_recoloring/src
	//
	// no hulk: (na pasta 2021_convex_recoloring/src):
	// --> unzip codigos.zip
	// --> cd..
	// --> javac -d bin -classpath .:/opt/cplex/cplex125/cplex/lib/cplex.jar src/common/*.java src/chopra/*.java src/columnGeneration/*.java src/columnGenerationForPaths/*.java src/main/*.java
	// ABRIR UM SCREEN PARA CADA (na pasta "bin")
	// --> java -Xss50m -classpath .:/opt/cplex/cplex125/cplex/lib/cplex.jar -Djava.library.path=/opt/cplex/cplex125/cplex/bin/x86-64_sles10_4.1 main.Main ARGS
	// 	
	//export CR_CP=.:/opt/cplex/cplex125/cplex/lib/cplex.jar
	//export CR_LP=/opt/cplex/cplex125/cplex/bin/x86-64_sles10_4.1
	
	// java -Xss50m -classpath $CR_CP -Djava.library.path=$CR_LP main.Main method=bp brule=leastfrac dataset=capa timelimit=300  > stdout_bp_capa.txt
	
	//java -Xss50m -classpath $CR_CP -Djava.library.path=$CR_LP main.Main ARGS

	/*
	 ARGS: 
	  	dataset=capa OR treebase OR petersen OR cyclecompl OR randpath OR randtree 
		method=bp OR bcp OR extended
		brule=mostfrac OR leastfrac
		timelimit=3600
		instance=../input/capa/CAPA_Instances_Simulada_M125_N250-1.txt
		
		For each command, if there are more than one arg, separate them with ':'
	 */

	// --------------------------------------------------------------------------------------------------------------------------------------------

	// --------------------------------- COMANDOS PARA OBTER OS ARQUIVOS COM OS RESULTADOS DOS EXPERIMENTOS ---------------------------------------
	// no ime.usp.br (na pasta TEMP):
	// --> rm *experimentos.txt
	// --> scp chegado@hulk:2021_convex_recoloring/bin/*.txt .
	//
	// no meu notebook (na pasta "experimentos publicados"):
	// --> rm *experimentos.txt
	// --> scp afreire@ime.usp.br:TEMP/*.txt .
	// --------------------------------------------------------------------------------------------------------------------------------------------
	// no meu note, pra rodar tem que setar
	// --> -Djava.library.path=/Applications/CPLEX_Studio129/cplex/bin/x86-64_osx
	public static void main(String[] a) throws Exception {
		ARGS args = readArgs(a);
		List<Configuration> configs = new LinkedList<Configuration>();
		
		if(args.branchingRules == null) {
			configs.add(new Configuration(BranchingRule.Rule.LEAST_FRACTIONAL));
		} else {
			for(BRANCHING_RULE br : args.branchingRules) {
				if(br.equals(BRANCHING_RULE.ADVANCED))
					configs.add(new Configuration(BranchingRule.Rule.ADVANCED));
				if(br.equals(BRANCHING_RULE.MOSTR_FRAC))
					configs.add(new Configuration(BranchingRule.Rule.MOST_FRACTIONAL));
				if(br.equals(BRANCHING_RULE.LEAST_FRAC))
					configs.add(new Configuration(BranchingRule.Rule.LEAST_FRACTIONAL));
			}
		}
		
		for(Configuration config : configs) {
			config.TIME_LIMIT = args.timeLimit == 0 ? 3600 : args.timeLimit;
			
			if(args.isSingleInstance) {
				List<String> instances = new LinkedList<String>();
				instances.add(args.file);
				solveAllInstances(args.method, instances, config);
				continue;
			}
			
			for(DATASET dts : args.datasets) {
				if(dts.equals(DATASET.RANDTREE))
					solveAllInstances(args.method, getRandomUniformTreeInstances(), config);
				
				if(dts.equals(DATASET.RANDPATH))
					solveAllInstances(args.method, getPathFromRandomInstances(), config);
				
				if(dts.equals(DATASET.TREEBASE))
					solveAllInstances(args.method, getChopraInstances(), config);
				
				if(dts.equals(DATASET.CYCLECOMPL))
					solveAllInstances(args.method, getPathFromCycleComplementInstances(), config);
				
				if(dts.equals(DATASET.PETERSEN))
					solveAllInstances(args.method, getPathFromGenPetersenInstances(), config);
				
				if(dts.equals(DATASET.CAPA))
					solveAllInstances(args.method, getCapaInstances(), config);
			}
		}
	}
	
	static ARGS readArgs(String[] args) {
		ARGS a = new ARGS();
		
		for(int i = 0; i < args.length; i++) {
			String[] s = args[i].split("=");
			String cmd = s[0];

			if(cmd.equals("dataset")) {
				a.datasets = readDatasetArg(s[1]);
			} else if(cmd.equals("method")) {
				a.method = readMethodArg(s[1]);
			} else if(cmd.equals("brule")) {
				a.branchingRules = readBranchingRuleArg(s[1]);
			} else if(cmd.equals("timelimit")) {
				a.timeLimit = Integer.parseInt(s[1]);
			} else if(cmd.equals("instance")) {
				a.isSingleInstance = true;
				a.file = s[1];
			} else {
				throw new RuntimeException("Badly formatted arg: " + cmd);
			}
		}
		
		return a;
	}

	static List<BRANCHING_RULE> readBranchingRuleArg(String s) {
		LinkedList<BRANCHING_RULE> brules = new LinkedList<BRANCHING_RULE>();
		String[] brs = {s};
		
		if(s.contains(":")) 
			brs = s.split(":");
		
		for(int i = 0; i < brs.length; i++) {
			if(brs[i].equals("mostfrac"))  
				brules.add(BRANCHING_RULE.MOSTR_FRAC);
			else if(brs[i].equals("leastfrac"))
				brules.add(BRANCHING_RULE.LEAST_FRAC);
			else if(brs[i].equals("advanced"))
				brules.add(BRANCHING_RULE.ADVANCED);
			else
				throw new RuntimeException("Badly formatted arg: branching rule");
		}
		
		return brules;
	}

	static METHOD readMethodArg(String s) {
		if(s.equals("bp"))
			return METHOD.BP;
		else if(s.equals("bcp"))
			return METHOD.BCP;
		else if(s.equals("extended"))
			return METHOD.EXTENDED;
		
		throw new RuntimeException("Badly formatted arg: method");
	}

	static List<DATASET> readDatasetArg(String s) {
		LinkedList<DATASET> datasets = new LinkedList<DATASET>();
		String[] dts = {s};
		
		if(s.contains(":")) 
			dts = s.split(":");
		
		for(int i = 0; i < dts.length; i++) {
			if(dts[i].equals("capa"))  
				datasets.add(DATASET.CAPA);
			else if(dts[i].equals("cyclecompl"))
				datasets.add(DATASET.CYCLECOMPL);
			else if(dts[i].equals("petersen"))
				datasets.add(DATASET.PETERSEN);
			else if(dts[i].equals("randpath"))
				datasets.add(DATASET.RANDPATH);
			else if(dts[i].equals("randtree"))
				datasets.add(DATASET.RANDTREE);
			else if(dts[i].equals("treebase"))
				datasets.add(DATASET.TREEBASE);
			else
				throw new RuntimeException("Badly formatted arg: dataset");
		}
		
		return datasets;
	}

	static void solveAllInstances(METHOD method, List<String> fileNames, Configuration config) {
		for (String instance : fileNames) {
			try {
				solve(method, config, instance);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	static void solve(METHOD method, Configuration config, String fileName) throws Exception {
		if(method.equals(METHOD.EXTENDED))
			solveChopra(fileName, config);

		if(method.equals(METHOD.BP))
			solveBranchAndPrice(fileName, config);
				
		if(method.equals(METHOD.BCP)) 
			solveBranchAndPriceForPaths(fileName, config);
	}

	static void solveBranchAndPriceForPaths(String fileName, Configuration config)  throws Exception {
		// solve colgen for "hot start"
		InputData input = new InputData();
		input.readFromFile(rootPath + fileName);
		BranchAndPricePQ bp = new BranchAndPricePQ(input, config);
		bp.solveLP();
		
		// solve B&C&P for paths 
		columnGenerationForPaths.InputData in = new columnGenerationForPaths.InputData();
		in.readFromFile(rootPath + fileName);
		columnGenerationForPaths.ColumnGeneration bpfp = 
				new columnGenerationForPaths.ColumnGeneration(in, 
						new columnGenerationForPaths.Configuration(config));
		
		bpfp.solve(bp.masterLP.addedCols);
	}

	static void solveChopra(String fileName, Configuration config) throws Exception {
		InputData input = new InputData();
		input.readFromFile(rootPath + fileName);
		CRP_Chopra chopra = new CRP_Chopra();
		chopra.leEntradaGambi(input, config);
		chopra.solve();
	}

	static void solveBranchAndPrice(String fileName, Configuration config) throws Exception {
		InputData input = new InputData();
		input.readFromFile(rootPath + fileName);
		BranchAndPricePQ bp = new BranchAndPricePQ(input, config);
		bp.solve();
	}

	public static List<String> getCapaInstances() {
		List<String> instances = getAllInstancesInDir("capa");
		Collections.sort(instances, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				try{
					int i = o1.indexOf('N')+1;
					int f = o1.indexOf('-');
					int n1 = Integer.parseInt(o1.substring(i, f));
					i = o2.indexOf('N')+1;
					f = o2.indexOf('-');
					int n2 = Integer.parseInt(o2.substring(i, f));
					return n1 - n2;
				} catch(Exception e) {
					System.err.println(o1);
					System.err.println(o2);
					throw e;
				}
			}
		});
		
		return instances;
	}

	public static List<String> getAllInstancesInDir(String dirName) {
		LinkedList<String> fileNames = new LinkedList<String>();
		File dir = new File(rootPath + "/" + dirName);
		File[] files = dir.listFiles();

		for (File f : files) {
			fileNames.add(dirName + "/" + f.getName());
		}

		Collections.sort(fileNames);
		return fileNames;
	}

	
	public static List<String> getChopraInstances() {
		return getAllInstancesInDir("chopra");
		/*
		LinkedList<String> files = new LinkedList<String>();
		String[] id = { "Tr60729", "Tr60079", "Tr6287", "Tr4755", "Tr2400", "Tr4756", "Tr6038", "Tr53777", "Tr25470", "Tr69195", "Tr60915", "Tr57261", "Tr46272", "Tr73427", "Tr47159", "Tr48025"};
		int[] s = { 25, 50, 75 };
		int[] t = { 5, 50, 500 };

		for (int i = 0; i < id.length; i++) {
			for (int j = 0; j < s.length; j++) {
				for (int k = 0; k < t.length; k++) {
					String fileName = "chopra/" + id[i] + "_" + s[j] + "_" + t[k] + ".txt";
					files.add(fileName);
				}
			}
		}

		return files;
		*/
	}

	public static List<String> getRandomUniformTreeInstances() {
		return getAllInstancesInDir("randomUniformTree");
		/*
		LinkedList<String> files = new LinkedList<String>();
		String prefix = "randomUniformTree/RANDOM-UNIFORM-TREE-";

		for (int i = 1500; i <= 2500; i += 250) {
			for (int j = 50; j <= 500; j += 50) {
				for (int l = 1; l <= 5; l++) {
					String fileName = prefix + i + "-" + j + "-" + 50 + "-" + l + ".txt";
					files.add(fileName);
				}
			}
		}

		return files;
		*/
	}

	public static List<String> getPathFromCycleComplementInstances() {
		return getAllInstancesInDir("pathLikeIndSetRedFromCycleComplement");
		/*
		LinkedList<String> files = new LinkedList<String>();

		for (int i = 10; i <= 25; i++) {
			String fileName = "pathLikeIndSetRedFromCycleComplement/PATH-FROM-CYCLE-COMPLEMENT-" + i + ".txt";
			files.add(fileName);
		}

		return files;
		*/
	}

	public static List<String> getPathFromGenPetersenInstances() {
		return getAllInstancesInDir("pathLikeIndSetRedFromGeneralizedPetersen");
		
		/*
		LinkedList<String> files = new LinkedList<String>();

		for (int i = 2; i <= 25; i++) {

			String fileName = "pathLikeIndSetRedFromGeneralizedPetersen/PATH-FROM-GENERALIZED-PETERSEN-" + i + ".txt";

			if (i < 10) {
				fileName = "pathLikeIndSetRedFromGeneralizedPetersen/PATH-FROM-GENERALIZED-PETERSEN-0" + i + ".txt";
			}

			files.add(fileName);
		}

		return files;
		*/
	}

	public static List<String> getPathFromRandomInstances() {
		return getAllInstancesInDir("pathLikeIndSetRedFromRandomGraph");
		
		/*
		LinkedList<String> files = new LinkedList<String>();

		for (int i = 20; i <= 40; i += 10) {
			for (int j = 20; j <= 80; j += 20) {
				for (int k = 1; k <= 5; k++) {
					String fileName = "pathLikeIndSetRedFromRandomGraph/PATH-FROM-RANDOM-GRAPH-" + i + "-" + j + "-" + k + ".txt";
					files.add(fileName);
				}
			}
		}

		return files;
		*/
	}
}

/*
public static List<String> getGambi() {
	LinkedList<String> files = new LinkedList<String>();
	files.add("pathLikeIndSetRedFromRandomGraph/PATH-FROM-RANDOM-GRAPH-40-60-4.txt");
	files.add("pathLikeIndSetRedFromRandomGraph/PATH-FROM-RANDOM-GRAPH-40-80-4.txt");
	files.add("pathLikeIndSetRedFromRandomGraph/PATH-FROM-RANDOM-GRAPH-40-80-2.txt");
	files.add("pathLikeIndSetRedFromRandomGraph/PATH-FROM-RANDOM-GRAPH-40-80-5.txt");
	files.add("pathLikeIndSetRedFromRandomGraph/PATH-FROM-RANDOM-GRAPH-40-60-5.txt");
	files.add("pathLikeIndSetRedFromRandomGraph/PATH-FROM-RANDOM-GRAPH-40-80-1.txt");
	files.add("pathLikeIndSetRedFromRandomGraph/PATH-FROM-RANDOM-GRAPH-40-60-2.txt");
	files.add("pathLikeIndSetRedFromRandomGraph/PATH-FROM-RANDOM-GRAPH-40-60-3.txt");
	files.add("pathLikeIndSetRedFromRandomGraph/PATH-FROM-RANDOM-GRAPH-40-80-3.txt");
	return files;
}
*/

/*
public static List<String> getCRonPathsRandomInstances() {
	LinkedList<String> files = new LinkedList<String>();

	for (int n = 20; n <= 40; n += 5) {
		for (int alpha = 1; alpha <= 3; alpha++) {
			for (int j = 1; j <= 20; j++) {
				String fileName = "CRonPathsRandom/CR_ON_PATHS_RANDOM_" + n + "_" + alpha + "_" + j + ".txt";

				if (j < 10) {
					fileName = "CRonPathsRandom/CR_ON_PATHS_RANDOM_" + n + "_" + alpha + "_0" + j + ".txt";
				}

				files.add(fileName);
			}
		}
	}

	return files;
}
*/

