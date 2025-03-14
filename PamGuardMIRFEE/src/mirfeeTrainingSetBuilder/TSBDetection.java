package mirfeeTrainingSetBuilder;

import mirfee.MIRFEEControlledUnit;

/**
 * Simple data object for storing individual data entries for output.
 * @author Holly LeBlond
 */
public class TSBDetection {
	
	protected TSBControl tsbControl;
	public String clusterID;
	public long uid;
	public String datetime;
	public int lf;
	public int hf;
	public int duration;
	//public double amplitude;
	public String species = "";
	public String callType = "";
	public String comment = "";
	public double[] featureVector;
	
	public TSBDetection(TSBControl tsbControl, int expectedVectorSize, String clusterID, String[] wmatData, String[] vector)
			throws AssertionError, Exception {
		this.tsbControl = tsbControl;
		assert wmatData.length >= 7;
		assert vector.length == expectedVectorSize;
		this.clusterID = clusterID;
		this.uid = Long.valueOf(wmatData[0]);
		assert MIRFEEControlledUnit.convertDateStringToLong(wmatData[1]) != -1;
		this.datetime = wmatData[1];
		this.lf = Double.valueOf(wmatData[2]).intValue();
		this.hf = Double.valueOf(wmatData[3]).intValue();
		this.duration = Double.valueOf(wmatData[4]).intValue();
		//this.amplitude = Double.valueOf(wmatData[5]);
		this.species = wmatData[6];
		if (wmatData.length >= 8) this.callType = wmatData[7];
		if (wmatData.length >= 9) this.comment = wmatData[8];
		this.featureVector = new double[vector.length];
		for (int i = 0; i < vector.length; i++) this.featureVector[i] = Double.valueOf(vector[i]);
	}
	
	public TSBDetection(TSBControl tsbControl, int expectedVectorSize, String[] tsData)
			throws AssertionError, Exception {
		this.tsbControl = tsbControl;
		assert tsData.length == 8 + expectedVectorSize;
		this.clusterID = tsData[0];
		this.uid = Long.valueOf(tsData[1]);
		assert MIRFEEControlledUnit.convertDateStringToLong(tsData[3]) != -1;
		this.datetime = tsData[3];
		this.lf = Double.valueOf(tsData[5]).intValue();
		this.hf = Double.valueOf(tsData[6]).intValue();
		this.duration = Double.valueOf(tsData[4]).intValue();
		this.species = tsData[7];
		this.featureVector = new double[tsData.length - 8];
		for (int i = 8; i < tsData.length; i++) this.featureVector[i-8] = Double.valueOf(tsData[i]);
	}
	
	public long getDateTimeAsLong() {
		return MIRFEEControlledUnit.convertDateStringToLong(datetime);
	}
	
	public long getEndTimeAsLong() {
		return getDateTimeAsLong() + this.duration;
	}
	
}