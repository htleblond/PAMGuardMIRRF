package wmnt;

import PamView.PamTable;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

/**
 * Data block for passing table update information to MIRRF Classifiers.
 * @author Taylor LeBlond
 */
public class WMNTDataBlock extends PamDataBlock<WMNTDataUnit> {
	
	private WMNTControl wmntControl;
	
	public WMNTDataBlock(String dataName, PamProcess parentProcess, int channelMap) {
		super(WMNTDataUnit.class, dataName, parentProcess, channelMap);
		this.wmntControl = (WMNTControl) parentProcess.getPamControlledUnit();
	}
	
	/**
	 * Does as usual, but it is recommended that updateLC be used instead
	 * of this due to occasional OutOfMemoryExceptions caused by too much
	 * data being fed into the unit's hash map at once (unless you can
	 * work around it). All data is immediately wiped from the block after
	 * being sent to the classifier, as it serves no other purpose.
	 * @author Taylor LeBlond
	 */
	@Override
	public void addPamData(WMNTDataUnit du) {
		addPamData(du, (long) 0);
	}
	
	/**
	 * Just use the other one, as the UID isn't even used.
	 * @author Taylor LeBlond
	 */
	@Override
	public void addPamData(WMNTDataUnit du, Long uid) {
		this.notifyObservers(du);
		this.clearAll();
	}
	
	/**
	 * Updates label and confusion matrix data for matching WMD detections in the MIRRF Live Classifier.
	 * @param doFullTable - If true, the entire table will be sent; otherwise, it will only do selected rows.
	 * @author Taylor LeBlond
	 */
	public void updateLC(boolean doFullTable) {
		final int loadnum = 1000;
		PamTable ttable = wmntControl.getSidePanel().getWMNTPanel().ttable;
		int[] selectedRows = ttable.getSelectedRows();
		if (doFullTable) {
			selectedRows = new int[ttable.getRowCount()];
			for (int i = 0; i < ttable.getRowCount(); i++) {
				selectedRows[i] = i;
			}
		}
		WMNTDataUnit du = new WMNTDataUnit(doFullTable, selectedRows.length >= loadnum, selectedRows.length);
		for (int i = 0; i < selectedRows.length; i++) {
			//System.out.println(String.valueOf(i)+" -> "+String.valueOf(selectedRows.length));
			du.uidMap.put(String.valueOf(ttable.getValueAt(selectedRows[i], 0))+", "+String.valueOf(ttable.getValueAt(selectedRows[i], 1)),
					new WMNTAnnotationInfo((String) ttable.getValueAt(selectedRows[i], 6),
										   (String) ttable.getValueAt(selectedRows[i], 7),
										   (String) ttable.getValueAt(selectedRows[i], 8)));
			// Sends it and resets the data unit every 1000th entry to prevent OutOfMemoryExceptions.
			if (i % loadnum == loadnum-1) {
				this.addPamData(du);
				du = new WMNTDataUnit(false, false, selectedRows.length);
			}
		}
		du.setEndLoadingBar(true);
		this.addPamData(du);
	}
}