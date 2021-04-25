package columnGeneration;

import java.util.Arrays;
import java.util.List;

public class Heuristic {
	InputData input;
	int[] color;
	boolean[] vis;
	static final int UNCOLORED = -1;
	
	@SuppressWarnings("unchecked")
	Heuristic(InputData input) {
		this.input = input;
		color = new int[input.nVertices];
		vis = new boolean[input.nVertices];
	}
	
	List<Subtree> solve() {
		for(int u = 0; u < input.nVertices; u++) {
			int max_gain_c = 0;
			
			for(int c = 0; c < input.nColors; c++) {
				if(input.getW(u, c) > input.getW(u, max_gain_c)) {
					max_gain_c = c;
				}
			}
			
			color[u] = max_gain_c;
		}
		
		Arrays.fill(vis, false);
		
		while(true) {
			for(int c = 0; c < input.nColors; c++) {
				if(!isColorConnected(c)) {
					
				}
			}
		}
		
	}
	
	boolean isColorConnected(int c) {
		for(int u = 0; u < input.nVertices; u++) {
			if(color[u] == c) {
				visit(u, c);
				
				for(int v = 0; v < input.nVertices; v++) 
					if(color[v] == c && !vis[v]) 
						return false;

				return true;
			}
		}
		
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
