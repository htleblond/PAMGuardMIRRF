package mirrfFeatureExtractor;

import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.ArrayList;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamView.GroupedSourceParameters;
import whistlesAndMoans.WhistleToneParameters;

import org.apache.commons.text.WordUtils;

public class FEParameters implements Serializable, Cloneable, ManagedParameters {
	
	public String tempFolder;
	
	public int sr;
	
	public boolean inputFromCSV;
	public String inputProcessName;
	public String inputCSVName;
	public ArrayList<String[]> inputCSVEntries;
	public ArrayList<Integer> inputCSVIndexes;
	public int inputCSVExpectedFileSize;
	public boolean inputIgnoreBlanks;
	public boolean inputIgnoreFalsePositives;
	
	public boolean outputCSVChecked;
	public String outputCSVName;
	//public boolean outputCSVOverwrite;
	
	public String audioSourceProcessName;
	public boolean audioAutoClipLength;
	public int audioClipLength;
	public int audioSTFTLength;
	public int audioHopSize;
	public String audioWindowFunction;
	public boolean audioNormalizeChecked;
	public boolean audioHPFChecked;
	public int audioHPFThreshold;
	public int audioHPFMagnitude;
	public boolean audioLPFChecked;
	public int audioLPFThreshold;
	public int audioLPFMagnitude;
	public boolean audioNRChecked;
	public int audioNRStart;
	public int audioNRLength;
	public double audioNRScalar;
	
	public String[][] featureList;
	
	public boolean miscClusterChecked;
	public int miscJoinDistance;
	public boolean miscIgnoreFileStartChecked;
	public int miscIgnoreFileStartLength;
	public boolean miscIgnoreLowFreqChecked;
	public int miscIgnoreLowFreq;
	public boolean miscIgnoreHighFreqChecked;
	public int miscIgnoreHighFreq;
	public boolean miscIgnoreShortDurChecked;
	public int miscIgnoreShortDur;
	public boolean miscIgnoreLongDurChecked;
	public int miscIgnoreLongDur;
	public boolean miscIgnoreQuietAmpChecked;
	public int miscIgnoreQuietAmp;
	public boolean miscIgnoreLoudAmpChecked;
	public int miscIgnoreLoudAmp;
	
	public FEParameters() {
		
		//this.tempFolder = "C:/Users/wtleb/Desktop/MIRRF Test Clips/";
		this.tempFolder = "";
		
		this.sr = 0;
		
		this.inputFromCSV = false;
		this.inputProcessName = "";
		this.inputCSVName = "";
		this.inputCSVEntries = new ArrayList<String[]>();
		this.inputCSVIndexes = new ArrayList<Integer>();
		this.inputCSVExpectedFileSize = 10;
		this.inputIgnoreBlanks = false;
		this.inputIgnoreFalsePositives = false;
		
		this.outputCSVChecked = false;
		this.outputCSVName = "";
		//this.outputCSVOverwrite = true;
		
		this.audioSourceProcessName = "";
		this.audioAutoClipLength = true;
		this.audioClipLength = 16384;
		this.audioSTFTLength = 2048;
		this.audioHopSize = 1024;
		this.audioWindowFunction = "Hann";
		this.audioNormalizeChecked = false;
		this.audioHPFChecked = false;
		this.audioHPFThreshold = 2000;
		this.audioHPFMagnitude = 1;
		this.audioLPFChecked = false;
		this.audioLPFThreshold = 20000;
		this.audioLPFMagnitude = 1;
		this.audioNRChecked = false;
		this.audioNRStart = 24000;
		this.audioNRLength = 16384;
		this.audioNRScalar = 1.0;
		
		this.featureList = new String[0][0];
		
		this.miscClusterChecked = true;
		this.miscJoinDistance = 2000;
		this.miscIgnoreFileStartChecked = true;
		this.miscIgnoreFileStartLength = 2000;
		this.miscIgnoreLowFreqChecked = false;
		this.miscIgnoreLowFreq = 0;
		this.miscIgnoreHighFreqChecked = false;
		this.miscIgnoreHighFreq = 0;
		this.miscIgnoreShortDurChecked = false;
		this.miscIgnoreShortDur = 0;
		this.miscIgnoreLongDurChecked = false;
		this.miscIgnoreLongDur = 0;
		this.miscIgnoreQuietAmpChecked = false;
		this.miscIgnoreQuietAmp = 0;
		this.miscIgnoreLoudAmpChecked = false;
		this.miscIgnoreLoudAmp = 0;
	}
	
	public String outputPythonParamsToText() {
		if (tempFolder.length() == 0) {
			return "";
		}
		String outp = "[";
		outp += String.valueOf(sr)+",";
		outp += WordUtils.capitalize(String.valueOf(audioAutoClipLength))+",";
		outp += String.valueOf(audioClipLength)+",";
		outp += String.valueOf(audioSTFTLength)+",";
		outp += String.valueOf(audioHopSize)+",";
		outp += "\""+String.valueOf(audioWindowFunction)+"\",";
		outp += WordUtils.capitalize(String.valueOf(audioNormalizeChecked))+",";
		outp += WordUtils.capitalize(String.valueOf(audioHPFChecked))+",";
		outp += String.valueOf(audioHPFThreshold)+",";
		outp += String.valueOf(audioHPFMagnitude)+",";
		outp += WordUtils.capitalize(String.valueOf(audioLPFChecked))+",";
		outp += String.valueOf(audioLPFThreshold)+",";
		outp += String.valueOf(audioLPFMagnitude)+",";
		outp += WordUtils.capitalize(String.valueOf(audioNRChecked))+",";
		outp += String.valueOf(audioNRStart)+",";
		outp += String.valueOf(audioNRLength)+",";
		outp += String.valueOf(audioNRScalar)+",";
		outp += "[";
		for (int i = 0; i < featureList.length; i++) {
			outp += "\""+featureList[i][1]+"\"";
			if (i < featureList.length-1) {
				outp += ",";
			} else {
				outp += "]";
			}
		}
		outp += "]";
		return outp;
	}

	@Override
	public PamParameterSet getParameterSet() {
		return PamParameterSet.autoGenerate(this);
	}
	
	@Override
	public FEParameters clone() {
		try {
			return (FEParameters) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}