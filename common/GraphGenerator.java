package common;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import columnGeneration.Edge;

public class GraphGenerator {
/*
	static Random rand = new Random();

	// 0 <= p <= 100
	static Graph generateRandomGraph(int nV, int p) {
		Graph g = new Graph(nV);

		// read the edges of the input graph and populate the adjacency list
		for (int u = 0; u < nV; u++) {
			for (int v = u + 1; v < nV; v++) {
				if (rand.nextInt(101) <= p) {
					g.addEdge(u, v);
				}
			}
		}

		return g;
	}

	static Graph generateColoredUniformRandomTree(int nV, int nC, int p) {
		Graph g = generateRandomGraph(nV, p);
		Graph t = new Graph(nV);
		Collections.shuffle(g.edges);
		int[] set = new int[nV];
		
		for(int i = 0; i < nV; i++) {
			set[i] = i;
			t.setColor(i, 1 + rand.nextInt(nC));
		}
		
		for(Edge e : g.edges) {
			if(set[e.u] != set[e.v]) {
				int a = set[e.u];
				
				// union 
				for(int i = 0; i < nV; i++) {
					if(set[i] == a) {
						set[i] = set[e.v];
					}
				}
				
				t.addEdge(e.u, e.v);
				
				if(t.edges.size() == nV - 1) {
					break;
				}
			}
		}
		
		return t;
	}
	
	static Graph generateGeneralizedPetersenGraph(int nTriangles) {
		int nInternalVertices = nTriangles * 3;
		int nV = nInternalVertices * 2;
		Graph g = new Graph(nV);

		
		for (int i = 0, j = nInternalVertices; i < nInternalVertices; i++, j++) {
			g.addEdge(i, j);
		}

		for(int i = nInternalVertices; i < nV-1; i++) {
			g.addEdge(i, i+1);
		}
		
		g.addEdge(nV-1, nInternalVertices);
		
		for(int i = 0; i < nInternalVertices; i++) {
			g.addEdge(i, (i+nTriangles) % nInternalVertices);
		}

		return g;
	}

	static Graph generateCycleComplement(int nV) {
		Graph g = new Graph(nV);

		// read the edges of the input graph and populate the adjacency list
		for (int u = 0; u < nV; u++) {
			for (int v = u + 2; v < nV; v++) {

				if (u == 0 && v == nV - 1)
					continue;

				g.addEdge(u, v);
			}
		}

		return g;
	}

	static Graph generateColoredRandomTree(int degree, int nV, int nC) {
		Graph g = new Graph(nV);
		int nextVertex = 0;
		LinkedList<Integer> queue = new LinkedList<Integer>();
		queue.add(nextVertex);
		g.setColor(nextVertex++, 1 + rand.nextInt(nC));

		while (!queue.isEmpty() && nextVertex < nV) {
			int u = queue.removeFirst();

			for (int i = 0; i < 2 * degree; i++) {
				if (rand.nextBoolean()) {
					int v = nextVertex++;
					g.setColor(v, 1 + rand.nextInt(nC));
					g.addEdge(u, v);
					queue.addLast(v);

					if (nextVertex == nV) {
						break;
					}
				}
			}
		}

		return g;
	}

	static Graph generatePath(int nV) {
		Graph path = new Graph(nV);

		for (int i = 0; i < path.nV - 1; i++) {
			path.addEdge(i, i + 1);
		}

		return path;
	}

	static Graph generateColoredPathLikeIndSetReduction(Graph g) {
		int nVertInPath = g.nV * 2 + g.nE * 2 + g.nV - 1;
		Graph path = generatePath(nVertInPath);
		int[] nextFreeIndexOfVertex = new int[g.nV];
		int nextAvailableColor = 1;
		int nextFreeIndex = 0;
		HashMap<Edge, Integer> edgeColor = new HashMap<Edge, Integer>();

		// place vertices u_1 and u_2 for each u in V
		// also place intermediate sub-paths N_i's
		for (int u = 0; u < g.nV; u++) {

			// we represent an intermediate sub-path by a single vertex
			// and forbid its color to be changed
			if (nextFreeIndex > 0) {
				path.setColor(nextFreeIndex - 1, Graph.FIXED_COLOR);
			}

			// leave |degree(u)| spaces between u_1 and u_2
			// these spaces will be filled in the next loop
			path.setColor(nextFreeIndex, nextAvailableColor);
			path.setColor(nextFreeIndex + g.adjList[u].size() + 1,
					nextAvailableColor++);

			nextFreeIndexOfVertex[u] = nextFreeIndex + 1;
			nextFreeIndex += g.adjList[u].size() + 3;
		}

		// place vertices e_uv and e_vu for each edge uv in E
		for (int u = 0; u < g.nV; u++) {
			for (Integer v : g.adjList[u]) {
				Edge e = new Edge(u, v);

				if (!edgeColor.containsKey(e)) {
					edgeColor.put(e, nextAvailableColor++);
				}

				path.setColor(nextFreeIndexOfVertex[u]++, edgeColor.get(e));
			}
		}

		return path;
	}
	*/
}
