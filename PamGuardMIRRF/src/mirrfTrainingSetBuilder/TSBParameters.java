package mirrfTrainingSetBuilder;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import mirrfFeatureExtractor.FEParameters;

public class TSBParameters implements Serializable, Cloneable, ManagedParameters {
	
	public static final int OVERLAP_SKIP_BOTH = 0;
	public static final int OVERLAP_KEEP_BOTH = 1;
	
	public static final int MULTILABEL_KEEP_MOST = 0;
	public static final int MULTILABEL_KEEP_ALL = 1;
	public static final int MULTILABEL_SKIP_CLUSTER = 2;
	
	public boolean includeCallType;
	public int overlapOption;
	public int multilabelOption;
	
	public boolean skipLowFreqChecked;
	public int skipLowFreq;
	public boolean skipHighFreqChecked;
	public int skipHighFreq;
	public boolean skipShortDurChecked;
	public int skipShortDur;
	public boolean skipLongDurChecked;
	public int skipLongDur;
	
	// NOTE: The umbrella class stuff is probably best kept in TSBControl, as there won't be any training set stored in the parameters and classes are best dealt with once the set has been loaded.
	
	public TSBParameters() {
		this.overlapOption = OVERLAP_SKIP_BOTH;
		this.multilabelOption = MULTILABEL_KEEP_MOST;
		
		this.skipLowFreqChecked = false;
		this.skipLowFreq = 0;
		this.skipHighFreqChecked = false;
		this.skipHighFreq = 0;
		this.skipShortDurChecked = false;
		this.skipShortDur = 0;
		this.skipLongDurChecked = false;
		this.skipLongDur = 0;
	}

	@Override
	public PamParameterSet getParameterSet() {
		return PamParameterSet.autoGenerate(this);
	}
	
	@Override
	public TSBParameters clone() {
		try {
			return (TSBParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
}