package mirrfTestClassifier;

import PamModel.parametermanager.PamParameterSet;
import mirrfLiveClassifier.LCParameters;
import mirrfLiveClassifier.LCTrainingSetInfo;

public class TCParameters extends LCParameters {
	
	// validation params
	public static final int LEAVEONEOUT = 0;
	public static final int KFOLD = 1;
	public static final int TESTSUBSET = 2;
	public static final int LABELLED = 3;
	public static final int UNLABELLED = 4;
	
	//public String testPath;
	public LCTrainingSetInfo loadedTestingSetInfo;
	
	public int validation;
	public int kNum;
	public String testSubset;
	
	public TCParameters() {
		super();
		
		//testPath = "";
		loadedTestingSetInfo = new LCTrainingSetInfo("");
		
		validation = LEAVEONEOUT;
		kNum = 10; // Must be int > 1, only used if validation == "kfold"
		testSubset = "";
	}
	
/*	@Override
	public String outputPythonParamsToText() {
		if (tempFolder.length() == 0) {
			return "";
		}
		String outp = "[\""+validation+"\","+String.valueOf(kNum)+",";
		outp += super.outputPythonParamsToText().substring(1);
		return outp;
	} */
	
	public LCTrainingSetInfo getTestingSetInfo() {
		return loadedTestingSetInfo;
	}
	
	public void setTestingSetInfo(LCTrainingSetInfo inp) {
		loadedTestingSetInfo = inp;
	}
	
	public String getTestPath() {
		return loadedTestingSetInfo.pathName;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		return PamParameterSet.autoGenerate(this);
	}
	
	@Override
	public TCParameters clone() {
		try {
			return (TCParameters) super.clone();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}