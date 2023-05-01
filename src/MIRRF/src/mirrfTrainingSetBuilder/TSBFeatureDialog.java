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
public class TSBFeatureDialog extends PamDialog {
	
	TSBControl tsbControl;
	private Window parentFrame;
	protected PamPanel mainPanel;
	protected DefaultListModel dlModel;
	protected JList featureSelectionList;
	//protected int[] featureIndices;
	
	public TSBFeatureDialog(Window parentFrame, TSBControl tsbControl) {
		super(parentFrame, "MIRRF Training Set Builder", true);
		
		this.tsbControl = tsbControl;
		this.parentFrame = parentFrame;
		//this.featureIndices = new int[0];
		
		mainPanel = new PamPanel(new GridBagLayout());
		GridBagConstraints b = new PamGridBagContraints();
		
		b.anchor = b.WEST;
		mainPanel.add(new JLabel("Select which features to include in the training set:"), b);
		dlModel = new DefaultListModel();
		for (int i = 0; i < tsbControl.getFeatureList().size(); i++) {
			dlModel.addElement(tsbControl.getFeatureList().get(i));
		}
		featureSelectionList = new JList<String>(dlModel);
		featureSelectionList.setSelectionModel(new DefaultListSelectionModel() {
		    @Override
		    public void setSelectionInterval(int index0, int index1) {
		        if(super.isSelectedIndex(index0)) {
		            super.removeSelectionInterval(index0, index1);
		        }
		        else {
		            super.addSelectionInterval(index0, index1);
		        }
		    }
		});
		featureSelectionList.setLayoutOrientation(JList.VERTICAL);
		int[] selectAll = new int[featureSelectionList.getModel().getSize()];
		for (int i = 0; i < selectAll.length; i++) {
			selectAll[i] = i;
		}
		featureSelectionList.setSelectedIndices(selectAll);
		JScrollPane sp = new JScrollPane(featureSelectionList);
		b.gridy++;
		mainPanel.add(sp, b);
		
		setDialogComponent(mainPanel);
	}

	@Override
	public boolean getParams() {
		if (featureSelectionList.getSelectedIndices().length < 2) {
			tsbControl.SimpleErrorDialog("At least two features must be selected.", 150);
			return false;
		}
		tsbControl.getTabPanel().getPanel().setOutputFeatureIndices(featureSelectionList.getSelectedIndices());
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		tsbControl.getTabPanel().getPanel().setOutputFeatureIndices(new int[0]);
	}

	@Override
	public void restoreDefaultSettings() {
		int[] selectAll = new int[featureSelectionList.getModel().getSize()];
		for (int i = 0; i < selectAll.length; i++) {
			selectAll[i] = i;
		}
		featureSelectionList.setSelectedIndices(selectAll);
	}
}