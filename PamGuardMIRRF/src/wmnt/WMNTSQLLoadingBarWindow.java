package wmnt;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class WMNTSQLLoadingBarWindow extends PamDialog {
	
	private int totalToUpdate;
	private int totalCounted = 0;
	private int successes = 0;
	private int failures = 0;
	private JLabel loadingMessage;
	private JProgressBar loadingBar;
	private JTextField successField;
	private JTextField failureField;

	public WMNTSQLLoadingBarWindow(Window parentFrame, int totalToUpdate) {
		super(parentFrame, "Whistle and Moan Navigation Tool", false);
		this.totalToUpdate = totalToUpdate;
		
		this.getOkButton().setEnabled(false);
		this.getCancelButton().setVisible(false);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints b = new PamGridBagContraints();
		b.anchor = b.NORTH;
		b.fill = b.HORIZONTAL;
		b.gridwidth = 2;
		loadingMessage = new JLabel("Committing to database...");
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
			finish();
		}
	}
	
	public void updateLoadingBar(boolean success) {
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
			finish();
		}
	}
	
	private void finish() {
		loadingMessage.setText("Commit completed!");
		loadingBar.setValue(100);
		loadingBar.setString(String.valueOf(totalCounted)+"/"+String.valueOf(totalToUpdate)+" (100.0%)");
		this.getOkButton().setEnabled(true);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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