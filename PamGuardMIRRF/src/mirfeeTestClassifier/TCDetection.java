package mirfeeTestClassifier;

/**
 * Data object for single data entries directly from the training set.
 * @author Holly LeBlond
 */
public class TCDetection {
	
	public String clusterID;
	public long uid;
	public String location;
	public String datetime;
	public int duration;
	public int lf;
	public int hf;
	public String species;
	public double[] featureVector;
	
	public TCDetection(String[] row) {
		clusterID = row[0];
		uid = Long.valueOf(row[1]);
		location = row[2];
		datetime = row[3];
		duration = Double.valueOf(row[4]).intValue();
		lf = Double.valueOf(row[5]).intValue();
		hf = Double.valueOf(row[6]).intValue();
		species = row[7];
		featureVector = new double[row.length-8];
		for (int i = 8; i < row.length; i++) featureVector[i-8] = Double.valueOf(row[i]);
	}
}