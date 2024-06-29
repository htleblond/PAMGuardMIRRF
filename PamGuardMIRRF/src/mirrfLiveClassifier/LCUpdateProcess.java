package mirrfLiveClassifier;

import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import wmnt.WMNTDataBlock;
import wmnt.WMNTDataUnit;

/**
 * Updates the "actual species" column in the classifier's table when changes are made in the WMNT's species column.
 * @author Holly LeBlond
 */
public class LCUpdateProcess extends PamProcess {
	
	private LCControl lcControl;
	
	public LCUpdateProcess(LCControl lcControl) {
		super(lcControl, null);
		this.lcControl = lcControl;
		WMNTDataBlock wmntdb = lcControl.getParams().getWMNTUpdateDataBlock(lcControl);
		if (wmntdb != null)
			this.setParentDataBlock(wmntdb);
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		WMNTDataUnit du = (WMNTDataUnit) arg;
		//System.out.println("LCUpdateProcess.newData");
		lcControl.getTabPanel().getPanel().wmntUpdate(du);
	}
	
	@Override
	public void pamStart() {
		// (Does nothing - only used in Viewer Mode.)
	}

	@Override
	public void pamStop() {
		// (Does nothing - only used in Viewer Mode.)
	}
	
}