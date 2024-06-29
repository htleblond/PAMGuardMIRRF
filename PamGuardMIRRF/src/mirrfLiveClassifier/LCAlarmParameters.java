package mirrfLiveClassifier;

import java.io.Serializable;
import java.util.HashMap;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamguardMVC.dataSelector.DataSelectParams;

public class LCAlarmParameters extends DataSelectParams implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public HashMap<String, Boolean> selectedLabelsMap;
	public String minLead;
	public int minDetections;
	
	public LCAlarmParameters() {
		this.selectedLabelsMap = new HashMap<String, Boolean>();
		this.minLead = "Very low";
		this.minDetections = 1;
	}

	@Override
	public LCAlarmParameters clone()  {
		try {
			return (LCAlarmParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}