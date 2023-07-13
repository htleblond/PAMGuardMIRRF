package mirrfLiveClassifier;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Data object representing groups of Whistle and Moan Detector contours.
 * The species (or whatever classification) of this object is what the classifier attempts to predict.
 * @author Holly LeBlond
 */
public class LCCallCluster implements Serializable, Cloneable {
	
	public String clusterID;
	public long[] uids;
	//public String location;
	public long[] datetimes;
	public int[] durations;
	public int[] lfs;
	public int[] hfs;
	//public String[] predictedSpecies;
	public double[][] probaList;
	public ArrayList<String> labelList;
	public int[] actualSpecies;
	
	/**
	 * @param labelOrder - Ordered array of the species names.
	 * @param size - How many contours are to be represented in this object.
	 */
	public LCCallCluster(String[] labelOrder, int size) {
		this.clusterID = "";
		this.uids = new long[size];
		//this.location = "";
		this.datetimes = new long[size];
		this.durations = new int[size];
		this.lfs = new int[size];
		this.hfs = new int[size];
		//this.predictedSpecies = new String[size];
		this.probaList = new double[size][labelOrder.length];
		this.labelList = new ArrayList<String>();
		for (int i = 0; i < labelOrder.length; i++) {
			this.labelList.add(labelOrder[i]);
		}
		this.actualSpecies = new int[size];
		for (int i = 0; i < size; i++) {
			this.actualSpecies[i] = -1;
		}
	}
	
	/**
	 * @return The number of contours represented in this object.
	 */
	public int getSize() {
		return uids.length;
	}
	
	/**
	 * @return An array of integers matching the indexes in labelOrder of each contour's predicted species.
	 */
	public int[] getPredictedSpeciesArray() {
		int[] outp = new int[getSize()];
		for (int i = 0; i < getSize(); i++) {
			int maxIndex = 0;
			double maxVal = probaList[i][0];
			for (int j = 1; j < probaList[i].length; j++) {
				if (probaList[i][j] > maxVal) {
					maxIndex = j;
					maxVal = probaList[i][j];
				}
			}
			outp[i] = maxIndex;
		}
		return outp;
	}
	
	/**
	 * @return The overall predicted species of the call cluster.
	 */
	public String getPredictedSpeciesString() {
		double[] ap = getAverageProba();
		double maxVal = ap[0];
		int maxIndex = 0;
		for (int i = 1; i < ap.length; i++) {
			if (ap[i] > maxVal) {
				maxVal = ap[i];
				maxIndex = i;
			}
		}
		return labelList.get(maxIndex);
	}
	
	/**
	 * @return Array of integers counting how many instances each species was predicted.
	 * Order matches labelOrder.
	 */
	public int[] getPredictedSpeciesCount() {
		int[] outp = new int[probaList[0].length];
		for (int i = 0; i < outp.length; i++) {
			outp[i] = 0;
		}
		for (int i = 0; i < probaList.length; i++) {
			double maxVal = probaList[i][0];
			int maxIndex = 0;
			for (int j = 1; j < probaList[i].length; j++) {
				if (probaList[i][j] > maxVal) {
					maxVal = probaList[i][j];
					maxIndex = j;
				}
			}
			outp[maxIndex]++;
		}
		return outp;
	}
	
	/**
	 * @return An array of integers matching the indexes in labelOrder of each contour's actual species.
	 * Note that they aren't always all the same.
	 * <br>-1 == unlabelled contours
	 * <br>-2 == species not in labelOrder
	 */
	public int[] getActualSpeciesArray() {
		return actualSpecies;
	}
	
	/**
	 * Returns array of integers (n = number of species + 2) representing how many instances of each species there are in the cluster.
	 * The second-last slot indicates how many instances of species not in the labelList there are.
	 * The last slot indicates how many unlabelled instances there are.
	 * @return int[]
	 */
	public int[] getActualSpeciesCount() {
		int[] outp = new int[labelList.size()+2];
		for (int i = 0; i < actualSpecies.length; i++) {
			if (actualSpecies[i] >= 0) {
				outp[actualSpecies[i]]++;
			} else if (actualSpecies[i] == -2) {
				outp[outp.length-2]++;
			} else if (actualSpecies[i] == -1) {
				outp[outp.length-1]++;
			}
		}
		return outp;
	}
	
