package columnGeneration;

public class Util {
	public static final double tolerance = 0.000001;

	// only for nonnegative numbers
	static public boolean areEqual(double a, double b) {
		return Math.abs(Math.abs(a) - Math.abs(b)) <= tolerance;
	}

	static public boolean isGreaterThan(double a, double b) {
		return Double.compare(a, b) > 0;
	}

	static public boolean isZero(double a) {
		return areEqual(a, 0.0);
	}

	static public boolean isOne(double a) {
		return areEqual(a, 1.0);
	}

	static public boolean isZeroOrOne(double a) {
		return isOne(a) || isZero(a);
	}
}
