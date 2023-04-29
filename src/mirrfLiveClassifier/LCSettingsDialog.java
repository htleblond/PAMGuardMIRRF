package mirrfLiveClassifier;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import PamController.PamController;
import PamUtils.PamFileChooser;
import mirrfFeatureExtractor.FEDataBlock;
import mirrfFeatureExtractor.FEDataUnit;
import PamView.dialog.GroupedSourcePanel;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;

public class LCSettingsDialog extends PamDialog {
	
	protected LCControl lcControl;
	private Window parentFrame;
	
	protected SourcePanel inputSourcePanel;
	protected JTextField trainSetField;
	protected JButton trainSetButton;
	
	protected JCheckBox kBestCheck;
	protected JTextField kBestField;
	protected ButtonGroup samplingRBG;
	protected JRadioButton fullSetRB;
	protected JRadioButton autoMaxRB;
	protected JRadioButton setMaxRB;
	protected JTextField setMaxField;
	protected JCheckBox clusterSizeCheck;
	protected JTextField clusterSizeField;
	protected JComboBox<String> tzBox;
	protected DefaultListModel<String> dlModel;
	protected JList<String> labelList;
	protected JButton moveUpButton;
	protected JButton moveDownButton;
	protected JButton manageColoursButton;
	
	protected JComboBox<String> classifierBox;
	protected CardLayout cl;
	protected JPanel classifierSettingsPanel;
	protected JPanel classifierCardsPanel;
	protected JPanel randomForestPanel;
	protected JPanel gradientBoostingPanel;
	protected JTextField nEstimatorsField;
	protected JComboBox<String> criterionBox;
	protected JCheckBox rfMaxDepthCheck;
	protected JTextField rfMaxDepthField;
	protected JComboBox<String> maxFeaturesBox;
	protected JTextField maxFeaturesField;
	protected JCheckBox bootstrapCheck;
	protected JComboBox<String> classWeightsBox;
	protected JTextField classWeightsField;
	protected JTextField learningRateField;
	protected JTextField maxIterationsField;
	protected JCheckBox hgbMaxDepthCheck;
	protected JTextField hgbMaxDepthField;
	
	protected JTextField minClusterField;
	protected JTextField veryLowField;
	protected JTextField lowField;
	protected JTextField averageField;
	protected JTextField highField;
	protected JComboBox<String> worstLeadBox;
	protected JCheckBox displayIgnoredCheck;
	
	//private String[] featureList;
	protected LCTrainingSetInfo loadedTrainingSet;
	protected HashMap<String, Color> currentColours;
	
	//protected volatile LCWaitingDialog wDialog;
	protected volatile LCWaitingDialogThread wdThread;
	
	public LCSettingsDialog(Window parentFrame, LCControl lcControl) {
		super(parentFrame, lcControl.getUnitName(), true);
		this.lcControl = lcControl;
		this.parentFrame = parentFrame;
		init();
	}
	
	protected void init() {
		//featureList = lcControl.featureList;
		currentColours = new HashMap<String, Color>(lcControl.getParams().labelColours);
		loadedTrainingSet = lcControl.getTrainingSetInfo();
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("Input", createInputPanel());
		tabbedPane.add("Training", createTrainingPanel());
		tabbedPane.add("Classification", createClassificationPanel());
		tabbedPane.add("Accuracy", createAccuracyPanel());
		actuallyGetParams();
		setDialogComponent(tabbedPane);
	}
	
	protected JPanel createInputPanel() {
		JPanel outp = new JPanel(new GridBagLayout());
		GridBagConstraints b = new PamGridBagContraints();
		b.fill = GridBagConstraints.HORIZONTAL;
		b.anchor = GridBagConstraints.NORTH;
		
		inputSourcePanel = new SourcePanel(this, "Data source", FEDataUnit.class, false, true);
		outp.add(inputSourcePanel.getPanel(), b);
		
		JPanel trainingSetPanel = new JPanel(new GridBagLayout());
		trainingSetPanel.setBorder(new TitledBorder("Training set"));
		trainSetField = new JTextField(20);
		trainSetField.setMinimumSize(new Dimension(trainSetField.getPreferredSize().width, trainSetField.getHeight()));
		trainSetField.setEnabled(false);
		trainSetField.setText(lcControl.getTrainPath());
		GridBagConstraints c = new PamGridBagContraints();
		trainingSetPanel.add(trainSetField, c);
		c.gridx++;
		trainSetButton = new JButton("Select file");
		trainSetButton.addActionListener(new TrainSetListener(false));
		trainingSetPanel.add(trainSetButton, c);
		b.gridy++;
		outp.add(trainingSetPanel, b);
		
		return outp;
	}
	
