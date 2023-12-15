package mirrfTrainingSetBuilder;

import java.awt.*;
import java.awt.List;
import java.awt.event.*;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.*;
import javax.swing.table.*; //
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import Layout.PamFramePlots;
import Layout.PamInternalFrame;
import PamDetection.RawDataUnit;

import javax.swing.filechooser.*;
import javax.swing.event.*;
import java.util.*; //
import java.text.*; //
import java.io.PrintWriter;

import javax.swing.border.TitledBorder;

import fftManager.Complex;
import mirrfFeatureExtractor.FEFeatureDialog;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayTabPanel;
import userDisplay.UserDisplayTabPanelControl;
import userDisplay.UserFramePlots;
import whistlesAndMoans.AbstractWhistleDataUnit;
import wmnt.WMNTSearchDialog;
import userDisplay.UserDisplayFrame;
import PamUtils.PamCalendar;
import PamUtils.SelectFolder;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.dialog.PamTextDisplay;
import PamView.panel.PamBorderPanel;
import PamView.panel.PamPanel;
import PamView.PamList; //
import PamView.PamTable;
import PamView.dialog.PamButton; //
import PamView.dialog.PamDialog;
import PamView.dialog.PamTextField; //
import PamView.dialog.SourcePanel;
import binaryFileStorage.*;
import PamguardMVC.DataUnitBaseData;
import Spectrogram.SpectrogramDisplay;
import PamUtils.SelectFolder;
//import PamController.PamFolders;
import pamScrollSystem.*;
import PamUtils.PamFileChooser;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The Training Set Builder's settings dialog.
 * @author Holly LeBlond
 */
public class TSBSettingsDialog extends PamDialog {
	
	TSBControl tsbControl;
	private Window parentFrame;
	protected PamPanel mainPanel;
	
	protected ButtonGroup yesOrNoCallType;
	protected JRadioButton noCallType;
	protected JRadioButton yesCallType;
	
	protected JComboBox<String> overlapOptionsBox;
	protected JComboBox<String> multilabelOptionsBox;
	
	protected JButton umbrellaButton;
	protected ArrayList<String> classList;
	protected ArrayList<String> umbrellaList;
	protected HashMap<String, String> classMap;
	protected ArrayList<JLabel> jLabelList;
	protected ArrayList<JComboBox<String>> jComboBoxList;
	
	public TSBSettingsDialog(Window parentFrame, TSBControl tsbControl) {
		super(parentFrame, "MIRRF Training Set Builder", false);
		
		this.tsbControl = tsbControl;
		this.parentFrame = parentFrame;
		
		classList = tsbControl.getFullClassList();
		umbrellaList = tsbControl.getUmbrellaClassList();
		classMap = tsbControl.getClassMap();
		
		mainPanel = new PamPanel(new GridBagLayout());
		GridBagConstraints b = new PamGridBagContraints();
		
		JPanel topPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		topPanel.setBorder(new TitledBorder("Class label settings"));
		c.anchor = c.WEST;
		noCallType = new JRadioButton();
		noCallType.setText("Only use species as label");
		topPanel.add(noCallType, c);
		c.gridy++;
		yesCallType = new JRadioButton();
		yesCallType.setText("Include call type in label with species (TBA)");
		yesCallType.setEnabled(false);
		topPanel.add(yesCallType, c);
		yesOrNoCallType = new ButtonGroup();
		yesOrNoCallType.add(noCallType);
		yesOrNoCallType.add(yesCallType);
		if (tsbControl.includeCallType) {
			yesOrNoCallType.setSelected(yesCallType.getModel(), true);
		} else {
			yesOrNoCallType.setSelected(noCallType.getModel(), true);
		}
		b.fill = b.HORIZONTAL;
		mainPanel.add(topPanel, b);
		
		JPanel middlePanel = new JPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		middlePanel.setBorder(new TitledBorder("Overlaps"));
		c.anchor = c.WEST;
		c.gridwidth = 1;
		middlePanel.add(new JLabel("Instances when contours with different species overlap:"), c);
		c.gridy++;
		overlapOptionsBox = new JComboBox<String>(new String[] {"Skip both", "Keep both"});
		overlapOptionsBox.setSelectedIndex(tsbControl.overlapOption);
		middlePanel.add(overlapOptionsBox, c);
		c.gridy++;
		middlePanel.add(new JLabel("Instances when multiple species occur in the same cluster:"), c);
		c.gridy++;
		multilabelOptionsBox = new JComboBox(new String[] {"Only keep most-occuring species", "Keep everything", "Skip entire cluster"});
		multilabelOptionsBox.setSelectedIndex(tsbControl.multilabelOption);
		middlePanel.add(multilabelOptionsBox, c);
		b.gridy++;
		mainPanel.add(middlePanel, b);
		
		
		JPanel bottomPanel = new JPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		bottomPanel.setBorder(new TitledBorder("Umbrella classes"));
		c.anchor = c.CENTER;
		c.gridwidth = 2;
		bottomPanel.add(new JLabel("* - Class labels are added by adding new subsets to the main table."), c);
		c.gridy++;
		umbrellaButton = new JButton("Add new umbrella class");
		umbrellaButton.addActionListener(new UmbrellaListener());
		bottomPanel.add(umbrellaButton, c);
		c.gridwidth = 1;
		jLabelList = new ArrayList<JLabel>();
		jComboBoxList = new ArrayList<JComboBox<String>>();
		for (int i = 0; i < classList.size(); i++) {
			c.gridy++;
			c.gridx = 0;
			jLabelList.add(new JLabel(classList.get(i)));
			bottomPanel.add(jLabelList.get(i), c);
			c.gridx++;
			JComboBox<String> newBox = new JComboBox<String>();
			newBox.addItem(classList.get(i));
			for (int j = 0; j < umbrellaList.size(); j++)
				newBox.addItem(umbrellaList.get(j));
			newBox.setSelectedItem(classMap.get(classList.get(i)));
			jComboBoxList.add(newBox);
			bottomPanel.add(jComboBoxList.get(i), c);
		}
		b.gridy++;
		mainPanel.add(bottomPanel, b);
		
		setDialogComponent(mainPanel);
	}
	
	/**
	 * Brings up dialog for adding "umbrella classes" to the list.
	 */
	protected class UmbrellaListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String newClass = JOptionPane.showInputDialog("Enter new umbrella class:");
			if (newClass == null) return;
			if (umbrellaList.contains(newClass)) {
				tsbControl.SimpleErrorDialog("Input umbrella class already exists", 250);
				return;
			}
			umbrellaList.add(newClass);
			for (int i = 0; i < jComboBoxList.size(); i++)
				jComboBoxList.get(i).addItem(newClass);
		}
	}

	@Override
	public boolean getParams() {
		tsbControl.includeCallType = yesCallType.isSelected();
		tsbControl.overlapOption = overlapOptionsBox.getSelectedIndex();
		tsbControl.multilabelOption = multilabelOptionsBox.getSelectedIndex();
		tsbControl.setUmbrellaClassList(umbrellaList);
		HashMap<String, String> outpMap = new HashMap<String, String>();
		for (int i = 0; i < jLabelList.size(); i++)
			outpMap.put(jLabelList.get(i).getText(), (String) jComboBoxList.get(i).getSelectedItem());
		tsbControl.setClassMap(outpMap);
		return true;
	}

	@Override
	public void cancelButtonPressed() {}

	@Override
	public void restoreDefaultSettings() {}
}


