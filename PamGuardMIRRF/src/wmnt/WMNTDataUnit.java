package wmnt;

import java.util.HashMap;

import PamguardMVC.PamDataUnit;

/**
 * Data unit for passing table update information to a MIRRF Classifier.
 * @author Holly LeBlond
 */
public class WMNTDataUnit extends PamDataUnit {
	
	/**
	 * Key should be the UID of an entry + ", " + the date/time of an entry formatted as "yyyy-MM-dd HH:mm:ss+SSS".
	 */
	public HashMap<String, WMNTAnnotationInfo> uidMap;
	
	public boolean clearAllFirst;
	public boolean startLoadingBar;
	public int totalRowsToUpdate;
	public boolean endLoadingBar;
	
	/**
	 * @param clearAllFirst - The results table and matrices in the MIRRF Classifier will be reset.
	 * @param startLoadingBar - Opens the MIRRF Classifier's loading window.
	 * @param totalRowsToUpdate - Total number of rows being updated (including ones not added to this unit).
	 */
	public WMNTDataUnit(boolean clearAllFirst, boolean startLoadingBar, int totalRowsToUpdate) {
		super(0);
		this.clearAllFirst = clearAllFirst;
		this.startLoadingBar = startLoadingBar;
		this.totalRowsToUpdate = totalRowsToUpdate;
		endLoadingBar = false;
		uidMap = new HashMap<String, WMNTAnnotationInfo>();
	}
	
	/**
	 * The MIRRF Classifier's loading window will close after the unit is processed.
	 */
	public void setEndLoadingBar(boolean boo) {
		endLoadingBar = boo;
	}
	
}