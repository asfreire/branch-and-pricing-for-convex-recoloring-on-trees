package columnGeneration;

public class ColoredVertex {
	int u;
	int color;
    
    ColoredVertex(int u, int c) {
    	this.u = u;
        color = c;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + color;
		result = prime * result + u;
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
		ColoredVertex other = (ColoredVertex) obj;
		if (color != other.color)
			return false;
		if (u != other.u)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "(" + u + "," + color + ")";
	}

}
