package mirrfTrainingSetBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class TSBClusterDetectionList extends ArrayList<TSBDetection> {
	
	public TSBClusterDetectionList() {
		super();
	}
	
	public boolean containsMultipleSpecies() {
		if (this.size() == 0) return false;
		String firstSpecies = this.get(0).species;
		for (int i = 1; i < this.size(); i++) {
			if (!this.get(i).species.equals(firstSpecies)) return true;
		}
		return false;
	}
	
	public TSBClusterDetectionList removeOverlaps(String subsetID) {
		TSBClusterDetectionList outp = (TSBClusterDetectionList) this.clone();
		if (!this.containsMultipleSpecies()) return outp;
		String clusterID = this.get(0).clusterID;
		ArrayList<Long> skipList = new ArrayList<Long>();
		Collections.sort(outp, Comparator.comparingLong(TSBDetection::getEndTimeAsLong));
		for (int i = 1; i < outp.size(); i++) {
			TSBDetection curri = outp.get(i);
			for (int j = i-1; j >= 0; j--) {
				TSBDetection currj = outp.get(j);
				if (curri.getDateTimeAsLong() > currj.getEndTimeAsLong()) break;
				if (curri.species.equals(currj.species)) continue;
				if (!skipList.contains(curri.uid)) skipList.add(curri.uid);
				if (!skipList.contains(currj.uid)) skipList.add(currj.uid);
			}
		}
		Collections.sort(outp, Comparator.comparingLong(TSBDetection::getDateTimeAsLong));
		for (int i = 0; i < outp.size()-1; i++) {
			TSBDetection curri = outp.get(i);
			for (int j = i+1; j < outp.size(); j++) {
				TSBDetection currj = outp.get(j);
				if (curri.getEndTimeAsLong() < currj.getDateTimeAsLong()) break;
				if (curri.species.equals(currj.species)) continue;
				if (!skipList.contains(curri.uid)) skipList.add(curri.uid);
				if (!skipList.contains(currj.uid)) skipList.add(currj.uid);
			}
		}
		int removalCount = 0;
		for (int i = 0; i < outp.size(); i++) {
			if (skipList.contains(outp.get(i).uid)) {
				outp.remove(i);
				removalCount++;
				i--;
			}
		}
		if (removalCount > 0)
			System.out.println("Removed "+String.valueOf(removalCount)+" detection(s) from cluster "+subsetID+"-"+clusterID+" due to overlap settings.");
		return outp;
	}
	
	public TSBClusterDetectionList removeLessOccuringSpecies(String subsetID) {
		TSBClusterDetectionList outp = (TSBClusterDetectionList) this.clone();
		if (!this.containsMultipleSpecies()) return outp;
		String clusterID = this.get(0).clusterID;
		HashMap<String, Integer> speciesCount = new HashMap<String, Integer>();
		for (int i = 0; i < outp.size(); i++) {
			TSBDetection curr = outp.get(i);
			if (!speciesCount.containsKey(curr.species)) speciesCount.put(curr.species, 0);
			speciesCount.put(curr.species, speciesCount.get(curr.species) + 1);
		}
		Iterator<String> it = speciesCount.keySet().iterator();
		String most = it.next();
		while (it.hasNext()) {
			String next = it.next();
			if (speciesCount.get(next) > speciesCount.get(most)) most = next;
		}
		int removalCount = 0;
		for (int i = 0; i < outp.size(); i++) {
			if (!outp.get(i).species.equals(most)) {
				outp.remove(i);
				removalCount++;
				i--;
			}
		}
		if (removalCount > 0)
			System.out.println("Removed "+String.valueOf(removalCount)+" non-"+most+" detection(s) from cluster "+subsetID+"-"+clusterID+" due to multilabel settings.");
		return outp;
	}
}