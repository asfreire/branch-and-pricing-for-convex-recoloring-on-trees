package columnGeneration;

public class BBNode {
	public ImplicitVar fracVarToBranch;
	public BBNode parent;
	public BBNode left;
	public BBNode right;
	public Integer UB;
	int level;
	double distFromInt;
	boolean proned;
	
	public BBNode(BBNode parent, boolean isLeft, int UB, double distFromInt) {
		this(parent, isLeft);
		this.UB = UB;
		this.distFromInt = distFromInt;
		
		if(parent == null) {
			level = 0;
		} else {
			level = parent.level + 1;
		}
	}
	
	public BBNode(BBNode parent, boolean isLeft) {
		this.parent = parent;

		if (parent != null) {
			if (isLeft) {
				parent.left = this;
			} else {
				parent.right = this;
			}
		}
	}
}
