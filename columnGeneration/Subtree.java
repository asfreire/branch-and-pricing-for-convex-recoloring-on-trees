package columnGeneration;

import ilog.concert.IloConversion;
import ilog.concert.IloNumVar;

import java.util.ArrayList;
import java.util.Collections;

public class Subtree {
	IloNumVar var;
	IloConversion conv;
	double w;
	public ArrayList<Integer> vertices;
	ArrayList<Edge> edges;
	public int color;
	double redPrice;
	boolean sorted;
	public double value;
	Path path = null;

	Subtree(Path p, double redPrice, InputData input) {
		this(p, input);
		this.redPrice = redPrice;
	}
	
	Subtree(int nVertices, int color) {
		this.color = color;
		vertices = new ArrayList<Integer>(nVertices);
		edges = new ArrayList<Edge>(nVertices - 1);
	}

	Subtree(int color) {
		this.color = color;
		vertices = new ArrayList<Integer>();
		edges = new ArrayList<Edge>();
	}

	public Subtree(Path p, InputData input) {
		this(p.t - p.s + 1, p.c);

		for (int u = p.s; u <= p.t; u++) {
			addVertex(u, input);
		}

		for (int u = p.s; u < p.t; u++) {
			addEdge(input.getEdge(u, u + 1));
		}
	}

	void addVertex(int u, InputData input) {
		vertices.add(u);
		w += input.getW(u, color);

		if (path == null) {
			path = new Path(u, u, color);
		} else {
			path.s = Math.min(path.s, u);
			path.t = Math.max(path.t, u);
		}
	}

	void addEdge(Edge e) {
		edges.add(e);
	}

	boolean containsVertex(int u) {
		if (!sorted) {
			Collections.sort(vertices);
			sorted = true;
		}

		int index = Collections.binarySearch(vertices, u);
		return index >= 0 && index < vertices.size();
	}

	// ------ these methods can be called only if the input graph is an increasing path -------
	boolean intersects(Subtree t) {
		return path.intersects(t.path);
	}

	boolean contains(Subtree t) {
		return path.contains(t.path);
	}
	// ----------------------------------------------------------------------------------------
}
