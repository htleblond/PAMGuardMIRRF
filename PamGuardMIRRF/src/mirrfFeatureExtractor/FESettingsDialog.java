package mirrfFeatureExtractor;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.Comparator;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import PamDetection.RawDataUnit;
import PamUtils.PamFileChooser;
import whistlesAndMoans.AbstractWhistleDataUnit;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;

/**
 * The settings dialog for the Feature Extractor.
 * @author Holly LeBlond
 */
//@SuppressWarnings("serial")
public class FESettingsDialog extends PamDialog {
	
	protected FEControl feControl;
	private Window parentFrame;
	
	protected ButtonGroup inputRBG;
	protected JRadioButton inputProcessRB;
	protected JRadioButton inputDataRB;
	protected SourcePanel inputSourcePanel;
	protected JTextField inputDataField;
	protected JButton inputDataButton;
	protected ArrayList<File> inputDataList;
	protected JTextField inputDataFileSizeField;
	protected JCheckBox inputIgnoreBlanksCheck;
	protected JCheckBox inputIgnore2SecondGlitchCheck;
	protected JCheckBox inputIgnoreFalsePositivesCheck;
	protected JCheckBox inputIgnoreUnkCheck;
	
	//protected JCheckBox outputDataCheck;
	protected JComboBox<String> outputDataBox;
	protected JTextField outputDataField;
	protected JButton outputDataButton;
	
	protected SourcePanel audioSourcePanel;
	protected ButtonGroup dynamicOrStatic;
	protected JRadioButton dynamicRB;
	protected JRadioButton staticRB;
	protected JTextField audioLengthField;
	protected JComboBox<String> audioSTFTBox;
	protected JTextField audioHopField;
	protected JComboBox<String> audioWindowBox;
	protected JCheckBox audioNormalizeCheck;
	protected JCheckBox audioHPFCheck;
	protected JTextField audioHPFThresholdField;
	protected JTextField audioHPFMagnitudeField;
	protected JCheckBox audioLPFCheck;
	protected JTextField audioLPFThresholdField;
	protected JTextField audioLPFMagnitudeField;
	protected JCheckBox audioNRCheck;
	protected JTextField audioNRStartField;
	protected JTextField audioNRLengthField;
	protected JTextField audioNRScalarField;
	protected JCheckBox audioSaveCheck;
	
	protected DefaultTableModel featureTableModel;
	protected JTable featureTable;
	protected JButton featureAddButton;
	protected JButton featureDeleteButton;
	protected JButton featureUpButton;
	protected JButton featureDownButton;
	protected JButton featureImportInputButton;
	protected JButton featureImportOutputButton;
	protected JButton featureImportSelectedButton;
	
	protected JCheckBox miscClusterCheck;
	protected JTextField miscJoinField;
	protected JCheckBox miscFileStartCheck;
	protected JTextField miscFileStartField;
	protected JCheckBox miscBelowFreqCheck;
	protected JTextField miscBelowFreqField;
	protected JCheckBox miscAboveFreqCheck;
	protected JTextField miscAboveFreqField;
	protected JCheckBox miscBelowDurCheck;
	protected JTextField miscBelowDurField;
	protected JCheckBox miscAboveDurCheck;
	protected JTextField miscAboveDurField;
	protected JCheckBox miscBelowAmpCheck;
	protected JTextField miscBelowAmpField;
	protected JCheckBox miscAboveAmpCheck;
	protected JTextField miscAboveAmpField;
	protected JCheckBox miscPrintJavaCheck;
	protected JCheckBox miscPrintInputCheck;
	protected JCheckBox miscPrintOutputCheck;
	protected JTextField miscTempField;
	protected JButton miscTempButton;
	
