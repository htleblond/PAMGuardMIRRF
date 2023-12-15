package mirrfFeatureExtractor;

import java.io.Serializable;

/**
 * MIRRF sorts contours into "call clusters". Any contours within the
 * specified "join distance" should be contained in the same call cluster.
 * <br><br>
 * This object is meant to be placed in an FEDataUnit.
 * @author Holly LeBlond
 */
//@SuppressWarnings("serial")
public class FECallCluster implements Serializable, Cloneable {
	
	public String clusterID;
	public long[] uids;
	public String[] locations; // only used with .mirrfts as input
	public long[] datetimes;
	public int[] durations;
	public int[] lfs;
	public int[] hfs;
	public String[] labels; // only used with .mirrfts as input
	public double[][] featureVector;
	
	/**
	 * Creates a call cluster with a fixed number of contours.
	 * <br><br>
	 * A cluster ID should be manually assigned afterwards, and
	 * UIDs, dates/times, durations, frequencies and feature vectors
	 * for each individual contour should be manually assigned
	 * afterwards as well.
	 * @param size - Number of contours in the call cluster.
	 * @param vectorSize - Number of features in the vector.
	 */
	public FECallCluster(int size, int vectorSize) {
		this.clusterID = "";
		this.uids = new long[size];
		this.locations = new String[size];
		this.datetimes = new long[size];
		this.durations = new int[size];
		this.lfs = new int[size];
		this.hfs = new int[size];
		this.labels = new String[size];
		this.featureVector = new double[size][vectorSize];
	}
	
	/**
	 * The number of contours in the call cluster.
	 */
	public int getSize() {
		return uids.length;
	}
	
	/**
	 * @return Size-2 long array containing the start and end times of
	 * cluster in milliseconds from 1970.
	 */
	public long[] getStartAndEnd() {
		long[] outp = new long[] {datetimes[0], datetimes[0]};
		for (int i = 0; i < datetimes.length; i++) {
			if (datetimes[i] < outp[0]) {
				outp[0] = datetimes[i];
			}
			for (int j = 0; j < durations.length; j++) {
				if (datetimes[i]+durations[j] > outp[1]) {
					outp[1] = datetimes[i]+durations[j];
				}
			}
		}
		return outp;
	}
	
	/**
	 * @return Size-2 int array containing the lowest and highest
	 * frequencies in the cluster.
	 */
	public int[] getFreqLimits() {
		int[] outp = new int[2];
		outp[0] = lfs[0];
		outp[1] = hfs[0];
		for (int i = 1; i < lfs.length; i++) {
			if (lfs[i] < outp[0]) {
				outp[0] = lfs[i];
			}
			if (hfs[i] > outp[1]) {
				outp[1] = hfs[i];
			}
		}
		return outp;
	}
}