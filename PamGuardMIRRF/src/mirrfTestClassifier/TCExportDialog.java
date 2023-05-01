package mirrfTestClassifier;

import java.awt.Window;

import mirrfLiveClassifier.LCControl;
import mirrfLiveClassifier.LCExportDialog;
import mirrfLiveClassifier.LCParameters;

public class TCExportDialog extends LCExportDialog {

	public TCExportDialog(TCControl tcControl, Window parentFrame) {
		super(tcControl, parentFrame);
		if (!tcControl.isViewer()) {
			optionsBox.addItem("Confusion matrix (.csv)");
			optionsBox.addItem("Full results with summary (.txt)");
		}
	}
	
	@Override
	protected StringBuilder produceFeatureExtractorInfo(StringBuilder sb) {
		sb = new StringBuilder();
		sb.append("FEATURE INFO\n\n");
		sb.append("Features (from training set): "+String.valueOf(getControl().getFeatureList().size()));
		for (int i = 0; i < getControl().getFeatureList().size(); i++) {
			sb.append("\t"+getControl().getFeatureList().get(i)+"\n");
		}
		sb.append("\n");
		pw.write(sb.toString());
		pw.flush();
		return sb;
	}
	
	@Override
	protected StringBuilder produceClassifierInfo(StringBuilder sb) {
		sb = new StringBuilder();
		sb.append("TEST CLASSIFIER PARAMETERS\n\n");
		TCParameters params = getControl().getParams();
		
		if (params.validation.equals("leaveoneout")) {
			sb.append("Validation: Leave-one-out cross-validation (by subset ID)");
			sb.append("Number of subsets: "+String.valueOf(getControl().getTrainingSetInfo().subsetCounts));
		} else if (params.validation.equals("kfold")) {
			sb.append("Validation: k-fold cross-validation");
			sb.append("Number of k-folds: "+String.valueOf(params.kNum));
		} else if (params.validation.equals("labelled")) {
			sb.append("Validation: Labelled testing set");
		} else if (params.validation.equals("unlabelled")) {
			sb.append("Validation: Unlabelled testing set");
		}
		sb.append("Training set: "+getControl().getTrainPath());
		if (params.validation.contains("labelled")) sb.append("Testing set: "+getControl().getTestPath());
		
		// Everything below here should be the same as in LC
		if (params.selectKBest) {
			sb.append("k-Best feature selection: On (k = "+String.valueOf(params.kBest)+")\n");
		} else {
			sb.append("k-Best feature selection: Off\n");
		}
		if (params.samplingLimits.equals("none")) {
			sb.append("Sampling limit: None\n");
		} else if (params.samplingLimits.equals("automax")) {
			sb.append("Sampling limit: Automatically set to size of least-populated class\n");
		} else {
			sb.append("Sampling limit: Manually-set maximum (n = "+String.valueOf(params.maxSamples)+")\n");
		}
		sb.append("Time zone: "+params.timeZone+"\n");
		sb.append("Class labels: "+String.valueOf(params.labelOrder.length)+"\n");
		for (int i = 0; i < params.labelOrder.length; i++) {
			sb.append("\t"+params.labelOrder[i]+"\n");
		}
		sb.append("Classifier model: "+params.classifierName+"\n");
		if (!params.classifierName.equals("HistGradientBoostingClassifier")) {
			sb.append("Number of estimators: "+String.valueOf(params.nEstimators)+"\n");
			sb.append("Criterion: "+params.criterion+"\n");
			if (params.hasMaxDepth) {
				sb.append("Max. tree depth: "+String.valueOf(params.maxDepth)+"\n");
			} else {
				sb.append("Max. tree depth: None\n");
			}
			if (!params.maxFeaturesMode.equals("Custom")) {
				sb.append("Max. features per tree: "+params.maxFeaturesMode+"\n");
			} else {
				sb.append("Max. features per tree: "+String.valueOf(params.maxFeatures)+"\n");
			}
			sb.append("Bootstrap: "+String.valueOf(params.bootstrap)+"\n");
			if (!params.classWeightMode.equals("Custom")) {
				sb.append("Class weights: "+params.classWeightMode+"\n");
			} else {
				sb.append("Class weights:");
				for (int i = 0; i < params.classWeights.length; i++) {
					sb.append(" "+String.valueOf(params.classWeights[i]));
				}
				sb.append("\n");
			}
		} else {
			sb.append("Learning rate: "+String.valueOf(params.learningRate)+"\n");
			sb.append("Max. number of iterations: "+String.valueOf(params.maxIterations)+"\n");
			if (params.hasMaxDepth) {
				sb.append("Max. tree depth: "+String.valueOf(params.maxDepth)+"\n");
			} else {
				sb.append("Max. tree depth: None\n");
			}
		}
		sb.append("Min. cluster size: "+String.valueOf(params.minClusterSize)+"\n");
		sb.append("Descriptors:\n");
		sb.append("\tVery low  <  "+String.valueOf(params.veryLow)+"\n");
		sb.append("\tLow       <  "+String.valueOf(params.low)+"\n");
		sb.append("\tAverage   <  "+String.valueOf(params.average)+"\n");
		sb.append("\tHigh      <  "+String.valueOf(params.high)+"\n");
		sb.append("\tVery high <= 1.00\n");
		sb.append("Worst acceptable lead: "+params.worstLead+"\n\n");
		pw.write(sb.toString());
		pw.flush();
		return sb;
	}
	
	protected TCControl getControl() {
		return (TCControl) lcControl;
	}
}