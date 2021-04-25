package columnGeneration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class RandomHeuristic {
	InputData input;
	int[] color;
	static final int UNCOLORED = -1;
	HashSet<Integer> hasColoredNeighbor = new HashSet<Integer>();
	ArrayList<Integer> shufled;
	ArrayList<Integer>[] parents;
	LinkedList<Integer>[] sameColorVertices;
	LinkedList<Edge>[] sameColorEdges;
	Random rand = new Random();
	
	@SuppressWarnings("unchecked")
	RandomHeuristic(InputData input) {
		this.input = input;
		color = new int[input.nVertices];
		parents = new ArrayList[input.nVertices];
		shufled = new ArrayList<Integer>(input.nVertices);
		sameColorVertices = new LinkedList[input.nColors];
		sameColorEdges = new LinkedList[input.nColors];
		
		for (int c = 0; c < input.nColors; c++) {
			sameColorVertices[c] = new LinkedList<Integer>();
			sameColorEdges[c] = new LinkedList<Edge>();
		}

		for (int u = 0; u < input.nVertices; u++) {
			shufled.add(u);
			parents[u] = new ArrayList<Integer>(input.neighbours[u].size());
		}
	}

	private void resetColors() {
		hasColoredNeighbor.clear();

		for (int u = 0; u < input.nVertices; u++) {
			color[u] = UNCOLORED;
			parents[u].clear();
		}

		for (int c = 0; c < input.nColors; c++) {
			sameColorVertices[c].clear();
			sameColorEdges[c].clear();
		}
	}
	
	/*
	private void setEdgeColor(Edge e, int c) { 
		sameColorEdges[c].add(e);
	}
	
	private void setVertexColor(int u, int c) {
		color[u] = c;
		sameColorVertices[c].add(u);
		hasColoredNeighbor.remove(u);
		
		for (int v : input.neighbours[u]) {
			if (color[v] == UNCOLORED) {
				// it is a set (no problem if 'v' is already added)
				hasColoredNeighbor.add(v); 
				parents[v].add(u);
			}
		}
	}
	*/

	/*
	public void presetColors(Subtree t) {
		resetColors();
		for(int u : t.vertices) {
			setVertexColor(u, t.color);
		}

		for(Edge e : t.edges) {
			setEdgeColor(e, t.color);
		}
		
		for(int c = 0; c < input.nColors; c++) {
			if(t.color == c) {
				continue;
			}
			
			int size = input.sameColorVertices[c].size();
			int u;
			
			do {
				u = input.sameColorVertices[c].get(rand.nextInt(size));
			} while(color[u] != UNCOLORED);
			
			setColor(u, u, c);
		}
	}
	*/
	
	private void setColor(int u, int p_u, int c) {
		color[u] = c;
		sameColorVertices[c].add(u);
		hasColoredNeighbor.remove(u);

		if(u != p_u) {
			sameColorEdges[c].add(input.getEdge(u, p_u));
		}
		
		for (int v : input.neighbours[u]) {
			if (color[v] == UNCOLORED) {
				// it is a set (no problem if 'v' is already added)
				hasColoredNeighbor.add(v); 
				parents[v].add(u);
			}
		}
	}

	// 1. needs to set at least one color before calling it
	// 2. the induced subtree of the preset colors must be connected
	private void findConvexRecoloring() {
		Collections.shuffle(shufled);

		while (hasColoredNeighbor.size() > 0) {
			for (int u : shufled) {
				if (hasColoredNeighbor.contains(u)) {
					// chooses randomly a color from one of its parents
					int p_u = parents[u].get(rand.nextInt(parents[u].size()));
					setColor(u, p_u, color[p_u]);
				}
			}
		}
	}

	private Subtree getSubtree(int c) {
		Subtree t = new Subtree(c);

		for(int u: sameColorVertices[c]) {
			t.addVertex(u, input);
		}
		
		for(Edge e : sameColorEdges[c]) {
			t.addEdge(e);
		}
		
		return t;
	}
	
	private List<Subtree> solveWithPresetColors() {
		LinkedList<Subtree> sol = new LinkedList<Subtree>();
		findConvexRecoloring();
		
		for(int c = 0; c < input.nColors; c++) {
			sol.add(getSubtree(c));
		}
		
		return sol;
	}
	
	List<Subtree> solveWithoutPresetColors() {
		/*Subtree t = new Subtree(0);
		
		for(int u = 0; u < input.nVertices; u++) {
			t.addVertex(u, input);
		}
		
		for(Edge e : input.edges) {
			t.addEdge(e);
		}
		
		LinkedList<Subtree> sol = new LinkedList<Subtree>();
		sol.add(t);
		return sol;
		*/
		
		Random rand = new Random();
		resetColors();
		
		for(int c = 0; c < input.nColors; c++) {
			if(input.sameColorVertices[c].isEmpty())
				continue;
			int size = input.sameColorVertices[c].size();
			int u = input.sameColorVertices[c].get(rand.nextInt(size));
			setColor(u, u, c);
		}
		
		return solveWithPresetColors();
	}
}
