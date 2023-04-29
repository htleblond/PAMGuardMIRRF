package mirrfFeatureExtractor;

import java.io.Serializable;

public class FECallCluster implements Serializable, Cloneable {
	
	public String clusterID;
	public long[] uids;
	public long[] datetimes;
	public int[] durations;
	public int[] lfs;
	public int[] hfs;
	public double[][] featureVector;
	
	public FECallCluster(int size, int vectorSize) {
		this.clusterID = "";
		this.uids = new long[size];
		this.datetimes = new long[size];
		this.durations = new int[size];
		this.lfs = new int[size];
		this.hfs = new int[size];
		this.featureVector = new double[size][vectorSize];
	}
	
	public int getSize() {
		return uids.length;
	}
	
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