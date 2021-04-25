package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class InstanceChecker {

	public static void main(String[] args) throws FileNotFoundException {
		check("input/chopra");
	}

	static void check(String dir) throws FileNotFoundException {
		File directory = new File(dir);
		File[] files = directory.listFiles();

		for (File file : files) {
			Scanner in = new Scanner(file);
			int nVertices = in.nextInt();
			int nColors = in.nextInt();

			for (int i = 0; i < nVertices - 1; i++) {
				in.nextInt();
				in.nextInt();
			}

			boolean[] usedColors = new boolean[nColors];

			for (int u = 0; u < nVertices; u++) {
				int color = in.nextInt();
				
				if(color < 0 || color > nColors -1) {
					System.out.println("Bad instance: " + file.getName() + " " + color + " of " + nColors);
					break;
				}
				usedColors[color] = true;
			}

			for (boolean b : usedColors) {
				if (!b) {
					System.out.println("Bad instance: " + file.getName());
					break;
				}
			}
			
			in.close();
		}
	}
}
