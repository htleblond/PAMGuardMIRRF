package mirfeeTrainingSetBuilder;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import mirfeeTrainingSetBuilder.TSBAudioTestBatchDialog.DetectionSetObject;

/**
 * Loading dialog for TSBAudioTestBatchDialog.
 * @author Holly LeBlond
 */
public class TSBAudioTestBatchLoadingBarWindow extends PamDialog {
	
	protected TSBControl tsbControl;
	protected TSBAudioTestBatchDialog parentDialog;
	protected ArrayList<String> outpClassLabels;
	protected GridBagConstraints b;
	
	protected JPanel mainPanel;
	protected JProgressBar loadingBar;
	protected JTextField savedField;
	protected JTextField ignoredField;
	protected JTextField errorField;
	protected JTextField[] countFields;
	
	protected boolean fileCountStarted = false;
	protected int totalCounted;
	
	public static final int SAVED = 0;
	public static final int IGNORED = 1;
	public static final int ERROR = 2;
	
	final int textFieldSize = 6;

	public TSBAudioTestBatchLoadingBarWindow(TSBAudioTestBatchDialog parentDialog, TSBControl tsbControl) {
		super(parentDialog, tsbControl.getUnitName(), false);
		this.tsbControl = tsbControl;
		this.parentDialog = parentDialog;
		//if (parentDialog.useLoaded) this.outpClassLabels = tsbControl.getOutputClassLabels();
		//else this.outpClassLabels = new ArrayList<String>();
		//this.outpClassLabels = outpClassLabels;
		this.totalCounted = 0;
		
		this.getOkButton().setEnabled(false);
		this.getCancelButton().removeActionListener(this.getCancelButton().getActionListeners()[0]);
		this.getCancelButton().addActionListener(new NewCancelButtonPressed());
		
		mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Generating audio test batch"));
		b = new PamGridBagContraints();
		b.anchor = b.WEST;
		b.fill = b.HORIZONTAL;
		b.gridwidth = 2;
		loadingBar = new JProgressBar();
		loadingBar.setPreferredSize(new Dimension(300, 20));
		loadingBar.setValue(0);
		loadingBar.setStringPainted(true);
		loadingBar.setString("Setting up...");
		mainPanel.add(loadingBar, b);
		b.gridy++;
		b.gridwidth = 1;
		b.fill = b.NONE;
		mainPanel.add(new JLabel("Files copied"), b);
		b.gridx += b.gridwidth;
		b.anchor = b.EAST;
		savedField = new JTextField(textFieldSize);
		savedField.setEnabled(false);
		savedField.setText("0");
		mainPanel.add(savedField, b);
		b.gridy++;
		b.gridx = 0;
		b.anchor = b.WEST;
		mainPanel.add(new JLabel("Files ignored"), b);
		b.gridx += b.gridwidth;
		b.anchor = b.EAST;
		ignoredField = new JTextField(textFieldSize);
		ignoredField.setEnabled(false);
		ignoredField.setText("0");
		mainPanel.add(ignoredField, b);
		b.gridy++;
		b.gridx = 0;
		b.anchor = b.WEST;
		mainPanel.add(new JLabel("Errors"), b);
		b.gridx += b.gridwidth;
		b.anchor = b.EAST;
		errorField = new JTextField(textFieldSize);
		errorField.setEnabled(false);
		errorField.setText("0");
		mainPanel.add(errorField, b);
		b.gridy++;
		b.gridx = 0;
		b.anchor = b.WEST;
		mainPanel.add(new JLabel("Label counts:"), b);
	/*	countFields = new JTextField[outpClassLabels.size()];
		for (int i = 0; i < outpClassLabels.size(); i++) {
			b.gridy++;
			b.gridx = 0;
			b.anchor = b.WEST;
			mainPanel.add(new JLabel(outpClassLabels.get(i)), b);
			b.gridx += b.gridwidth;
			b.anchor = b.EAST;
			countFields[i] = new JTextField(textFieldSize);
			countFields[i].setEnabled(false);
			countFields[i].setText("0");
			mainPanel.add(countFields[i], b);
		} */
		
		this.setDialogComponent(mainPanel);
	}
	
	/**
	 * Begins the "searching through folders" bit in the loading bar.
	 * @param numberOfFolders - Total number of folders that are to be searched.
	 */
	public void startFolderCheck(int numberOfFolders) {
		loadingBar.setValue(0);
		loadingBar.setString("Searching through folders (0/"+String.valueOf(numberOfFolders)+")");
	}
	
	/**
	 * Updates the number of folders that have been loaded.
	 * @param folderNum - Current number of folders that have been searched.
	 * @param numberOfFolders - Total number of folders that are to be searched.
	 */
	public void updateFolderLoad(int folderNum, int numberOfFolders) {
		loadingBar.setValue((int) 100*folderNum/numberOfFolders);
		loadingBar.setString("Searching through folders ("+String.valueOf(folderNum)+"/"+String.valueOf(numberOfFolders)+")");
	}
	
