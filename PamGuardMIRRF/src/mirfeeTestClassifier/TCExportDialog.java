package mirfeeTestClassifier;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import mirfeeFeatureExtractor.FEParameters;
import mirfeeLiveClassifier.LCCallCluster;
import mirfeeLiveClassifier.LCDataBlock;
import mirfeeLiveClassifier.LCDataUnit;
import mirfeeLiveClassifier.LCExportDialog;

/**
 * Dialog for exporting classification results.
 * Subclass of the Live Classifier's export dialog.
 * @author Holly LeBlond
 */
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
		TCParameters params = getControl().getParams();
		boolean printTestSetParamsToo = false;
		if (params.validation >= params.LABELLED) {
			FEParameters feParams = new FEParameters();
			if (feParams.findUnmatchedParameters(params.getTrainingSetInfo().feParamsMap, 
					params.getTestingSetInfo().feParamsMap, false).size() > 0) {
				printTestSetParamsToo = true;
				sb.append("NOTE - The training and testing sets have contradictory Feature Extractor settings.\n\n");
			}
		}
		sb = this.printFEParamsFromTrainingSetFile(sb, params.getTrainingSetInfo().feParamsMap,
				null, "FEATURE EXTRACTOR PARAMETERS FOUND IN TRAINING SET");
		if (printTestSetParamsToo) sb = this.printFEParamsFromTrainingSetFile(sb, params.getTestingSetInfo().feParamsMap,
				null, "FEATURE EXTRACTOR PARAMETERS FOUND IN TESTING SET");
		return sb;
	}
	
	@Override
	protected StringBuilder produceClassifierInfo(StringBuilder sb) {
		sb = new StringBuilder();
		sb.append("TEST CLASSIFIER PARAMETERS\n\n");
		TCParameters params = getControl().getParams();
		
		if (params.validation == params.LEAVEONEOUTBOTHDIGITS) {
			sb.append("Validation: Leave-one-out cross-validation (by subset ID)\n");
			sb.append("Number of subsets: "+String.valueOf(params.getTrainingSetInfo().subsetCounts.size())+"\n");
		} else if (params.validation == params.LEAVEONEOUTFIRSTDIGIT) {
			sb.append("Validation: Leave-one-out cross-validation (by first digit in subset ID)\n");
			Iterator<String> it = params.getTrainingSetInfo().subsetCounts.keySet().iterator();
			ArrayList<String> firstDigits = new ArrayList<String>();
			while (it.hasNext()) {
				String next = it.next().substring(0, 1);
				if (!firstDigits.contains(next)) firstDigits.add(next);
			}
			sb.append("Number of subsets: "+String.valueOf(firstDigits.size())+"\n");
		} else if (params.validation == params.KFOLD) {
			sb.append("Validation: k-fold cross-validation\n");
			sb.append("Number of k-folds: "+String.valueOf(params.kNum)+"\n");
		} else if (params.validation == params.TESTSUBSET) {
			sb.append("Validation: Testing on a single subset or set of subsets\n");
			sb.append("Test subset(s): "+String.valueOf(params.testSubset)+"\n");
		} else if (params.validation == params.LABELLED) {
			sb.append("Validation: Labelled testing set\n");
		} else if (params.validation == params.UNLABELLED) {
			sb.append("Validation: Unlabelled testing set\n");
		}
		sb.append("Training set: "+params.getTrainPath()+"\n");
		if (params.validation >= params.LABELLED) sb.append("Testing set: "+params.getTestPath()+"\n");
		
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
		} else if (params.samplingLimits.equals("setmax")) {
			sb.append("Sampling limit: Manually-set maximum (n = "+String.valueOf(params.maxSamples)+")\n");
		} else if (params.samplingLimits.equals("duplicate")) {
			sb.append("Sampling limit: Entries in smaller classes duplicated to match largest class\n");
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
		sb.append("Worst acceptable certainty: "+params.worstCertainty+"\n\n");
		pw.write(sb.toString());
		pw.flush();
		return sb;
	}
	
	@Override
	protected StringBuilder produceMatrixInfo(StringBuilder sb) {
		sb = super.produceMatrixInfo(sb);
		sb = new StringBuilder();
		TCParameters params = getControl().getParams();
		//if (params.validation != params.LEAVEONEOUTBOTHDIGITS && params.validation != params.LEAVEONEOUTFIRSTDIGIT) return sb;
		HashMap<String, int[][]> matrixMap = new HashMap<String, int[][]>();
		ArrayList<String> labelList = params.getLabelOrderAsList();
		ArrayList<String> subsetList = new ArrayList<String>();
		String[] accuracyKeys = new String[] {"LOW","AVERAGE","HIGH","VERY HIGH"};
		for (int i = 0; i < accuracyKeys.length; i++)
			matrixMap.put(accuracyKeys[i], new int[labelList.size()][labelList.size()]);
		LCDataBlock db = (LCDataBlock) getControl().getProcess().getOutputDataBlock(0); // TODO
		for (int i = 0; i < db.getUnitsCount(); i++) {
			LCCallCluster cc = db.getDataUnit(i, db.REFERENCE_ABSOLUTE).getCluster();
			String key;
			if (params.validation == params.LEAVEONEOUTFIRSTDIGIT)
				key = cc.clusterID.substring(0, 1);
			else key = cc.clusterID.substring(0, 2); //LEAVEONEOUTFIRSTDIGIT
			if (!matrixMap.containsKey(key)) {
				matrixMap.put(key, new int[labelList.size()][labelList.size()]);
				subsetList.add(key);
			}
			int [][] currMatrix = matrixMap.get(key);
		/*	for (int j = 0; j < labelList.size(); j++) System.out.print(labelList.get(j)+" ");
			System.out.println(labelList.indexOf(cc.getActualSpeciesString()));
			System.out.println(labelList.indexOf(cc.getPredictedSpeciesString())); */
			int actualIndex = labelList.indexOf(cc.getActualSpeciesString());
			int predictedIndex = labelList.indexOf(cc.getPredictedSpeciesString());
			if (actualIndex == -1 || predictedIndex == -1) continue;
			//TODO No idea why the case above is sometimes true - will definitely need to fix it later.
			currMatrix[actualIndex][predictedIndex]++;
			matrixMap.put(key, currMatrix);
			if (cc.getCertainty() >= params.veryLow) {
				int[][] lMatrix = matrixMap.get("LOW");
				lMatrix[labelList.indexOf(cc.getActualSpeciesString())][labelList.indexOf(cc.getPredictedSpeciesString())]++;
			}
			if (cc.getCertainty() >= params.low) {
				int[][] aMatrix = matrixMap.get("AVERAGE");
				aMatrix[labelList.indexOf(cc.getActualSpeciesString())][labelList.indexOf(cc.getPredictedSpeciesString())]++;
			}
			if (cc.getCertainty() >= params.average) {
				int[][] hMatrix = matrixMap.get("HIGH");
				hMatrix[labelList.indexOf(cc.getActualSpeciesString())][labelList.indexOf(cc.getPredictedSpeciesString())]++;
			}
			if (cc.getCertainty() >= params.high) {
				int[][] vhMatrix = matrixMap.get("VERY HIGH");
				vhMatrix[labelList.indexOf(cc.getActualSpeciesString())][labelList.indexOf(cc.getPredictedSpeciesString())]++;
			}
		}
		Collections.sort(subsetList);
		for (int i = 0; i < subsetList.size(); i++) {
			String key = subsetList.get(i);
			if (params.validation == params.LEAVEONEOUTFIRSTDIGIT)
				sb.append("CONFUSION MATRIX FOR SUBSETS BEGINNING WITH '"+key+"'\n\n");
			else sb.append("CONFUSION MATRIX FOR SUBSET "+key+"\n\n");
			int[][] matrix = matrixMap.get(key);
			sb = produceConfusionMatrixString(sb, params.labelOrder, matrix);
			sb.append("Cluster count: "+String.valueOf(getMatrixSum(matrix)));
			sb.append(" ("+String.format("%.1f", 100 * (float) getMatrixSum(matrix)/db.getUnitsCount())+"% of full set)\n\n");
		}
		for (int i = 0; i < accuracyKeys.length; i++) {
			String key = accuracyKeys[i];
			sb.append("CONFUSION MATRIX FOR CLUSTERS WITH AN ACCURACY OF '"+key+"' OR ABOVE\n\n");
			int[][] matrix = matrixMap.get(key);
			sb = produceConfusionMatrixString(sb, params.labelOrder, matrix);
			sb.append("Cluster count: "+String.valueOf(getMatrixSum(matrix))+"\n");
			sb.append("Percentage of full set ignored: ");
			sb.append(String.format("%.1f", 100 * (float) (db.getUnitsCount()-getMatrixSum(matrix))/db.getUnitsCount())+"%");
			sb.append("\n\n");
		}
		pw.write(sb.toString());
		pw.flush();
		return sb;
	}
	
	protected StringBuilder produceConfusionMatrixString(StringBuilder sb, String[] labels, int[][] matrix) {
		if (labels.length == 0 || labels.length != matrix.length || matrix.length != matrix[0].length) {
			sb.append("COULD NOT CONSTRUCT SUBSET CONFUSION MATRICES - INPUT ARRAYS WERE INCONGRUOUS.\n\n");
			return sb;
		}
		sb.append("\t");
		for (int i = 0; i < labels.length; i++) sb.append(labels[i]+"\t");
		sb.append("Recall\n");
		int correctSum = 0;
		int fullSum = 0;
		int[] precisionSums = new int[matrix.length];
		for (int i = 0; i < labels.length; i++) {
			sb.append(labels[i]+"\t");
			correctSum += matrix[i][i];
			int recallSum = 0;
			for (int j = 0; j < matrix[i].length; j++) {
				sb.append(String.valueOf(matrix[i][j])+"\t");
				fullSum += matrix[i][j];
				recallSum += matrix[i][j];
				precisionSums[j] += matrix[i][j];
			}
			if (recallSum > 0) sb.append(String.format("%.1f", 100 * (float) matrix[i][i]/recallSum)+"%\n");
			else sb.append("-%\n");
		}
		sb.append("Prcsn.\t");
		for (int i = 0; i < precisionSums.length; i++) {
			if (precisionSums[i] > 0) sb.append(String.format("%.1f", 100 * (float) matrix[i][i]/precisionSums[i])+"%\t");
			else sb.append("-%\t");
		}
		if (fullSum > 0) sb.append(String.format("%.1f", 100 * (float) correctSum/fullSum)+"%");
		else sb.append("-%");
		sb.append("\n");
		return sb;
	}
	
	protected int getMatrixSum(int[][] matrix) {
		int sum = 0;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) sum += matrix[i][j];
		}
		return sum;
	}
	
	protected TCControl getControl() {
		return (TCControl) lcControl;
	}
}