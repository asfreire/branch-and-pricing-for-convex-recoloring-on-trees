package columnGeneration;

import java.util.HashMap;

public class Debug {
	static final boolean PRINT_ALL_DEBUGS = false;

	static void printOptPricing(Subtree t) {
		boolean PRINT_THIS = false;
		
		if (PRINT_ALL_DEBUGS && PRINT_THIS) {
			if (t != null) {
				System.out.println("Solution found by pricing");
				System.out.println("Color: " + t.color);
				System.out.println("Red Price: " + t.redPrice);
				System.out.println("Vertices: " + t.vertices);
				System.out.println("Path: " + t.path);
			}
		}
	}

	static void printProunedByBound() {
		boolean PRINT_THIS = true;
		
		if (PRINT_ALL_DEBUGS && PRINT_THIS) {
			System.out.println("BB node prouned by bound");
		}
	}

	static void printNewUB(double val) {
		boolean PRINT_THIS = true;
		
		if (PRINT_ALL_DEBUGS && PRINT_THIS) {
			System.out.println("new global LB = " + val);
		}
	}

	static void printColGenSolVal(double val) {
		boolean PRINT_THIS = true;
		
		if (PRINT_ALL_DEBUGS && PRINT_THIS) {
			System.out.println("col gen opt sol val: " + val);
		}
	}

	static void printBounds(double LB, double UB, int nBBNodes) {
		boolean PRINT_THIS = true;
		
		if (PRINT_ALL_DEBUGS && PRINT_THIS) {
			System.out.println("LB: " + LB);
			System.out.println("UB: " + UB);
			System.out.println("#BBN: " + nBBNodes);
		}
	}

	@SuppressWarnings("rawtypes")
	static void printFixedVars(HashMap implicitVarsToFix1, HashMap implicitVarsToFix0) {
		boolean PRINT_THIS = true;
		
		if (PRINT_ALL_DEBUGS && PRINT_THIS) {
			System.out.println("exploring children");
			System.out.println("Fix 1 (" + implicitVarsToFix1.size() + "): " + implicitVarsToFix1.values());
			System.out.println("Fix 0 (" + implicitVarsToFix0.size() + "): " + implicitVarsToFix0.values());
		}
	}
}
