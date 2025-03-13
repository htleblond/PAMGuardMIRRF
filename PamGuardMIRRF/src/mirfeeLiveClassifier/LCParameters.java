package mirfeeLiveClassifier;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import PamModel.parametermanager.PamParameterSet;
import PamguardMVC.PamDataBlock;
import mirfee.MIRFEEParameters;
import mirfeeFeatureExtractor.FEDataBlock;
import wmat.WMATDataBlock;

import org.apache.commons.text.WordUtils;

import PamController.PamControlledUnit;

/**
 * The parameters object for the Live Classifier.
 * @author Holly LeBlond
 */
//@SuppressWarnings("serial")
public class LCParameters extends MIRFEEParameters {
	
	protected LCTrainingSetInfo loadedTrainingSetInfo;
	
	//public FEDataBlock inputFEDataBlock;
	//public WMNTDataBlock wmntUpdateDataBlock;
	public String inputFEDataBlockName;
	public String wmatUpdateDataBlockName;
	
	public int kNum;
	public boolean selectKBest;
	public int kBest;
	public String samplingLimits;
	public int maxSamples;
	public boolean limitClusterSize;
	public int maxClusterSize;
	public String timeZone;
	public String[] labelOrder;
	public HashMap<String, Color> labelColours;
	public HashMap<String, String> labelAliases;
	
	public String classifierName;
	public int nEstimators;
	public String criterion;
	public boolean hasMaxDepth;
	public int maxDepth;
	public String maxFeaturesMode;
	public int maxFeatures;
	public boolean bootstrap;
	public String classWeightMode;
	public double[] classWeights;
	public double learningRate;
	public int maxIterations;
	
	public int minClusterSize;
	public double veryLow;
	public double low;
	public double average;
	public double high;
	public String worstCertainty;
	public boolean displayIgnored;
	
	public int samplingRate;
	public int fftLength;
	
	public boolean printJava;
	public boolean printInput;
	public boolean printOutput;
	
	public LCParameters() {
		
		this.loadedTrainingSetInfo = new LCTrainingSetInfo("");
		
		this.kNum = 10; // Must be int > 1, only used if validation == "kfold"
		this.selectKBest = false;
		this.kBest = 10; // Must be int > 1, only used if selectKBest == true
		this.samplingLimits = "none"; // Can also be "automax", "setmax" or "duplicate"
		this.maxSamples = 10000; // Must be int > 0, only used if samplingLimits == "setmax"
		this.limitClusterSize = false;
		this.maxClusterSize = 2;
		this.timeZone = null;
		this.labelOrder = new String[0];
		this.labelColours = new HashMap<String, Color>();
		this.labelAliases = new HashMap<String, String>();
		
		this.classifierName = "HistGradientBoostingClassifier"; // Can also be "ExtraTreesClassifier", "HistGradientBoostingClassifier" or "LinearDiscriminantAnalysis"
		this.nEstimators = 100; // Must be int > 0
		this.criterion = "Gini"; // Can also be "Log. loss" or "Entropy"
		this.hasMaxDepth = false;
		this.maxDepth = 2; // Must be int > 1, only used if hasMaxDepth == true
		this.maxFeaturesMode = "Square root"; // Can also be "Log2" or "Custom"
		this.maxFeatures = 2; // Only used if maxFeaturesMode == "custom"
		this.bootstrap = false;
		this.classWeightMode = "None"; // Can also be "Balanced", "Balanced subsample", or "Custom"
		this.classWeights = new double[0]; // Only used if classWeightMode == "custom"
		this.learningRate = 0.1; // Must be double > 0, <= 1
		this.maxIterations = 100; // Must be int > 1
		
		this.minClusterSize = 1; // Auto set to 1 if 0 input
		this.veryLow = 0.6; // Must be double >= 0, < 1, <= low
		this.low = 0.7; // Must be double >= 0, < 1, >= veryLow, <= average
		this.average = 0.8; // Must be double >= 0, < 1, >= low, <= high
		this.high = 0.9; // Must be double >= 0, < 1, >= average
		this.worstCertainty = "Very low"; // Can also be "Low", "Average", "High", or "Very high"
		this.displayIgnored = true;
		
		this.samplingRate = 48000;
		this.fftLength = 2048;
		
		this.printJava = false;
		this.printInput = false;
		this.printOutput = false;
	}
	
	/**
	 * Easy means of representing the values in LCParameters as a String, for Python input.
	 */
	@Override
	public String outputPythonParamsToText() {
		if (tempFolder.length() == 0) {
			return "";
		}
		String outp = "[";
		outp += String.valueOf(kNum)+",";
		outp += WordUtils.capitalize(String.valueOf(selectKBest))+",";
		outp += String.valueOf(kBest)+",";
		outp += "\""+samplingLimits+"\",";
		outp += String.valueOf(maxSamples)+",";
		outp += WordUtils.capitalize(String.valueOf(limitClusterSize))+",";
		outp += String.valueOf(maxClusterSize)+",";
		outp += "[";
		if (labelOrder.length > 0) {
			outp += "\""+labelOrder[0]+"\"";
			for (int i = 1; i < labelOrder.length; i++) {
				outp += ",\""+labelOrder[i]+"\"";
			}
		}
		outp += "],";
		outp += "\""+classifierName+"\",";
		outp += String.valueOf(nEstimators)+",";
		outp += "\""+criterion+"\",";
		outp += WordUtils.capitalize(String.valueOf(hasMaxDepth))+",";
		outp += String.valueOf(maxDepth)+",";
		outp += "\""+maxFeaturesMode+"\",";
		outp += String.valueOf(maxFeatures)+",";
		outp += WordUtils.capitalize(String.valueOf(bootstrap))+",";
		outp += "\""+classWeightMode+"\",";
		outp += "[";
		if (classWeights.length > 0) {
			outp += classWeights[0];
			for (int i = 1; i < classWeights.length; i++) {
				outp += ","+classWeights[i];
			}
		}
		outp += "],";
		outp += String.valueOf(learningRate)+",";
		outp += String.valueOf(maxIterations)+",";
		outp += String.valueOf(minClusterSize)+",";
		outp += WordUtils.capitalize(String.valueOf(displayIgnored))+",";
		outp += "[";
		if (getFeatureList().size() > 0) {
        	outp += "\""+getFeatureList().get(0)+"\"";
			for (int i = 1; i < getFeatureList().size(); i++) {
				outp += ",\""+getFeatureList().get(i)+"\"";
			}
		}
        outp += "]";
        outp += "]";
		return outp;
	}
	
