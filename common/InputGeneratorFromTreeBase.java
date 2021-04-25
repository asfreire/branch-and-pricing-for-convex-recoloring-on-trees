package common;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class InputGeneratorFromTreeBase {

	static final String ROOT_DIRECTORY = "input/";

	static int nextColor;
	static int nextIndex;
	static Random random = new Random();
	static int[] NOISE, MUTABILITY;

	static Comparator<Node> compHeight = new Comparator<Node>() {
		@Override
		public int compare(Node o1, Node o2) {
			return new Integer(o2.height).compareTo(o1.height);
		}
	};

	static class Node {
		int index;
		List<Node> children = new LinkedList<InputGeneratorFromTreeBase.Node>();
		Node father;
		int level;
		int height;
		int color;

		// add as leaf
		Node(Node father) {
			this.father = father;

			if (father != null) {
				father.children.add(this);
				level = father.level + 1;
			}

			setHeight(0);
		}

		void setHeight(int height) {
			this.height = height;

			if (this.father != null) {
				if (father.height <= this.height) {
					father.setHeight(this.height + 1);
				}
			}
		}
	}

	static void applyNoise(Node root, int noise, int numberOfColors) {
		int r = random.nextInt(100);

		if (r <= noise) {
			root.color = random.nextInt(numberOfColors);
		}

		for (Iterator<Node> it = root.children.iterator(); it.hasNext();) {
			applyNoise(it.next(), noise, numberOfColors);
		}
	}

	/* 1 <= p <= 999 */
	static void generateColorsAndIndexes(Node root, int p) {
		nextColor = 1;
		nextIndex = 0;
		root.color = 0;
		generateColorsAndIndexesRec(root, p);
	}

	static void generateColorsAndIndexesRec(Node subroot, int p) {
		subroot.index = nextIndex++;

		for (Iterator<Node> it = subroot.children.iterator(); it.hasNext();) {
			Node child = it.next();

			int r = random.nextInt(1000);

			// mutation?
			if (r <= p) {
				child.color = nextColor++;
			} else {
				child.color = subroot.color;
			}
		}

		for (Iterator<Node> it = subroot.children.iterator(); it.hasNext();) {
			generateColorsAndIndexesRec(it.next(), p);
		}
	}

	static LinkedList<Node> getSubtreeVertices(Node tag) {
		LinkedList<Node> subtree = new LinkedList<InputGeneratorFromTreeBase.Node>();
		getSubtreeVerticesRec(tag, subtree);
		return subtree;
	}

	static void getSubtreeVerticesRec(Node tag, LinkedList<Node> subtree) {
		subtree.add(tag);

		for (Iterator<Node> it = tag.children.iterator(); it.hasNext();) {
			Node child = it.next();
			getSubtreeVerticesRec(child, subtree);
		}
	}

	static void printTree(LinkedList<Node> tree, Node root, String file) throws FileNotFoundException {
		for (int iNoise = 0; iNoise < NOISE.length; iNoise++) {
			int noise = NOISE[iNoise];
			for (int iMutability = 0; iMutability < MUTABILITY.length; iMutability++) {
				int mutability = MUTABILITY[iMutability];
				String filename = file + "_" + noise + "_" + mutability + ".txt";

				PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File(ROOT_DIRECTORY + "chopra/" + filename), false)));

				generateColorsAndIndexes(root, mutability);
				int nVertices = tree.size();
				int nColors = nextColor;
				applyNoise(root, noise, nColors);

				// color[i] = has the color of vertex i
				int[] color = new int[nVertices];
				for (Iterator<Node> it = tree.iterator(); it.hasNext();) {
					Node tag = it.next();
					color[tag.index] = tag.color;
				}
				
				nColors = removeUnusedColors(nVertices, color);

				out.println(nVertices + " " + nColors);

				System.out.println(filename + " " + nVertices + " " + nColors);

				printEdges(root, out);

				// print colors
				for(int c : color) {
					out.println(c);
				}

				out.close();
			}
		}
	}

	static int removeUnusedColors(int nVertices, int[] color) {
		int nextAvailableColor = 0;
		HashMap<Integer, Integer> mapColor = new HashMap<Integer, Integer>();

		for (int u = 0; u < nVertices; u++) {
		      if (mapColor.containsKey(color[u])) {
		            color[u] = mapColor.get(color[u]);
		      } else {
		            mapColor.put(color[u], nextAvailableColor);
		            color[u] = nextAvailableColor++;
		     }
		}
		
		return nextAvailableColor;
	}
	

	static void printEdges(Node tag, PrintStream out) {
		for (Iterator<Node> it = tag.children.iterator(); it.hasNext();) {
			Node child = it.next();
			out.println(tag.index + " " + child.index);
			printEdges(child, out);
		}
	}

	static void generateTreeBase() throws Exception {
		String[] files = { "Tr60729", "Tr60079", "Tr6287", "Tr4755", "Tr2400", "Tr4756", "Tr6038", "Tr53777", "Tr25470", "Tr69195", "Tr60915", "Tr57261", "Tr46272", "Tr73427", "Tr47159", "Tr48025" };

		for (int file = 0; file < files.length; file++) {
			Node root = readTREEBASE_and_generateTree(files[file]);

			try {
				printTree(getSubtreeVertices(root), root, files[file]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	static Node readTREEBASE_and_generateTree(String file) throws Exception {
		Scanner in = null;

		try {
			in = new Scanner(new File(ROOT_DIRECTORY + "TREEBASE_ORG_DATA/" + file + ".txt"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		System.out.println("FILE: " + file);
		StringBuffer input = new StringBuffer();

		while (in.hasNextLine()) {
			input.append(in.nextLine().trim());
		}

		LinkedList<Node> stack = new LinkedList<InputGeneratorFromTreeBase.Node>();
		Node root = new Node(null);
		stack.addLast(root);

		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) == '(') {
				stack.addLast(new Node(stack.getLast()));
			} else if (input.charAt(i) == ',') {
				stack.removeLast();

				if (input.charAt(i) != '(') {
					stack.addLast(new Node(stack.getLast()));
				}
			} else if (input.charAt(i) == ')') {
				stack.removeLast();
			}
		}

		return root;
	}

	public static void main(String[] args) throws Exception {
		/*NOISE = new int[] { 25, 50, 75 };
		MUTABILITY = new int[] { 5, 50, 500 };
		generateTreeBase();*/
		
		for(int n = 50; n <= 1000; n+=50) {
			int k_baixo = (n * 25) / 100;
			int k_medio = (n * 50) / 100;
			int k_alto = (n * 75) / 100;
			int[] k = {k_baixo, k_medio, k_alto};
			
			for(int i = 0; i < k.length; i++) {
				String[] file = {"capa/CAPA_Instances_Simulada_M" + k[i] + "_N" + n + "-1.txt", 
								 "capa/CRP_Instance_Simulada_M" + k[i] + "_N" + n + "-1.txt"};

				String[] conv = {"conv/CAPA_Instances_Simulada_M" + k[i] + "_N" + n + "-1.txt", 
						 "conv/CRP_Instance_Simulada_M" + k[i] + "_N" + n + "-1.txt"};

				for(int j = 0; j < file.length; j++) {
					Scanner in = new Scanner(new File(file[j]));
					PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(conv[j]), false)));
					out.println(n + " " + k[i]);

					for(int u = 0; u < n - 1; u++)
						out.println(u + " " + (u+1));
					
					for(int u = 0; u < n; u++)
						out.println("-1");
					
					for(int c = 0; c < k[i]; c++) {
						for(int u = 0; u < n; u++) {
							int x = (int) in.nextDouble();
							out.print(x + " ");
						}
						
						out.println();
					}
					
					out.close();
					in.close();
				}
			}
			
			
			
			/*
			System.out.println("java -jar RunGerarInstancia.jar ~/capa/ ~/capa/ 2 1 " + 
					k_baixo + " " +  n);
			System.out.println("java -jar RunGerarInstancia.jar ~/capa/ ~/capa/ 2 1 " + 
					k_medio + " " +  n);
			System.out.println("java -jar RunGerarInstancia.jar ~/capa/ ~/capa/ 2 1 " + 
					k_alto + " " +  n);
					*/
		}
	}
}
