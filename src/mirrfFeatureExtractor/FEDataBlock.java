package mirrfFeatureExtractor;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

import PamController.PamControlledUnit;
import PamView.GeneralProjector;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObserver;
import Spectrogram.SpectrogramDisplay;
import userDisplay.UserDisplayControl;
import whistlesAndMoans.AbstractWhistleDataBlock;

public class FEDataBlock extends PamDataBlock<FEDataUnit> {
	
	protected FEControl feControl;
	protected String[] featureNames;
	protected volatile boolean finished;
	
	private int fftLength;
	private int fftHop;
	
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
	
	public String[][] getFeatureList() {
		return feControl.getParams().featureList;
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	protected void setFinished(boolean inp) {
		finished = inp;
		if (inp) {
			System.out.println("FE: FINISHED");
		}
	}
	
	@Deprecated
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
	}
	
	public float getSampleRate() {
		return (float) 0.0;
	}
	
	public String getInputDataName() {
		return feControl.getParams().inputProcessName;
	}
}