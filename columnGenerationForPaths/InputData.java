package columnGenerationForPaths;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Scanner;

public class InputData {
	public int nVertices;
	public int nColors;
	public LinkedList<Integer>[] neighbours;
	public Hashtable<Edge, Integer> edgesIndexes = new Hashtable<Edge, Integer>();
	public ArrayList<Edge> edges;
	public int[] color;
	public String inputFileName;
	public int[] parent;
	public int[][] dist;
	public ArrayList<Integer>[] sameColorVertices;
	public int root;
	public int[] level;
	boolean inputIsIncreasingPathFrom;
	boolean isCapaInstance;
	int w[][];

	// used to eliminate unused colors in input (it should not contain unused
	// colors...)
	private HashMap<Integer, Integer> mapColor = new HashMap<Integer, Integer>();

	@SuppressWarnings("unchecked")
	public void readFromFile(String fileName) throws Exception {
		Scanner in = new Scanner(new File(fileName));
		inputFileName = fileName;
		isCapaInstance = fileName.contains("capa") ? true : false;
		nVertices = in.nextInt();
		nColors = in.nextInt();
		sameColorVertices = new ArrayList[nColors];
		neighbours = new LinkedList[nVertices];
		edges = new ArrayList<Edge>(nVertices - 1);
		color = new int[nVertices];
		parent = new int[nVertices];
		level = new int[nVertices];
		dist = new int[nVertices][nVertices];
		int nextAvailableColor = 0;

		for (int u = 0; u < nVertices; u++) {
			dist[u][u] = 0;

			for (int v = u + 1; v < nVertices; v++) {
				dist[u][v] = dist[v][u] = -1;
			}
		}

		for (int c = 0; c < nColors; c++) {
			sameColorVertices[c] = new ArrayList<Integer>();
		}

		for (int u = 0; u < nVertices; u++) {
			neighbours[u] = new LinkedList<Integer>();
		}

		for (int i = 0; i < nVertices - 1; i++) {
			Edge e = new Edge(in.nextInt(), in.nextInt());
			edges.add(e);
			neighbours[e.getU()].add(e.getV());
			neighbours[e.getV()].add(e.getU());
		}

		int nextColor = 0;
		
		for (int u = 0; u < nVertices; u++) {
			color[u] = in.nextInt();
			
			// In capa instances, initially, the vertices have no color.
			// In this case, we can put any color in each vertex.
			// We decided to "use" all colors as below:
			if(isCapaInstance) {
				color[u] = nextColor;
				nextColor = (nextColor + 1) % nColors;
			}

			if (mapColor.containsKey(color[u])) {
				color[u] = mapColor.get(color[u]);
			} else {
				int c = nextAvailableColor++;
				mapColor.put(color[u], c);
				color[u] = c;
			}

			sameColorVertices[color[u]].add(u);
		}

		nColors = mapColor.size();
		sameColorVertices = Arrays.copyOf(sameColorVertices, nColors);
		resetEdgesIndexes();
		root = 0;
		buildRootedTree(root);
		
		if(isCapaInstance) {
			w = new int[nVertices][nColors];
			
			for(int c = 0; c < nColors; c++) {
				for(int u = 0; u < nVertices; u++) {
					w[u][c] = in.nextInt();
				}
			}
		}
		
		in.close();
		
		// verifies if the input graph is an "increasing path"
		inputIsIncreasingPathFrom = true;
		for (int u = 0; u < nVertices - 1; u++) {
			if (!neighbours[u].contains(u + 1)) {
				System.err.println("Warning: " + inputFileName
						+ " is not an increasing path.");
				inputIsIncreasingPathFrom = false;
				break;
			}
		}
	}

	void resetEdgesIndexes() {
		int indexCount = 0;
		edgesIndexes.clear();

		for (Edge e : edges) {
			e.index = indexCount++;
			edgesIndexes.put(e, e.index);
		}
	}

	public double getW(int u, int c) {
		if(isCapaInstance) {
			return w[u][c];
		} else {
			return color[u] == c ? 1.0 : 0.0;
		}
	}

	Edge getEdge(int u, int v) {
		return edges.get(edgesIndexes.get(new Edge(u, v)));
	}

	int getEdgesDist(Edge e, Edge f) {
		int minDist = nVertices + 1; // infinity
		minDist = Math.min(minDist, getDist(e.getU(), f.getU()));
		minDist = Math.min(minDist, getDist(e.getU(), f.getV()));
		minDist = Math.min(minDist, getDist(e.getV(), f.getU()));
		minDist = Math.min(minDist, getDist(e.getV(), f.getV()));
		return minDist;
	}

	private int getDist(int u, int v) {
		if (dist[u][v] == -1) {
			dist[u][v] = level[u] >= level[v] ? 1 + getDist(parent[u], v)
					: 1 + getDist(u, parent[v]);
			dist[v][u] = dist[u][v];
		}

		return dist[u][v];
	}

	private void buildRootedTree(int u) {
		// in the root node we have that "parent[u] = u"
		buildRootedTreeRec(u, u, 0);
	}

	private void buildRootedTreeRec(int u, int p_u, int level) {
		this.level[u] = level;
		parent[u] = p_u;

		for (int v : neighbours[u]) {
			if (v != p_u) {
				buildRootedTreeRec(v, u, level + 1);
			}
		}
	}
}
