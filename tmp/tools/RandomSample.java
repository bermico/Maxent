package density.tools;

import density.*;
import gnu.getopt.*;
import java.io.*;
import java.util.*;

public class RandomSample {

    static void startLine(String species, String x, String y) {
	for (int i=0; i<SampleSet.speciesIndex; i++) System.out.print(",");
	System.out.print(species);
	for (int i=0; i<SampleSet.xIndex - SampleSet.speciesIndex; i++) 
	    System.out.print(",");
	System.out.print(x + "," + y);
	for (int i=0; i<SampleSet.firstEnvVar - SampleSet.yIndex - 1; i++) 
	    System.out.print(",");
    }

    public static void main(String[] args) {
	if (args.length < 2) {
	    System.out.println("Usage: RandomSample numSamples layer1 layer2 ...");
	    System.exit(1);
	}
	try { 
	    new RandomSample().go(args);
	}
	catch (IOException e) { 
	    System.out.println("Error in RandomSample: " + e.toString());
	    System.exit(1);
	}
    }

    void go(String[] args) throws IOException {
	int ngrids = args.length-1;
	String[] fileNames = new String[ngrids];
	int numSamples = Integer.parseInt(args[0]);
	for (int i=0; i<ngrids; i++)
	    fileNames[i] = args[i+1];

	density.Utils.generator = new Random(0);
	Extractor extractor = new Extractor();
	extractor.extractSamples(fileNames, numSamples, null, null, new String[0]);
	Feature[] f = extractor.toFeatures();

	startLine("species","x","y");
	for (int i=0; i<ngrids; i++)
	    System.out.print(","+extractor.getLayers()[i].getName());
	System.out.println();
	java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance(Locale.US);
	nf.setGroupingUsed(false);
	nf.setMaximumFractionDigits(12);
	for (int i=0; i<f[0].getN(); i++) {
	    startLine("background", "" + nf.format(extractor.getX(i)), "" + nf.format(extractor.getY(i)));
	    for (int j=0; j<ngrids; j++) {
		double v = f[j].eval(i);
		if (v==(int)v) 
		    System.out.print(","+(int) v);
		else
		    System.out.print(","+v);
	    }
	    System.out.println();
	}
    }
}
