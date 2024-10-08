package mirrfLiveClassifier;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.tensorflow.proto.example.FeatureList;

import PamController.PamControlledUnit;
import PamView.PamTable;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.PamDataBlock;
import binaryFileStorage.BinaryStore;
import generalDatabase.DBControl;
import mirrf.MIRRFInfo;
import mirrfFeatureExtractor.FEControl;
import mirrfFeatureExtractor.FEDataBlock;
import mirrfFeatureExtractor.FEParameters;
import wmnt.WMNTControl;

/**
 * Dialog for exporting predictions to WMNT.
 * @author Holly LeBlond
 */
public class LCWMNTExportDialog extends PamDialog {
	
	protected LCControl lcControl;
	protected WMNTControl wmntControl;
	protected JComboBox<String> exportOptionBox;
	protected JComboBox<String> minLeadBox;
	protected JCheckBox overwriteCheck;
	
	public LCWMNTExportDialog(LCControl lcControl, WMNTControl wmntControl, Window parentFrame) {
		super(parentFrame, "MIRRF Live Classifier", false);
		this.lcControl = lcControl;
		this.wmntControl = wmntControl;
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints b = new PamGridBagContraints();
		mainPanel.setBorder(new TitledBorder("Export what?"));
		b.anchor = b.NORTHWEST;
		b.fill = b.HORIZONTAL;
		b.gridwidth = 2;
		//mainPanel.add(new JLabel("Export what?"), b);
		//b.gridy++;
		exportOptionBox = new JComboBox<String>(new String[] {"Overall cluster predictions", "Individual contour predictions"});
		mainPanel.add(exportOptionBox, b);
		b.gridy++;
		b.gridwidth = 1;
		mainPanel.add(new JLabel("Minimum lead:"), b);
		b.gridx++;
		minLeadBox = new JComboBox<String>(new String[] {"Very low", "Low", "Average", "High", "Very high"});
		minLeadBox.setSelectedItem(lcControl.getParams().worstLead);
		mainPanel.add(minLeadBox, b);
		b.gridy++;
		b.gridx = 0;
		b.gridwidth = 2;
		overwriteCheck = new JCheckBox("Overwrite non-blank annotations");
		overwriteCheck.setSelected(false);
		mainPanel.add(overwriteCheck, b);
		this.setDialogComponent(mainPanel);
		
	}

	@Override
	public boolean getParams() {
		int res = JOptionPane.showOptionDialog(
				this,
				lcControl.makeHTML("Prediction data will be written into the WMNT species column. Optionally, you can mark in the comments column that the "
						+ "annotation was produced by the Live Classifier, which is strongly recommended. Proceed?", 300),
				"MIRRF Live Classifier",
				JOptionPane.YES_NO_CANCEL_OPTION, 
				JOptionPane.WARNING_MESSAGE,
				null,
				new String[] {"Yes, mark in comments", "Yes, without comments", "Cancel"},
				"Yes, mark in comments");
		if (res == JOptionPane.CANCEL_OPTION)
			return false;
		if (overwriteCheck.isSelected()) {
			String message;
			if (res == JOptionPane.YES_OPTION)
				message = "Are you sure? Pre-existing annotations in the species and comments columns will be overwritten. This can be undone with the Undo button.";
			else
				message = "Are you sure? Pre-existing annotations in the species column will be overwritten. This can be undone with the Undo button.";
			int res2 = JOptionPane.showConfirmDialog(
					this,
					lcControl.makeHTML(message, 300),
					"MIRRF Live Classifier",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (res2 == JOptionPane.NO_OPTION)
				return false;
		}
		double minLead = 0.0; // Very low
		if (minLeadBox.getSelectedIndex() == 1) // Low
			minLead = lcControl.getParams().veryLow;
		if (minLeadBox.getSelectedIndex() == 2) // Average
			minLead = lcControl.getParams().low;
		if (minLeadBox.getSelectedIndex() == 3) // High
			minLead = lcControl.getParams().average;
		if (minLeadBox.getSelectedIndex() == 4) // Very high
			minLead = lcControl.getParams().high;
		int updateCount = wmntControl.importLCPredictions(lcControl.getProcess().resultsDataBlock, exportOptionBox.getSelectedIndex() == 1,
															res == JOptionPane.YES_OPTION, overwriteCheck.isSelected(), minLead);
		if (updateCount == -1) {
			lcControl.SimpleErrorDialog("No data has been loaded into the WMNT.");
		} else if (updateCount == 0) {
			JOptionPane.showMessageDialog(this, "No entries in the WMNT were updated, as no matches were found.", "MIRRF Live Classifier", JOptionPane.WARNING_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this, String.valueOf(updateCount)+" entries in the WMNT were updated.");
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		// (Closes dialog.)
	}

	@Override
	public void restoreDefaultSettings() {
		// (Button disabled.)
	}
	
}