	//@SuppressWarnings("static-access")
	public FESettingsDialog(Window parentFrame, FEControl feControl) {
		super(parentFrame, "MIRRF Feature Extractor", true);
		this.feControl = feControl;
		this.parentFrame = parentFrame;
		
		JTabbedPane tabbedPane = new JTabbedPane();
		
		JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints b = new PamGridBagContraints();
		b.gridy = 0;
		b.gridx = 0;
		b.fill = GridBagConstraints.HORIZONTAL;
		b.anchor = GridBagConstraints.WEST;
		
		JPanel inputFP1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel inputProcessPanel = new JPanel(new GridBagLayout());
		inputFP1.setBorder(new TitledBorder(""));
		inputProcessPanel.setAlignmentX(LEFT_ALIGNMENT);
		GridBagConstraints c = new PamGridBagContraints();
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		inputProcessRB = new JRadioButton();
		inputProcessRB.setText("Input data from Whistle and Moan Detector");
		inputProcessRB.addActionListener(new RadioButtonListener(inputProcessRB));
		inputProcessPanel.add(inputProcessRB, c);
		c.gridy++;
		c.anchor = GridBagConstraints.CENTER;
		inputSourcePanel = new SourcePanel(this, "Contour data source", AbstractWhistleDataUnit.class, false, true);
		inputProcessPanel.add(inputSourcePanel.getPanel(), c);
		inputFP1.add(inputProcessPanel);
		mainPanel.add(inputFP1, b);
		
		b.gridy++;
		JPanel inputFP2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel inputDataPanel = new JPanel(new GridBagLayout());
		inputFP2.setBorder(new TitledBorder(""));
		inputDataPanel.setAlignmentX(LEFT_ALIGNMENT);
		c = new PamGridBagContraints();
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		inputDataRB = new JRadioButton();
		inputDataRB.setText("Input pre-existing data from .wmnt or .mirrfts file");
		inputDataRB.addActionListener(new RadioButtonListener(inputDataRB));
		inputDataPanel.add(inputDataRB, c);
		inputRBG = new ButtonGroup();
		inputRBG.add(inputProcessRB);
		inputRBG.add(inputDataRB);
		inputRBG.setSelected(inputProcessRB.getModel(), true);
		c.gridy++;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.CENTER;
		inputDataField = new JTextField(20);
		inputDataField.setEnabled(false);
		inputDataPanel.add(inputDataField, c);
		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		inputDataButton = new JButton("Select file");
		inputDataButton.addActionListener(new CSVListener(this, false));
		inputDataPanel.add(inputDataButton, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		inputDataPanel.add(new JLabel("Expected audio file size (in minutes):"), c);
		c.gridx += c.gridwidth;
		c.fill = c.NONE;
		inputDataFileSizeField = new JTextField(4);
		inputDataFileSizeField.setDocument(JIntFilter());
		inputDataFileSizeField.setEnabled(inputDataRB.isSelected());
		inputDataPanel.add(inputDataFileSizeField, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 4;
		c.fill = c.HORIZONTAL;
		inputDataPanel.add(new JLabel("* - Slightly overestimating this number is recommended."), c);
		c.gridy++;
		c.gridx = 0;
		inputIgnoreBlanksCheck = new JCheckBox();
		inputIgnoreBlanksCheck.setText("Ignore entries with no species label");
		inputDataPanel.add(inputIgnoreBlanksCheck, c);
		c.gridy++;
		inputIgnore2SecondGlitchCheck = new JCheckBox();
		inputIgnore2SecondGlitchCheck.setText("Ignore entries with '2-second glitch' label");
		inputDataPanel.add(inputIgnore2SecondGlitchCheck, c);
		c.gridy++;
		inputIgnoreFalsePositivesCheck = new JCheckBox();
		inputIgnoreFalsePositivesCheck.setText("Ignore entries with 'False Positive' label");
		inputDataPanel.add(inputIgnoreFalsePositivesCheck, c);
		c.gridy++;
		inputIgnoreUnkCheck = new JCheckBox();
		inputIgnoreUnkCheck.setText("Ignore entries with 'Unk' or 'Unknown' labels");
		inputDataPanel.add(inputIgnoreUnkCheck, c);
		inputFP2.add(inputDataPanel);
		mainPanel.add(inputFP2, b);
		
		p.add(mainPanel);
		tabbedPane.add("Input", p);
		
		JPanel p2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel mainPanel2 = new JPanel(new GridBagLayout());
		b = new PamGridBagContraints();
		b.gridy = 0;
		b.gridx = 0;
		b.fill = GridBagConstraints.HORIZONTAL;
		b.anchor = GridBagConstraints.WEST;
		
		JPanel outputFP1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel outputDataPanel = new JPanel(new GridBagLayout());
		outputFP1.setBorder(new TitledBorder(""));
		outputDataPanel.setAlignmentX(LEFT_ALIGNMENT);
		c = new PamGridBagContraints();
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		//outputDataCheck = new JCheckBox();
		//outputDataCheck.setText("Output feature data to .mirrffe file");
		//outputDataCheck.addActionListener(new CheckBoxListener(outputDataCheck));
		//outputDataPanel.add(outputDataCheck, c);
		outputDataBox = new JComboBox<String>(new String[] {"No output", "Output .mirrffe file", "Output .mirrfts file (must input .mirrfts file)"});
		outputDataBox.addActionListener(new outputDataBoxListener());
		outputDataPanel.add(outputDataBox, c);
		c.gridy++;
		c.gridwidth = 3;
		outputDataField = new JTextField(20);
		outputDataField.setEnabled(false);
		outputDataPanel.add(outputDataField, c);
		c.gridx = 3;
		c.gridwidth = 1;
		outputDataButton = new JButton("Select file");
		outputDataButton.addActionListener(new CSVListener(this, true));
		outputDataPanel.add(outputDataButton, c);
		outputFP1.add(outputDataPanel);
		mainPanel2.add(outputFP1, b);
		
		p2.add(mainPanel2);
		tabbedPane.add("Output", p2);
		
		JPanel p3 = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel mainPanel3 = new JPanel(new GridBagLayout());
		b = new PamGridBagContraints();
		b.gridy = 0;
		b.gridx = 0;
		b.fill = GridBagConstraints.BOTH;
		b.anchor = GridBagConstraints.WEST;
		
		b.gridwidth = 2;
		audioSourcePanel = new SourcePanel(this, "Audio Data Source", RawDataUnit.class, false, true);
		mainPanel3.add(audioSourcePanel.getPanel(), b);
		
		b.gridy++;
		b.gridwidth = 1;
		JPanel audioFP2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel audioLengthPanel = new JPanel(new GridBagLayout());
		audioFP2.setBorder(new TitledBorder("Audio clip length"));
		audioLengthPanel.setAlignmentX(LEFT_ALIGNMENT);
		c = new PamGridBagContraints();
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		dynamicRB = new JRadioButton();
		dynamicRB.setText("Automatically span length of each contour");
		dynamicRB.addActionListener(new RadioButtonListener(dynamicRB));
		audioLengthPanel.add(dynamicRB, c);
		c.gridy++;
		staticRB = new JRadioButton();
		staticRB.setText("Manually set constant length");
		staticRB.addActionListener(new RadioButtonListener(staticRB));
		audioLengthPanel.add(staticRB, c);
		dynamicOrStatic = new ButtonGroup();
		dynamicOrStatic.add(dynamicRB);
		dynamicOrStatic.add(staticRB);
		dynamicOrStatic.setSelected(dynamicRB.getModel(), true);
		c.gridy++;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.CENTER;
		audioLengthField = new JTextField(5);
		audioLengthField.setText("16384");
		audioLengthField.setDocument(JIntFilter());
		audioLengthPanel.add(audioLengthField, c);
		c.gridx++;
		audioLengthPanel.add(new JLabel("ms"), c);
		audioFP2.add(audioLengthPanel);
		mainPanel3.add(audioFP2, b);
		
		b.gridx++;
		JPanel audioFP3 = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel audioSTFTPanel = new JPanel(new GridBagLayout());
		audioFP3.setBorder(new TitledBorder("STFT"));
		c = new PamGridBagContraints();
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.EAST;
		audioSTFTPanel.add(new JLabel("STFT length ", SwingConstants.RIGHT), c);
		c.gridx++;
		c.anchor = GridBagConstraints.CENTER;
		String[] STFTlengths = {"128","256","512","1024","2048","4096","8192","16384","32768","65536"};
		audioSTFTBox = new JComboBox<String>(STFTlengths);
		audioSTFTBox.setSelectedIndex(4);
		// TODO ADD LISTENER TO AUTOMATICALLY CHANGE THE HOP SIZE WHEN A NEW STFT LENGTH IS SELECTED
		audioSTFTPanel.add(audioSTFTBox, c);
		c.gridx++;
		c.anchor = GridBagConstraints.WEST;
		audioSTFTPanel.add(new JLabel(" bins"), c);
		c.gridy++;
		c.gridx = 0;
		c.anchor = GridBagConstraints.EAST;
		audioSTFTPanel.add(new JLabel("Hop size ", SwingConstants.RIGHT), c);
		c.gridx++;
		c.anchor = GridBagConstraints.CENTER;
		audioHopField = new JTextField(5);
		audioHopField.setText("1024");
		audioHopField.setDocument(JIntFilter());
		audioSTFTPanel.add(audioHopField, c);
		c.gridx++;
		c.anchor = GridBagConstraints.WEST;
		audioSTFTPanel.add(new JLabel(" samples"), c);
		c.gridy++;
		c.gridx = 0;
		c.anchor = GridBagConstraints.EAST;
		audioSTFTPanel.add(new JLabel("Window function ", SwingConstants.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		String[] WinFuncs = {"Bartlett","Bartlett-Hann","Blackman","Blackman-Harris","Bohman","Cosine","Flat top","Hamming","Hann","Nuttall",
				"Parzen","Rectangular","Triangular"};
		audioWindowBox = new JComboBox<String>(WinFuncs);
		audioWindowBox.setSelectedIndex(8);
		audioSTFTPanel.add(audioWindowBox, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.EAST;
		audioSTFTPanel.add(new JLabel("Normalize ", SwingConstants.RIGHT), c);
		c.gridx++;
		c.anchor = GridBagConstraints.WEST;
		audioNormalizeCheck = new JCheckBox();
		audioSTFTPanel.add(audioNormalizeCheck, c);
		audioFP3.add(audioSTFTPanel);
		mainPanel3.add(audioFP3, b);
		
		b.gridy++;
		b.gridx = 0;
		JPanel audioFP4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel audioFilterPanel = new JPanel(new GridBagLayout());
		audioFP4.setBorder(new TitledBorder("Filters (via SciPy)"));
		c = new PamGridBagContraints();
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		audioHPFCheck = new JCheckBox();
		audioHPFCheck.setText("High-pass Butterworth filter");
		audioHPFCheck.addActionListener(new CheckBoxListener(audioHPFCheck));
		audioFilterPanel.add(audioHPFCheck, c);
		c.gridy++;
		c.gridx = 1;
		c.gridwidth = 1;
		audioFilterPanel.add(new JLabel("Threshold ", SwingConstants.RIGHT), c);
		c.gridx++;
		audioHPFThresholdField = new JTextField(5);
		audioHPFThresholdField.setDocument(JIntFilter());
		audioFilterPanel.add(audioHPFThresholdField, c);
		c.gridx++;
		audioFilterPanel.add(new JLabel(" Hz", SwingConstants.LEFT), c);
		c.gridy++;
		c.gridx = 1;
		audioFilterPanel.add(new JLabel("Magnitude ", SwingConstants.RIGHT), c);
		c.gridx++;
		audioHPFMagnitudeField = new JTextField(5);
		audioHPFMagnitudeField.setDocument(JIntFilter());
		audioFilterPanel.add(audioHPFMagnitudeField, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 4;
		audioLPFCheck = new JCheckBox();
		audioLPFCheck.setText("Low-pass Butterworth filter");
		audioLPFCheck.addActionListener(new CheckBoxListener(audioLPFCheck));
		audioFilterPanel.add(audioLPFCheck, c);
		c.gridy++;
		c.gridx = 1;
		c.gridwidth = 1;
		audioFilterPanel.add(new JLabel("Threshold ", SwingConstants.RIGHT), c);
		c.gridx++;
		audioLPFThresholdField = new JTextField(5);
		audioLPFThresholdField.setDocument(JIntFilter());
		audioFilterPanel.add(audioLPFThresholdField, c);
		c.gridx++;
		audioFilterPanel.add(new JLabel(" Hz", SwingConstants.LEFT), c);
		c.gridy++;
		c.gridx = 1;
		audioFilterPanel.add(new JLabel("Magnitude ", SwingConstants.RIGHT), c);
		c.gridx++;
		audioLPFMagnitudeField = new JTextField(5);
		audioLPFMagnitudeField.setDocument(JIntFilter());
		audioFilterPanel.add(audioLPFMagnitudeField, c);
		audioFP4.add(audioFilterPanel);
		mainPanel3.add(audioFP4, b);
		
		b.gridx++;
		JPanel audioFP5 = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel audioNRPanel = new JPanel(new GridBagLayout());
		audioFP5.setBorder(new TitledBorder("Noise reduction"));
		c = new PamGridBagContraints();
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		audioNRCheck = new JCheckBox();
		audioNRCheck.setText("Pre-detection noise profile reduction");
		audioNRCheck.addActionListener(new CheckBoxListener(audioNRCheck));
		audioNRPanel.add(audioNRCheck, c);
		c.gridy++;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.EAST;
		audioNRPanel.add(new JLabel("Start time before first detection ", SwingConstants.RIGHT), c);
		c.gridx++;
		c.anchor = GridBagConstraints.CENTER;
		audioNRStartField = new JTextField(5);
		audioNRStartField.setDocument(JIntFilter());
		audioNRPanel.add(audioNRStartField, c);
		c.gridx++;
		c.anchor = GridBagConstraints.WEST;
		audioNRPanel.add(new JLabel(" ms", SwingConstants.LEFT), c);
		c.gridy++;
		c.gridx = 0;
		c.anchor = GridBagConstraints.EAST;
		audioNRPanel.add(new JLabel("Noise profile clip length ", SwingConstants.RIGHT), c);
		c.gridx++;
		c.anchor = GridBagConstraints.CENTER;
		audioNRLengthField = new JTextField(5);
		audioNRLengthField.setDocument(JIntFilter());
		audioNRPanel.add(audioNRLengthField, c);
		c.gridx++;
		c.anchor = GridBagConstraints.WEST;
		audioNRPanel.add(new JLabel(" ms", SwingConstants.LEFT), c);
		c.gridy++;
		c.gridx = 0;
		c.anchor = GridBagConstraints.EAST;
		audioNRPanel.add(new JLabel("Scalar ", SwingConstants.RIGHT), c);
		c.gridx++;
		c.anchor = GridBagConstraints.CENTER;
		audioNRScalarField = new JTextField(5);
		audioNRScalarField.setDocument(JDoubleFilter(audioNRScalarField));
		audioNRPanel.add(audioNRScalarField, c);
		audioFP5.add(audioNRPanel);
		mainPanel3.add(audioFP5, b);
		
		b.gridy++;
		b.gridx = 0;
		b.gridwidth = 2;
		JPanel audioFP6 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel audioSavePanel = new JPanel(new GridBagLayout());
		audioFP6.setBorder(new TitledBorder("Save audio clips"));
		audioSavePanel.setAlignmentX(LEFT_ALIGNMENT);
		c = new PamGridBagContraints();
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		audioSaveCheck = new JCheckBox();
		audioSaveCheck.setText("Save audio clips to folder (TBA)");
		audioSaveCheck.setEnabled(false);
		audioSavePanel.add(audioSaveCheck, c);
		audioFP6.add(audioSavePanel);
		mainPanel3.add(audioFP6, b);
		
		p3.add(mainPanel3);
		tabbedPane.add("Audio", p3);
		
		JPanel p4 = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel mainPanel4 = new JPanel(new GridBagLayout());
		b = new PamGridBagContraints();
		b.gridy = 0;
		b.gridx = 0;
		b.fill = GridBagConstraints.HORIZONTAL;
		b.anchor = GridBagConstraints.WEST;
		
		JPanel featureFP1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel featureTablePanel = new JPanel(new GridBagLayout());
		featureFP1.setBorder(new TitledBorder(""));
		featureTablePanel.setAlignmentX(LEFT_ALIGNMENT);
		c = new PamGridBagContraints();
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		String[] columnNames = {"Feature", "Shorthand"};
		featureTableModel = new DefaultTableModel(columnNames,0) {
			Class[] types = {String.class, String.class};
			boolean[] canEdit = {false, false};
			
			@Override
			public Class getColumnClass(int index) {
				return this.types[index];
			}
			
			@Override
			public boolean isCellEditable(int row, int column) {
				return this.canEdit[column];
			}
		};
		featureTable = new JTable(featureTableModel);
		// Kudos to this: https://stackoverflow.com/questions/20903136/disable-column-header-sorting-on-a-jtable
		TableRowSorter<TableModel> featureTableSorter = new TableRowSorter<TableModel>(featureTable.getModel()) {
		    @Override
		    public boolean isSortable(int column) {
		        return false;
		    };
		};
		featureTableSorter.setSortable(0, false);
		featureTableSorter.setSortable(1, false);
		featureTable.setRowSorter(featureTableSorter);
		featureTable.getTableHeader().setReorderingAllowed(false);
		featureTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		featureTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		featureTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		featureTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		featureTable.setSize(200, 300);
		JScrollPane sp = new JScrollPane(featureTable);
		sp.setPreferredSize(new Dimension(200,300));
		featureTablePanel.add(sp, c);
		c.gridy++;
		c.gridwidth = 4;
		JPanel featureButtonPanel = new JPanel(new GridLayout(1, 4, 5, 5));
		featureAddButton = new JButton("Add");
		featureAddButton.addActionListener(new AddButtonListener(this));
		featureButtonPanel.add(featureAddButton);
		featureDeleteButton = new JButton("Delete");
		featureDeleteButton.addActionListener(new DeleteButtonListener());
		featureButtonPanel.add(featureDeleteButton);
		featureUpButton = new JButton("Move Up");
		featureUpButton.addActionListener(new MoveUpButtonListener());
		featureButtonPanel.add(featureUpButton);
		featureDownButton = new JButton("Move Down");
		featureDownButton.addActionListener(new MoveDownButtonListener());
		featureButtonPanel.add(featureDownButton);
		featureTablePanel.add(featureButtonPanel, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 4;
		featureImportInputButton = new JButton("Import features from selected input file (if .mirrfts)");
		featureImportInputButton.addActionListener(new ImportCSVFeaturesButtonListener(ImportCSVFeaturesButtonListener.FROM_INPUT));
		featureTablePanel.add(featureImportInputButton, c);
		c.gridy++;
		featureImportOutputButton = new JButton("Import features from selected output file");
		featureImportOutputButton.addActionListener(new ImportCSVFeaturesButtonListener(ImportCSVFeaturesButtonListener.FROM_OUTPUT));
		featureTablePanel.add(featureImportOutputButton, c);
		c.gridy++;
		featureImportSelectedButton = new JButton("Import features from other .mirrffe file or .mirrfts file");
		featureImportSelectedButton.addActionListener(new ImportCSVFeaturesButtonListener(ImportCSVFeaturesButtonListener.FROM_SELECTED));
		featureTablePanel.add(featureImportSelectedButton, c);
		featureFP1.add(featureTablePanel);
		mainPanel4.add(featureFP1, b);
		
		p4.add(mainPanel4);
		tabbedPane.add("Features", p4);
		
		JPanel p5 = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel mainPanel5 = new JPanel(new GridBagLayout());
		b = new PamGridBagContraints();
		b.gridy = 0;
		b.gridx = 0;
		b.fill = GridBagConstraints.HORIZONTAL;
		b.anchor = GridBagConstraints.WEST;
		
		JPanel miscFP1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel miscClusterPanel = new JPanel(new GridBagLayout());
		miscFP1.setBorder(new TitledBorder("Clustering"));
		miscClusterPanel.setAlignmentX(LEFT_ALIGNMENT);
		c = new PamGridBagContraints();
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		miscClusterCheck = new JCheckBox();
		miscClusterCheck.addActionListener(new CheckBoxListener(miscClusterCheck));
		miscClusterPanel.add(miscClusterCheck, c);
		c.gridx++;
		miscClusterPanel.add(new JLabel("Sort contours into clusters (recommended)", SwingConstants.LEFT), c);
		c.gridy++;
		c.gridwidth = 1;
		miscClusterPanel.add(new JLabel("Maximum join distance:", SwingConstants.RIGHT), c);
		c.gridx++;
		miscJoinField = new JTextField(5);
		miscJoinField.setDocument(JIntFilter());
		miscClusterPanel.add(miscJoinField, c);
		c.gridx++;
		miscClusterPanel.add(new JLabel("ms", SwingConstants.LEFT), c);
		miscFP1.add(miscClusterPanel);
		mainPanel5.add(miscFP1, b);
		
		b.gridy++;
		JPanel miscFP2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel miscIgnorePanel = new JPanel(new GridBagLayout());
		miscFP2.setBorder(new TitledBorder("Ignores"));
		miscIgnorePanel.setAlignmentX(LEFT_ALIGNMENT);
		c = new PamGridBagContraints();
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		miscIgnorePanel.add(new JLabel("Ignore all contours that...", SwingConstants.LEFT), c);
		c.gridy++;
		c.gridwidth = 1;
		miscFileStartCheck = new JCheckBox();
		miscFileStartCheck.addActionListener(new CheckBoxListener(miscFileStartCheck));
		miscIgnorePanel.add(miscFileStartCheck, c);
		c.gridx++;
		miscIgnorePanel.add(new JLabel("...occur within this time after the start of a file: ", SwingConstants.LEFT), c);
		c.gridx++;
		miscFileStartField = new JTextField(5);
		miscFileStartField.setDocument(JIntFilter());
		miscIgnorePanel.add(miscFileStartField, c);
		c.gridx++;
		miscIgnorePanel.add(new JLabel("ms", SwingConstants.LEFT), c);
		c.gridy++;
		c.gridx = 0;
		miscBelowFreqCheck = new JCheckBox();
		miscBelowFreqCheck.addActionListener(new CheckBoxListener(miscBelowFreqCheck));
		miscIgnorePanel.add(miscBelowFreqCheck, c);
		c.gridx++;
		miscIgnorePanel.add(new JLabel("...are of a frequency lower than: ", SwingConstants.LEFT), c);
		c.gridx++;
		miscBelowFreqField = new JTextField(5);
		miscBelowFreqField.setDocument(JIntFilter());
		miscIgnorePanel.add(miscBelowFreqField, c);
		c.gridx++;
		miscIgnorePanel.add(new JLabel("Hz", SwingConstants.LEFT), c);
		c.gridy++;
		c.gridx = 0;
		miscAboveFreqCheck = new JCheckBox();
		miscAboveFreqCheck.addActionListener(new CheckBoxListener(miscAboveFreqCheck));
		miscIgnorePanel.add(miscAboveFreqCheck, c);
		c.gridx++;
		miscIgnorePanel.add(new JLabel("...are of a frequency higher than: ", SwingConstants.LEFT), c);
		c.gridx++;
		miscAboveFreqField = new JTextField(5);
		miscAboveFreqField.setDocument(JIntFilter());
		miscIgnorePanel.add(miscAboveFreqField, c);
		c.gridx++;
		miscIgnorePanel.add(new JLabel("Hz", SwingConstants.LEFT), c);
		c.gridy++;
		c.gridx = 0;
		miscBelowDurCheck = new JCheckBox();
		miscBelowDurCheck.addActionListener(new CheckBoxListener(miscBelowDurCheck));
		miscIgnorePanel.add(miscBelowDurCheck, c);
		c.gridx++;
		miscIgnorePanel.add(new JLabel("...are shorter than: ", SwingConstants.LEFT), c);
		c.gridx++;
		miscBelowDurField = new JTextField(5);
		miscBelowDurField.setDocument(JIntFilter());
		miscIgnorePanel.add(miscBelowDurField, c);
		c.gridx++;
		miscIgnorePanel.add(new JLabel("ms", SwingConstants.LEFT), c);
		c.gridy++;
		c.gridx = 0;
		miscAboveDurCheck = new JCheckBox();
		miscAboveDurCheck.addActionListener(new CheckBoxListener(miscAboveDurCheck));
		miscIgnorePanel.add(miscAboveDurCheck, c);
		c.gridx++;
		miscIgnorePanel.add(new JLabel("...are longer than: ", SwingConstants.LEFT), c);
		c.gridx++;
		miscAboveDurField = new JTextField(5);
		miscAboveDurField.setDocument(JIntFilter());
		miscIgnorePanel.add(miscAboveDurField, c);
		c.gridx++;
		miscIgnorePanel.add(new JLabel("ms", SwingConstants.LEFT), c);
		c.gridy++;
		c.gridx = 0;
		miscBelowAmpCheck = new JCheckBox();
		miscBelowAmpCheck.addActionListener(new CheckBoxListener(miscBelowAmpCheck));
		miscIgnorePanel.add(miscBelowAmpCheck, c);
		c.gridx++;
		miscIgnorePanel.add(new JLabel("...are quieter than: ", SwingConstants.LEFT), c);
		c.gridx++;
		miscBelowAmpField = new JTextField(5);
		miscBelowAmpField.setDocument(JIntFilter());
		miscIgnorePanel.add(miscBelowAmpField, c);
		c.gridx++;
		miscIgnorePanel.add(new JLabel("dB re SPSL", SwingConstants.LEFT), c);
		c.gridy++;
		c.gridx = 0;
		miscAboveAmpCheck = new JCheckBox();
		miscAboveAmpCheck.addActionListener(new CheckBoxListener(miscAboveAmpCheck));
		miscIgnorePanel.add(miscAboveAmpCheck, c);
		c.gridx++;
		miscIgnorePanel.add(new JLabel("...are louder than: ", SwingConstants.LEFT), c);
		c.gridx++;
		miscAboveAmpField = new JTextField(5);
		miscAboveAmpField.setDocument(JIntFilter());
		miscIgnorePanel.add(miscAboveAmpField, c);
		c.gridx++;
		miscIgnorePanel.add(new JLabel("dB re SPSL", SwingConstants.LEFT), c);
		miscFP2.add(miscIgnorePanel);
		mainPanel5.add(miscFP2, b);
		
		b.gridy++;
		JPanel miscFP3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel miscPrintPanel = new JPanel(new GridBagLayout());
		miscFP3.setBorder(new TitledBorder("Troubleshooting"));
		miscPrintPanel.setAlignmentX(LEFT_ALIGNMENT);
		c = new PamGridBagContraints();
		c.anchor = c.WEST;
		miscPrintJavaCheck = new JCheckBox("Enable troubleshooting print statements from Java");
		miscPrintPanel.add(miscPrintJavaCheck, c);
		c.gridy++;
		miscPrintInputCheck = new JCheckBox("Enable print statements for input Python commands");
		miscPrintPanel.add(miscPrintInputCheck, c);
		c.gridy++;
		miscPrintOutputCheck = new JCheckBox("Enable print statements from Python output");
		miscPrintPanel.add(miscPrintOutputCheck, c);
		miscFP3.add(miscPrintPanel);
		mainPanel5.add(miscFP3, b);
		
		b.gridy++;
		JPanel miscFP4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel miscTempPanel = new JPanel(new GridBagLayout());
		miscFP4.setBorder(new TitledBorder("Temporary file storage"));
		miscTempPanel.setAlignmentX(LEFT_ALIGNMENT);
		c = new PamGridBagContraints();
		miscTempField = new JTextField(30);
		miscTempField.setEnabled(false);
		miscTempPanel.add(miscTempField, c);
		c.gridx++;
		miscTempButton = new JButton("Change (TBA)");
		miscTempButton.setEnabled(false);
		// add listener
		miscTempPanel.add(miscTempButton, c);
		miscFP4.add(miscTempPanel);
		mainPanel5.add(miscFP4, b);
		
		p5.add(mainPanel5);
		tabbedPane.add("Miscellaneous", p5);
		
		setDialogComponent(tabbedPane);
		
		actuallyGetParams();
		
	/*	if (feControl.isViewer()) {
			this.getCancelButton().setVisible(false);
			this.getDefaultButton().setVisible(false);
			
			this.inputProcessRB.setEnabled(false);
			this.inputSourcePanel.setEnabled(false);
			this.inputDataRB.setEnabled(false);
			this.inputDataButton.setEnabled(false);
			this.inputIgnoreBlanksCheck.setEnabled(false);
			this.inputIgnore2SecondGlitchCheck.setEnabled(false);
			this.inputIgnoreFalsePositivesCheck.setEnabled(false);
			this.inputIgnoreUnkCheck.setEnabled(false);
			
			this.outputDataCheck.setEnabled(false);
			this.outputDataButton.setEnabled(false);
			
			this.audioSourcePanel.setEnabled(false);
			this.dynamicRB.setEnabled(false);
			this.staticRB.setEnabled(false);
			this.audioLengthField.setEnabled(false);
			this.audioSTFTBox.setEnabled(false);
			this.audioHopField.setEnabled(false);
			this.audioWindowBox.setEnabled(false);
			this.audioNormalizeCheck.setEnabled(false);
			this.audioHPFCheck.setEnabled(false);
			this.audioHPFThresholdField.setEnabled(false);
			this.audioHPFMagnitudeField.setEnabled(false);
			this.audioLPFCheck.setEnabled(false);
			this.audioLPFThresholdField.setEnabled(false);
			this.audioLPFMagnitudeField.setEnabled(false);
			this.audioNRCheck.setEnabled(false);
			this.audioNRStartField.setEnabled(false);
			this.audioNRLengthField.setEnabled(false);
			this.audioNRScalarField.setEnabled(false);
			this.audioSaveCheck.setEnabled(false);
			
			this.featureAddButton.setEnabled(false);
			this.featureDeleteButton.setEnabled(false);
			this.featureUpButton.setEnabled(false);
			this.featureDownButton.setEnabled(false);
			this.featureImportLoadedButton.setEnabled(false);
			this.featureImportNewButton.setEnabled(false);
			
			this.miscClusterCheck.setEnabled(false);
			this.miscJoinField.setEnabled(false);
			this.miscFileStartCheck.setEnabled(false);
			this.miscFileStartField.setEnabled(false);
			this.miscBelowFreqCheck.setEnabled(false);
			this.miscBelowFreqField.setEnabled(false);
			this.miscAboveFreqCheck.setEnabled(false);
			this.miscAboveFreqField.setEnabled(false);
			this.miscBelowDurCheck.setEnabled(false);
			this.miscBelowDurField.setEnabled(false);
			this.miscAboveDurCheck.setEnabled(false);
			this.miscAboveDurField.setEnabled(false);
			this.miscBelowAmpCheck.setEnabled(false);
			this.miscBelowAmpField.setEnabled(false);
			this.miscAboveAmpCheck.setEnabled(false);
			this.miscAboveAmpField.setEnabled(false);
			//this.miscPrintJavaCheck.setEnabled(false);
			//this.miscPrintInputCheck.setEnabled(false);
			//this.miscPrintOutputCheck.setEnabled(false);
			this.miscTempField.setEnabled(false);
			this.miscTempButton.setEnabled(false);
		} */
	}
	
	/**
	 * Enables or disables certain features depending on the input component.
	 */
	protected void switchOn(Object box, boolean boo) {
		if (box.equals(inputProcessRB)) {
			inputSourcePanel.setEnabled(boo);
			inputDataButton.setEnabled(!boo);
			inputDataFileSizeField.setEnabled(!boo);
			inputIgnoreBlanksCheck.setEnabled(!boo);
			inputIgnore2SecondGlitchCheck.setEnabled(!boo);
			inputIgnoreFalsePositivesCheck.setEnabled(!boo);
			inputIgnoreUnkCheck.setEnabled(!boo);
		} else if (box.equals(inputDataRB)) {
			inputSourcePanel.setEnabled(!boo);
			inputDataButton.setEnabled(boo);
			inputDataFileSizeField.setEnabled(boo);
			inputIgnoreBlanksCheck.setEnabled(boo);
			inputIgnore2SecondGlitchCheck.setEnabled(boo);
			inputIgnoreFalsePositivesCheck.setEnabled(boo);
			inputIgnoreUnkCheck.setEnabled(boo);
		} else if (box.equals(outputDataBox)) {
			outputDataButton.setEnabled(boo);
		} else if (box.equals(dynamicRB)) {
			audioLengthField.setEnabled(!boo);
		} else if (box.equals(staticRB)) {
			audioLengthField.setEnabled(boo);
		} else if (box.equals(audioHPFCheck)) {
			audioHPFThresholdField.setEnabled(boo);
			audioHPFMagnitudeField.setEnabled(boo);
		} else if (box.equals(audioLPFCheck)) {
			audioLPFThresholdField.setEnabled(boo);
			audioLPFMagnitudeField.setEnabled(boo);
		} else if (box.equals(audioNRCheck)) {
			audioNRStartField.setEnabled(boo);
			audioNRLengthField.setEnabled(boo);
			audioNRScalarField.setEnabled(boo);
		} else if (box.equals(miscClusterCheck)) {
			miscJoinField.setEnabled(boo);
		} else if (box.equals(miscFileStartCheck)) {
			miscFileStartField.setEnabled(boo);
		} else if (box.equals(miscBelowFreqCheck)) {
			miscBelowFreqField.setEnabled(boo);
		} else if (box.equals(miscAboveFreqCheck)) {
			miscAboveFreqField.setEnabled(boo);
		} else if (box.equals(miscBelowDurCheck)) {
			miscBelowDurField.setEnabled(boo);
		} else if (box.equals(miscAboveDurCheck)) {
			miscAboveDurField.setEnabled(boo);
		} else if (box.equals(miscBelowAmpCheck)) {
			miscBelowAmpField.setEnabled(boo);
		} else if (box.equals(miscAboveAmpCheck)) {
			miscAboveAmpField.setEnabled(boo);
		}
	}
	
	/**
	 * For selecting the input .wmnt or .mirrfts file and checking if it's valid or not.
	 */
	class CSVListener implements ActionListener{
		
		protected FESettingsDialog dialog;
		protected boolean forOutput;
		
		public CSVListener(FESettingsDialog dialog, boolean forOutput) {
			this.dialog = dialog;
			this.forOutput = forOutput;
		}
		
		public void actionPerformed(ActionEvent e) {
			PamFileChooser fc = new PamFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setMultiSelectionEnabled(true);
			if (forOutput) {
				if (outputDataBox.getSelectedIndex() == 1)
					fc.addChoosableFileFilter(new FileNameExtensionFilter("MIRRF feature vector data file (*.mirrffe)","mirrffe"));
				else if (outputDataBox.getSelectedIndex() == 2)
					fc.addChoosableFileFilter(new FileNameExtensionFilter("MIRRF training set file (*.mirrfts)","mirrfts"));
			} else {
				fc.addChoosableFileFilter(new FileNameExtensionFilter("WMNT table export file (*.wmnt)","wmnt"));
				fc.addChoosableFileFilter(new FileNameExtensionFilter("MIRRF training set file (*.mirrfts)","mirrfts"));
				//fc.addChoosableFileFilter(new FileNameExtensionFilter("Comma-separated values file (*.csv)","csv"));
			}
			int returnVal = fc.showOpenDialog(parentFrame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				if (forOutput) {
					File f = getSelectedFileWithExtension(fc);
					f.setWritable(true, false);
					outputDataField.setText(f.getPath());
				} else {
					//File f = getSelectedFileWithExtension(fc);
					File[] fs = fc.getSelectedFiles();
					ArrayList<File> placeholderList = new ArrayList<File>();
					for (int i = 0; i < fs.length; i++) {
						File f = fs[i];
						if (!f.exists()) {
							feControl.SimpleErrorDialog("Selected file does not exist.");
							return;
						}
						Scanner sc;
						try {
							sc = new Scanner(f);
							if (!sc.hasNextLine()) {
								sc.close();
								feControl.SimpleErrorDialog("Selected file is blank.");
								return;
							}
							String firstLine = sc.nextLine();
							sc.close();
							if (f.getPath().endsWith(".wmnt")) {
								if (firstLine.startsWith("uid,datetime,lf,hf,duration,amplitude,species,calltype,comment,slicedata")) {
									//inputDataField.setText(f.getPath());
									placeholderList.add(f);
								} else {
									//feControl.SimpleErrorDialog("Header in selected .wmnt file is not formatted correctly.");
									System.out.println("Error: Header in "+f.getName()+" is not formatted correctly.");
									continue;
								}
							} else { // ends with .mirrfts
								ArrayList<String> features = feControl.findFeaturesInFile(f);
								if (features != null && features.size() > 0) {
									//inputDataField.setText(f.getPath());
									placeholderList.add(f);
								} else {
									System.out.println("Error: Header in "+f.getName()+" is not formatted correctly.");
									continue;
								}
							}
						} catch (Exception e2) {
							e2.printStackTrace();
							//feControl.SimpleErrorDialog("Could not parse selected file.");
							//return;
							System.out.println("Error: "+f.getName()+" could not be parsed.");
						}
					}
					if (placeholderList.size() == 0) {
						feControl.SimpleErrorDialog("No valid .wmnt or .mirrfts files were selected.");
						return;
					} else if (placeholderList.size() < fs.length) {
						String message = String.valueOf(fs.length - placeholderList.size())+" selected file(s) were invalid (see console).\n\n"
								+ "Proceed with the rest?";
						int res = JOptionPane.showConfirmDialog(dialog,
								feControl.makeHTML(message, 250),
								feControl.getUnitName(),
								JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE);
						if (res != JOptionPane.YES_OPTION) return;
					}
					inputDataList = placeholderList;
					if (inputDataList.get(0).getAbsolutePath().endsWith(".mirrfts"))
						inputDataField.setText(String.valueOf(inputDataList.size())+" .mirrfts file(s) selected.");
					else inputDataField.setText(String.valueOf(inputDataList.size())+" .wmnt file(s) selected.");
				}
			}
		}
	}
	
	/**
	 * Returns the selected file from a JFileChooser, including the extension from
	 * the file filter.
	 * <br><br>
	 * Copied from here: https://stackoverflow.com/questions/16846078/jfilechoosershowsavedialog-cant-get-the-value-of-the-extension-file-chosen
	 * <br>Author page: https://stackoverflow.com/users/964243/boann
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
	
/*	// Kudos to this: https://stackoverflow.com/questions/1842223/java-linebreaks-in-jlabels
	public String makeHTML(String inp, int width) {
		String outp = "<html><p style=\"width:"+Integer.toString(width)+"px\">"+inp+"</p></html>";
		return outp;
	} */
	
	/**
	 * ActionListener for checkboxes.
	 */
	protected class CheckBoxListener implements ActionListener {
		private JCheckBox box;
		public CheckBoxListener(JCheckBox box) {
			this.box = box;
		}
		public void actionPerformed(ActionEvent e) {
			switchOn(box, box.isSelected());
		}
	}
	
	/**
	 * ActionListener for outputDataBox.
	 */
	protected class outputDataBoxListener implements ActionListener {
		
		public outputDataBoxListener() {}
		
		public void actionPerformed(ActionEvent e) {
			switchOn(outputDataBox, outputDataBox.getSelectedIndex() > 0);
		}
	}
	
	/**
	 * ActionListener for radio buttons.
	 */
	protected class RadioButtonListener implements ActionListener {
		private JRadioButton rb;
		public RadioButtonListener(JRadioButton rb) {
			this.rb = rb;
		}
		public void actionPerformed(ActionEvent e) {
			switchOn(rb, rb.isSelected());
		}
	}
	
	/**
	 * ActionListener for the "add" button in the "Features" tab.
	 */
	protected class AddButtonListener implements ActionListener {
		protected FESettingsDialog settingsDialog;
		
		protected AddButtonListener(FESettingsDialog settingsDialog) {
			super();
			this.settingsDialog = settingsDialog;
		}
		
		public void actionPerformed(ActionEvent e) {
			FEFeatureDialog featureDialog = new FEFeatureDialog(feControl.getPamView().getGuiFrame(), feControl,
					settingsDialog, featureTable);
			featureDialog.setVisible(true);
		}
	}
	
	/**
	 * ActionListener for the "delete" button in the "Features" tab.
	 */
	protected class DeleteButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if (featureTable.getSelectedRow() > -1) {
				int currIndex = featureTable.getSelectedRow();
				featureTableModel.removeRow(currIndex);
				if (featureTableModel.getRowCount() > 0) {
					if (currIndex == featureTableModel.getRowCount()) {
						featureTable.setRowSelectionInterval(currIndex-1,currIndex-1);
					} else {
						featureTable.setRowSelectionInterval(currIndex,currIndex);
					}
				}
			}
		}
	}
	
	/**
	 * ActionListener for the "move up" button in the "Features" tab.
	 */
	protected class MoveUpButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if (featureTable.getSelectedRow() > 0) {
				featureTableModel.moveRow(featureTable.getSelectedRow(),featureTable.getSelectedRow(),featureTable.getSelectedRow()-1);
				featureTable.setRowSelectionInterval(featureTable.getSelectedRow()-1, featureTable.getSelectedRow()-1);
			}
		}
	}
	
	/**
	 * ActionListener for the "move down" button in the "Features" tab.
	 */
	protected class MoveDownButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if (featureTable.getSelectedRow() > -1 && featureTable.getSelectedRow() < featureTable.getRowCount()-1) {
				featureTableModel.moveRow(featureTable.getSelectedRow(),featureTable.getSelectedRow(),featureTable.getSelectedRow()+1);
				featureTable.setRowSelectionInterval(featureTable.getSelectedRow()+1, featureTable.getSelectedRow()+1);
			}
		}
	}
	
	/**
	 * ActionListener for the "import" buttons in the "Features" tab.
	 */
	public class ImportCSVFeaturesButtonListener implements ActionListener{
		public static final int FROM_INPUT = 0;
		public static final int FROM_OUTPUT = 1;
		public static final int FROM_SELECTED = 2;
		
		protected int importMode;
		
		public ImportCSVFeaturesButtonListener(int importMode) {
			this.importMode = importMode;
		}
		
		public void actionPerformed(ActionEvent e) {
			File f;
			if (importMode == FROM_INPUT) {
				if (!inputDataField.getText().endsWith(".mirrfts")) {
					feControl.SimpleErrorDialog("Input file must be .mirrfts.");
					return;
				}
				f = new File(inputDataField.getText());
			} else if (importMode == FROM_OUTPUT) {
				if (outputDataField.getText().length() == 0) {
					feControl.SimpleErrorDialog("No output file has been selected.");
					return;
				}
				f = new File(outputDataField.getText());
			} else { // importMode == FROM_SELECTED
				PamFileChooser fc = new PamFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setMultiSelectionEnabled(false);
				fc.addChoosableFileFilter(new FileNameExtensionFilter("MIRRF feature vector data file (*.mirrffe)","mirrffe"));
				fc.addChoosableFileFilter(new FileNameExtensionFilter("MIRRF training set file (*.mirrfts)","mirrfts"));
				//fc.addChoosableFileFilter(new FileNameExtensionFilter("Comma-separated values file (*.csv)","csv"));
				int returnVal = fc.showOpenDialog(parentFrame);
				if (returnVal == JFileChooser.CANCEL_OPTION) {
					return;
				}
				f = fc.getSelectedFile();
			}
			ArrayList<String> features = feControl.findFeaturesInFile(f);
			if (features == null || features.size() == 0) {
				feControl.SimpleErrorDialog("No features were found in the selected file.");
				return;
			}
			addFeaturesToTable(features);
		}
	}
	
	/**
	 * Adds valid features to the table; produces warning if any invalid features are present.
	 */
	protected boolean addFeaturesToTable(ArrayList<String> inp) {
		ArrayList<String> validFeatures = new ArrayList<String>();
		ArrayList<String> invalidFeatures = new ArrayList<String>();
		ArrayList<String> calcs = new ArrayList<String>(Arrays.asList("mean","med","std","min","max","rng"));
		for (int i = 0; i < inp.size(); i++) {
			String[] tokens = inp.get(i).split("_");
			if (tokens.length == 1) {
				ArrayList<String> oners = new ArrayList<String>(Arrays.asList("amplitude","duration","frange","fslopehd","freqsdelbow","freqsdslope"));
				if (oners.contains(tokens[0])) {
					validFeatures.add(inp.get(i));
					continue;
				}
			} else if (tokens.length == 2) {
				ArrayList<String> twofers = new ArrayList<String>(Arrays.asList("freqhd","rms","centroid","flux","zcr","freqsd","freqsdd1","freqsdd2"));
				if (twofers.contains(tokens[0])) {
					if ((tokens[0].equals("freqhd") && (tokens[1].equals("min") || tokens[1].equals("max"))) ||
						(!tokens[0].equals("freqhd") && calcs.contains(tokens[1]))) {
						validFeatures.add(inp.get(i));
						continue;
					}
				}
			} else if (tokens.length == 3) {
				ArrayList<String> threefers = new ArrayList<String>(Arrays.asList("flatness","rolloff"));
				if (threefers.contains(tokens[0])) {
					try {
						if (Integer.valueOf(tokens[1]) >= 1 && calcs.contains(tokens[2])) {
							validFeatures.add(inp.get(i));
							continue;
						}
					} catch (Exception e2) {
						// do nothing
					}
				}
			} else if (tokens.length == 4) {
				ArrayList<String> fourfers = new ArrayList<String>(Arrays.asList("formantcount","mfcc","poly","bandwidth","specmag","praat"));
				if (fourfers.contains(tokens[0])) {
					if (tokens[0].equals("formantcount")) {
						try {
							double max_exp_freq = Double.valueOf(tokens[1]);
							double min_freq = Double.valueOf(tokens[2]);
							double max_bandwidth = Double.valueOf(tokens[3]);
							if (max_exp_freq >= 10) {
								validFeatures.add(inp.get(i));
								continue;
							}
						} catch (Exception e2) {
							// do nothing
						}
					} else if (tokens[0].equals("mfcc")) {
						try {
							int order = Integer.valueOf(tokens[1]);
							if (tokens[2].equals("all")) {
								validFeatures.add(inp.get(i));
								continue;
							}
							int coefficient = Integer.valueOf(tokens[2]);
							if (coefficient > 0 && coefficient <= order && calcs.contains(tokens[3])) {
								validFeatures.add(inp.get(i));
								continue;
							}
						} catch (Exception e2) {
							// do nothing
						}
					} else if (tokens[0].equals("poly")) {
						try {
							int order = Integer.valueOf(tokens[1]);
							if (tokens[2].equals("all")) {
								validFeatures.add(inp.get(i));
								continue;
							}
							int coefficient = Integer.valueOf(tokens[2]);
							if (coefficient >= 0 && coefficient <= order && calcs.contains(tokens[3])) {
								validFeatures.add(inp.get(i));
								continue;
							}
						} catch (Exception e2) {
							// do nothing
						}
					} else if (tokens[0].equals("bandwidth")) {
						try {
							int power = Integer.valueOf(tokens[1]);
							if ((power > 0 && (tokens[2].equals("ny") || tokens[2].equals("nn"))) && calcs.contains(tokens[3])) {
								validFeatures.add(inp.get(i));
								continue;
							}
						} catch (Exception e2) {
							// do nothing
						}
					} else if (tokens[0].equals("specmag") || tokens[0].equals("praat")) {
						try {
							int min = Integer.valueOf(tokens[1]);
							int max = Integer.valueOf(tokens[2]);
							if (min < max && calcs.contains(tokens[3])) {
								validFeatures.add(inp.get(i));
								continue;
							}
						} catch (Exception e2) {
							// do nothing
						}
					}
				}
			} else if (tokens.length == 5) {
				ArrayList<String> fivers = new ArrayList<String>(Arrays.asList("formantfreq","formantdiff","contrast","thd","hbr"));
				if (fivers.contains(tokens[0])) {
					if (tokens[0].equals("formantfreq") || tokens[0].equals("formantdiff")) {
						try {
							double max_exp_freq = Double.valueOf(tokens[1]);
							double min_freq = Double.valueOf(tokens[2]);
							double max_bandwidth = Double.valueOf(tokens[3]);
							int formant_num = Integer.valueOf(tokens[4]);
							if (max_exp_freq >= 10 && formant_num > 0) {
								validFeatures.add(inp.get(i));
								continue;
							}
						} catch (Exception e2) {
							// do nothing
						}
					} else if (tokens[0].equals("contrast")) {
						try {
							int cutoff = Integer.valueOf(tokens[1]);
							int bands = Integer.valueOf(tokens[2]);
							if (cutoff > 0 && bands > 1 && (tokens[3].equals("lin") || tokens[3].equals("log")) && calcs.contains(tokens[4])) {
								validFeatures.add(inp.get(i));
								continue;
							}
						} catch (Exception e2) {
							// do nothing
						}
					} else {
						try {
							int harmonics = Integer.valueOf(tokens[1]);
							int min = Integer.valueOf(tokens[2]);
							int max = Integer.valueOf(tokens[3]);
							if (harmonics >= 2 && min < max && calcs.contains(tokens[4])) {
								validFeatures.add(inp.get(i));
								continue;
							}
						} catch (Exception e2) {
							// do nothing
						}
					}
				}
			} else if (tokens.length == 6) {
				ArrayList<String> sixers = new ArrayList<String>(Arrays.asList("hcentroid","hfr"));
				if (sixers.contains(tokens[0])) {
					try {
						int nh = Integer.valueOf(tokens[1]);
						int min = Integer.valueOf(tokens[2]);
						int max = Integer.valueOf(tokens[3]);
						if (tokens[0].equals("hcentroid")) {
							ArrayList<String> fcalcs = new ArrayList<String>(Arrays.asList("mean","med","std","mode"));
							if (nh >= 2 && min < max && fcalcs.contains(tokens[4]) && calcs.contains(tokens[5])) {
								validFeatures.add(inp.get(i));
								continue;
							}
						} else if (tokens[0].equals("hfr")) {
							if (Double.valueOf(tokens[4]) >= 1.0 && calcs.contains(tokens[5])) {
								validFeatures.add(inp.get(i));
								continue;
							}
						}
					} catch (Exception e2) {
						// do nothing
					}
				}
			}
			invalidFeatures.add(inp.get(i));
		}
		if (invalidFeatures.size() > 0) {
			if (validFeatures.size() == 0) {
				feControl.SimpleErrorDialog("Selected file does not contain any valid feature names.");
				return false;
			}
			String message = "The following features found in the selected file are not valid:\n\n";
			for (int i = 0; i < invalidFeatures.size(); i++) {
				message += invalidFeatures.get(i)+"\n";
			}
			message += "\n"+String.valueOf(validFeatures.size())+" valid features were found. Proceed?";
			int res = JOptionPane.showConfirmDialog(parentFrame,
					feControl.makeHTML(message, 325),
					"MIRRF Feature Extractor",
					JOptionPane.YES_NO_OPTION);
			if (res == JOptionPane.NO_OPTION) {
				return false;
			}
		}
		// Not actually used as a dialog, just receiving the full feature names.
		HashMap<String, String> s2n = shorthandsToNames();
		while (featureTableModel.getRowCount() > 0) {
			featureTableModel.removeRow(0);
		}
		for (int i = 0; i < validFeatures.size(); i++) {
			featureTableModel.addRow(new Object[] {s2n.get(validFeatures.get(i).split("_")[0]), validFeatures.get(i)});
		}
		return true;
	}
	
	/**
	 * @return Easy-to-access conversion of feature shorthands to full names.
	 */
	public HashMap<String, String> shorthandsToNames() {
		HashMap<String, String> outp = new HashMap<String, String>();
		outp.put("amplitude","Amplitude");
		outp.put("duration","Duration");
		outp.put("freqhd","Frequency (header data)");
		outp.put("frange","Frequency range (header data)");
		outp.put("fslopehd","Frequency slope (header data)");
		outp.put("freqsd","Frequency (slice data)");
		outp.put("freqsdd1","Frequency, 1st derivative (slice data)");
		outp.put("freqsdd2","Frequency, 2nd derivative (slice data)");
		outp.put("freqsdelbow","Frequency, elbow angle (slice data)");
		outp.put("freqsdslope","Frequency, start-to-end slope (slice data)");
		outp.put("formantfreq","Formant frequency");
		outp.put("formantcount","Valid formant count");
		outp.put("formantdiff","Difference between two formants");
		outp.put("mfcc","Mel-frequency cepstral coefficients");
		outp.put("poly","Polynomial features");
		outp.put("praat","Praat fundamental frequency");
		outp.put("thd","Total harmonic distortion");
		outp.put("hbr","Harmonics-to-background ratio");
		outp.put("hcentroid","Harmonic centroid");
		outp.put("hfr","Harmonic-to-fundamental ratio");
		outp.put("rms","Root mean square");
		outp.put("bandwidth","Spectral bandwidth");
		outp.put("centroid","Spectral centroid");
		outp.put("contrast","Spectral contrast");
		outp.put("flatness","Spectral flatness");
		outp.put("flux","Spectral flux (onset strength)");
		outp.put("specmag","Spectral magnitude");
		outp.put("rolloff","Spectral rolloff");
		//outp.put("yin","YIN fundamental frequency");
		//outp.put("harmmags","Sum of harmonic magnitudes");
		//outp.put("hbr","Harmonics-to-background ratio");
		//outp.put("hcentrmean","Harmonic centroid mean");
		//outp.put("hcentrstd","Harmonic centroid standard deviation");
		outp.put("zcr","Zero-crossing rate");
		return outp;
	}
	
	/**
	 * Limits entry in text field to numbers only.
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
	
	/**
	 * Fills components with values from FEParameters.
	 */
	public boolean actuallyGetParams() {
		FEParameters params = feControl.getParams();
		if (params.inputFromCSV) {
			inputRBG.setSelected(inputDataRB.getModel(), true);
			switchOn(inputDataRB, true);
		} else {
			inputRBG.setSelected(inputProcessRB.getModel(), true);
			switchOn(inputProcessRB, true);
		}
		if (params.inputProcessName.length() > 0) {
			if (!inputSourcePanel.setSource(params.inputProcessName)) {
				// Sets to source set in params in IF statement. Don't worry about this.
				inputSourcePanel.setSourceIndex(0);
			}
		}
		if (params.inputDataFiles.size() > 0) {
			inputDataList = new ArrayList<File>(params.inputDataFiles);
			if (params.inputFilesAreMIRRFTS()) {
				inputDataField.setText(String.valueOf(inputDataList.size())+" .mirrfts file(s) selected.");
			} else {
				inputDataField.setText(String.valueOf(inputDataList.size())+" .wmnt file(s) selected.");
			}
		} else {
			inputDataField.setText("No file(s) selected.");
		}
		inputDataFileSizeField.setText(String.valueOf(params.inputDataExpectedFileSize));
		inputIgnoreBlanksCheck.setSelected(params.inputIgnoreBlanks);
		inputIgnore2SecondGlitchCheck.setSelected(params.inputIgnore2SecondGlitch);
		inputIgnoreFalsePositivesCheck.setSelected(params.inputIgnoreFalsePositives);
		inputIgnoreUnkCheck.setSelected(params.inputIgnoreUnk);
		outputDataBox.setSelectedIndex(params.outputDataOption);
		switchOn(outputDataBox, outputDataBox.getSelectedIndex() > 0);
		if (params.outputDataName.length() > 0) {
			outputDataField.setText(params.outputDataName);
			//featureImportLoadedButton.setEnabled(true);
		} else {
			outputDataField.setText("No file selected.");
			//featureImportLoadedButton.setEnabled(false);
		}
		if (params.audioSourceProcessName.length() > 0) {
			if (!audioSourcePanel.setSource(params.audioSourceProcessName)) {
				// Sets to source set in params in IF statement. Don't worry about this.
				audioSourcePanel.setSourceIndex(0);
			}
		}
		if (params.audioAutoClipLength) {
			dynamicOrStatic.setSelected(dynamicRB.getModel(), true);
			switchOn(dynamicRB, true);
		} else {
			dynamicOrStatic.setSelected(staticRB.getModel(), true);
			switchOn(staticRB, true);
		}
		audioLengthField.setText(Integer.toString(params.audioClipLength));
		audioSTFTBox.setSelectedItem(Integer.toString(params.audioSTFTLength));
		audioHopField.setText(Integer.toString(params.audioHopSize));
		audioWindowBox.setSelectedItem(params.audioWindowFunction);
		audioNormalizeCheck.setSelected(params.audioNormalizeChecked);
		audioHPFCheck.setSelected(params.audioHPFChecked);
		switchOn(audioHPFCheck, params.audioHPFChecked);
		audioHPFThresholdField.setText(Integer.toString(params.audioHPFThreshold));
		audioHPFMagnitudeField.setText(Integer.toString(params.audioHPFMagnitude));
		audioLPFCheck.setSelected(params.audioLPFChecked);
		switchOn(audioLPFCheck, params.audioLPFChecked);
		audioLPFThresholdField.setText(Integer.toString(params.audioLPFThreshold));
		audioLPFMagnitudeField.setText(Integer.toString(params.audioLPFMagnitude));
		audioNRCheck.setSelected(params.audioNRChecked);
		switchOn(audioNRCheck, params.audioNRChecked);
		audioNRStartField.setText(Integer.toString(params.audioNRStart));
		audioNRLengthField.setText(Integer.toString(params.audioNRLength));
		audioNRScalarField.setText(Double.toString(params.audioNRScalar));
		featureTableModel.setRowCount(0);
		for (int i = 0; i < params.featureList.length; i++) {
			featureTableModel.addRow(new Object[]{params.featureList[i][0],params.featureList[i][1]});
		}
		miscClusterCheck.setSelected(params.miscClusterChecked);
		switchOn(miscClusterCheck, params.miscClusterChecked);
		miscJoinField.setText(Integer.toString(params.miscJoinDistance));
		miscFileStartCheck.setSelected(params.miscIgnoreFileStartChecked);
		switchOn(miscFileStartCheck, params.miscIgnoreFileStartChecked);
		miscFileStartField.setText(Integer.toString(params.miscIgnoreFileStartLength));
		miscBelowFreqCheck.setSelected(params.miscIgnoreLowFreqChecked);
		switchOn(miscBelowFreqCheck, params.miscIgnoreLowFreqChecked);
		miscBelowFreqField.setText(Integer.toString(params.miscIgnoreLowFreq));
		miscAboveFreqCheck.setSelected(params.miscIgnoreHighFreqChecked);
		switchOn(miscAboveFreqCheck, params.miscIgnoreHighFreqChecked);
		miscAboveFreqField.setText(Integer.toString(params.miscIgnoreHighFreq));
		miscBelowDurCheck.setSelected(params.miscIgnoreShortDurChecked);
		switchOn(miscBelowDurCheck, params.miscIgnoreShortDurChecked);
		miscBelowDurField.setText(Integer.toString(params.miscIgnoreShortDur));
		miscAboveDurCheck.setSelected(params.miscIgnoreLongDurChecked);
		switchOn(miscAboveDurCheck, params.miscIgnoreLongDurChecked);
		miscAboveDurField.setText(Integer.toString(params.miscIgnoreLongDur));
		miscBelowAmpCheck.setSelected(params.miscIgnoreQuietAmpChecked);
		switchOn(miscBelowAmpCheck, params.miscIgnoreQuietAmpChecked);
		miscBelowAmpField.setText(Integer.toString(params.miscIgnoreQuietAmp));
		miscAboveAmpCheck.setSelected(params.miscIgnoreLoudAmpChecked);
		switchOn(miscAboveAmpCheck, params.miscIgnoreLoudAmpChecked);
		miscAboveAmpField.setText(Integer.toString(params.miscIgnoreLoudAmp));
		miscPrintJavaCheck.setSelected(params.miscPrintJavaChecked);
		miscPrintInputCheck.setSelected(params.miscPrintInputChecked);
		miscPrintOutputCheck.setSelected(params.miscPrintOutputChecked);
		miscTempField.setText(params.tempFolder);
		
		return true;
	}
	
	public ArrayList<String> checkForUnmatchedOutputFileSettings(FEParameters newParams) {
		ArrayList<String> outp = new ArrayList<String>();
		Scanner sc;
		File f = new File(outputDataField.getText());
		if (!f.exists()) {
			outp.add("Error 1");
			return outp;
		}
		HashMap<String, String> newMap = newParams.outputParamsToHashMap();
		HashMap<String, String> fileMap = new HashMap<String, String>();
		boolean ended = false;
		try {
			sc = new Scanner(f);
			if (!sc.hasNextLine()) {
				sc.close();
				outp.add("Error 2");
				return outp;
			}
			String nextLine = sc.nextLine();
			if (!nextLine.equals("EXTRACTOR PARAMS START")) {
				sc.close();
				outp.add("Error 3");
				return outp;
			}
			while (sc.hasNextLine()) {
				nextLine = sc.nextLine();
				if (nextLine.equals("EXTRACTOR PARAMS END")) {
					if (!sc.hasNextLine()) {
						sc.close();
						outp.add("Error 4");
						return outp;
					}
					nextLine = sc.nextLine();
					if ((f.getPath().endsWith(".wmnt") && !nextLine.equals("cluster,uid,date,duration,lf,hf,"+newParams.getFeatureAbbrsAsString())) ||
							(f.getPath().endsWith(".mirrfts") && !nextLine.equals("cluster,uid,location,date,duration,lf,hf,label,"+newParams.getFeatureAbbrsAsString()))) {
						sc.close();
						outp.add("Error 5");
						return outp;
					}
					// dead code?
				/*	else if (!nextLine.startsWith("cluster,uid,date,duration,lf,hf,")) {
						sc.close();
						outp.add("Error 6");
						return outp;
					} */
					ended = true;
					break;
				}
				String[] split = nextLine.split("=");
				if (split.length < 2) continue;
				if (!newMap.containsKey(split[0])) continue;
				fileMap.put(split[0], split[1]);
			}
			sc.close();
			if (!ended) {
				outp.add("Error 7");
				return outp;
			}
		} catch (Exception e) {
			e.printStackTrace();
			outp.add("Error 8");
			return outp;
		}
		
		outp = newParams.findUnmatchedParameters(fileMap, false);
		
		return outp;
	}
	
	protected boolean clearFileAndAddSettingsInfo(FEParameters newParams) {
		File f = new File(outputDataField.getText());
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				feControl.SimpleErrorDialog("Could not create output file.", 250);
				return false;
			}
		}
		if (!f.delete()) {
			feControl.SimpleErrorDialog("Could not clear output file.", 250);
			return false;
		}
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			feControl.SimpleErrorDialog("Could not create output file.", 250);
			return false;
		}
		PrintWriter pw;
		StringBuilder sb;
		try {
			pw = new PrintWriter(f);
			sb = new StringBuilder();
			sb.append("EXTRACTOR PARAMS START\n");
			pw.write(sb.toString());
			pw.flush();
			HashMap<String, String> newMap = newParams.outputParamsToHashMap();
			Iterator<String> it = newMap.keySet().iterator();
			while (it.hasNext()) {
				String nextKey = it.next();
				sb = new StringBuilder();
				sb.append(nextKey+"="+newMap.get(nextKey)+"\n");
				pw.write(sb.toString());
				pw.flush();
			}
			sb = new StringBuilder();
			sb.append("EXTRACTOR PARAMS END\n");
			if (newParams.inputFilesAreMIRRFTS())
				sb.append("cluster,uid,location,date,duration,lf,hf,label,"+newParams.getFeatureAbbrsAsString()+"\n");
			else sb.append("cluster,uid,date,duration,lf,hf,"+newParams.getFeatureAbbrsAsString()+"\n");
			pw.write(sb.toString());
			pw.flush();
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
			feControl.SimpleErrorDialog("Error while writing settings info to file.", 250);
			return false;
		}
		return true;
	}
	
	public float getSamplingRateFromAudioSource() {
		if (audioSourcePanel.getSource() == null) return -1;
		return audioSourcePanel.getSource().getSampleRate();
	}
	
	@Override
	public boolean getParams() {
		//if (feControl.isViewer()) return true;
		if (inputProcessRB.isSelected() && inputSourcePanel.getSourceCount() == 0) {
			SimpleErrorDialog("No Whistle and Moan Detector module found in current configuration.\n"
					+ "You should either set one up or import contour data from a .wmnt file.");
			return false;
		} else if (inputDataRB.isSelected() && inputDataField.getText() == "No file selected.") {
			SimpleErrorDialog("No .wmnt file has been selected as input.");
			return false;
		} else if (inputDataRB.isSelected() && inputDataFileSizeField.getText().length() == 0) {
			SimpleErrorDialog("Expected audio file size must be an integer.");
			return false;
		} else if (outputDataBox.getSelectedIndex() > 0 && outputDataField.getText() == "No file selected.") {
			SimpleErrorDialog("No .wmnt file has been selected for output.");
			return false;
		} else if (outputDataBox.getSelectedIndex() == 2 && 
				!(inputDataRB.isSelected() && inputDataList.size() > 0 && inputDataList.get(0).getAbsolutePath().endsWith(".mirrfts"))) {
			SimpleErrorDialog("Output .mirrfts files must take input data from a pre-existing .mirrfts file.");
			return false;
		} else if (outputDataBox.getSelectedIndex() == 1 &&
				(inputDataRB.isSelected() && inputDataList.size() > 0 && inputDataList.get(0).getAbsolutePath().endsWith(".mirrfts"))) {
			SimpleErrorDialog("Input .mirrfts files can only output to other .mirrfts files and vice-versa.");
			return false;
		} else if (inputDataField.getText().equals(outputDataField.getText())) {
			SimpleErrorDialog("Input and output files cannot be the same.");
			return false;
		} else if (audioSourcePanel.getSourceCount() < 1) {
			SimpleErrorDialog("No audio source found.\n"
					+ "You should add a Sound Acquisition module to the configuration.");
			return false;
		} else if (audioHPFCheck.isSelected() && audioLPFCheck.isSelected() && 
				Integer.valueOf(audioHPFThresholdField.getText()) >= Integer.valueOf(audioLPFThresholdField.getText())) {
			SimpleErrorDialog("High pass filter threshold should be lower than that of the low pass filter,\n"
					+ "if both are enabled.");
			return false;
		} else if (audioNRCheck.isSelected() && Integer.valueOf(audioNRStartField.getText()) < Integer.valueOf(audioNRLengthField.getText())) {
			SimpleErrorDialog("Noise removal clip length must be shorter than or of equal length to\n"
					+ "the start time.");
			return false;
		} else if (featureTableModel.getRowCount() == 0) {
			SimpleErrorDialog("No features have been selected.");
			return false;
		}
		
		FEParameters newParams = feControl.getParams().clone();
		newParams.inputDataEntries.clear();
		newParams.inputDataIndexes.clear();
		//FEParameters newParams = new FEParameters();
		
		newParams.inputFromCSV = inputDataRB.isSelected();
		if (inputProcessRB.isSelected()) {
			if (inputSourcePanel.getSourceIndex() > -1) {
				newParams.inputProcessName = (String) inputSourcePanel.getSourceName();
			} else {
				newParams.inputProcessName = "";
			}
			//feControl.getSidePanel().getFEPanel().getReloadCSVButton().setEnabled(false);
		} else if (inputDataRB.isSelected()) {
			if (inputDataList.size() == 0) {
				feControl.SimpleErrorDialog("No input files have been selected.", 250);
				return false;
			}
			ArrayList<String[]> startsAndEnds = new ArrayList<String[]>();
			for (int k = 0; k < inputDataList.size(); k++) {
				try {
					File f = inputDataList.get(k);
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss+SSS");
					df.setTimeZone(TimeZone.getTimeZone("UTC"));
					if (!f.exists()) {
						feControl.SimpleErrorDialog(f.getName()+" does not exist.");
						return false;
					}
					Scanner sc;
					if (f.getAbsolutePath().endsWith(".wmnt")) {
						try {
							sc = new Scanner(f);
							if (!sc.hasNextLine()) {
								sc.close();
								feControl.SimpleErrorDialog(f.getName()+" is blank.", 250);
								return false;
							}
							String firstLine = sc.nextLine();
							if (!firstLine.startsWith("uid,datetime,lf,hf,duration,amplitude,species,calltype,comment,slicedata")) {
								sc.close();
								feControl.SimpleErrorDialog(f.getName()+" is not correctly formatted.", 250);
								return false;
							}
							String start = "";
							String end = "";
							while (sc.hasNextLine()) {
								String[] nextSplit = sc.nextLine().split(",");
								try {
									if ((inputIgnoreBlanksCheck.isSelected() && nextSplit[6].length() == 0)
											|| (inputIgnore2SecondGlitchCheck.isSelected() && nextSplit[6].equals("2-second glitch"))
											|| (inputIgnoreFalsePositivesCheck.isSelected() && nextSplit[6].equals("False positive"))
											|| (inputIgnoreUnkCheck.isSelected() && 
													(nextSplit[6].equals("Unk") || nextSplit[6].equals("Unknown")))) {
										continue;
									}
									String datetime = nextSplit[1];
									if (start.length() == 0 || start.compareTo(datetime) > 0) start = datetime;
									if (end.length() == 0 || end.compareTo(datetime) < 0) end = datetime;
									newParams.inputDataEntries.add(new FEInputDataObject(nextSplit, false, null, null));
								} catch (Exception e) {
									e.printStackTrace(); // TODO Remove if it becomes a problem.
									continue;
								}
								//newParams.inputDataEntries.add(nextSplit);
							}
							startsAndEnds.add(new String[] {start, end});
							sc.close();
						} catch (Exception e) {
							e.printStackTrace();
							feControl.SimpleErrorDialog("Error occured when attempting to read "+f.getName()+".", 350);
							return false;
						}
					} else { // ends with .mirrfts
						try {
							sc = new Scanner(f);
							if (!sc.hasNextLine()) {
								sc.close();
								feControl.SimpleErrorDialog(f.getName()+" is blank.", 250);
								return false;
							}
							ArrayList<String> foundFeatures = feControl.findFeaturesInFile(f);
							if (foundFeatures == null || foundFeatures.size() == 0) {
								sc.close();
								feControl.SimpleErrorDialog(f.getName()+" is not correctly formatted.", 250);
								return false;
							}
							boolean foundHeader = false;
							String header = "cluster,uid,location,date,duration,lf,hf,label";
							for (int i = 0; i < foundFeatures.size(); i++)
								header += ","+foundFeatures.get(i);
							String start = "";
							String end = "";
							while (sc.hasNextLine()) {
								String next = sc.nextLine();
								if (!foundHeader) {
									if (next.equals(header)) foundHeader = true;
									continue;
								}
								String[] nextSplit = next.split(",");
								try {
									if ((inputIgnoreBlanksCheck.isSelected() && nextSplit[7].length() == 0)
											|| (inputIgnore2SecondGlitchCheck.isSelected() && nextSplit[7].equals("2-second glitch"))
											|| (inputIgnoreFalsePositivesCheck.isSelected() && nextSplit[7].equals("False positive"))
											|| (inputIgnoreUnkCheck.isSelected() && 
													(nextSplit[7].equals("Unk") || nextSplit[7].equals("Unknown")))) {
										continue;
									}
									ArrayList<String> problematicFeatures = feControl.findProblematicFeaturesInFile(f);
									ArrayList<String> cantRetrieve = new ArrayList<String>();
									for (int j = 0; j < featureTable.getRowCount(); j++) {
										String value = (String) featureTable.getValueAt(j, 1);
										String[] tokens = value.split("_");
										if (tokens[0].equals("amplitude") || tokens[0].equals("freqsd") || tokens[0].equals("freqsdd1") ||
												tokens[0].equals("freqsdd2") || tokens[0].equals("freqsdslope") || tokens[0].equals("freqsdelbow"))
											if (!problematicFeatures.contains(value)) cantRetrieve.add(value);
									}
									if (cantRetrieve.size() > 0) {
										String txt = "Since you're taking input from a .mirrfts file, the following header features can't be retrieved, "
												+ "as amplitude and slice data are not normally stored in .mirrfts files and the selected features weren't "
												+ "found in the input file to copy over:\n";
										for (int j = 0; j < cantRetrieve.size(); j++)
											txt += "\n"+cantRetrieve.get(j);
										feControl.SimpleErrorDialog(txt, 350);
										return false;
									}
									String datetime = nextSplit[3];
									if (start.length() == 0 || start.compareTo(datetime) > 0) start = datetime;
									if (end.length() == 0 || end.compareTo(datetime) < 0) end = datetime;
									newParams.inputDataEntries.add(new FEInputDataObject(nextSplit, true, foundFeatures, problematicFeatures));
								} catch (Exception e) {
									e.printStackTrace(); // TODO Remove if it becomes a problem.
									continue;
								}
								//newParams.inputDataEntries.add(nextSplit);
							}
							startsAndEnds.add(new String[] {start, end});
							sc.close();
						} catch (Exception e) {
							e.printStackTrace();
							feControl.SimpleErrorDialog("Error occured when attempting to read "+f.getName()+".", 350);
							return false;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					feControl.SimpleErrorDialog("Error occured when attempting to read input files.", 350);
					return false;
				}
			}
			// Kudos to Lukas Eder on https://stackoverflow.com/questions/4699807/sort-arraylist-of-array-in-java.
			startsAndEnds.sort(Comparator.comparing(a -> a[0]));
			for (int i = 0; i < startsAndEnds.size()-1; i++) {
				if (startsAndEnds.get(i)[1].compareTo(startsAndEnds.get(i+1)[0]) >= 0) {
					String message = "The selected input files overlap each other in terms of date/time. This may result in features being extracted "
							+ "from the wrong audio file and multiple instances of the same data entry appearing in the output file.\n\n"
							+ "Proceed? (It is highly advised that you don't!)";
					int res = JOptionPane.showConfirmDialog(this,
							feControl.makeHTML(message, 350),
							feControl.getUnitName(),
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE);
					if (res != JOptionPane.YES_OPTION) return false;
					break;
				}
			}
			if (newParams.inputDataEntries.size() == 0) {
				feControl.SimpleErrorDialog("Input data files contain no valid entries.", 300);
				return false;
			}
			newParams.inputDataFiles = inputDataList;
			newParams.inputDataEntries.sort(Comparator.comparing(a -> a.datetime));
			ArrayList<Integer> intList = new ArrayList<Integer>();
			for (int i = 0; i < newParams.inputDataEntries.size(); i++) {
				intList.add(i);
			}
			newParams.inputDataIndexes = new ArrayList<Integer>(intList);
			newParams.inputDataExpectedFileSize = Integer.valueOf(inputDataFileSizeField.getText());
			newParams.inputIgnoreBlanks = inputIgnoreBlanksCheck.isSelected();
			newParams.inputIgnore2SecondGlitch = inputIgnore2SecondGlitchCheck.isSelected();
			newParams.inputIgnoreFalsePositives = inputIgnoreFalsePositivesCheck.isSelected();
			newParams.inputIgnoreUnk = inputIgnoreUnkCheck.isSelected();
		}
		newParams.outputDataOption = outputDataBox.getSelectedIndex();
		if (outputDataBox.getSelectedIndex() > 0) {
			if (outputDataField.getText() != "No file selected.") {
				newParams.outputDataName = outputDataField.getText();
			} else {
				newParams.outputDataName = "";
			}
		}
		newParams.audioSourceProcessName = audioSourcePanel.getSourceName();
		PamDataBlock dbForSR = feControl.getPamController().getDataBlockByLongName(audioSourcePanel.getSourceName());
		if (dbForSR != null) newParams.sr = (int) dbForSR.getSampleRate();
		newParams.audioAutoClipLength = dynamicRB.isSelected();
		if (!dynamicRB.isSelected()) {
			newParams.audioClipLength = Integer.valueOf(audioLengthField.getText());
		}
		newParams.audioSTFTLength = Integer.valueOf((String) audioSTFTBox.getSelectedItem());
		newParams.audioHopSize = Integer.valueOf(audioHopField.getText());
		newParams.audioWindowFunction = (String) audioWindowBox.getSelectedItem();
		newParams.audioNormalizeChecked = audioNormalizeCheck.isSelected();
		newParams.audioHPFChecked = audioHPFCheck.isSelected();
		if (audioHPFCheck.isSelected()) {
			newParams.audioHPFThreshold = Integer.valueOf(audioHPFThresholdField.getText());
			newParams.audioHPFMagnitude = Integer.valueOf(audioHPFMagnitudeField.getText());
		}
		newParams.audioLPFChecked = audioLPFCheck.isSelected();
		if (audioLPFCheck.isSelected()) {
			newParams.audioLPFThreshold = Integer.valueOf(audioLPFThresholdField.getText());
			newParams.audioLPFMagnitude = Integer.valueOf(audioLPFMagnitudeField.getText());
		}
		newParams.audioNRChecked = audioNRCheck.isSelected();
		if (audioNRCheck.isSelected()) {
			newParams.audioNRStart = Integer.valueOf(audioNRStartField.getText());
			newParams.audioNRLength = Integer.valueOf(audioNRLengthField.getText());
			newParams.audioNRScalar = Double.valueOf(audioNRScalarField.getText());
		}
		String[][] tableOutp = new String[featureTable.getModel().getRowCount()][2];
		for (int i = 0; i < featureTable.getModel().getRowCount(); i++) {
			tableOutp[i][0] = (String) featureTable.getValueAt(i, 0);
			tableOutp[i][1] = (String) featureTable.getValueAt(i, 1);
		}
		newParams.featureList = tableOutp;
		newParams.miscClusterChecked = miscClusterCheck.isSelected();
		if (miscClusterCheck.isSelected()) {
			newParams.miscJoinDistance = Integer.valueOf(miscJoinField.getText());
		}
		newParams.miscIgnoreFileStartChecked = miscFileStartCheck.isSelected();
		if (miscFileStartCheck.isSelected()) {
			newParams.miscIgnoreFileStartLength = Integer.valueOf(miscFileStartField.getText());
		}
		newParams.miscIgnoreLowFreqChecked = miscBelowFreqCheck.isSelected();
		if (miscBelowFreqCheck.isSelected()) {
			newParams.miscIgnoreLowFreq = Integer.valueOf(miscBelowFreqField.getText());
		}
		newParams.miscIgnoreHighFreqChecked = miscAboveFreqCheck.isSelected();
		if (miscAboveFreqCheck.isSelected()) {
			newParams.miscIgnoreHighFreq = Integer.valueOf(miscAboveFreqField.getText());
		}
		newParams.miscIgnoreShortDurChecked = miscBelowDurCheck.isSelected();
		if (miscBelowDurCheck.isSelected()) {
			newParams.miscIgnoreShortDur = Integer.valueOf(miscBelowDurField.getText());
		}
		newParams.miscIgnoreLongDurChecked = miscAboveDurCheck.isSelected();
		if (miscAboveDurCheck.isSelected()) {
			newParams.miscIgnoreLongDur = Integer.valueOf(miscAboveDurField.getText());
		}
		newParams.miscIgnoreQuietAmpChecked = miscBelowAmpCheck.isSelected();
		if (miscBelowAmpCheck.isSelected()) {
			newParams.miscIgnoreQuietAmp = Integer.valueOf(miscBelowAmpField.getText());
		}
		newParams.miscIgnoreLoudAmpChecked = miscAboveAmpCheck.isSelected();
		if (miscAboveAmpCheck.isSelected()) {
			newParams.miscIgnoreLoudAmp = Integer.valueOf(miscAboveAmpField.getText());
		}
		newParams.miscPrintJavaChecked = miscPrintJavaCheck.isSelected();
		newParams.miscPrintInputChecked = miscPrintInputCheck.isSelected();
		newParams.miscPrintOutputChecked = miscPrintOutputCheck.isSelected();
		
		//if (outputDataCheck.isSelected()) {
		if (outputDataBox.getSelectedIndex() > 0) {
			ArrayList<String> unmatchedSettings = checkForUnmatchedOutputFileSettings(newParams);
			if (unmatchedSettings.size() > 0) {
				if (unmatchedSettings.get(0).equals("Error 1") || unmatchedSettings.get(0).equals("Error 2")) {
					if (!clearFileAndAddSettingsInfo(newParams)) return false;
				} else if (unmatchedSettings.get(0).equals("Error 8")) {
					feControl.SimpleErrorDialog("Error occured while attempting to parse output file.", 250);
					return false;
				} else {
					String message = "";
					if (unmatchedSettings.get(0).equals("Error 6")) {
						message = "The selected file does not contain the same features.\n";
					} else if (unmatchedSettings.get(0).startsWith("Error")) {
						System.out.println("Unmatched settings: "+unmatchedSettings.get(0));
						message = "The selected file is not formatted correctly.\n";
					} else {
						message = "Selected file contained settings that don't match:\n\n";
						for (int i = 0; i < unmatchedSettings.size(); i++)
							message += unmatchedSettings.get(i) + "\n";
					}
					message += "\nThe selected file needs to be cleared in order to proceed.\n\nContinue?";
					int res = JOptionPane.showConfirmDialog(this,
							feControl.makeHTML(message, 300),
							feControl.getUnitName(),
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.WARNING_MESSAGE);
					if (res != JOptionPane.OK_OPTION) return false;
					res = JOptionPane.showConfirmDialog(this,
							feControl.makeHTML("Are you sure? This will delete everything in the file.", 300),
							feControl.getUnitName(),
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.WARNING_MESSAGE);
					if (res != JOptionPane.OK_OPTION) return false;
					if (!clearFileAndAddSettingsInfo(newParams)) return false;
				}
			} else {
				int res = JOptionPane.showOptionDialog(this,
						feControl.makeHTML("The selected file already contains data using the same features and settings "
								+ "as those selected. Would you like to clear the file or keep its contents?", 300),
						feControl.getUnitName(),
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE,
						null, 
						new Object[] {"Keep contents", "Clear file", "Cancel"},
						"Cancel");
				// (YES_OPTION does nothing and accepts it as is.)
				if (res == JOptionPane.NO_OPTION) {
					res = JOptionPane.showConfirmDialog(this,
							feControl.makeHTML("Are you sure? This will delete everything in the file.", 300),
							feControl.getUnitName(),
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.WARNING_MESSAGE);
					if (res != JOptionPane.OK_OPTION) return false;
					if (!clearFileAndAddSettingsInfo(newParams)) return false;
				} else if (res == JOptionPane.CANCEL_OPTION) return false;
			}
		}
		
		feControl.setParams(newParams);
		
		if (feControl.getThreadManager().isActive()) {
			feControl.getThreadManager().resetTxtParams();
		}
		
		return true;
	}
	
	/**
	 * @return The JTable containing feature names.
	 */
	public JTable getTable() {
		return featureTable;
	}

	@Override
	public void restoreDefaultSettings() {
		feControl.feParameters = new FEParameters();
		getParams();
	}
}