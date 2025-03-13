package mirfee;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * Parameters template for MIRFEE modules that use Python scripts.
 * @author Holly LeBlond
 */
//@SuppressWarnings("serial")
public abstract class MIRFEEParameters implements Serializable, Cloneable, ManagedParameters {
	
	public String tempFolder;
	public int tempKey;
	
	public MIRFEEParameters() {
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
	public MIRFEEParameters clone() {
		try {
			return (MIRFEEParameters) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}