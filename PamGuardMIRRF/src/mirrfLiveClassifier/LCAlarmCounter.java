package mirrfLiveClassifier;

import java.awt.Window;
import java.io.Serializable;
import java.util.ArrayList;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamguardMVC.PamDataUnit;
import alarm.AlarmControl;
import alarm.AlarmCounter;
import alarm.AlarmParameters;
import whistlesAndMoans.alarm.WMAlarmDialog;
import whistlesAndMoans.alarm.WMAlarmParameters;

public class LCAlarmCounter extends AlarmCounter implements PamSettings {
	
	protected LCControl lcControl;
	protected LCAlarmParameters params = new LCAlarmParameters();
	
	public LCAlarmCounter(AlarmControl alarmControl, LCControl lcControl) {
		super(alarmControl);
		this.lcControl = lcControl;
		PamSettingManager.getInstance().registerSettings(this);
	}
	
	@Override
	public double getValue(int countType, PamDataUnit dataUnit) {
		LCDataUnit du = (LCDataUnit) dataUnit;
		LCCallCluster cc = du.getCluster();
		String[] descriptors = new String[] {"Very low", "Low", "Average", "High", "Very high"};
		ArrayList<String> dList = new ArrayList<String>();
		for (int i = 0; i < descriptors.length; i++) dList.add(descriptors[i]);
		if (!params.selectedLabelsMap.containsKey(cc.getPredictedSpeciesString()) ||
				!params.selectedLabelsMap.get(cc.getPredictedSpeciesString()) ||
				cc.getSize() < params.minDetections ||
				dList.indexOf(lcControl.parameters.getLeadDescriptor(cc.getLead())) < dList.indexOf(params.minLead))
			return 0.0;
		if (countType == AlarmParameters.COUNT_SCORES) return cc.getLead();
		return 1.0;
	}

	@Override
	public void resetCounter() {}

	@Override
	public boolean hasOptions() {
		return true;
	}

	@Override
	public boolean showOptions(Window parent) {
		LCAlarmParameters newParams = LCAlarmDialog.showDialog(parent, lcControl, params);
		if (newParams != null) {
			params = newParams.clone();
			return true;
		}
		return false;
	}
	
	@Override
	public String getUnitName() {
		return getAlarmControl().getUnitName();
	}

	@Override
	public String getUnitType() {
		return "MIRRFLCAlarmParameters";
	}

	@Override
	public Serializable getSettingsReference() {
		return params;
	}

	@Override
	public long getSettingsVersion() {
		return 0;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		this.params = ((LCAlarmParameters) pamControlledUnitSettings.getSettings()).clone();
		return params != null;
	}
	
}