	protected JPanel createTrainingPanel() {
		JPanel outp = new JPanel(new GridBagLayout());
		GridBagConstraints b = new PamGridBagContraints();
		b.fill = GridBagConstraints.HORIZONTAL;
		b.anchor = GridBagConstraints.WEST;
		
		JPanel kBestPanel = new JPanel(new GridBagLayout());
		kBestPanel.setBorder(new TitledBorder("Feature selection"));
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 2;
		c.anchor = c.CENTER;
		c.fill = c.NONE;
		kBestCheck = new JCheckBox();
		kBestCheck.setText("Use k-Best Feature Selection");
		kBestCheck.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				kBestField.setEnabled(kBestCheck.isSelected());
			}
		});
		kBestPanel.add(kBestCheck, c);
		c.gridy++;
		c.gridwidth = 1;
		kBestPanel.add(new JLabel("k = "), c);
		c.gridx++;
		c.anchor = c.WEST;
		kBestField = new JTextField(3);
		kBestField.setDocument(JIntFilter());
		kBestPanel.add(kBestField, c);
		b.gridy++;
		outp.add(kBestPanel, b);
		
		JPanel samplingPanel = new JPanel(new GridBagLayout());
		samplingPanel.setBorder(new TitledBorder("Sampling"));
		c = new PamGridBagContraints();
		c.gridwidth = 2;
		c.anchor = c.WEST;
		c.fill = c.NONE;
		fullSetRB = new JRadioButton();
		fullSetRB.setText("Use full set");
		fullSetRB.addActionListener(new samplingRBListener());
		samplingPanel.add(fullSetRB, c);
		c.gridy++;
		autoMaxRB = new JRadioButton();
		autoMaxRB.setText("Automatically set maximum per class as number of entries in least-populated class");
		autoMaxRB.addActionListener(new samplingRBListener());
		samplingPanel.add(autoMaxRB, c);
		c.gridy++;
		c.gridwidth = 1;
		setMaxRB = new JRadioButton();
		setMaxRB.setText("Manually set maximum per class: ");
		setMaxRB.addActionListener(new samplingRBListener());
		samplingPanel.add(setMaxRB, c);
		samplingRBG = new ButtonGroup();
		samplingRBG.add(fullSetRB);
		samplingRBG.add(autoMaxRB);
		samplingRBG.add(setMaxRB);
		c.gridx++;
		setMaxField = new JTextField(7);
		setMaxField.setDocument(JIntFilter());
		samplingPanel.add(setMaxField, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		JPanel clusterSizePanel = new JPanel(new GridBagLayout());
		GridBagConstraints d = new PamGridBagContraints();
		d.anchor = d.WEST;
		d.fill = d.NONE;
		clusterSizeCheck = new JCheckBox();
		clusterSizeCheck.setText("Limit cluster size to ");
		clusterSizeCheck.addActionListener(new clusterSizeListener());
		clusterSizePanel.add(clusterSizeCheck, d);
		d.gridx++;
		clusterSizeField = new JTextField(4);
		clusterSizeField.setDocument(JIntFilter());
		clusterSizePanel.add(clusterSizeField, d);
		d.gridx++;
		clusterSizePanel.add(new JLabel(" contours"), d);
		samplingPanel.add(clusterSizePanel, c);
		b.gridy++;
		outp.add(samplingPanel, b);
		
		JPanel timeZonePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		timeZonePanel.setBorder(new TitledBorder("Time zone"));
		String[] tz_list = TimeZone.getAvailableIDs();
		tzBox = new JComboBox<String>(tz_list);
		tzBox.setSelectedItem(lcControl.getParams().timeZone);
		tzBox.setSize(200, tzBox.getHeight());
		timeZonePanel.add(tzBox);
		b.gridy++;
		outp.add(timeZonePanel, b);
		
		JPanel labelOrderPanel = new JPanel(new GridBagLayout());
		labelOrderPanel.setBorder(new TitledBorder("Class label order"));
		c = new PamGridBagContraints();
		c.fill = c.HORIZONTAL;
		c.anchor = c.CENTER;
		dlModel = new DefaultListModel<String>();
		labelList = new JList<String>(dlModel);
		labelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		labelList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				moveUpButton.setEnabled(labelList.getSelectedIndex() > 0);
				moveDownButton.setEnabled(labelList.getSelectedIndex() < labelList.getModel().getSize()-1 && labelList.getSelectedIndex() != -1);
			}
		});
		labelList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane sp = new JScrollPane(labelList);
		labelOrderPanel.add(sp, c);
		c.gridy++;
		JPanel labelOrderButtonPanel = new JPanel(new GridLayout(1,2,5,5));
		moveUpButton = new JButton("Move up");
		moveUpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int selection = labelList.getSelectedIndex();
				if (selection > 0) {
					ArrayList<String> oldVals = new ArrayList<String>();
					for (int i = 0; i < labelList.getModel().getSize(); i++) {
						oldVals.add((String) labelList.getModel().getElementAt(i));
					}
					dlModel.removeAllElements();
					for (int i = 0; i < oldVals.size(); i++) {
						if (i == selection-1) {
							dlModel.addElement(oldVals.get(i+1));
						} else if (i == selection) {
							dlModel.addElement(oldVals.get(i-1));
						} else {
							dlModel.addElement(oldVals.get(i));
						}
					}
					labelList.setSelectedIndex(selection-1);
				}
			}
		});
		labelOrderButtonPanel.add(moveUpButton);
		moveDownButton = new JButton("Move down");
		moveDownButton.setEnabled(false);
		moveDownButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int selection = labelList.getSelectedIndex();
				if (selection < labelList.getModel().getSize()-1 && selection != -1) {
					ArrayList<String> oldVals = new ArrayList<String>();
					for (int i = 0; i < labelList.getModel().getSize(); i++) {
						oldVals.add((String) labelList.getModel().getElementAt(i));
					}
					dlModel.removeAllElements();
					for (int i = 0; i < oldVals.size(); i++) {
						if (i == selection) {
							dlModel.addElement(oldVals.get(i+1));
						} else if (i == selection+1) {
							dlModel.addElement(oldVals.get(i-1));
						} else {
							dlModel.addElement(oldVals.get(i));
						}
					}
					labelList.setSelectedIndex(selection+1);
				}
			}
		});
		labelOrderButtonPanel.add(moveDownButton);
		labelOrderPanel.add(labelOrderButtonPanel, c);
		c.gridy++;
		manageColoursButton = new JButton("Manage colours...");
		manageColoursButton.setEnabled(false);
		manageColoursButton.addActionListener(new ColourListener(this));
		labelOrderPanel.add(manageColoursButton, c);
		b.gridy++;
		outp.add(labelOrderPanel, b);
		
		return outp;
	}
	
	protected JPanel createClassificationPanel() {
		JPanel outp = new JPanel(new GridBagLayout());
		GridBagConstraints b = new PamGridBagContraints();
		b.fill = GridBagConstraints.HORIZONTAL;
		b.anchor = GridBagConstraints.WEST;
		
		JPanel classifierBoxPanel = new JPanel(new GridBagLayout());
		classifierBoxPanel.setBorder(new TitledBorder("Select classifier model"));
		GridBagConstraints c = new PamGridBagContraints();
		c.fill = c.HORIZONTAL;
		c.anchor = c.WEST;
		classifierBox = new JComboBox<String>(new String[] {"RandomForestClassifier (Scikit-Learn)",
				"ExtraTreesClassifier (Scikit-Learn)", "HistGradientBoostingClassifier (Scikit-Learn)"});
		classifierBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (classifierBox.getSelectedIndex() < 2) {
					cl.show(classifierCardsPanel, "RandomForest");
				} else {
					cl.show(classifierCardsPanel, "GradientBoosting");
				}
			}
		});
		classifierBoxPanel.add(classifierBox, c);
		outp.add(classifierBoxPanel, b);
		
		classifierSettingsPanel = new JPanel(new BorderLayout());
		classifierSettingsPanel.setBorder(new TitledBorder("Classifier settings"));
		classifierCardsPanel = new JPanel(new CardLayout());
		cl = (CardLayout) classifierCardsPanel.getLayout();
		
		randomForestPanel = new JPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		c.fill = c.HORIZONTAL;
		c.anchor = c.WEST;
		randomForestPanel.add(new JLabel("Number of estimators"), c);
		c.gridx++;
		c.gridwidth = 2;
		nEstimatorsField = new JTextField(7);
		nEstimatorsField.setDocument(JIntFilter());
		randomForestPanel.add(nEstimatorsField, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		randomForestPanel.add(new JLabel("Criterion"), c);
		c.gridx++;
		c.gridwidth = 2;
		criterionBox = new JComboBox<String>(new String[] {"Gini", "Log. loss", "Entropy"});
		randomForestPanel.add(criterionBox, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		randomForestPanel.add(new JLabel("Max. depth"), c);
		c.gridx++;
		rfMaxDepthCheck = new JCheckBox();
		rfMaxDepthCheck.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				rfMaxDepthField.setEnabled(rfMaxDepthCheck.isSelected());
			}
		});
		randomForestPanel.add(rfMaxDepthCheck, c);
		c.gridx++;
		rfMaxDepthField = new JTextField(5);
		rfMaxDepthField.setDocument(JIntFilter());
		randomForestPanel.add(rfMaxDepthField, c);
		c.gridy++;
		c.gridx = 0;
		randomForestPanel.add(new JLabel("Max. features"), c);
		c.gridx++;
		c.gridwidth = 2;
		maxFeaturesBox = new JComboBox<String>(new String[] {"Square root", "Log2", "Custom"});
		maxFeaturesBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				maxFeaturesField.setEnabled(maxFeaturesBox.getSelectedItem().equals("Custom"));
			}
		});
		randomForestPanel.add(maxFeaturesBox, c);
		c.gridy++;
		maxFeaturesField = new JTextField(7);
		maxFeaturesField.setDocument(JIntFilter());
		randomForestPanel.add(maxFeaturesField, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		randomForestPanel.add(new JLabel("Bootstrap"), c);
		c.gridx++;
		bootstrapCheck = new JCheckBox();
		randomForestPanel.add(bootstrapCheck, c);
		c.gridy++;
		c.gridx = 0;
		randomForestPanel.add(new JLabel("Class weights"), c);
		c.gridx++;
		c.gridwidth = 2;
		classWeightsBox = new JComboBox<String>(new String[] {"None", "Balanced", "Balanced subsample", "Custom"});
		classWeightsBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				classWeightsField.setEnabled(classWeightsBox.getSelectedItem().equals("Custom"));
			}
		});
		randomForestPanel.add(classWeightsBox, c);
		c.gridy++;
		classWeightsField = new JTextField(7);
		classWeightsField.setDocument(ClassWeightsFilter());
		randomForestPanel.add(classWeightsField, c);
		classifierCardsPanel.add(randomForestPanel, "RandomForest");
		
		gradientBoostingPanel = new JPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		c.fill = c.HORIZONTAL;
		c.anchor = c.WEST;
		gradientBoostingPanel.add(new JLabel("Learning rate"), c);
		c.gridx++;
		c.gridwidth = 2;
		learningRateField = new JTextField(7);
		learningRateField.setDocument(JDoubleFilter(learningRateField));
		gradientBoostingPanel.add(learningRateField, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		gradientBoostingPanel.add(new JLabel("Max. iterations"), c);
		c.gridx++;
		c.gridwidth = 2;
		maxIterationsField = new JTextField(7);
		maxIterationsField.setDocument(JIntFilter());
		gradientBoostingPanel.add(maxIterationsField, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		gradientBoostingPanel.add(new JLabel("Max. depth"), c);
		c.gridx++;
		hgbMaxDepthCheck = new JCheckBox();
		hgbMaxDepthCheck.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hgbMaxDepthField.setEnabled(hgbMaxDepthCheck.isSelected());
			}
		});
		gradientBoostingPanel.add(hgbMaxDepthCheck, c);
		c.gridx++;
		hgbMaxDepthField = new JTextField(5);
		hgbMaxDepthField.setDocument(JIntFilter());
		gradientBoostingPanel.add(hgbMaxDepthField, c);
		classifierCardsPanel.add(gradientBoostingPanel, "GradientBoosting");
		
		classifierSettingsPanel.add(classifierCardsPanel);
		b.gridy++;
		outp.add(classifierSettingsPanel, b);
		
		return outp;
	}
	
	protected JPanel createAccuracyPanel() {
		JPanel outp = new JPanel(new GridBagLayout());
		GridBagConstraints b = new PamGridBagContraints();
		b.fill = GridBagConstraints.HORIZONTAL;
		b.anchor = GridBagConstraints.WEST;
		
		JPanel accuracyPanel = new JPanel(new GridBagLayout());
		accuracyPanel.setBorder(new TitledBorder(""));
		GridBagConstraints c = new PamGridBagContraints();
		c.fill = c.HORIZONTAL;
		c.anchor = c.WEST;
		accuracyPanel.add(new JLabel("Min. cluster size"), c);
		c.gridx++;
		c.gridwidth = 2;
		minClusterField = new JTextField(7);
		minClusterField.setDocument(JIntFilter());
		accuracyPanel.add(minClusterField, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		accuracyPanel.add(new JLabel("\nLead descriptors:"), c);
		c.gridy++;
		accuracyPanel.add(new JLabel("Very low "), c);
		c.gridx++;
		accuracyPanel.add(new JLabel("< 0."), c);
		c.gridx++;
		veryLowField = new JTextField(5);
		veryLowField.setDocument(JIntFilter());
		accuracyPanel.add(veryLowField, c);
		c.gridy++;
		c.gridx = 0;
		accuracyPanel.add(new JLabel("Low "), c);
		c.gridx++;
		accuracyPanel.add(new JLabel("< 0."), c);
		c.gridx++;
		lowField = new JTextField(5);
		lowField.setDocument(JIntFilter());
		accuracyPanel.add(lowField, c);
		c.gridy++;
		c.gridx = 0;
		accuracyPanel.add(new JLabel("Average "), c);
		c.gridx++;
		accuracyPanel.add(new JLabel("< 0."), c);
		c.gridx++;
		averageField = new JTextField(5);
		averageField.setDocument(JIntFilter());
		accuracyPanel.add(averageField, c);
		c.gridy++;
		c.gridx = 0;
		accuracyPanel.add(new JLabel("High "), c);
		c.gridx++;
		accuracyPanel.add(new JLabel("< 0."), c);
		c.gridx++;
		highField = new JTextField(5);
		highField.setDocument(JIntFilter());
		accuracyPanel.add(highField, c);
		c.gridy++;
		c.gridx = 0;
		accuracyPanel.add(new JLabel("Very high "), c);
		c.gridx++;
		c.gridwidth = 2;
		accuracyPanel.add(new JLabel("<= 1.00"), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		accuracyPanel.add(new JLabel("Worst acceptable lead:"), c);
		c.gridx++;
		c.gridwidth = 2;
		worstLeadBox = new JComboBox<String>(new String[] {"Very low", "Low", "Average", "High", "Very high"});
		accuracyPanel.add(worstLeadBox, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		accuracyPanel.add(new JLabel("Display ignored clusters"), c);
		c.gridx++;
		displayIgnoredCheck = new JCheckBox();
		accuracyPanel.add(displayIgnoredCheck, c);
		outp.add(accuracyPanel, b);
		
		return outp;
	}
	
/*	public class WaitThread extends Thread {
		protected String message;
		
		public WaitThread(String message) {
			this.message = message;
		}
		
		@Override
		public void run() {
			wdThread = new LCWaitingDialogThread(parentFrame, getControl(), message);
			wdThread.start();
		}
	} */
	
	public class TrainSetListener implements ActionListener {
		protected boolean testSet;
		
		public TrainSetListener (boolean testSet) {
			this.testSet = testSet;
		}
		
		public void actionPerformed(ActionEvent e) {
			if (inputSourcePanel.getSourceCount() == 0) {
				lcControl.SimpleErrorDialog("No Feature Extractor module found. One should be added before selecting the training set.", 300);
				return;
			}
			LCTrainingSetInfo tsi = readTrainingSet(testSet);
			if (tsi == null) {
				if (wdThread != null) wdThread.halt();
				return;
			}
			//lcControl.setTrainPath(pathName);
			loadedTrainingSet = tsi;
			trainSetField.setText(tsi.pathName);
			updateLabelList(tsi);
			if (wdThread != null) wdThread.halt();
		}
	}
	
	protected void updateLabelList(LCTrainingSetInfo tsi) {
		boolean matchingTable = true;
		if (tsi.labelCounts.size() == dlModel.size()) {
			for (int i = 0; i < dlModel.size(); i++) {
				if (!tsi.labelCounts.containsKey(dlModel.elementAt(i))) {
					matchingTable = false;
					break;
				}
			}
		} else {
			matchingTable = false;
		}
		if (!matchingTable) {
			dlModel.clear();
			ArrayList<String> newLabels = tsi.getSortedLabelList();
			for (int i = 0; i < newLabels.size(); i++) {
				dlModel.addElement(newLabels.get(i));
			}
			String[] labelArr = new String[newLabels.size()];
			for (int i = 0; i < newLabels.size(); i++) {
				labelArr[i] = newLabels.get(i);
			}
			currentColours = lcControl.getParams().generateColours(labelArr);
		}
		manageColoursButton.setEnabled(true);
	}
	
	protected LCTrainingSetInfo readTrainingSet(boolean testSet) {
		if (testSet && loadedTrainingSet == null) {
			lcControl.SimpleErrorDialog("Training set must be selected first.", 250);
			return null;
		}
		PamFileChooser fc = new PamFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(false);
		fc.addChoosableFileFilter(new FileNameExtensionFilter("Comma-separated values file (*.csv)","csv"));
		int returnVal = fc.showOpenDialog(lcControl.getPamView().getGuiFrame());
		if (returnVal == fc.APPROVE_OPTION) {
			File f = getSelectedFileWithExtension(fc);
			return readTrainingSet(testSet, f);
		}
		return null;
	}
	
	protected LCTrainingSetInfo readTrainingSet(boolean testSet, File f) {
		String message = "Validating training set...";
		if (testSet) message = "Validating testing set...";
		//WaitThread wt = new WaitThread(message);
		//wt.start();
		wdThread = new LCWaitingDialogThread(parentFrame, getControl(), message);
		wdThread.start();
		if (f.exists()) {
			Scanner sc;
			try {
				LCTrainingSetInfo outp = new LCTrainingSetInfo(f.getPath());
				sc = new Scanner(f);
				if (sc.hasNextLine()) {
					String[] firstLine = sc.nextLine().split(",");
					if (firstLine.length >= 10 && firstLine[0].equals("cluster") && firstLine[1].equals("uid")
							&& firstLine[2].equals("location") && firstLine[3].equals("date") && firstLine[4].equals("duration")
							&& firstLine[5].equals("lf") && firstLine[6].equals("hf") && firstLine[7].equals("label")) {
						ArrayList<String[]> dataLines = new ArrayList<String[]>();
						//ArrayList<String> currLabels = new ArrayList<String>();
						while (sc.hasNextLine()) {
							String[] nextLine = sc.nextLine().split(",");
							if (nextLine.length == firstLine.length) {
								boolean allValid = true;
								if (nextLine[0].length() < 2) continue;
								for (int i = 0; i < nextLine.length; i++) {
									try {
										if ((i < 4 && i != 3) || i == 7) {
											assert nextLine[i].length() > 0;
										} else if (i != 3) {
											double test = Double.valueOf(nextLine[i]);
										}
									} catch (Exception e2) {
										allValid = false;
										break;
									}
								}
								if (allValid) {
									outp.addBatchID(nextLine[0].substring(0,2));
									dataLines.add(nextLine);
									//if (!currLabels.contains(nextLine[7])) {
									//	currLabels.add(nextLine[7]);
									//}
									outp.addLabel(nextLine[7]);
								}
							}
						}
						if (dataLines.size() > 0) {
							if (outp.labelCounts.size() > 1 || (testSet && outp.labelCounts.size() > 0)) {
								//lcControl.trainPath = f.getPath();
								//trainSetField.setText(lcControl.trainPath);
							/*	featureList = new String[firstLine.length-8];
								for (int i = 8; i < firstLine.length; i++) {
									featureList[i-8] = firstLine[i];
								}
								dlModel.clear();
								for (int i = 0; i < currLabels.size(); i++) {
									dlModel.addElement(currLabels.get(i));
								}
								String[] labelArr = new String[currLabels.size()];
								for (int i = 0; i < currLabels.size(); i++) {
									labelArr[i] = currLabels.get(i);
								}
								currentColours = lcControl.getParams().generateColours(labelArr);
								manageColoursButton.setEnabled(true);
								return f.getPath(); */
								for (int i = 8; i < firstLine.length; i++) {
									outp.featureList.add(firstLine[i]);
								}
								sc.close();
								return outp;
							} else {
								lcControl.SimpleErrorDialog("Selected training set must include at least two classes.", 250);
								sc.close();
								return null;
							}
						} else {
							lcControl.SimpleErrorDialog("Selected set does not contain any valid data.", 250);
							sc.close();
							return null;
						}
					} else {
						lcControl.SimpleErrorDialog("Selected set is not formatted like Training Set Builder output.", 250);
						sc.close();
						return null;
					}
				} else {
					lcControl.SimpleErrorDialog("Selected set is a blank file.", 250);
					sc.close();
					return null;
				}
			} catch (Exception e2) {
				e2.printStackTrace();
				lcControl.SimpleErrorDialog("Error encountered when attempting to load set.", 250);
				return null;
			}
		}
		lcControl.SimpleErrorDialog("Selected file does not exist.", 250);
		return null;
	}
	
	class ColourListener implements ActionListener {
		private LCSettingsDialog sDialog;
		
		public ColourListener(LCSettingsDialog sDialog) {
			this.sDialog = sDialog;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			LCColourDialog cDialog = new LCColourDialog(parentFrame, lcControl, sDialog);
			cDialog.setVisible(true);
		}
	}
	
	public void actuallyGetParams() {
		LCParameters params = lcControl.getParams();
		inputSourcePanel.setSource(params.inputProcessName);
		kBestCheck.setSelected(params.selectKBest);
		kBestField.setEnabled(params.selectKBest);
		kBestField.setText(String.valueOf(params.kBest));
		fullSetRB.setSelected(params.samplingLimits.equals("none"));
		autoMaxRB.setSelected(params.samplingLimits.equals("automax"));
		setMaxRB.setSelected(params.samplingLimits.equals("setmax"));
		setMaxField.setEnabled(params.samplingLimits.equals("setmax"));
		setMaxField.setText(String.valueOf(params.maxSamples));
		clusterSizeCheck.setSelected(params.limitClusterSize);
		clusterSizeField.setEnabled(params.limitClusterSize);
		clusterSizeField.setText(String.valueOf(params.maxClusterSize));
		tzBox.setSelectedItem(params.timeZone);
		for (int i = 0; i < params.labelOrder.length; i++) {
			dlModel.addElement(params.labelOrder[i]);
		}
		manageColoursButton.setEnabled(params.labelOrder.length > 0);
		if (params.classifierName.equals("RandomForestClassifier")) {
			classifierBox.setSelectedIndex(0);
			cl.show(classifierCardsPanel, "RandomForest");
		} else if (params.classifierName.equals("ExtraTreesClassifier")) {
			classifierBox.setSelectedIndex(1);
			cl.show(classifierCardsPanel, "RandomForest");
		} else if (params.classifierName.equals("HistGradientBoostingClassifier")) {
			classifierBox.setSelectedIndex(2);
			cl.show(classifierCardsPanel, "GradientBoosting");
		}
		nEstimatorsField.setText(String.valueOf(params.nEstimators));
		criterionBox.setSelectedItem(params.criterion);
		rfMaxDepthCheck.setSelected(params.hasMaxDepth);
		rfMaxDepthField.setEnabled(params.hasMaxDepth);
		rfMaxDepthField.setText(String.valueOf(params.maxDepth));
		hgbMaxDepthCheck.setSelected(params.hasMaxDepth);
		hgbMaxDepthField.setEnabled(params.hasMaxDepth);
		hgbMaxDepthField.setText(String.valueOf(params.maxDepth));
		maxFeaturesBox.setSelectedItem(params.maxFeaturesMode);
		maxFeaturesField.setEnabled(params.maxFeaturesMode.equals("Custom"));
		maxFeaturesField.setText(String.valueOf(params.maxFeatures));
		bootstrapCheck.setSelected(params.bootstrap);
		classWeightsBox.setSelectedItem(params.classWeightMode);
		classWeightsField.setEnabled(params.classWeightMode.equals("Custom"));
		String weights = "";
		for (int i = 0; i < params.classWeights.length; i++) {
			if (i != 0) {
				weights += ",";
			}
			weights += String.valueOf(params.classWeights[i]);
		}
		classWeightsField.setText(weights);
		learningRateField.setText(String.valueOf(params.learningRate));
		maxIterationsField.setText(String.valueOf(params.maxIterations));
		minClusterField.setText(String.valueOf(params.minClusterSize));
		veryLowField.setText(String.valueOf(params.veryLow).substring(2));
		lowField.setText(String.valueOf(params.low).substring(2));
		averageField.setText(String.valueOf(params.average).substring(2));
		highField.setText(String.valueOf(params.high).substring(2));
		worstLeadBox.setSelectedItem(params.worstLead);
		displayIgnoredCheck.setSelected(params.displayIgnored);
	}
	
	public void switchOn(Object box, boolean boo) {
		// TODO
	}
	
	class CheckBoxListener implements ActionListener {
		private JCheckBox box;
		public CheckBoxListener(JCheckBox box) {
			this.box = box;
		}
		public void actionPerformed(ActionEvent e) {
			switchOn(box, box.isSelected());
		}
	}
	
	class samplingRBListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			setMaxField.setEnabled(setMaxRB.isSelected());
		}
	}
	
	class clusterSizeListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			clusterSizeField.setEnabled(clusterSizeCheck.isSelected());
		}
	}
	
	/**
	 * Returns the selected file from a JFileChooser, including the extension from
	 * the file filter.
	 * 
	 * Copied from here: https://stackoverflow.com/questions/16846078/jfilechoosershowsavedialog-cant-get-the-value-of-the-extension-file-chosen
	 * Author page: https://stackoverflow.com/users/964243/boann
	 */
	public static File getSelectedFileWithExtension(JFileChooser c) {
	    File file = c.getSelectedFile();
	    if (c.getFileFilter() instanceof FileNameExtensionFilter) {
	        String[] exts = ((FileNameExtensionFilter)c.getFileFilter()).getExtensions();
	        String nameLower = file.getName().toLowerCase();
	        for (String ext : exts) { // check if it already has a valid extension
	            if (nameLower.endsWith('.' + ext.toLowerCase())) {
	                return file; // if yes, return as-is
	            }
	        }
	        // if not, append the first extension from the selected filter
	        file = new File(file.toString() + '.' + exts[0]);
	    }
	    return file;
	}
	
	// Kudos to this: https://stackoverflow.com/questions/1842223/java-linebreaks-in-jlabels
	public String makeHTML(String inp, int width) {
		String outp = "<html><p style=\"width:"+Integer.toString(width)+"px\">"+inp+"</p></html>";
		return outp;
	}
	
	class RadioButtonListener implements ActionListener {
		private JRadioButton rb;
		public RadioButtonListener(JRadioButton rb) {
			this.rb = rb;
		}
		public void actionPerformed(ActionEvent e) {
			switchOn(rb, rb.isSelected());
		}
	}
	
	/**
	 * Limits entry in text field to numbers only.
	 * @return PlainDocument
	 */
	public PlainDocument JIntFilter() {
		PlainDocument d = new PlainDocument() {
			@Override
	        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
	            char c = str.charAt(0);
	            if (c >= '0' && c <= '9') {
	            	super.insertString(offs, str, a);
		        }
	        }
		};
		return d;
	}
	
	/**
	 * Limits entry in text field to numbers and a single decimal point only.
	 * @return PlainDocument
	 */
	public PlainDocument JDoubleFilter(JTextField field) {
		PlainDocument d = new PlainDocument() {
			@Override
	        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
	            char c = str.charAt(0);
	            String fieldText = field.getText();
	            if ((!fieldText.contains(".")) && ((c >= '0' && c <= '9') || (c == '.'))) {
	            	super.insertString(offs, str, a);
		        } else if (fieldText.contains(".") && (c >= '0' && c <= '9')) {
		        	super.insertString(offs, str, a);
		        }
	        }
		};
		return d;
	}
	
	/**
	 * Limits entry in text field to numbers and a single decimal point only.
	 * @return PlainDocument
	 */
	public PlainDocument ClassWeightsFilter() {
		PlainDocument d = new PlainDocument() {
			@Override
	        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
	            char c = str.charAt(0);
	            if ((c >= '0' && c <= '9') || c == '.' || c == ',') {
	            	super.insertString(offs, str, a);
	            }
	        }
		};
		return d;
	}
	
	@Override
	public void cancelButtonPressed() {
		
	}
	
	/**
	 * Streamlined error dialog with an editable message.
	 */
	public void SimpleErrorDialog(String inptext) {
		JOptionPane.showMessageDialog(null,
			inptext,
			"",
			JOptionPane.ERROR_MESSAGE);
	}
	
	protected boolean checkIfSettingsAreValid() {
		if (loadedTrainingSet == null || loadedTrainingSet.pathName.length() == 0) {
			lcControl.SimpleErrorDialog("No training set has been loaded.", 250);
			return false;
		}
		if (kBestCheck.isSelected()) {
			try {
				if (Integer.valueOf(kBestField.getText()) < 2) {
					throw new Exception();
				}
			} catch (Exception e) {
				lcControl.SimpleErrorDialog("Number of features for k-best feature selection must be at least 2.", 250);
				return false;
			}
		}
		if (setMaxRB.isSelected()) {
			try {
				if (Integer.valueOf(setMaxField.getText()) < 1) {
					throw new Exception();
				}
			} catch (Exception e) {
				lcControl.SimpleErrorDialog("Maximum number of samples per class must be a positive integer.", 250);
				return false;
			}
		}
		if (clusterSizeCheck.isSelected()) {
			try {
				if (Integer.valueOf(clusterSizeField.getText()) < 1) {
					throw new Exception();
				}
			} catch (Exception e) {
				lcControl.SimpleErrorDialog("Maximum number of samples per cluster must be a positive integer.", 250);
				return false;
			}
		}
		if (classifierBox.getSelectedIndex() < 2) {
			try {
				if (Integer.valueOf(nEstimatorsField.getText()) < 1) {
					throw new Exception();
				}
			} catch (Exception e) {
				lcControl.SimpleErrorDialog("Number of estimators must at least be 1.", 250);
				return false;
			}
			if (rfMaxDepthCheck.isSelected()) {
				try {
					if (Integer.valueOf(rfMaxDepthField.getText()) < 2) {
						throw new Exception();
					}
				} catch (Exception e) {
					lcControl.SimpleErrorDialog("Maximum tree depth must at least be 2.", 250);
					return false;
				}
			}
			if (maxFeaturesBox.getSelectedItem().equals("Custom")) {
				try {
					if (Integer.valueOf(maxFeaturesField.getText()) < 2) {
						throw new Exception();
					}
				} catch (Exception e) {
					lcControl.SimpleErrorDialog("Maximum number of features per tree must at least be 2.", 250);
					return false;
				}
			}
			if (classWeightsBox.getSelectedItem().equals("Custom")) {
				try {
					String[] tokens = classWeightsField.getText().split(",");
					for (int i = 0; i < tokens.length; i++) {
						double test = Double.valueOf(tokens[i]);
					}
					if (!(dlModel.getSize() == 0 || dlModel.getSize() == tokens.length)) {
						throw new Exception();
					}
				} catch (NumberFormatException e) {
					lcControl.SimpleErrorDialog("Class weights must all be valid numbers with or without a decimal point and separated by commas.", 250);
					return false;
				} catch (Exception e) {
					lcControl.SimpleErrorDialog("Number of class weights must match the number of classes in selected training set.", 250);
					return false;
				}
			}
		} else {
			try {
				if (Double.valueOf(learningRateField.getText()) <= 0.0 || Double.valueOf(learningRateField.getText()) > 1.0) {
					throw new Exception();
				}
			} catch (Exception e) {
				lcControl.SimpleErrorDialog("Learning rate must be greater than 0 and less than or equal to 1.", 250);
				return false;
			}
			try {
				if (Integer.valueOf(maxIterationsField.getText()) < 1) {
					throw new Exception();
				}
			} catch (Exception e) {
				lcControl.SimpleErrorDialog("Maximum number of iterations must at least be 1.", 250);
				return false;
			}
			if (hgbMaxDepthCheck.isSelected()) {
				try {
					if (Integer.valueOf(hgbMaxDepthField.getText()) < 2) {
						throw new Exception();
					}
				} catch (Exception e) {
					lcControl.SimpleErrorDialog("Maximum tree depth must at least be 2.", 250);
					return false;
				}
			}
		}
		return true;
	}
	
	public LCParameters generateParameters() {
		LCParameters params = lcControl.getParams();
		if (inputSourcePanel.getSource() != null)
			params.inputProcessName = inputSourcePanel.getSource().getDataName();
		params.selectKBest = kBestCheck.isSelected();
		if (params.selectKBest) {
			params.kBest = Integer.valueOf(kBestField.getText());
		}
		if (fullSetRB.isSelected()) {
			params.samplingLimits = "none";
		} else if (autoMaxRB.isSelected()) {
			params.samplingLimits = "automax";
		} else if (setMaxRB.isSelected()) {
			params.samplingLimits = "setmax";
			params.maxSamples = Integer.valueOf(setMaxField.getText());
		}
		params.limitClusterSize = clusterSizeCheck.isSelected();
		if (clusterSizeCheck.isSelected()) {
			params.maxClusterSize = Integer.valueOf(clusterSizeField.getText());
		}
		params.timeZone = (String) tzBox.getSelectedItem();
		params.labelOrder = new String[dlModel.getSize()];
		for (int i = 0; i < dlModel.getSize(); i++) {
			params.labelOrder[i] = dlModel.get(i);
		}
		params.labelColours = currentColours;
		String cn = (String) classifierBox.getSelectedItem();
		String[] tokens = cn.split(" ");
		params.classifierName = tokens[0];
		if (classifierBox.getSelectedIndex() < 2) {
			params.nEstimators = Integer.valueOf(nEstimatorsField.getText());
			params.criterion = (String) criterionBox.getSelectedItem();
			params.hasMaxDepth = rfMaxDepthCheck.isSelected();
			if (params.hasMaxDepth) {
				params.maxDepth = Integer.valueOf(rfMaxDepthField.getText());
			}
			params.maxFeaturesMode = (String) maxFeaturesBox.getSelectedItem();
			if (params.maxFeaturesMode.equals("Custom")) {
				params.maxFeatures = Integer.valueOf(maxFeaturesField.getText());
			}
			params.bootstrap = bootstrapCheck.isSelected();
			params.classWeightMode = (String) classWeightsBox.getSelectedItem();
			if (params.classWeightMode.equals("Custom")) {
				tokens = classWeightsField.getText().split(",");
				params.classWeights = new double[tokens.length];
				for (int i = 0; i < tokens.length; i++) {
					params.classWeights[i] = Double.valueOf(tokens[i]);
				}
			}
		} else {
			params.learningRate = Double.valueOf(learningRateField.getText());
			params.maxIterations = Integer.valueOf(maxIterationsField.getText());
			params.hasMaxDepth = hgbMaxDepthCheck.isSelected();
			if (params.hasMaxDepth) {
				params.maxDepth = Integer.valueOf(hgbMaxDepthField.getText());
			}
		}
		params.minClusterSize = Integer.valueOf(minClusterField.getText());
		if (params.minClusterSize < 1) {
			params.minClusterSize = 1;
		}
		params.veryLow = Double.valueOf("0."+veryLowField.getText());
		if (Double.valueOf("0."+lowField.getText()) < params.veryLow) {
			params.low = params.veryLow;
		} else {
			params.low = Double.valueOf("0."+lowField.getText());
		}
		if (Double.valueOf("0."+averageField.getText()) < params.low) {
			params.average = params.low;
		} else {
			params.average = Double.valueOf("0."+averageField.getText());
		}
		if (Double.valueOf("0."+highField.getText()) < params.average) {
			params.high = params.average;
		} else {
			params.high = Double.valueOf("0."+highField.getText());
		}
		params.worstLead = (String) worstLeadBox.getSelectedItem();
		params.displayIgnored = displayIgnoredCheck.isSelected();
		return params;
	}
	
	@Override
	public boolean getParams() {
		if (!checkIfSettingsAreValid()) {
			return false;
		}
		
		lcControl.setTrainingSetStatus(false);
		
		FEDataBlock vectorDataBlock = (FEDataBlock) inputSourcePanel.getSource();
		if (loadedTrainingSet.featureList.size() == vectorDataBlock.getFeatureList().length) {
			for (int i = 0; i < loadedTrainingSet.featureList.size(); i++) {
				if (!loadedTrainingSet.featureList.get(i).equals(vectorDataBlock.getFeatureList()[i][1])) {
					lcControl.SimpleErrorDialog("Selected features in the Feature Extractor do not match the features in the selected training set.", 250);
					return false;
				}
			}
		} else {
			lcControl.SimpleErrorDialog("Selected features in the Feature Extractor do not match the features in the selected training set.", 250);
			return false;
		}
		
		LCParameters params = generateParameters();
		
		//lcControl.featureList = featureList;
		
		int result = JOptionPane.showConfirmDialog(this, 
				lcControl.makeHTML("Training set will be sent to Python to be fit into a machine learning model. "
						+ "This may take a few minutes. Proceed?", 300),
				lcControl.getUnitName(),
				JOptionPane.YES_NO_OPTION);
		if (result != JOptionPane.YES_OPTION) {
			return false;
		}
		
		String message = "Awaiting response from Python script...";
		wdThread = new LCWaitingDialogThread(parentFrame, getControl(), message);
		wdThread.start();
		
		String pyParams = params.outputPythonParamsToText();
        if (loadedTrainingSet.featureList.size() > 0) {
        	pyParams += "\""+loadedTrainingSet.featureList.get(0)+"\"";
			for (int i = 1; i < loadedTrainingSet.featureList.size(); i++) {
				pyParams += ",\""+loadedTrainingSet.featureList.get(i)+"\"";
			}
		}
        pyParams += "]";
        pyParams += "]";
        String initCommand = "tcm = LCPythonScript.TCModel(r\""+loadedTrainingSet.pathName+"\","+pyParams+",[],[])";
        lcControl.setModelFittingStatus(false);
        lcControl.getThreadManager().addCommand(initCommand);
        
        while (!lcControl.isModelFittingFinished()) {
        	try {
				TimeUnit.MILLISECONDS.sleep(50);
			} catch (Exception e) {
				System.out.println("Sleep exception.");
				e.printStackTrace();
			}
        }
        
        if (!lcControl.isTrainingSetLoaded()) {
        	wdThread.halt();
        	lcControl.SimpleErrorDialog("Training model initialization failed. See console for details.", 250);
        	lcControl.getParams().trainPath = "";
        	lcControl.setTrainingSetInfo(new LCTrainingSetInfo(""));
    		return false;
        }
        params.trainPath = trainSetField.getText();
        lcControl.setTrainingSetInfo(loadedTrainingSet);
        
		// TODO Set GUI signal ?????
    	lcControl.setParams(params);
    	lcControl.getProcess().setParentDataBlock(vectorDataBlock, true);
    	lcControl.getTabPanel().getPanel().createMatrices(params.labelOrder);
    	wdThread.halt();
    	return true;
	}

	@Override
	public void restoreDefaultSettings() {
		kBestCheck.setSelected(false);
		kBestField.setEnabled(false);
		kBestField.setText("10");
		fullSetRB.setSelected(true);
		setMaxField.setEnabled(false);
		setMaxField.setText("10000");
		tzBox.setSelectedItem("UTC");
		classifierBox.setSelectedIndex(0);
		cl.show(classifierCardsPanel, "RandomForest");
		nEstimatorsField.setText("100");
		criterionBox.setSelectedIndex(0);
		rfMaxDepthCheck.setSelected(false);
		rfMaxDepthField.setEnabled(false);
		rfMaxDepthField.setText("2");
		maxFeaturesBox.setSelectedIndex(0);
		maxFeaturesField.setEnabled(false);
		maxFeaturesField.setText("2");
		bootstrapCheck.setSelected(false);
		classWeightsBox.setSelectedIndex(0);
		classWeightsField.setEnabled(false);
		classWeightsField.setText("");
		learningRateField.setText("0.1");
		maxIterationsField.setText("100");
		hgbMaxDepthCheck.setSelected(false);
		hgbMaxDepthField.setEnabled(false);
		hgbMaxDepthField.setText("2");
		minClusterField.setText("1");
		veryLowField.setText("2");
		lowField.setText("4");
		averageField.setText("6");
		highField.setText("8");
		worstLeadBox.setSelectedIndex(0);
		displayIgnoredCheck.setSelected(true);
	}
	
	public HashMap<String, Color> getCurrentColours() {
		return currentColours;
	}
	
	public void setCurrentColours(HashMap<String, Color> inp) {
		currentColours = inp;
	}
	
	protected LCControl getControl() {
		return lcControl;
	}

/*	
	private class FragmentationListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			enableControls();
		}
	}
*/
}