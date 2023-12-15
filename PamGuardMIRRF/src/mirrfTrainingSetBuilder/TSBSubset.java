package mirrfTrainingSetBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Data object for storing subset data.
 * @author Holly LeBlond
 */
public class TSBSubset implements Cloneable {
	
	public String featurePath;
	public String wmntPath;
	public String id;
	public String location;
	public String start;
	public String end; // TODO NOTE THAT THIS IS JUST THE START TIME OF THE LAST CONTOUR - MAKE SURE THIS DOESN'T CAUSE ANY PROBLEMS !!!
	public ArrayList<String> classList;
	public int[] selectionArray;
	public ArrayList<ArrayList<TSBDetection>> validEntriesList;
	
	public TSBSubset() {
		this.featurePath = "";
		this.wmntPath = "";
		this.id = "";
		this.location = "";
		this.start = "";
		this.end = "";
		this.classList = new ArrayList<String>();
		this.selectionArray = new int[0];
		this.validEntriesList = new ArrayList<ArrayList<TSBDetection>>();
	}
	
	/**
	 * Adds a TSBDetection to the corresponding list in validEntriesList.
	 * Also changes start and end dates if the TSBDetection falls outside the range. 
	 * @param inp - The input TSBDetection.
	 * @param selected - If species not previously in classList, adds the corresponding int to selectionArray if true.
	 */
	public void addEntry(TSBDetection inp, boolean selected) {
		if (start.length() == 0 || start.compareTo(inp.datetime) > 0) start = inp.datetime;
		if (end.length() == 0 || end.compareTo(inp.datetime) < 0) end = inp.datetime;
		if (!classList.contains(inp.species)) {
			classList.add(inp.species);
			if (selected) {
				int[] newSelectionArray = new int[selectionArray.length+1];
				for (int i = 0; i < selectionArray.length; i++) newSelectionArray[i] = selectionArray[i];
				newSelectionArray[newSelectionArray.length-1] = classList.indexOf(inp.species);
				this.selectionArray = newSelectionArray;
			}
			validEntriesList.add(new ArrayList<TSBDetection>());
		}
		validEntriesList.get(classList.indexOf(inp.species)).add(inp);
	}
	
	public HashMap<String, TSBClusterDetectionList> createTSBClusterHashMap() {
		HashMap<String, TSBClusterDetectionList> outp = new HashMap<String, TSBClusterDetectionList>();
		for (int i = 0; i < this.validEntriesList.size(); i++) {
			ArrayList<TSBDetection> currList = this.validEntriesList.get(i);
			for (int j = 0; j < currList.size(); j++) {
				TSBDetection curr = currList.get(j);
				if (!outp.containsKey(curr.clusterID)) outp.put(curr.clusterID, new TSBClusterDetectionList());
				TSBClusterDetectionList currValue = outp.get(curr.clusterID);
				currValue.add(curr);
				outp.put(curr.clusterID, currValue);
			}
		}
		return outp;
	}
	
	
/*	public int getSize() {
		int total = 0;
		for (int i = 0; i < this.validEntriesList.size(); i++) 
			total += this.validEntriesList.get(i).size();
		return total;
	} */
	
/*	public boolean containsMultipleSpecies() {
		int most = 0;
		for (int i = 0; i < this.validEntriesList.size(); i++)
			if (this.validEntriesList.get(i).size() > most) most = this.validEntriesList.get(i).size();
		return most < getSize();
	} */
	
