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
 * The panel where the GUI components are written.
 * @author Taylor LeBlond
 */
public class TSBSettingsDialog extends PamDialog {
	
	TSBControl tsbControl;
	private Window parentFrame;
	protected PamPanel mainPanel;
	
	protected ButtonGroup yesOrNoCallType;
	protected JRadioButton noCallType;
	protected JRadioButton yesCallType;
	
	protected JButton umbrellaButton;
	protected ArrayList<String> classList;
	protected ArrayList<String> umbrellaList;
	protected HashMap<String, String> classMap;
	protected ArrayList<JLabel> jLabelList;
	protected ArrayList<JComboBox<String>> jComboBoxList;
	
	//protected DefaultListModel dlModel;
	//protected JList labelList;
	//protected JButton moveUpButton;
	//protected JButton moveDownButton;
	//protected JButton deleteButton;
	
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
				newBox.addItem(umbrellaList.get(i));
			newBox.setSelectedItem(classMap.get(classList.get(i)));
			jComboBoxList.add(newBox);
			bottomPanel.add(jComboBoxList.get(i), c);
		}
		b.gridy++;
		mainPanel.add(bottomPanel, b);
		
	/*	JPanel bottomPanel = new JPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		bottomPanel.setBorder(new TitledBorder("Class label order"));
		c.anchor = c.CENTER;
		bottomPanel.add(new JLabel("* - Class labels are added by adding new subsets to the main table."), c);
		dlModel = new DefaultListModel();
		for (int i = 0; i < tsbControl.getFullClassList().size(); i++) {
			dlModel.addElement(tsbControl.getFullClassList().get(i));
		}
		labelList = new JList<String>(dlModel);
		labelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		labelList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				moveUpButton.setEnabled(labelList.getSelectedIndex() > 0);
				moveDownButton.setEnabled(labelList.getSelectedIndex() < labelList.getModel().getSize()-1 && labelList.getSelectedIndex() != -1);
				deleteButton.setEnabled(labelList.getSelectedIndex() != -1);
			}
		});
		labelList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane sp = new JScrollPane(labelList);
		c.gridy++;
		bottomPanel.add(sp, c);
		JPanel bottomButtonPanel = new JPanel(new GridLayout(1, 3, 5, 5));
		moveUpButton = new JButton("Move up");
		moveUpButton.addActionListener(new MoveUpListener());
		moveUpButton.setEnabled(false);
		bottomButtonPanel.add(moveUpButton);
		moveDownButton = new JButton("Move down");
		moveDownButton.addActionListener(new MoveDownListener());
		moveDownButton.setEnabled(false);
		bottomButtonPanel.add(moveDownButton);
		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new DeleteListener());
		deleteButton.setEnabled(false);
		bottomButtonPanel.add(deleteButton);
		c.gridy++;
		bottomPanel.add(bottomButtonPanel, c);
		b.gridy++;
		mainPanel.add(bottomPanel, b); */
		
		setDialogComponent(mainPanel);
	}
	
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
	
