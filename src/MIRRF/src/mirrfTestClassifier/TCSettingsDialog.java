package mirrfTestClassifier;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import mirrfFeatureExtractor.FEDataUnit;
import mirrfLiveClassifier.LCControl;
import mirrfLiveClassifier.LCParameters;
import mirrfLiveClassifier.LCSettingsDialog;
import mirrfLiveClassifier.LCTrainingSetInfo;
import mirrfLiveClassifier.LCSettingsDialog.TrainSetListener;

public class TCSettingsDialog extends LCSettingsDialog {
	
	protected JTextField testSetField;
	protected JButton testSetButton;
	protected ButtonGroup validationRBG;
	protected JRadioButton leaveOneOutRB;
	protected JRadioButton kFoldRB;
	protected JRadioButton testSubsetRB;
	protected JRadioButton labelledRB;
	protected JRadioButton unlabelledRB;
	protected JTextField kFoldField;
	protected JComboBox<String> testSubsetBox;
	
	protected LCTrainingSetInfo loadedTestingSet;
	
	public TCSettingsDialog(Window parentFrame, TCControl tcControl) {
		super(parentFrame, tcControl);
	}
	
	@Override
	protected TCControl getControl() {
		return (TCControl) lcControl;
	}
	
	@Override
	protected void init() {
		super.init();
		loadedTestingSet = getControl().getTestingSetInfo();
		System.out.println("TCSettingsDialog: Init happened");
	}
	
