package wmnt;

import PamController.PamControlledUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

/**
 * Doesn't really do anything other than hold the output data block, as the WMNT only opens in Viewer Mode.
 * @author Holly LeBlond
 */
public class WMNTProcess extends PamProcess {

	public WMNTProcess(PamControlledUnit pamControlledUnit, PamDataBlock parentDataBlock) {
		super(pamControlledUnit, parentDataBlock);
		WMNTDataBlock db = new WMNTDataBlock("WMNT annotation updates", this, 0);
		this.addOutputDataBlock(db);
	}

	@Override
	public void pamStart() {
		// (Does nothing - WMNT only works in Viewer Mode.)
	}

	@Override
	public void pamStop() {
		// (Does nothing - WMNT only works in Viewer Mode.)
	}
	
}