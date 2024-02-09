package wmnt;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

/**
 * Loading dialog for matching database entries to binary entries.
 * @author Holly LeBlond
 */
public class WMNTDatabaseLoadingBarWindow extends PamDialog {
	
	private int totalToUpdate = 0;
	private int totalCounted = 0;
	protected JLabel statusLabel;
	protected JProgressBar loadingBar;

	public WMNTDatabaseLoadingBarWindow(Window parentFrame) {
		super(parentFrame, "Whistle and Moan Navigation Tool", false);
		//this.totalToUpdate = totalToUpdate;
		totalCounted = 0;

		this.getOkButton().setVisible(false);
		this.getCancelButton().setVisible(false);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints b = new PamGridBagContraints();
		b.anchor = b.WEST;
		b.fill = b.HORIZONTAL;
		statusLabel = new JLabel("Querying database...");
		mainPanel.add(statusLabel, b);
		b.gridy++;
		loadingBar = new JProgressBar();
		loadingBar.setValue(0);
		loadingBar.setStringPainted(true);
		//loadingBar.setString("0/"+String.valueOf(totalToUpdate)+" (0.0%)");
		loadingBar.setString("Pending...");
		mainPanel.add(loadingBar, b);
		
		setDialogComponent(mainPanel);
	}
	
	/**
	 * Signals the dialog that query has finished and reading has started.
	 * Throws exception if no valid entries were found.
	 */
	public void startReadingCount(int totalToUpdate) {
		if (totalToUpdate == 0) throw new ArithmeticException("totalToUpdate must be greater than zero.");
		this.totalToUpdate = totalToUpdate;
		statusLabel.setText("Matching database entries...");
		loadingBar.setString("0/"+String.valueOf(totalToUpdate)+" (0.0%)");
	}
	
	/**
	 * Adds one to the lading bar once reading has started.
	 */
	public void addOneToLoadingBar() {
		totalCounted++;
		loadingBar.setValue((int) Math.floor(100*(((double) totalCounted)/totalToUpdate)));
		loadingBar.setString(String.valueOf(totalCounted)+"/"+String.valueOf(totalToUpdate)+" ("+
				String.format("%.1f", (float) 100*((double) totalCounted)/totalToUpdate)+"%)");
	}

	@Override
	public boolean getParams() {
		// Button disabled
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