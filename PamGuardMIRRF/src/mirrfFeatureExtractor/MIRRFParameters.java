package mirrfFeatureExtractor;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

//@SuppressWarnings("serial")
public abstract class MIRRFParameters implements Serializable, Cloneable, ManagedParameters {
	
	public String tempFolder;
	public int tempKey;
	
	public MIRRFParameters() {
		this.tempFolder = "";
		this.tempKey = -1;
	}
	
	public String outputPythonParamsToText() {
		return "[]";
	}

	@Override
	public PamParameterSet getParameterSet() {
		return PamParameterSet.autoGenerate(this);
	}
	
	@Override
	public MIRRFParameters clone() {
		try {
			return (MIRRFParameters) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}