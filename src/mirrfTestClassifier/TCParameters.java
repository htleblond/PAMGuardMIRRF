package mirrfTestClassifier;

import PamModel.parametermanager.PamParameterSet;
import mirrfLiveClassifier.LCParameters;

public class TCParameters extends LCParameters {
	
	public String testPath;
	
	public String validation;
	public int kNum;
	public String testSubset;
	
	public TCParameters() {
		super();
		
		testPath = "";
		
		validation = "leaveoneout"; // Can also be "kfold", "testsubset", "labelled", or "unlabelled"
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