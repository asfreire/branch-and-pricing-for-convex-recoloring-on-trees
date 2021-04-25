package columnGeneration;

import java.util.Arrays;
import java.util.List;

public class SolChecker {
	InputData input;
	int[] color;
	boolean[] vis; 
	
	SolChecker(InputData input) {
		this.input = input;
		color = new int[input.nVertices];
		vis = new boolean[input.nVertices];
	}
	
	void check(List<Subtree> sol, double val) {
		Arrays.fill(color, -1);
		Arrays.fill(vis, false);
		double objVal = 0;
		
		for(Subtree t : sol) {
			for(int v: t.vertices) {
				if(color[v] != -1) 
					throw new RuntimeException("Same vertex appears in two or more subtrees");
				
				if(t.color >= input.nColors || t.color < 0)
					throw new RuntimeException("Vertex with invalid color");
				
				color[v] = t.color;
				objVal += input.getW(v, t.color);
			}
		}
		
		if(!Util.areEqual(objVal, val))
			throw new RuntimeException("Objective value is not correct");
			
		/*
		for(int u = 0; u < input.nVertices; u++) {
			if(color[u] == -1)
				throw new RuntimeException("Uncolored vertex");
		}
		*/
		
		for(int c = 0; c < input.nColors; c++) {
			for(int u = 0; u < input.nVertices; u++) {
				if(color[u] == c) {
					if(isColorConnected(u, c))
						break;
					else
						throw new RuntimeException("Has unconnected colors");
				}
			}			
		}
	}

	boolean isColorConnected(int u, int c) {
		visit(u, c);

		for(int v = 0; v < input.nVertices; v++) 
			if(color[v] == c && !vis[v]) 
				return false;
		
		return true;
	}
	
	void visit(int u, int c) {
		if(!vis[u] && color[u] == c) {
			vis[u] = true;
			
			for(int v : input.neighbours[u]) {
				visit(v, c);
			}
		}
	}
}
