package mirrfFeatureExtractor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class FEInputDataObject implements Serializable {
	
	public boolean isMIRRFTS;
	
	public String clusterID; // .mirrfts only
	public long uid;
	public String location; // .mirrfts only
	public String datetime;
	public int duration;
	public int amplitude; // .wmnt only
	public int lf;
	public int hf;
	public String label;
	public double[][] slicedata; // .wmnt only
	public HashMap<String, Double> problematicFeatures; // .mirrfts only
	
	public FEInputDataObject(String[] data, boolean isMIRRFTS, ArrayList<String> foundFeatureNames)
			throws NullPointerException, NumberFormatException, AssertionError {
		this.isMIRRFTS = isMIRRFTS;
		if (isMIRRFTS) {
			String[] split = data[0].split("-");
			assert split[0].length() == 2;
			Long.valueOf(split[1]);
			Long.valueOf(split[2]);
			this.clusterID = data[0];
			this.uid = Long.valueOf(data[1]);
			this.location = data[2];
			assert FEControl.convertDateStringToLong(data[3]) > -1;
			this.datetime = data[3];
			this.duration = Integer.valueOf(data[4]);
			this.lf = Integer.valueOf(data[5]);
			this.hf = Integer.valueOf(data[6]);
			this.label = data[7];
			this.problematicFeatures = new HashMap<String, Double>();
			for (int i = 0; i < foundFeatureNames.size(); i++)
				this.problematicFeatures.put(foundFeatureNames.get(i), Double.valueOf(data[i+8]));
		} else { // is .wmnt
			this.uid = Long.valueOf(data[0]);
			assert FEControl.convertDateStringToLong(data[1]) > -1;
			this.datetime = data[1];
			this.lf = Integer.valueOf(data[2]);
			this.hf = Integer.valueOf(data[3]);
			this.duration = Integer.valueOf(data[4]);
			this.amplitude = Integer.valueOf(data[5]);
			this.label = data[6];
			this.slicedata = new double[data.length-9][2];
			for (int i = 9; i < data.length; i++) {
				String[] split = data[i].split(">");
				slicedata[i-9][0] = Long.valueOf(split[0]);
				slicedata[i-9][1] = Double.valueOf(split[1]);
			}
		}
	}
	
}