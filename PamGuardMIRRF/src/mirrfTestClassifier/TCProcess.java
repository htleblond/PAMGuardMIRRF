package mirrfTestClassifier;

import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import mirrfLiveClassifier.LCDataBlock;
import mirrfLiveClassifier.LCDatagramProvider;
import mirrfLiveClassifier.LCProcess;

/**
 * The Test Classifier's PamProcess, which is only used for setting the output data block.
 * Subclass of the Live Classifier's PamProcess.
 * @author Holly LeBlond
 */
public class TCProcess extends LCProcess {

	public TCProcess(TCControl tcControl) {
		super(tcControl);
	}
	
	@Override
	protected void init() {
		resultsDataBlock = new LCDataBlock(getControl(), streamName, this, 0);
		this.addOutputDataBlock(resultsDataBlock);
		resultsDataBlock.setOverlayDraw(new TCOverlayGraphics(getControl(), resultsDataBlock));
		//resultsDataBlock.setBinaryDataSource(new TCBinaryDataSource(getControl(), resultsDataBlock, streamName));
		//resultsDataBlock.setShouldBinary(true); // TODO
		resultsDataBlock.setDatagramProvider(new LCDatagramProvider(getControl(), resultsDataBlock));
	}
	
	protected TCControl getControl() {
		return (TCControl) lcControl;
	}
	
	@Override
	public void newData(PamObservable o, PamDataUnit arg) {}
	
	@Override
	public void prepareProcess() {}
	
	@Override
	public void pamStart() {}
	
	@Override
	public void pamStop() {}
	
}