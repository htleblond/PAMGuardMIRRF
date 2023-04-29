package mirrfLiveClassifier;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.ListIterator;

import PamController.PamControlledUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import binaryFileStorage.BinaryOfflineDataMap;
import binaryFileStorage.BinaryOfflineDataMapPoint;
import binaryFileStorage.BinaryStore;
import pamScrollSystem.ViewLoadObserver;

public class LCDataBlock extends PamDataBlock<LCDataUnit> {

	protected LCControl lcControl;

	public LCDataBlock(LCControl lcControl, String dataName, LCProcess lcProcess, int channelMap) {
		super(LCDataUnit.class, dataName, lcProcess, channelMap);
		this.lcControl = lcControl;
	}
	
	@Override
	public void addPamData(LCDataUnit du) {
		if (this.getUidHandler() != null) {
			addPamData(du, this.getUidHandler().getNextUID(du));
		} else {
			addPamData(du, 0L);
		}
	}
	
	@Override
	public void addPamData(LCDataUnit du, Long uid) {
		LCCallCluster cc = du.getCluster();
		if (lcControl.getParams().shouldIgnoreCluster(cc)) {
			if (lcControl.getParams().displayIgnored) {
				lcControl.getTabPanel().getPanel().addResultToTable(du);
			}
		} else {
			super.addPamData(du, uid);
			lcControl.getTabPanel().getPanel().addResultToTable(du);
		}
	}
	
	@Override
	synchronized public boolean loadViewerData(OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
		System.out.println("REACHED loadViewerData");
		for (int i = 0; i < lcControl.getPamController().getNumControlledUnits(); i++) {
			PamControlledUnit pcu = lcControl.getPamController().getControlledUnit(i);
			//System.out.println(pcu.getUnitName()+" : "+pcu.getUnitType());
			if (pcu.getUnitName().equals("Binary Store")) {
				BinaryStore bs = (BinaryStore) pcu;
				BinaryOfflineDataMap dataMap = (BinaryOfflineDataMap) this.getOfflineDataMap(bs);
				for (int j = 0; j < dataMap.getMapPoints().size(); j++) {
					BinaryOfflineDataMapPoint mp = dataMap.getMapPoints().get(j);
					//System.out.println("mp: "+mp.getBinaryFile(bs).getPath()+", "+
					//		String.valueOf(mp.getStartTime())+" -> "+String.valueOf(mp.getEndTime()));
					if (offlineDataLoadInfo.getStartMillis() > mp.getStartTime()) {
						offlineDataLoadInfo.setStartMillis(mp.getStartTime());
					}
					if (offlineDataLoadInfo.getEndMillis() < mp.getEndTime()) {
						offlineDataLoadInfo.setEndMillis(mp.getEndTime());
					}
				}
			}
		}
		System.out.println(String.valueOf(offlineDataLoadInfo.getStartMillis())+" -> "+String.valueOf(offlineDataLoadInfo.getEndMillis()));
		boolean loadOk = super.loadViewerData(offlineDataLoadInfo, loadObserver);
		return loadOk;
	}
	
	/**
	 * If present, retrieves the data unit with a call cluster with a matching
	 * clusterID and start date/time. Instead of using this function in a for-loop,
	 * it is recommended that retrieveDataUnitsByIDandDate be used instead for that
	 * task.
	 * @param clusterID
	 * @param datetime - Start date and time of cluster in UTC as string formatted as "yyyy-MM-dd HH:mm:ss+SSS".
	 * @return LCDataUnit
	 * @author Taylor LeBlond
	 */
	public LCDataUnit retrieveDataUnit(String clusterID, String datetime) {
		for (int i = 0; i < this.getUnitsCount(); i++) {
			LCDataUnit du = this.getDataUnit(i, this.REFERENCE_CURRENT);
			LCCallCluster cc = du.getCluster();
			if (clusterID.equals(cc.clusterID) && datetime.equals(lcControl.convertLocalLongToUTC(cc.getStartAndEnd()[0]))) {
				return du;
			}
		}
		return null;
	}
	
	/**
	 * If present, retrieves the data unit with a call cluster with a matching
	 * clusterID and start date/time. Instead of using this function in a for-loop,
	 * it is recommended that retrieveDataUnitsByIDandDate be used instead for that
	 * task.
	 * @param clusterID
	 * @param datetime - Start date and time of cluster in UTC as a long.
	 * @return LCDataUnit
	 * @author Taylor LeBlond
	 */
	public LCDataUnit retrieveDataUnit(String clusterID, Long datetime) {
		return retrieveDataUnit(clusterID, lcControl.convertLocalLongToUTC(datetime));
	}
	
	/**
	 * Returns a HashMap containing each LCDataUnit that match a clusterID and date/time
	 * in the input list. Each list entry should be clusterID+", "+date/time. The output
	 * key will also follow this format.
	 * The date/time string should be formatted as "yyyy-MM-dd HH:mm:ss+SSS".
	 * @param idsAndDates
	 * @return HashMap<String, LCDataUnit>
	 * @author Taylor LeBlond
	 */
	public HashMap<String, LCDataUnit> retrieveDataUnitsByIDandDate(ArrayList<String> idsAndDates) {
		HashMap<String, LCDataUnit> outp = new HashMap<String, LCDataUnit>();
		for (int i = 0; i < this.getUnitsCount(); i++) {
			LCDataUnit du = this.getDataUnit(i, this.REFERENCE_CURRENT);
			if (du != null) {
				LCCallCluster cc = du.getCluster();
				String key = cc.clusterID+", "+lcControl.convertLocalLongToUTC(cc.getStartAndEnd()[0]);
				if (idsAndDates.contains(key)) {
					outp.put(key, du);
				}
			}
		}
		return outp;
	}
	
	public HashMap<String, ArrayList<Long>> retrieveAllUIDsByIDandDate() {
		HashMap<String, ArrayList<Long>> outp = new HashMap<String, ArrayList<Long>>();
		for (int i = 0; i < this.getUnitsCount(); i++) {
			LCDataUnit du = this.getDataUnit(i, this.REFERENCE_CURRENT);
			LCCallCluster cc = du.getCluster();
			ArrayList<Long> currList = new ArrayList<Long>();
			for (int j = 0; j < cc.getSize(); j++) {
				currList.add(cc.uids[j]);
			}
			Collections.sort(currList);
			outp.put(cc.clusterID+", "+lcControl.convertLocalLongToUTC(cc.getStartAndEnd()[0]), currList);
		}
		return outp;
	}
	
	@Override
	public ListIterator<LCDataUnit> getListIterator(int inp){
		//ListIterator<LCDataUnit> outp = super.getListIterator(inp);
		//while (outp.hasNext()) {
			//System.out.println("ListIterator: "+String.valueOf(outp.next().getTimeMilliseconds()));
		//}
		ListIterator<LCDataUnit> outp = super.getListIterator(inp);
		return outp;
	}
}