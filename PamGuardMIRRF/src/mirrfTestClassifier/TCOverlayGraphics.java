package mirrfTestClassifier;

import PamController.PamControlledUnit;
import PamguardMVC.PamProcess;
import fftManager.FFTDataBlock;
import mirrfFeatureExtractor.FEDataBlock;
import mirrfLiveClassifier.LCDataBlock;
import mirrfLiveClassifier.LCOverlayGraphics;
import whistlesAndMoans.WhistleMoanControl;

/**
 * The Test Classifier's overlay graphics.
 * Subclass of the Live Classifier's overlay graphics.
 * @author Holly LeBlond
 */
public class TCOverlayGraphics extends LCOverlayGraphics {

	public TCOverlayGraphics(TCControl tcControl, LCDataBlock dataBlock) {
		super(tcControl, dataBlock);
		isViewer = true;
	}
	
	// TODO REPLACE THIS CRAP WITH THE ABILITY TO SELECT THE FFT BLOCK IN THE SETTINGS
	@Override
	protected FFTDataBlock retrieveFFTDataBlock() {
		try {
			for (int i = 0; i < getControl().getPamController().getNumControlledUnits(); i++) {
				PamControlledUnit pcu = lcControl.getPamController().getControlledUnit(i);
				if (!(pcu instanceof fftManager.PamFFTControl)) continue;
				for (int j = 0; j < pcu.getNumPamProcesses(); j++) {
					PamProcess pp = pcu.getPamProcess(j);
					for (int k = 0; k < pp.getNumOutputDataBlocks(); k++) {
						if (pp.getOutputDataBlock(k) == null) continue;
						if (pp.getOutputDataBlock(k) instanceof FFTDataBlock)
							return (FFTDataBlock) pp.getOutputDataBlock(k);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected TCControl getControl() {
		return (TCControl) lcControl;
	}
	
}