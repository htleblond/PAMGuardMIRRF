package wmat;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

/**
 * Loading bar window for SQL commits.
 * @author Holly LeBlond
 */
public class WMATCommitLoadingBarWindow extends PamDialog {
	
	public static final int NOTHING_TO_UPDATE = -1;
	public static final int UPDATE_WITHOUT_COMMIT = 0;
	public static final int COMMIT_SUCCEEDED = 1;
	public static final int COMMIT_FAILED = 2;
	
	protected WMATControl wmatControl;
	
	protected int totalToUpdate;
	protected int totalCounted = 0;
	protected int successes = 0;
	protected int failures = 0;
	protected JLabel loadingMessage;
	protected JProgressBar loadingBar;
	protected JTextField successField;
	protected JTextField failureField;

	public WMATCommitLoadingBarWindow(WMATControl wmatControl, Window parentFrame, int totalToUpdate) {
		super(parentFrame, wmatControl.getUnitName(), false);
		this.wmatControl = wmatControl;
		this.totalToUpdate = totalToUpdate;
		
		this.getOkButton().setEnabled(false);
		this.getCancelButton().setVisible(false);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder(""));
		GridBagConstraints b = new PamGridBagContraints();
		b.anchor = b.NORTH;
		b.fill = b.HORIZONTAL;
		b.gridwidth = 2;
		loadingMessage = new JLabel("Executing SQL updates...");
		mainPanel.add(loadingMessage, b);
		b.gridy++;
		loadingBar = new JProgressBar();
		loadingBar.setValue(0);
		loadingBar.setStringPainted(true);
		loadingBar.setString("0/"+String.valueOf(totalToUpdate)+" (0.0%)");
		mainPanel.add(loadingBar, b);
		b.gridy++;
		b.anchor = b.WEST;
		b.gridwidth = 1;
		mainPanel.add(new JLabel("Successes:"), b);
		b.gridx++;
		b.anchor = b.EAST;
		successField = new JTextField(5);
		successField.setEnabled(false);
		successField.setText("0");
		mainPanel.add(successField, b);
		b.gridy++;
		b.gridx = 0;
		b.anchor = b.WEST;
		mainPanel.add(new JLabel("Failures:"), b);
		b.gridx++;
		b.anchor = b.EAST;
		failureField = new JTextField(5);
		failureField.setEnabled(false);
		failureField.setText("0");
		mainPanel.add(failureField, b);
		
		setDialogComponent(mainPanel);
		
		if (totalToUpdate == 0) {
			finish(NOTHING_TO_UPDATE);
		}
	}
	
	/**
	 * Adds one to the loading bar and one to either the success or failure counter.
	 * @param success - Whether or not the single commit was successful.
	 */
	public void updateLoadingBar(boolean success, boolean waitForCommit) {
		totalCounted++;
		loadingBar.setValue((int) Math.floor(100*(((double) totalCounted)/totalToUpdate)));
		loadingBar.setString(String.valueOf(totalCounted)+"/"+String.valueOf(totalToUpdate)+" ("+
				String.format("%.1f", (float) 100*((double) totalCounted)/totalToUpdate)+"%)");
		if (success) {
			successes++;
			successField.setText(String.valueOf(successes));
		} else {
			failures++;
			failureField.setText(String.valueOf(failures));
		}
		if (totalCounted == totalToUpdate) {
			if (waitForCommit) loadingMessage.setText("Committing changes..."); // finish(COMMIT_SUCCEEDED/FAILED) called in WMATSQLLogging
			else finish(UPDATE_WITHOUT_COMMIT);
		}
	}
	
	/**
	 * For when all commits have finished.
	 */
	public void finish(int status) {
		if (status == NOTHING_TO_UPDATE) loadingMessage.setText("Nothing to update!");
		else if (status == COMMIT_SUCCEEDED) loadingMessage.setText("Commit completed!");
		else if (status == COMMIT_FAILED) {
			loadingMessage.setText("Commit failed!");
			JOptionPane.showMessageDialog(this,
					"Commit failed - try \"File > Save Data\" instead.",
					wmatControl.getUnitName(),
					JOptionPane.ERROR_MESSAGE);
		} else loadingMessage.setText("Updates executed!");
		loadingBar.setValue(100);
		loadingBar.setString(String.valueOf(totalCounted)+"/"+String.valueOf(totalToUpdate)+" (100.0%)");
		this.getOkButton().setEnabled(true);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		if (status == COMMIT_FAILED) this.okButtonPressed();
	}

	@Override
	public boolean getParams() {
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		// Button disabled
	}

	@Override
	public void restoreDefaultSettings() {
		// Button disabled
	}
	
}