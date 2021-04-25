package columnGeneration;

import ilog.concert.IloRange;

public class Path implements Comparable<Path>{
	public int s;
	public int t;
	public int c;
	public double dualVarVal;
	public IloRange cut;

	Path(int s, int t, int c, IloRange cut) {
		this(s, t, c);
		this.cut = cut;
	}

	public Path(int s, int t, int c) {
		this.s = s;
		this.t = t;
		this.c = c;
	}
	
	public Path(Path p) {
		this(p.s, p.t, p.c);
	}

	@Override
	public int compareTo(Path o) {
		if(s == o.s) {
			return t - o.t;
		} else {
			return s - o.s;
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + c;
		result = prime * result + s;
		result = prime * result + t;
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
		Path other = (Path) obj;
		if (c != other.c)
			return false;
		if (s != other.s)
			return false;
		if (t != other.t)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "(" + s + ", " + t + ")-path with color " + c;   
	}

	public boolean contains(Path p) {
		return s <= p.s && t >= p.t;
	}
	
	public boolean intersects(Path p) {
		return !(p.t < s || p.s > t); 
	}
}
