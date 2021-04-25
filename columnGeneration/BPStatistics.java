package columnGeneration;

import common.Statistics;

public class BPStatistics extends Statistics {

	public int nDPs;
	
	public BPStatistics(String method, InputData input, Configuration config) {
		super(method, input, config);
	}

	@Override
	public void print() throws Exception {
		System.out.println("----------------------------------------");
		super.print();
		System.out.println("# DPs: " + nDPs);
		String timePerDP = String.format("%.8f", ((double)totalTime / nDPs));
		System.out.println("Total Time / nDPs: " + timePerDP);
		System.out.println("----------------------------------------");
	}
}
