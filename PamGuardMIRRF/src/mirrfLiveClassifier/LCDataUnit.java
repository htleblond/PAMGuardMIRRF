package mirrfLiveClassifier;

import PamguardMVC.PamDataUnit;

/**
 * The Live Classifier's output data unit.
 * @author Holly LeBlond
 */
public class LCDataUnit extends PamDataUnit {
	
	protected LCControl lcControl;
	protected LCCallCluster cluster;
	
	public LCDataUnit(LCControl lcControl, LCCallCluster cluster) {
		super(cluster.getStartAndEnd()[0], 0, -1, cluster.getStartAndEnd()[1] - cluster.getStartAndEnd()[0]);
		this.lcControl = lcControl;
		this.cluster = cluster;
		this.getBasicData().setFrequency(getFreqsHz());
		this.setSequenceBitmap(1);
	}
	
	/**
	 * @return The data unit's LCCallCluster object.
	 * Most useful info is contained here.
	 */
	public LCCallCluster getCluster() {
		return cluster;
	}
	
	/**
	 * @return Size-2 array of doubles containing the cluster's start and end times in seconds, respectively.
	 */
	public double[] getTimesInSeconds() {
		double[] outp = new double[2];
		outp[0] = Double.valueOf(String.valueOf(cluster.getStartAndEnd()[0]));
		outp[1] = Double.valueOf(String.valueOf(cluster.getStartAndEnd()[1]));
		return outp;
	}
	
	/**
	 * @return Size-2 array of doubles containing the cluster's lowest and highest frequencies, respectively.
	 */
	public double[] getFreqsHz() {
		double[] outp = new double[2];
		outp[0] = (double) cluster.getFreqLimits()[0];
		outp[1] = (double) cluster.getFreqLimits()[1];
		return outp;
	}
}