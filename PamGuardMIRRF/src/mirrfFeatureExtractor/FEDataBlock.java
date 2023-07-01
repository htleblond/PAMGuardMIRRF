package mirrfFeatureExtractor;

import PamguardMVC.PamDataBlock;

/**
 * The data block for the Feature Extractor.
 * @author Holly LeBlond
 */
public class FEDataBlock extends PamDataBlock<FEDataUnit> {
	
	protected FEControl feControl;
	protected String[] featureNames;
	protected volatile boolean finished;
	
	//private int fftLength;
	//private int fftHop;
	
	public FEDataBlock(FEControl feControl, String dataName, FEProcess feProcess, int channelMap) {
		super(FEDataUnit.class, dataName, feProcess, channelMap);
		this.feControl = feControl;
		featureNames = new String[feControl.getParams().featureList.length];
		for (int i = 0; i < featureNames.length; i++) {
			featureNames[i] = feControl.getParams().featureList[i][1];
		}
	}
	
	@Override
	public void addPamData(FEDataUnit du) {
		super.addPamData(du);
		System.out.println("REACHED FEDataBlock.addPamData");
	}
	
	// Just use the features specified in FEParameters via getFeatureList() instead.
	@Deprecated
	public void setFeatureNames(String[] inp) {
		featureNames = inp;
	}
	
	@Deprecated
	public void setFeatureNames(String[][] inp) {
		String[] outp = new String[inp.length];
		for (int i = 0; i < outp.length; i++) {
			outp[i] = inp[i][1];
		}
		featureNames = outp;
	}
	
	@Deprecated
	public String[] getFeatureNames() {
		return featureNames;
	}
	
	/**
	 * @return Nx2-sized 2D array of Strings.
	 * For each row, index 0 contains a feature's shorthand,
	 * whilst index 1 contains said features full name.
	 */
	public String[][] getFeatureList() {
		return feControl.getParams().featureList;
	}
	
	/**
	 * @return A clone of the module's FEParameters.
	 */
	public FEParameters getParamsClone() {
		return feControl.getParams().clone();
	}
	
	/**
	 * Signals whether or not the Feature Extractor has processed
	 * everything in its queue.
	 */
	public boolean isFinished() {
		return finished;
	}
	
	/**
	 * Sets the signal as to whether or not the Feature Extractor
	 * has processed everything in its queue.
	 */
	protected void setFinished(boolean inp) {
		finished = inp;
		if (inp) {
			System.out.println("FE: FINISHED");
		}
	}
	
/*	@Deprecated
	protected void setFFTLength(int inp) {
		fftLength = inp;
	}
	
	@Deprecated
	protected void setFFTHop(int inp) {
		fftHop = inp;
	}
	
	@Deprecated
	public int getFFTLength() {
		return fftLength;
	}
	
	@Deprecated
	public int getFFTHop() {
		return fftHop;
	} */
	
	/**
	 * May incorrectly return 0 if the Feature Extractor hasn't been run yet.
	 */
	public float getSampleRate() {
		return (float) feControl.getParams().sr;
	}
	
	/**
	 * @return The name of the block's input detection data process.
	 */
	public String getInputDataName() {
		return feControl.getParams().inputProcessName;
	}
}