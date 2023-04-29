package mirrfLiveClassifier;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamView.GroupedSourceParameters;

import org.apache.commons.text.WordUtils;

public class LCParameters implements Serializable, Cloneable, ManagedParameters {
	
	public String tempFolder;
	
	public String trainPath; // This should be accessed from loadedTrainingSetInfo in control in most cases.
	
	public String inputProcessName;
	public String updateProcessName;
	
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
	public String worstLead;
	public boolean displayIgnored;
	
	public int samplingRate;
	public int fftLength;
	
	public LCParameters() {
		this.tempFolder = "";
		
		this.trainPath = "";
		
		this.inputProcessName = "";
		this.updateProcessName = "";
		
		this.kNum = 10; // Must be int > 1, only used if validation == "kfold"
		this.selectKBest = false;
		this.kBest = 10; // Must be int > 1, only used if selectKBest == true
		this.samplingLimits = "none"; // Can also be "automax" or "setmax"
		this.maxSamples = 10000; // Must be int > 0, only used if samplingLimits == "setmax"
		this.limitClusterSize = false;
		this.maxClusterSize = 2;
		this.timeZone = null;
		this.labelOrder = new String[0];
		this.labelColours = new HashMap<String, Color>();
		
		this.classifierName = "RandomForestClassifier"; // Can also be "ExtraTreesClassifier" or "HistGradientBoostingClassifier"
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
		this.veryLow = 0.2; // Must be double >= 0, < 1, <= low
		this.low = 0.4; // Must be double >= 0, < 1, >= veryLow, <= average
		this.average = 0.6; // Must be double >= 0, < 1, >= low, <= high
		this.high = 0.8; // Must be double >= 0, < 1, >= average
		this.worstLead = "Very low"; // Can also be "Low", "Average", "High", or "Very high"
		this.displayIgnored = true;
		
		this.samplingRate = 48000;
		this.fftLength = 2048;
	}
	
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
		// Thread manager and TCPanel.RunListener add the rest.
		return outp;
	}
	
	public HashMap<String, Color> generateColours(String[] newLabels) {
		Color[] defaultColours = new Color[] {new Color(0,255,0), new Color(0,0,255), new Color(255,0,0), new Color(255,175,0), new Color(255,0,255),
											  new Color(0,255,0), new Color(0,255,255), new Color(255,150,150), new Color(150,150,150), new Color(255,255,255)};
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
	
	public boolean shouldIgnoreCluster(LCCallCluster cc) {
		if (cc.getSize() < minClusterSize) {
			return true;
		} else if ((worstLead.equals("Low") && cc.getLead() < veryLow) ||
				   (worstLead.equals("Average") && cc.getLead() < low) ||
				   (worstLead.equals("High") && cc.getLead() < average) ||
				   (worstLead.equals("Very high") && cc.getLead() < high)) {
			return true;
		}
		return false;
	}
	
	public String getLeadDescriptor(double inp) {
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
	
	public ArrayList<String> getLabelOrderAsList() {
		ArrayList<String> outp = new ArrayList<String>();
		for (int i = 0; i < labelOrder.length; i++) outp.add(labelOrder[i]);
		return outp;
	}

	@Override
	public PamParameterSet getParameterSet() {
		return PamParameterSet.autoGenerate(this);
	}
	
	@Override
	public LCParameters clone() {
		try {
			return (LCParameters) super.clone();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}