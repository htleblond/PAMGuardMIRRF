package wmat;

import PamController.PamControlledUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

/**
 * Doesn't really do anything other than hold the output data block, as the WMAT only opens in Viewer Mode.
 * @author Holly LeBlond
 */
public class WMATProcess extends PamProcess {

	public WMATProcess(PamControlledUnit pamControlledUnit, PamDataBlock parentDataBlock) {
		super(pamControlledUnit, parentDataBlock);
		WMATDataBlock db = new WMATDataBlock("WMAT annotation updates", this, 0);
		this.addOutputDataBlock(db);
	}

	@Override
	public void pamStart() {
		// (Does nothing - WMAT only works in Viewer Mode.)
	}

	@Override
	public void pamStop() {
		// (Does nothing - WMAT only works in Viewer Mode.)
	}
	
}