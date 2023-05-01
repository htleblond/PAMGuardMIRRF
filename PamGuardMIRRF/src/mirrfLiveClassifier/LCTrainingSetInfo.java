package mirrfLiveClassifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

public class LCTrainingSetInfo {
	public String pathName;
	public ArrayList<String> featureList;
	public HashMap<String, Integer> labelCounts;
	public HashMap<String, Integer> subsetCounts;
	
	public LCTrainingSetInfo(String pathName) {
		this.pathName = pathName;
		this.featureList = new ArrayList<String>();
		this.labelCounts = new HashMap<String, Integer>();
		this.subsetCounts = new HashMap<String, Integer>();
	}
	
	public void addFeature(String feature) {
		featureList.add(feature);
	}
	
	public void addLabel(String label) {
		if (labelCounts.containsKey(label)) labelCounts.put(label, labelCounts.get(label)+1);
		else labelCounts.put(label, 1);
	}
	
	public boolean removeLabel(String label) {
		return labelCounts.remove(label) != null;
	}
	
	public void addBatchID(String id) {
		if (subsetCounts.containsKey(id)) subsetCounts.put(id, subsetCounts.put(id, subsetCounts.get(id)+1));
		else subsetCounts.put(id, 1);
	}
	
	public boolean removeBatchID(String id) {
		return subsetCounts.remove(id) != null;
	}
	
	public ArrayList<String> getSortedLabelList() {
		ArrayList<String> outp = new ArrayList<String>();
		Iterator<String> it = labelCounts.keySet().iterator();
		while (it.hasNext()) outp.add(it.next());
		outp.sort(Comparator.naturalOrder());
		return outp;
	}
	
	public ArrayList<String> getSortedSubsetList() {
		ArrayList<String> outp = new ArrayList<String>();
		Iterator<String> it = subsetCounts.keySet().iterator();
		while (it.hasNext()) outp.add(it.next());
		outp.sort(Comparator.naturalOrder());
		return outp;
	}
	
	public boolean compare(LCTrainingSetInfo inp) {
		if (!this.pathName.equals(inp.pathName)) return false;
		if (this.labelCounts.size() != inp.labelCounts.size()) return false;
		if (this.subsetCounts.size() != inp.subsetCounts.size()) return false;
		for (int i = 0; i < featureList.size(); i++) if (!featureList.get(i).equals(inp.featureList.get(i))) return false;
		Iterator<String> it = labelCounts.keySet().iterator();
		while (it.hasNext()) {
			String next = it.next();
			Integer value = inp.labelCounts.get(next);
			if (value == null || value.intValue() != labelCounts.get(next).intValue()) return false;
		}
		it = subsetCounts.keySet().iterator();
		while (it.hasNext()) {
			String next = it.next();
			Integer value = inp.subsetCounts.get(next);
			if (value == null || value.intValue() != subsetCounts.get(next).intValue()) return false;
		}
		return true;
	}
}