	@Override
	protected JPanel createInputPanel() {
		// inputSourcePanel is never used here, I'm only initializing it to prevent a nullPointerException in actuallyGetParams().
		inputSourcePanel = new SourcePanel(this, "Data source", FEDataUnit.class, false, true);
		
		JPanel outp = new JPanel(new GridBagLayout());
		GridBagConstraints b = new PamGridBagContraints();
		b.fill = GridBagConstraints.HORIZONTAL;
		b.anchor = GridBagConstraints.NORTH;
		
		JPanel trainingSetPanel = new JPanel(new GridBagLayout());
		trainingSetPanel.setBorder(new TitledBorder("Training set"));
		trainSetField = new JTextField(20);
		trainSetField.setMinimumSize(new Dimension(trainSetField.getPreferredSize().width, trainSetField.getHeight()));
		trainSetField.setEnabled(false);
		trainSetField.setText(getControl().getTrainPath());
		GridBagConstraints c = new PamGridBagContraints();
		trainingSetPanel.add(trainSetField, c);
		c.gridx++;
		trainSetButton = new JButton("Select file");
		trainSetButton.addActionListener(new TCTrainSetListener(false));
		trainingSetPanel.add(trainSetButton, c);
		outp.add(trainingSetPanel, b);
		
		JPanel testingSetPanel = new JPanel(new GridBagLayout());
		testingSetPanel.setBorder(new TitledBorder("Testing set"));
		testSetField = new JTextField(20);
		testSetField.setMinimumSize(new Dimension(testSetField.getPreferredSize().width, testSetField.getHeight()));
		testSetField.setEnabled(false);
		testSetField.setText(getControl().getTestPath());
		c = new PamGridBagContraints();
		testingSetPanel.add(testSetField, c);
		c.gridx++;
		testSetButton = new JButton("Select file");
		testSetButton.addActionListener(new TCTrainSetListener(true));
		testingSetPanel.add(testSetButton, c);
		b.gridy++;
		outp.add(testingSetPanel, b);
		
		JPanel validationPanel = new JPanel(new GridBagLayout());
		validationPanel.setBorder(new TitledBorder("Validation"));
		c = new PamGridBagContraints();
		leaveOneOutRB = new JRadioButton();
		leaveOneOutRB.setText("Leave-one-subset-out cross-validation on training set");
		leaveOneOutRB.addActionListener(new ValidationRBListener(ValidationRBListener.LEAVEONEOUT));
		c.gridwidth = 4;
		c.anchor = c.WEST;
		c.fill = c.NONE;
		validationPanel.add(leaveOneOutRB, c);
		c.gridy++;
		c.gridwidth = 1;
		kFoldRB = new JRadioButton();
		kFoldRB.addActionListener(new ValidationRBListener(ValidationRBListener.KFOLD));
		validationPanel.add(kFoldRB, c);
		c.gridx++;
		kFoldField = new JTextField(4);
		kFoldField.setDocument(JIntFilter());
		validationPanel.add(kFoldField, c);
		c.gridx++;
		c.gridwidth = 2;
		validationPanel.add(new JLabel("-fold cross-validation on training set"), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 3;
		testSubsetRB = new JRadioButton();
		testSubsetRB.setText("Test subset within training set:");
		testSubsetRB.addActionListener(new ValidationRBListener(ValidationRBListener.TESTSUBSET));
		validationPanel.add(testSubsetRB, c);
		c.gridx += 3;
		c.gridwidth = 1;
		testSubsetBox = new JComboBox<String>();
		fillTestSubsetBox();
		validationPanel.add(testSubsetBox, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 4;
		labelledRB = new JRadioButton();
		labelledRB.setText("Use labelled testing set from Training Set Builder");
		labelledRB.addActionListener(new ValidationRBListener(ValidationRBListener.TESTSET));
		validationPanel.add(labelledRB, c);
		c.gridy++;
		unlabelledRB = new JRadioButton();
		unlabelledRB.setText("Use unlabelled testing set from Feature Extractor (TBA)");
		unlabelledRB.addActionListener(new ValidationRBListener(ValidationRBListener.TESTSET));
		unlabelledRB.setEnabled(false);
		validationPanel.add(unlabelledRB, c);
		validationRBG = new ButtonGroup();
		validationRBG.add(leaveOneOutRB);
		validationRBG.add(kFoldRB);
		validationRBG.add(testSubsetRB);
		validationRBG.add(labelledRB);
		validationRBG.add(unlabelledRB);
		b.gridy++;
		outp.add(validationPanel, b);
		
		return outp;
	}
	
	protected void fillTestSubsetBox() {
		testSubsetBox.removeAllItems();
		if (loadedTrainingSet == null) return;
		ArrayList<String> subsetList = loadedTrainingSet.getSortedSubsetList();
		ArrayList<String> outp = new ArrayList<String>();
		for (int i = 0; i < subsetList.size(); i++) {
			String firstDigit = subsetList.get(i).substring(0, 1);
			if (!outp.contains("All from "+firstDigit)) outp.add("All from "+firstDigit);
		}
		for (int i = 0; i < subsetList.size(); i++) {
			if (!outp.contains(subsetList.get(i))) outp.add(subsetList.get(i));
		}
		for (int i = 0; i < outp.size(); i++) testSubsetBox.addItem(outp.get(i));
		String currID = getControl().getParams().testSubset;
		if (currID.length() == 1) testSubsetBox.setSelectedItem("All from "+currID);
		else if  (currID.length() == 2) testSubsetBox.setSelectedItem(currID);
	}
	
	protected class ValidationRBListener implements ActionListener {
		public static final int LEAVEONEOUT = 0;
		public static final int KFOLD = 1;
		public static final int TESTSUBSET = 2;
		public static final int TESTSET = 3;
		private int selection;
		
		protected ValidationRBListener(int selection) {
			this.selection = selection;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			testSetButton.setEnabled(selection == TESTSET);
			kFoldField.setEnabled(selection == KFOLD);
			testSubsetBox.setEnabled(selection == TESTSUBSET);
		}
	}
	
	protected class LabelNotFoundException extends Exception {}
	
	// TODO Consider doing this check in TCProcess.
	public boolean checkClassSpread(LCTrainingSetInfo curr, boolean kFold, boolean testSubset) {
		if (kFold) testSubset = false; // kFold overrides testSubset.
		if (testSubset && testSubsetBox.getItemCount() == 0) {
			getControl().SimpleErrorDialog("Invalid testing subset selected.", 250);
			return false;
		}
		TCParameters params = generateParameters();
		HashMap<String, boolean[]> containMap = new HashMap<String, boolean[]>();
		File f = new File(curr.pathName);
		if (!f.exists()) {
			getControl().SimpleErrorDialog("Selected training set apparently no longer exists.", 250);
			return false;
		}
		Scanner sc = null;
		try {
			sc = new Scanner(f);
			int numLines = -1;
			ArrayList<String> clusterList = new ArrayList<String>();
			while (sc.hasNextLine()) {
				String[] nextLine = sc.nextLine().split(",");
				if (nextLine.length > 0 && !clusterList.contains(nextLine[0]) && numLines > -1) clusterList.add(nextLine[0]);
				numLines++;
			}
			sc.close();
			if (numLines <= 0) {
				getControl().SimpleErrorDialog("Selected training set is apparently now empty.", 250);
				return false;
			}
			int kNum = params.kNum;
			if (kFold && clusterList.size() < kNum) {
				getControl().SimpleErrorDialog("k-fold number must be greater than the number of call clusters in the table.", 250);
				return false;
			}
			clusterList.sort(Comparator.naturalOrder());
			sc = new Scanner(f);
			String[] nextLine = sc.nextLine().split(",");
			int lineNum = 0;
			ArrayList<String> labelList = params.getLabelOrderAsList();
			while (sc.hasNextLine()) {
				nextLine = sc.nextLine().split(",");
				if (nextLine.length < 8+curr.featureList.size() || nextLine[0].length() < 2) {
					continue;
				}
				String key = nextLine[0].substring(0,2);
				if (kFold) {
					key = String.valueOf((int) Math.floor(kNum * (double) clusterList.indexOf(nextLine[0]) / clusterList.size()));
				} else if (testSubset) {
					String currID = (String) testSubsetBox.getSelectedItem();
					if (currID.length() == 1 && key.substring(0, 1).equals(currID)) continue;
					if (currID.equals(key)) continue;
				}
				if (!containMap.containsKey(key)) {
					boolean[] boolArr = new boolean[labelList.size()];
					for (int i = 0; i < labelList.size(); i++) boolArr[i] = false;
					containMap.put(key, boolArr);
				}
				containMap.get(key)[labelList.indexOf(nextLine[7])] = true;
				lineNum++;
			}
			sc.close();
			if (testSubset && containMap.size() == 0) {
				getControl().SimpleErrorDialog("Training set must contain at least one other subset for training in order to "
						+ "test individual subsets.", 250);
				return false;
			}
			for (int i = 0; i < labelList.size(); i++) {
				int occursIn = 0;
				Iterator<String> it = containMap.keySet().iterator();
				while (it.hasNext()) {
					String next = it.next();
					if (containMap.get(next)[i]) occursIn++;
				}
				if (occursIn == 0) {
					if (testSubset) {
						getControl().SimpleErrorDialog("\""+labelList.get(i)+"\" only occurs in the selected "
								+ "testing subset. It must occur somewhere else in the set as well.", 250);
						return false;
					}
					else throw new LabelNotFoundException();
				}
				if (occursIn == 1 && !testSubset) {
					if (kFold) getControl().SimpleErrorDialog("\""+labelList.get(i)+"\" only occurs in one fold. "
								+ "Try again with a new k-value or consider spreading the label "
								+ "out through the set more.", 250);
					else getControl().SimpleErrorDialog("\""+labelList.get(i)+"\" only occurs in one subset. "
								+ "It must occur in multiple subsets in order for leave-one-out "
								+ "cross-validation to work.", 250);
					return false;
				}
			}
		} catch (FileNotFoundException e) {
			getControl().SimpleErrorDialog("Selected training set apparently no longer exists.", 250);
			return false;
		} catch (LabelNotFoundException e2) {
			e2.printStackTrace();
			getControl().SimpleErrorDialog("Label was not found in the set during the second scan. "
					+ "This should not happen and is entirely the developer's fault.", 250);
			return false;
		} catch (Exception e3) {
			sc.close();
			e3.printStackTrace();
			getControl().SimpleErrorDialog("Could not read selected training set. It may not be formatted properly.", 250);
			return false;
		}
		return true;
	}
	
	public boolean checkIfTrainingSetIsValid(LCTrainingSetInfo inp, boolean readThroughCSV) {
		TCParameters params = generateParameters();
		LCTrainingSetInfo curr = inp;
		if (readThroughCSV) {
			curr = readTrainingSet(false, new File(params.trainPath));
			if (curr == null) return false;
			if (!loadedTrainingSet.compare(curr)) {
				getControl().SimpleErrorDialog("Training set appears to have changed. Re-select it and try again.", 250);
				return false;
			}
		}
		if (curr == null) {
			getControl().SimpleErrorDialog("No training set has been selected.", 250);
			return false;
		}
		if (curr.labelCounts.keySet().size() <= 1) {
			getControl().SimpleErrorDialog("Training set must contain at least two different class labels.", 250);
			return false;
		}
		if (curr.featureList.size() <= 1) {
			getControl().SimpleErrorDialog("Training set must contain at least two features.", 250);
			return false;
		}
		if (params.validation.equals("leaveoneout")) {
			if (!checkClassSpread(curr, false, false)) return false;
		} else if (params.validation.equals("kfold")) {
			if (!checkClassSpread(curr, true, false)) return false;
		} else if (params.validation.equals("testsubset")) {
			if (!checkClassSpread(curr, false, true)) return false;
		}
		return true;
	}
	
	public boolean checkIfTestingSetIsValid(LCTrainingSetInfo inp, boolean readThroughCSV) {
		TCParameters params = generateParameters();
		if (inp == null || inp.pathName.length() == 0) {
			getControl().SimpleErrorDialog("No testing set has been selected.", 250);
			return false;
		}
		if (loadedTrainingSet == null || loadedTrainingSet.pathName.length() == 0) {
			getControl().SimpleErrorDialog("Training set needs to be selected first.", 250);
			return false;
		}
		LCTrainingSetInfo curr = inp;
		if (readThroughCSV) curr = readTrainingSet(true, new File(params.testPath));
		if (curr.pathName.equals(loadedTrainingSet.pathName)) {
			getControl().SimpleErrorDialog("Training and testing sets cannot be the same file.", 250);
			return false;
		}
		if (curr.featureList.size() != loadedTrainingSet.featureList.size()) {
			getControl().SimpleErrorDialog("Features between training and testing sets are not the same.", 250);
			return false;
		}
		for (int i = 0; i < curr.featureList.size(); i++) {
			if (!curr.featureList.get(i).equals(loadedTrainingSet.featureList.get(i))) {
				getControl().SimpleErrorDialog("Features between training and testing sets are not the same.", 250);
				return false;
			}
		}
		int matchingClasses = 0;
		int mismatchedClasses = 0;
		Iterator<String> it = curr.labelCounts.keySet().iterator();
		while (it.hasNext()) {
			String currLabel = it.next();
			if (loadedTrainingSet.labelCounts.keySet().contains(currLabel)) {
				matchingClasses++;
			} else {
				mismatchedClasses++;
			}
		}
		if (matchingClasses == 0) {
			getControl().SimpleErrorDialog("Testing set shares no class labels with training set.", 250);
			return false;
		}
		if (mismatchedClasses > 0) {
			int result = JOptionPane.showConfirmDialog(this, 
					getControl().makeHTML("Testing set contains class labels that aren't present in the "
							+ "training set. These will be ignored. Proceed?", 300),
					getControl().getUnitName(),
					JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.NO_OPTION) {
				return false;
			}
		}
		return true;
	}
	
	public class TCTrainSetListener extends TrainSetListener {
		
		public TCTrainSetListener (boolean testSet) {
			super(testSet);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			LCTrainingSetInfo tsi = readTrainingSet(testSet);
			if (tsi == null) {
				if (wdThread != null) wdThread.halt();
				return;
			}
			if (testSet) {
				if (checkIfTestingSetIsValid(tsi, false)) {
					loadedTestingSet = tsi;
					testSetField.setText(tsi.pathName);
				}
			} else {
				loadedTrainingSet = tsi;
				trainSetField.setText(tsi.pathName);
				fillTestSubsetBox();
				loadedTestingSet = getControl().getTestingSetInfo();
				testSetField.setText(getControl().getTestPath());
				updateLabelList(tsi);
			}
			if (wdThread != null) wdThread.halt();
		}
	}
	
	@Override
	public void actuallyGetParams() {
		super.actuallyGetParams();
		TCParameters params = getControl().getParams();
		if (params.validation.equals("leaveoneout")) leaveOneOutRB.doClick();
		else if (params.validation.equals("kfold")) kFoldRB.doClick();
		else if (params.validation.equals("testsubset")) testSubsetRB.doClick();
		else if (params.validation.equals("labelled")) labelledRB.doClick();
		else if (params.validation.equals("unlabelled")) unlabelledRB.doClick();
		kFoldField.setText(String.valueOf(params.kNum));
	}
	
	@Override
	protected boolean checkIfSettingsAreValid() {
		if (!super.checkIfSettingsAreValid()) return false;
		if (kFoldRB.isSelected() && (kFoldField.getText().length() == 0 || Integer.valueOf(kFoldField.getText()) < 2)) return false;
		if ((labelledRB.isSelected() || unlabelledRB.isSelected()) && !checkIfTestingSetIsValid(loadedTestingSet, true)) return false;
		if (testSubsetRB.isSelected() && testSubsetBox.getItemCount() == 0) return false;
		return true;
	}
	
	@Override
	public TCParameters generateParameters() {
		TCParameters params = (TCParameters) super.generateParameters();
		if (leaveOneOutRB.isSelected()) {
			params.validation = "leaveoneout";
		} else if (kFoldRB.isSelected()) {
			params.validation = "kfold";
			params.kNum = Integer.valueOf(kFoldField.getText());
		} else if (testSubsetRB.isSelected()) {
			params.validation = "testsubset";
			String testSubset = (String) testSubsetBox.getSelectedItem();
			if (testSubset.contains("All from ")) params.testSubset = testSubset.substring(9);
			else params.testSubset = testSubset;
		} else if (labelledRB.isSelected()) {
			params.validation = "labelled";
			params.testPath = loadedTestingSet.pathName;
		} else if (unlabelledRB.isSelected()) {
			params.validation = "unlabelled";
			params.testPath = loadedTestingSet.pathName;
		}
		return params;
	}
	
	@Override
	public boolean getParams() {
		if (!checkIfSettingsAreValid()) {
			return false;
		}
		getControl().setTrainingSetStatus(false);
		TCParameters params = generateParameters();
		params.trainPath = trainSetField.getText();
        getControl().setTrainingSetInfo(loadedTrainingSet);
        if (labelledRB.isSelected() || unlabelledRB.isSelected()) {
			params.testPath = testSetField.getText();
			getControl().setTestingSetInfo(loadedTestingSet);
		}
        
		// TODO Set GUI signal ?????
        getControl().setParams(params);
        getControl().getTabPanel().getPanel().createMatrices(params.labelOrder);
    	getControl().getSidePanel().getTCSidePanelPanel().startButton.setEnabled(true);
    	return true;
	}
	
	@Override
	public void restoreDefaultSettings() {
		super.restoreDefaultSettings();
		leaveOneOutRB.doClick();
		kFoldField.setText("10");
	}
}