	/**
	 * @return Hash map that assigns colours to species.
	 * If a species is already in the pre-existing labelColours map, it retains its old colour.
	 */
	public HashMap<String, Color> generateColours(String[] newLabels) {
		Color[] defaultColours = new Color[] {new Color(0,255,0), new Color(255,0,0), new Color(0,255,255), new Color(255,175,0), new Color(255,0,255),
											  new Color(0,255,0), new Color(0,0,255), new Color(255,150,150), new Color(150,150,150), new Color(255,255,255)};
		HashMap<String, Color> newColours = new HashMap<String, Color>();
		for (int i = 0 ; i < newLabels.length; i++) {
			if (labelColours.containsKey(newLabels[i])) {
				newColours.put(newLabels[i], labelColours.get(newLabels[i]));
			}
		}
		for (int i = 0; i < newLabels.length; i++) {
			if (!newColours.containsKey(newLabels[i])) {
				for (int j = 0; j < defaultColours.length; j++) {
					if (!newColours.containsValue(defaultColours[j]) || j == defaultColours.length-1) {
						newColours.put(newLabels[i], defaultColours[j]);
						break;
					}
				}
			}
		}
		return newColours;
	}
	
	/**
	 * @return Whether or not a cluster should be ignored based off of certain settings.
	 */
	public boolean shouldIgnoreCluster(LCCallCluster cc) {
		if (cc.getSize() < minClusterSize) {
			return true;
		} else if ((worstCertainty.equals("Low") && cc.getCertainty() < veryLow) ||
				   (worstCertainty.equals("Average") && cc.getCertainty() < low) ||
				   (worstCertainty.equals("High") && cc.getCertainty() < average) ||
				   (worstCertainty.equals("Very high") && cc.getCertainty() < high)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the subjective "certainty descriptor" matching the input number, based off of specified settings.
	 */
	public String getCertaintyDescriptor(double inp) {
		if (inp < this.veryLow) {
			return "Very low";
		} else if (inp < this.low) {
			return "Low";
		} else if (inp < this.average) {
			return "Average";
		} else if (inp < this.high) {
			return "High";
		}
		return "Very high";
	}
	
	/**
	 * @return labelOrder as an ArrayList.
	 */
	public ArrayList<String> getLabelOrderAsList() {
		ArrayList<String> outp = new ArrayList<String>();
		for (int i = 0; i < labelOrder.length; i++) outp.add(labelOrder[i]);
		return outp;
	}
	
	/**
	 * @return The object representing the input training set.
	 */
	public LCTrainingSetInfo getTrainingSetInfo() {
		return loadedTrainingSetInfo;
	}
	
	/**
	 * Sets the object representing the input training set.
	 */
	public void setTrainingSetInfo(LCTrainingSetInfo inp) {
		loadedTrainingSetInfo = inp;
	}
	
	/**
	 * @return The training set file's path, stored in loadedTrainingSetInfo.
	 */
	public String getTrainPath() {
		return loadedTrainingSetInfo.pathName;
	}
	
	/**
	 * @return The list of features stored in loadedTrainingSetInfo.
	 */
	public ArrayList<String> getFeatureList() {
		return loadedTrainingSetInfo.featureList;
	}
	
	public FEDataBlock getInputFEDataBlock(PamControlledUnit controlledUnit) {
		return (FEDataBlock) findDataBlock(controlledUnit, this.inputFEDataBlockName);
	}
	
	public WMATDataBlock getWMATUpdateDataBlock(PamControlledUnit controlledUnit) {
		return (WMATDataBlock) findDataBlock(controlledUnit, this.wmatUpdateDataBlockName);
	}
	
	private PamDataBlock findDataBlock(PamControlledUnit controlledUnit, String name) {
		// I know there's gotta be a better way of doing this, but getDataBlock(class, name) keeps throwing exceptions.
		if (name == null || name.length() == 0) return null;
		ArrayList<PamDataBlock> dbs = controlledUnit.getPamController().getDataBlocks();
		for (int i = 0; i < dbs.size(); i++)
			if (dbs.get(i).getDataName().equals(name)) return dbs.get(i);
		return null;
	}
	
	/**
	 * @return The hash map in loadedTrainingSetInfo counting how many of each label are in the set.
	 */
	public HashMap<String, Integer> getLabelCounts() {
		return loadedTrainingSetInfo.labelCounts;
	}
	
	/**
	 * @return The hash map in loadedTrainingSetInfo counting how big each "subset" in the set is.
	 */
	public HashMap<String, Integer> getSubsetCounts() {
		return loadedTrainingSetInfo.subsetCounts;
	}

	@Override
	public PamParameterSet getParameterSet() {
		return PamParameterSet.autoGenerate(this);
	}
	
	@Override
	public LCParameters clone() {
		return (LCParameters) super.clone();
	}
}