	/**
	 * @return A copy of the subset, but without any contours from different species that overlap time-wise.
	 * Returns null if a CloneNotSupportedException occurs.
	 */
/*	public TSBSubset removeOverlaps() {
		if (!containsMultipleSpecies()) return this;
		TSBSubset outp;
		try {
			outp = (TSBSubset) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
		ArrayList<TSBDetection> allDetectionsSortStart = new ArrayList<TSBDetection>();
		ArrayList<TSBDetection> allDetectionsSortEnd = new ArrayList<TSBDetection>();
		for (int i = 0; i < this.validEntriesList.size(); i++) {
			for (int j = 0; j < this.validEntriesList.get(i).size(); j++) {
				allDetectionsSortStart.add(this.validEntriesList.get(i).get(j));
				allDetectionsSortEnd.add(this.validEntriesList.get(i).get(j));
			}
		}
		ArrayList<Long> skipList = new ArrayList<Long>();
		Collections.sort(allDetectionsSortStart, Comparator.comparingLong(TSBDetection::getDateTimeAsLong));
		Collections.sort(allDetectionsSortEnd, Comparator.comparingLong(TSBDetection::getEndTimeAsLong));
		for (int i = 0; i < allDetectionsSortEnd.size(); i++) {
			TSBDetection curri = allDetectionsSortEnd.get(i);
			for (int j = i-1; j >= 0; j--) {
				TSBDetection currj = allDetectionsSortEnd.get(j);
				if (curri.getDateTimeAsLong() > currj.getEndTimeAsLong()) break;
				if (curri.species.equals(currj.species)) continue;
				if (!skipList.contains(curri.uid)) skipList.add(curri.uid);
				if (!skipList.contains(currj.uid)) skipList.add(currj.uid);
			}
		}
		for (int i = 0; i < allDetectionsSortStart.size(); i++) {
			TSBDetection curri = allDetectionsSortStart.get(i);
			for (int j = i+1; j < allDetectionsSortStart.size(); j++) {
				TSBDetection currj = allDetectionsSortStart.get(j);
				if (curri.getEndTimeAsLong() < currj.getDateTimeAsLong()) break;
				if (curri.species.equals(currj.species)) continue;
				if (!skipList.contains(curri.uid)) skipList.add(curri.uid);
				if (!skipList.contains(currj.uid)) skipList.add(currj.uid);
			}
		}
		int removalCount = 0;
		for (int i = 0; i < allDetectionsSortStart.size(); i++) {
			if (skipList.contains(allDetectionsSortStart.get(i).uid)) {
				allDetectionsSortStart.remove(i);
				removalCount++;
				i--;
			}
		}
		for (int i = 0; i < outp.validEntriesList.size(); i++) outp.validEntriesList.get(i).clear();
		if (allDetectionsSortStart.size() == 0) {
			outp.start = "";
			outp.end = "";
		} else {
			outp.start = allDetectionsSortStart.get(0).datetime;
			outp.end = allDetectionsSortStart.get(allDetectionsSortStart.size()-1).datetime;
			for (int i = 0; i < allDetectionsSortStart.size(); i++) {
				TSBDetection curr = allDetectionsSortStart.get(i);
				outp.validEntriesList.get(classList.indexOf(curr.species)).add(curr);
			}
		}
		if (removalCount > 0)
			System.out.println("Removed "+String.valueOf(removalCount)+" detections from cluster "+id+" due to overlap settings.");
		return outp;
	} */
	
	/**
	 * @return A copy of the subset, but only including contours from the most-occurring species in the subset.
	 * Returns null if a CloneNotSupportedException occurs.
	 */
/*	public TSBSubset removeLessOccuringSpecies() {
		if (!containsMultipleSpecies()) return this;
		TSBSubset outp;
		try {
			outp = (TSBSubset) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
		int mostIndex = 0;
		for (int i = 1; i < outp.validEntriesList.size(); i++) {
			if (outp.validEntriesList.get(i).size() > outp.validEntriesList.get(mostIndex).size()) mostIndex = i;
		}
		if (outp.validEntriesList.get(mostIndex).size() == 0) {
			outp.start = "";
			outp.end = "";
		} else {
			Collections.sort(outp.validEntriesList.get(mostIndex), Comparator.comparingLong(TSBDetection::getDateTimeAsLong));
			outp.start = outp.validEntriesList.get(mostIndex).get(0).datetime;
			outp.end = outp.validEntriesList.get(mostIndex).get(outp.validEntriesList.get(mostIndex).size()-1).datetime;
			for (int i = 0; i < outp.validEntriesList.size(); i++) {
				if (i != mostIndex) outp.validEntriesList.get(i).clear();
			}
		}
		int removalCount = getSize() - outp.validEntriesList.get(mostIndex).size();
		if (removalCount > 0)
			System.out.println("Removed "+String.valueOf(removalCount)+" non-"+classList.get(mostIndex)+" detections from cluster "+id+" due to multilabel settings.");
		return outp;
	} */
}