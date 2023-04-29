package mirrfFeatureExtractor;

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
import PamView.panel.PamPanel;
import PamView.PamList; //
import PamView.PamTable;
import PamView.dialog.PamButton; //
import PamView.dialog.PamTextField; //
import PamView.dialog.SourcePanel;
import binaryFileStorage.*;
import PamguardMVC.DataUnitBaseData;
import Spectrogram.SpectrogramDisplay;
import PamUtils.SelectFolder;
//import PamController.PamFolders;
import pamScrollSystem.*;
import PamUtils.PamFileChooser;

import java.util.TimeZone;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The panel where the GUI components are written.
 * @author Taylor LeBlond
 */
public class FEPanel {
	
	public static final int SUCCESS = 0;
	public static final int FAILURE = 1;
	public static final int IGNORE = 2;
	public static final int PENDING = 3;

	protected FEControl feControl;
	protected FESettingsDialog settingsDialog;
	
	protected PamPanel mainPanel;
	
	//protected JTextField fileField;
	//protected JButton settingsButton;
	//protected JProgressBar loadingBar;
	protected JTextField successField;
	protected JTextField failureField;
	protected JTextField ignoreField;
	protected JTextField pendingField;
	protected JButton resetButton;
	protected JButton reloadCSVButton; // This is useless now.
	
	public FEPanel(FEControl feControl) {
		this.feControl = feControl;
		
		mainPanel = new PamPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		PamPanel memPanel = new PamPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		memPanel.setBorder(new TitledBorder("MIRRF Feature Extractor"));
		
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 1;
		memPanel.add(new JLabel("Successes: "), c);
		c.gridx++;
		successField = new JTextField(3);
		memPanel.add(successField, c);
		c.gridx++;
		memPanel.add(new JLabel("Failures: "), c);
		c.gridx++;
		failureField = new JTextField(3);
		memPanel.add(failureField, c);
		c.gridx++;
		memPanel.add(new JLabel("Ignores: "), c);
		c.gridx++;
		ignoreField = new JTextField(3);
		memPanel.add(ignoreField, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		memPanel.add(new JLabel("Pending: "), c);
		c.gridx++;
		pendingField = new JTextField(3);
		memPanel.add(pendingField, c);
		c.gridx++;
		c.gridwidth = 2;
		resetButton = new JButton("Reset counters");
		resetButton.addActionListener(new ResetListener());
		memPanel.add(resetButton, c);
		c.gridx += 2;
		reloadCSVButton = new JButton("Reload .csv");
		reloadCSVButton.addActionListener(new ReloadCSVListener());
		reloadCSVButton.setEnabled(false);
		//memPanel.add(reloadCSVButton, c); // This is useless now.
		
		mainPanel.add(memPanel);
		
		successField.setEnabled(false);
		failureField.setEnabled(false);
		ignoreField.setEnabled(false);
		pendingField.setEnabled(false);
		
		pendingField.setText("0");
		resetCounters();
	}
	
	public void resetCounters() {
		successField.setText("0");
		failureField.setText("0");
		ignoreField.setText("0");
	}
	
	public void addOneToCounter(int i, String uid) {
		try {
			if (i == SUCCESS) {
				successField.setText(String.valueOf(Integer.valueOf(successField.getText())+1));
				System.out.println("Successfully processed contour "+uid+".");
			} else if (i == FAILURE) {
				failureField.setText(String.valueOf(Integer.valueOf(failureField.getText())+1));
				System.out.println("Could not process contour "+uid+".");
			} else if (i == IGNORE) {
				ignoreField.setText(String.valueOf(Integer.valueOf(ignoreField.getText())+1));
				System.out.println("Ignored contour "+uid+" due to settings.");
			} else if (i == PENDING) {
				pendingField.setText(String.valueOf(Integer.valueOf(pendingField.getText())+1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean subtractOneFromPendingCounter() {
		try {
			if (Integer.valueOf(pendingField.getText()) > 0) {
				pendingField.setText(String.valueOf(Integer.valueOf(pendingField.getText())-1));
			} else {
				return false;
			}
		} catch (Exception e) {
			System.out.println("ERROR: subtractOneFromPendingCounter failed.");
			return false;
		}
		return true;
	}
	
	public int getSuccessCount() {
		int outp = -1;
		try {
			outp = Integer.valueOf(successField.getText());
		} catch (Exception e) {
			System.out.println("ERROR: getSuccessCount failed.");
		}
		return outp;
	}
	
/*	public JTextField getFileField() {
		return fileField;
	} */
	
	/**
	 * The listener for the 'Select by search' button.
	 */
	class SettingsListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			SourcePanel audioSourcePanel = new SourcePanel(null, "", RawDataUnit.class, false, true);
			SourcePanel contourSourcePanel = new SourcePanel(null, "", AbstractWhistleDataUnit.class, false, true);
			if (audioSourcePanel.getSourceCount() == 0 && contourSourcePanel.getSourceCount() == 0) {
				SimpleErrorDialog("No Sound Acquisition or Whistle and Moan Detector modules found.\n"
						+ "Please add one of both to the current configuration.");
			} else if (audioSourcePanel.getSourceCount() == 0) {
				SimpleErrorDialog("No Sound Acquisition module found.\n"
						+ "Please add one to the current configuration.");
		/*	} else if (contourSourcePanel.getSourceCount() == 0) {
				SimpleErrorDialog("No Whistle and Moan Detector module found.\n"
						+ "Please add one to the current configuration."); */
			} else {
				settingsDialog = new FESettingsDialog(feControl.getPamView().getGuiFrame(), feControl);
				feControl.setSettingsDialog(settingsDialog);
				settingsDialog.setVisible(true);
			}
		}
	}
	
	class ReloadCSVListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			ArrayList<Integer> intList = new ArrayList<Integer>();
			int csvSize = feControl.getParams().inputCSVEntries.size();
			for (int i = 0; i < csvSize; i++) {
				intList.add(i);
			}
			feControl.getParams().inputCSVIndexes = new ArrayList<Integer>(intList);
		}
	}
	
	class ResetListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			resetCounters();
		}
	}
	
	public JButton getReloadCSVButton() {
		return reloadCSVButton;
	}
	
	/**
	 * Streamlined error dialog.
	 */
	public void SimpleErrorDialog() {
		JOptionPane.showMessageDialog(null,
			"An error has occured.\nSee console for details.",
			"",
			JOptionPane.ERROR_MESSAGE);
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
	 * @return mainPanel
	 */
	public JComponent getComponent() {
		return mainPanel;
	}

}