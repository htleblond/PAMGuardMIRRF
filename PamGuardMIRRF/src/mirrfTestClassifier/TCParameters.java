package mirrfTestClassifier;

import PamModel.parametermanager.PamParameterSet;
import mirrfLiveClassifier.LCParameters;
import mirrfLiveClassifier.LCTrainingSetInfo;

/**
 * The parameters object for the Test Classifier.
 * Subclass of the Live Classifier's parameters.
 * @author Holly LeBlond
 */
public class TCParameters extends LCParameters {
	
	public static final int LEAVEONEOUT = 0;
	public static final int KFOLD = 1;
	public static final int TESTSUBSET = 2;
	public static final int LABELLED = 3;
	public static final int UNLABELLED = 4;
	
	public LCTrainingSetInfo loadedTestingSetInfo;
	
	public int validation;
	public int kNum;
	public String testSubset;
	
	public TCParameters() {
		super();
		
		loadedTestingSetInfo = new LCTrainingSetInfo("");
		
		validation = LEAVEONEOUT;
		kNum = 10; // Must be int > 1, only used if validation == "kfold"
		testSubset = "";
	}
	
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