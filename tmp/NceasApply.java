package density;
import java.io.*;
import java.util.*;

// Assumes each sample maps feature names to doubles
public class NceasApply {
    static String[] headers;
    static boolean cumulative = true, logistic = true, odds=false;

    static Sample[] readSamples(String sampleFile) throws IOException {
	final Csv csv = new Csv(sampleFile);
	headers = csv.headers();
	int i;
	for (i=0; i<headers.length; i++) if (headers[i].equals("")) break;
	final int last = i;  // deals with nz, which has blank fields at end
	final ArrayList samples = new ArrayList();
	csv.apply(csv.new Applier() {
		public void process() {
		    HashMap map = new HashMap();
		    for (int j=0; j<4; j++)
			map.put(headers[j], csv.get(j));
		    for (int j=4; j<last; j++) {
			Double val = new Double(Double.parseDouble(csv.get(j)));
			map.put(headers[j], val);
			map.put(headers[j]+"a", val);
		    }
		    samples.add(new Sample(0, 0, 0, 0, 0, "", map));
		}
	    });
	return (Sample[]) samples.toArray(new Sample[0]);
    }

    public static void main(String[] args) {
	if (args.length<3) {
	    System.out.println("Usage: NceasApply lambdaFile [lambdaFile2...] sampleFile outPrefix");
	    System.exit(0);
	}
	try {
	    String sampleFile = args[args.length-2], outPrefix = args[args.length-1];
	    Sample[] samples = readSamples(sampleFile);
	    for (int i=0; i<args.length-2; i++) {
		String lambdaFile = args[i];
		System.out.println(lambdaFile);
		String species = new File(lambdaFile).getName().replaceAll(".lambdas","").toLowerCase();
		GridSetDummy gs = new GridSetDummy();
		FeaturedSpace X = new FeaturedSpace(gs, lambdaFile, true);
		PrintWriter out = Utils.writer(outPrefix + species + ".csv");
		double[][] raw2cum=null;
		if (!logistic)
		    raw2cum = Project.readCumulativeIndex(Runner.raw2cumfile(lambdaFile));
		out.println("dataset,siteid,pred");
		double entropy = X.entropy;
		for (int j=0; j<samples.length; j++) {
		    double val = Math.exp(X.linearPredictor(samples[j]) - X.getLinearPredictorNormalizer()) / X.getDensityNormalizer();
		    if (logistic && entropy!=-1) {
			val = Project.logistic(val, entropy, 0.5);
			if (odds) val = val/(1-val);
		    }
		    else if (cumulative)
			val = Project.interpolateCumulative(raw2cum, val);
		    else {
			double max = raw2cum[0][raw2cum[0].length-1];
			if (val > max) val = max;
		    }
		    out.println(samples[j].featureMap.get("dataset") + "," + samples[j].featureMap.get("siteid") + "," + val);
		}
		out.close();
	    }
	}
	catch (IOException e) {
	    System.out.println("Error: " + e.toString());
	    System.exit(1);
	}
    }

}
