package columnGeneration;

public class ColoredEdge {
	private Edge edge;
	private int color;
    
    ColoredEdge(Edge e, int c) {
    	edge = e;
        color = c;
    }

    public ColoredEdge(int u, int v, int c) {
    	this(new Edge(u, v), c);
	}
    
    public Edge getEdge() {
		return edge;
	}

    public int getU() {
		return edge.getU();
	}

    public int getV() {
		return edge.getV();
	}

	public int getColor() {
		return color;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + color;
		result = prime * result + ((edge == null) ? 0 : edge.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColoredEdge other = (ColoredEdge) obj;
		if (color != other.color)
			return false;
		if (edge == null) {
			if (other.edge != null)
				return false;
		} else if (!edge.equals(other.edge))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + edge.getU() + "," + edge.getV() + "," + color + ")";
	}
}
