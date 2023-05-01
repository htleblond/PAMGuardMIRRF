package mirrfLiveClassifier;

import PamController.PamControlledUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import wmnt.WMNTDataBlock;
import wmnt.WMNTDataUnit;

public class LCUpdateProcess extends PamProcess {
	
	private LCControl lcControl;
	
	public LCUpdateProcess(LCControl lcControl) {
		super(lcControl, null);
		this.lcControl = lcControl;
		if (lcControl.getParams().updateProcessName.length() > 0) {
			this.setParentDataBlock(lcControl.getPamController().getDataBlock(WMNTDataBlock.class, lcControl.getParams().updateProcessName));
		}
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