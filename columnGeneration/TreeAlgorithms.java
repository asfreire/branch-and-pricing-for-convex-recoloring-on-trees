package columnGeneration;

import java.util.Collection;
import java.util.LinkedList;

public class TreeAlgorithms {
	static boolean[] visited;
	static InputData input;

	private TreeAlgorithms() {
	}

	static Subtree findSteinerTree(int c) {
		return findSteinerTree(input.sameColorVertices[c], c);
	}

	private static boolean[] cleanVisitedArray() {
		if (visited == null) {
			visited = new boolean[input.nVertices];
		} else {
			for (int i = 0; i < input.nVertices; i++) {
				visited[i] = false;
			}
		}

		return visited;
	}
	
	static Subtree findSteinerTree(Collection<Integer> vertices, int c) {
		@SuppressWarnings("unchecked")
		LinkedList<Integer>[] aux = new LinkedList[input.nVertices];
		cleanVisitedArray();
		int activeNodes = vertices.size();
		Subtree st = new Subtree(c);
		
		for(int u : vertices) {
			visited[u] = true;

			if(aux[input.level[u]] == null) {
				aux[input.level[u]] = new LinkedList<Integer>();
			}
			
			aux[input.level[u]].add(u);
		}
		
		for(int l = input.nVertices - 1; l >= 0; l--) {
			if(aux[l] != null) {
				if(activeNodes == 1) {
					st.addVertex(aux[l].getFirst(), input);
					break;
				}
				
				for(int u : aux[l]) {
					activeNodes--;
					st.addVertex(u, input);
					st.addEdge(input.getEdge(u, input.parent[u]));
					
					if(!visited[input.parent[u]]) {
						visited[input.parent[u]] = true;
						
						if(aux[l-1] == null) {
							aux[l-1] = new LinkedList<Integer>();
						}
						
						aux[l-1].add(input.parent[u]);
						activeNodes++;
					}
				}
			}
		}
		
		if(st.vertices.size() - 1 != st.edges.size()) throw new RuntimeException();
		return st;
	}
}
