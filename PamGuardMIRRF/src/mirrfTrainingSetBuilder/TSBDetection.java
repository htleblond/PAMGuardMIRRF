package mirrfTrainingSetBuilder;

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
	
	public TSBDetection(TSBControl tsbControl, int expectedVectorSize, String clusterID, String[] wmntData, String[] vector)
			throws AssertionError, Exception {
		this.tsbControl = tsbControl;
		assert wmntData.length >= 7;
		assert vector.length == expectedVectorSize;
		this.clusterID = clusterID;
		this.uid = Long.valueOf(wmntData[0]);
		assert tsbControl.convertDateStringToLong(wmntData[1]) != -1;
		this.datetime = wmntData[1];
		this.lf = Double.valueOf(wmntData[2]).intValue();
		this.hf = Double.valueOf(wmntData[3]).intValue();
		this.duration = Double.valueOf(wmntData[4]).intValue();
		//this.amplitude = Double.valueOf(wmntData[5]);
		this.species = wmntData[6];
		if (wmntData.length >= 8) this.callType = wmntData[7];
		if (wmntData.length >= 9) this.comment = wmntData[8];
		this.featureVector = new double[vector.length];
		for (int i = 0; i < vector.length; i++) this.featureVector[i] = Double.valueOf(vector[i]);
	}
	
	public TSBDetection(TSBControl tsbControl, int expectedVectorSize, String[] tsData) throws AssertionError, Exception {
		this.tsbControl = tsbControl;
		assert tsData.length == 8 + expectedVectorSize;
		this.clusterID = tsData[0];
		this.uid = Long.valueOf(tsData[1]);
		assert tsbControl.convertDateStringToLong(tsData[3]) != -1;
		this.datetime = tsData[3];
		this.lf = Double.valueOf(tsData[5]).intValue();
		this.hf = Double.valueOf(tsData[6]).intValue();
		this.duration = Double.valueOf(tsData[4]).intValue();
		this.species = tsData[7];
		this.featureVector = new double[tsData.length - 8];
		for (int i = 8; i < tsData.length; i++) this.featureVector[i-8] = Double.valueOf(tsData[i]);
	}
	
	public long getDateTimeAsLong() {
		return tsbControl.convertDateStringToLong(datetime);
	}
	
}