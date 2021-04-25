package columnGenerationForPaths;

public class Edge {
	private int u;
	private int v;
	public int index;

	public Edge(int u, int v) {
		this.u = Math.min(u, v);
		this.v = Math.max(u, v);
	}

	public Edge(int u, int v, int index) {
		this(u, v);
		this.index = index;
	}
	
	public int getU(){
		return u;
	}
	
	public int getV() {
		return v;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + u;
		result = prime * result + v;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		Edge other = (Edge) obj;
		if (u != other.u)
			return false;
		if (v != other.v)
			return false;
		return true;
	}
}