	/**
	 * @param n - Number of files run through.
	 * @param numberOfFiles - Total number of files to run through.
	 * @return String - "Copying files ([n]/[numberOfFiles], [100*n/numberOfFiles]%)"
	 */
	public String getFileIterationString(int n, int numberOfFiles) {
		return "Copying files ("+String.valueOf(n)+"/"+String.valueOf(numberOfFiles)+
				", "+String.format("%.1f", (float) 100*((double) n)/numberOfFiles)+"%)";
	}
	
	/**
	 * Starts the file counting process.
	 * @param numberOfFiles - Total number of files to run through.
	 * @param initialIgnoreCount - Number of files that have already been ignored.
	 * @param initialErrorCount - Number of files that have already caused errors.
	 */
	public void startFileCount(int numberOfFiles, int initialIgnoreCount, int initialErrorCount, ArrayList<String> outpClassLabels) {
		this.outpClassLabels = outpClassLabels;
		countFields = new JTextField[outpClassLabels.size()];
		for (int i = 0; i < outpClassLabels.size(); i++) {
			b.gridy++;
			b.gridx = 0;
			b.gridwidth = 1;
			mainPanel.add(new JLabel(outpClassLabels.get(i)), b);
			b.gridx++;
			countFields[i] = new JTextField(textFieldSize);
			countFields[i].setEnabled(false);
			countFields[i].setText("0");
			mainPanel.add(countFields[i], b);
		}
		this.setDialogComponent(mainPanel);
		int initialCounted = initialIgnoreCount + initialErrorCount;
		loadingBar.setValue((int) 100*initialCounted/numberOfFiles);
		loadingBar.setString(getFileIterationString(initialCounted, numberOfFiles));
		ignoredField.setText(String.valueOf(initialIgnoreCount));
		errorField.setText(String.valueOf(initialErrorCount));
		totalCounted = initialCounted;
		this.fileCountStarted = true;
	}
	
	/**
	 * Adds the number of detections in the DetectionSetObject to the specified counter.
	 * @param counterID - Which counter is being added to.
	 * <br>0 == SAVED
	 * <br>1 == IGNORED
	 * <br>2 == ERROR
	 * @param numberOfFiles - Total number of files to run through.
	 * @param dso - The DetectionSetObject in question.
	 * @throws AssertionError - If startFileCount hasn't been run yet.
	 */
	public void addToCounter(int counterID, int numberOfFiles, DetectionSetObject dso) throws AssertionError {
		assert fileCountStarted;
		totalCounted += 1;
		loadingBar.setValue((int) 100*totalCounted/numberOfFiles);
		loadingBar.setString(getFileIterationString(totalCounted, numberOfFiles));
		if (counterID == SAVED) {
			savedField.setText(String.valueOf(Integer.valueOf(savedField.getText()).intValue() + 1));
			for (int i = 0; i < outpClassLabels.size(); i++) {
				countFields[i].setText(String.valueOf(Integer.valueOf(countFields[i].getText()).intValue()
						+ dso.getSpeciesCount(outpClassLabels.get(i))));
			}
		} else if (counterID == IGNORED) {
			ignoredField.setText(String.valueOf(Integer.valueOf(ignoredField.getText()).intValue() + 1));
		} else if (counterID == ERROR) {
			errorField.setText(String.valueOf(Integer.valueOf(errorField.getText()).intValue() + 1));
		}
	}
	
	public boolean hasFileCountStarted() {
		return fileCountStarted;
	}
	
	/**
	 * Enables the OK button when processing has finished.
	 */
	public void setToFinished() {
		this.getOkButton().setEnabled(true);
		this.getCancelButton().setEnabled(false);
	}

	@Override
	public boolean getParams() {
		this.setVisible(false);
		parentDialog.interrupt();
		return true;
	}
	
	/**
	 * Replaces the cancel button's action listener.
	 */
	protected class NewCancelButtonPressed implements ActionListener {
		public void actionPerformed(ActionEvent e) {
		/*	if (cancelObserver != null) {
				boolean ans = cancelObserver.cancelPressed();
				if (ans == false) {
					return;
				}
			} */
			cancelButtonPressed();
			//setVisible(false);
		}
	}
	
	@Override
	public void cancelButtonPressed() {
		if (!this.getCancelButton().isEnabled()) { // For the "X" button if processing has finished.
			okButtonPressed();
			return;
		}
		int result = JOptionPane.showConfirmDialog(this,
				"Stop copying files?",
				tsbControl.getUnitName(),
				JOptionPane.YES_NO_OPTION);
		if (result != JOptionPane.YES_OPTION) return;
		this.setVisible(false);
		parentDialog.interrupt();
	}

	@Override
	public void restoreDefaultSettings() {}
}