	/**
	 * Sets actual species (as index in labelList) of detection with matching UID value.
	 * Assigns -1 if unlabelled or -2 if input species is not in labelList.
	 * @return boolean - true if cluster contains input UID, false otherwise
	 */
	public boolean setIndividualActualSpecies(long uid, String label) {
		for (int i = 0; i < uids.length; i++) {
			if (uid == uids[i]) {
				if (label.equals("")) {
					actualSpecies[i] = -1;
				} else if (labelList.contains(label)) {
					actualSpecies[i] = labelList.indexOf(label);
				} else {
					actualSpecies[i] = -2;
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @return The actual species represented by this cluster.
	 * Will return "unlabelled" if no contours are labelled.
	 * If not all contours are labelled the same, the one with the largest plurality
	 * is returned, and an asterisk is appended to the string to mark this.
	 * In a tie, the one that occurs first in labelOrder is chosen.
	 */
	public String getActualSpeciesString() {
		int[] counts = getActualSpeciesCount();
		if (counts[counts.length-1] == getSize()) {
			return "Unlabelled";
		}
		int maxVal = counts[0];
		int maxIndex = 0;
		for (int i = 1; i < counts.length-1; i++) {
			if (counts[i] > maxVal) {
				maxVal = counts[i];
				maxIndex = i;
			}
		}
		String outp = "";
		if (maxIndex == counts.length-2) {
			outp = "Other";
		} else {
			outp = labelList.get(maxIndex);
		}
		if (maxVal < getSize()) {
			return outp+" *";
		}
		return outp;
	}
	
	/**
	 * @return The actual species of the contour specified by "index".
	 * Returns "Unlabelled" if not labelled and "Other" if not in labelOrder.
	 */
	public String getIndividualActualSpeciesString(int index) {
		if (actualSpecies[index] == -1) {
			return "Unlabelled";
		} else if (actualSpecies[index] == -2) {
			return "Other";
		}
		return labelList.get(actualSpecies[index]);
	}
	
	/**
	 * @return The actual species of the contour specified by "index".
	 */
	public String getIndividualPredictedSpeciesString(int index) {
		int maxIndex = 0;
		double maxVal = probaList[index][0];
		for (int i = 1; i < probaList[index].length; i++) {
			if (probaList[index][i] > maxVal) {
				maxIndex = i;
				maxVal = probaList[index][i];
			}
		}
		return labelList.get(maxIndex);
	}
	
	/**
	 * @return The average probability scores for each species across all contours.
	 * The predicted species is the one with the highest score here.
	 */
	public double[] getAverageProba() {
		double[] outp = new double[probaList[0].length];
		for (int j = 0; j < outp.length; j++) {
			double sum = 0.0;
			for (int i = 0; i < probaList.length; i++) {
				sum += probaList[i][j];
			}
			outp[j] = sum/probaList.length;
		}
		return outp;
	}
	
	/**
	 * @return The average probability score of the first-placed species minus that of second place.
	 */
	public double getLead() {
		double[] ap = getAverageProba();
		double p1 = ap[0];
		double p2 = 0.0;
		for (int i = 1; i < ap.length; i++) {
			if (ap[i] > p1) {
				p2 = p1;
				p1 = ap[i];
			} else if (ap[i] > p2) {
				p2 = ap[i];
			}
		}
		return p1-p2;
	}
	
	/**
	 * @return The probability score of the first-placed species minus that of second place in an individual contour.
	 */
	public double getIndividualLead(int inp) {
		double[] row = this.probaList[inp];
		double p1 = row[0];
		double p2 = 0.0;
		for (int i = 1; i < row.length; i++) {
			if (row[i] > p1) {
				p2 = p1;
				p1 = row[i];
			} else if (row[i] > p2) {
				p2 = row[i];
			}
		}
		return p1-p2;
	}
	
	/**
	 * @return Size-2 array of longs containing the overall start and end times of the cluster, respectively.
	 */
	public long[] getStartAndEnd() {
		long[] outp = new long[2];
		outp[0] = datetimes[0];
		outp[1] = datetimes[0] + durations[0];
		for (int i = 1; i < durations.length; i++) {
			
			if (datetimes[i] < outp[0]) {
				outp[0] = datetimes[i];
			}
			if (datetimes[i] + durations[i] > outp[1]) {
				outp[1] = datetimes[i] + durations[i];
			}
		}
		return outp;
	}
	
	/**
	 * @return Size-2 array of integers containing the lowest and highest frequencies of the cluster, respectively.
	 */
	public int[] getFreqLimits() {
		int[] outp = new int[2];
		outp[0] = lfs[0];
		outp[1] = hfs[0];
		for (int i = 1; i < lfs.length; i++) {
			if (lfs[i] < outp[0]) {
				outp[0] = lfs[i];
			}
			if (hfs[i] > outp[1]) {
				outp[1] = hfs[i];
			}
		}
		return outp;
	}
	
	/**
	 * @return The string that's supposed to go in the "Pr. counter" column in the table.
	 * Basically an array representing the actual species counts next to an array representing the predicted species counts.
	 */
	public String getColumn4String(boolean isViewer) {
		String col4 = "[";
		if (isViewer) {
			int[] actualSpeciesCount = this.getActualSpeciesCount();
			for (int j = 0; j < actualSpeciesCount.length; j++) {
				col4 += String.valueOf(actualSpeciesCount[j]);
				if (j == actualSpeciesCount.length-3) {
					col4 += " (";
				} else if (j == actualSpeciesCount.length-1) {
					col4 += ")] -> [";
				} else {
					col4 += " ";
				}
			}
		}
		int[] predictedSpecies = this.getPredictedSpeciesCount();
		for (int j = 0; j < predictedSpecies.length; j++) {
			col4 += String.valueOf(predictedSpecies[j]);
			if (j < predictedSpecies.length-1) {
				col4 += " ";
			}
		}
		col4 += "]";
		return col4;
	}
	
	/**
	 * @return The string that's supposed to go in the "Pr. proba." column in the table.
	 * Basically the array of values produced by getAverageProba().
	 */
	public String getAverageProbaAsString() {
		String outp = "[";
		for (int i = 0; i < this.labelList.size(); i++) {
			outp += String.format("%.2f", (float) this.getAverageProba()[i]);
			if (i < this.labelList.size()-1) {
				outp += " ";
			}
		}
		outp += "]";
		return outp;
	}
}