package mirfeeLiveClassifier;

import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import wmat.WMATDataBlock;
import wmat.WMATDataUnit;

/**
 * Updates the "actual species" column in the classifier's table when changes are made in the WMAT's species column.
 * @author Holly LeBlond
 */
public class LCUpdateProcess extends PamProcess {
	
	private LCControl lcControl;
	
	public LCUpdateProcess(LCControl lcControl) {
		super(lcControl, null);
		this.lcControl = lcControl;
		WMATDataBlock wmatdb = lcControl.getParams().getWMATUpdateDataBlock(lcControl);
		if (wmatdb != null)
			this.setParentDataBlock(wmatdb);
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		WMATDataUnit du = (WMATDataUnit) arg;
		//System.out.println("LCUpdateProcess.newData");
		lcControl.getTabPanel().getPanel().wmatUpdate(du);
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