/*	class MoveUpListener implements ActionListener{
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
	}
	
	class MoveDownListener implements ActionListener{
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
	}
	
	class DeleteListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			int selection = labelList.getSelectedIndex();
			dlModel.remove(selection);
			if (selection == labelList.getModel().getSize()) {
				labelList.setSelectedIndex(selection-1);
			} else {
				labelList.setSelectedIndex(selection);
			}
		}
	} */

	@Override
	public boolean getParams() {
	/*	ArrayList<String> oldIDs = new ArrayList<String>();
		for (int i = 0; i < tsbControl.getTabPanel().getPanel().getSubsetTable().getModel().getRowCount(); i++) {
			oldIDs.add((String) tsbControl.getTabPanel().getPanel().getSubsetTable().getModel().getValueAt(i, 0));
		}
		ArrayList<String> outpClassList = new ArrayList<String>();
		for (int i = 0; i < dlModel.size(); i++) {
			outpClassList.add((String) dlModel.get(i));
		}
		boolean classListChanged = false;
		if (outpClassList.size() == tsbControl.getFullClassList().size()) {
			for (int i = 0; i < outpClassList.size(); i++) {
				if (!outpClassList.get(i).equals(tsbControl.getFullClassList().get(i))) {
					classListChanged = true;
					break;
				}
			}
		} else {
			classListChanged = true;
		} */
		
		// TODO THIS SHOULD BE RE-ADDED WHEN NECESSARY
	/*	if (((tsbControl.includeCallType && noCallType.isSelected()) || (!tsbControl.includeCallType && yesCallType.isSelected())) &&
				(tsbControl.getFullClassList().size() > 0 || tsbControl.getSubsetList().size() > 0)) {
			int res = JOptionPane.showConfirmDialog(tsbControl.getPamView().getGuiFrame(),
					tsbControl.makeHTML("When changing the call type setting, it is strongly recommended that the subset table "
							+ "and class list are cleared. Would you like to do so? (This will erase all progress.)", 300),
					"MIRRF Training Set Builder",
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (res == JOptionPane.YES_OPTION) {
				outpClassList.clear();
				oldIDs.clear();
				tsbControl.getFeatureList().clear();
				tsbControl.getSubsetList().clear();
				classListChanged = true;
				tsbControl.getTabPanel().getPanel().clearButton.setEnabled(false);
				tsbControl.getTabPanel().getPanel().saveButton.setEnabled(false);
			} else if (res == JOptionPane.CANCEL_OPTION) {
				return false;
			}
		} */
		
		tsbControl.includeCallType = yesCallType.isSelected();
		
	/*	if (classListChanged) {
			tsbControl.setFullClassList(outpClassList);
			String[] columnNames = new String[5 + outpClassList.size()];
			columnNames[0] = "ID";
			columnNames[1] = "Location";
			columnNames[2] = "Start";
			columnNames[3] = "End";
			columnNames[4] = "Total";
			for(int i = 0; i < outpClassList.size(); i++) {
				columnNames[i+5] = outpClassList.get(i);
			}
			Class[] typesPre = new Class[columnNames.length];
			for (int i = 0; i < typesPre.length; i++) {
				if (i < 4) {
					typesPre[i] = String.class;
				} else {
					typesPre[i] = Integer.class;
				}
			}
			boolean[] canEditPre = new boolean[columnNames.length];
			for (int i = 0; i < canEditPre.length; i++) {
				canEditPre[i] = false;
			}
			DefaultTableModel subsetTableModel = new DefaultTableModel(columnNames,0) {
				Class[] types = typesPre;
				boolean[] canEdit = canEditPre;
				
				@Override
				public Class getColumnClass(int index) {
					return this.types[index];
				}
				
				@Override
				public boolean isCellEditable(int row, int column) {
					return this.canEdit[column];
				}
			};
			ArrayList<Object[]> rowList = new ArrayList<Object[]>();
			for (int i = 0; i < oldIDs.size(); i++) {
				TSBSubset curr = null;
				for (int j = 0; j < tsbControl.getSubsetList().size(); j++) {
					if (tsbControl.getSubsetList().get(j).id.equals(oldIDs.get(i))) {
						curr = tsbControl.getSubsetList().get(j);
						break;
					}
				}
				if (curr != null) {
					Object[] row = new Object[columnNames.length];
					row[0] = curr.id;
					row[1] = curr.location;
					row[2] = curr.start;
					row[3] = curr.end;
					int currTotal = 0;
					for (int j = 5; j < columnNames.length; j++) {
						int classTotal = 0;
						for (int k = 0; k < curr.selectionArray.length; k++) {
							if (columnNames[j].equals(curr.classList.get(curr.selectionArray[k]))) {
								classTotal = curr.validEntriesList.get(curr.selectionArray[k]).size();
								currTotal += classTotal;
								break;
							}
						}
						row[j] = classTotal;
					}
					row[4] = currTotal;
					rowList.add(row);
				}
			}
			tsbControl.getTabPanel().getPanel().setTableModel(subsetTableModel);
			tsbControl.getTabPanel().getPanel().getSubsetTable().getColumnModel().getColumn(0).setPreferredWidth(30);
			tsbControl.getTabPanel().getPanel().getSubsetTable().getColumnModel().getColumn(1).setPreferredWidth(100);
			tsbControl.getTabPanel().getPanel().getSubsetTable().getColumnModel().getColumn(2).setPreferredWidth(150);
			tsbControl.getTabPanel().getPanel().getSubsetTable().getColumnModel().getColumn(3).setPreferredWidth(150);
			for (int i = 4; i < tsbControl.getTabPanel().getPanel().getSubsetTable().getColumnCount(); i++) {
				tsbControl.getTabPanel().getPanel().getSubsetTable().getColumnModel().getColumn(i).setPreferredWidth(50);
			}
			for (int i = 0; i < rowList.size(); i++) {
				tsbControl.getTabPanel().getPanel().subsetTableModel.addRow(rowList.get(i));
			}
		} */
		
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


