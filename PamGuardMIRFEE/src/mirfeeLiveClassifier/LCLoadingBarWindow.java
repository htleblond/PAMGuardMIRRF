package mirfeeLiveClassifier;

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
 * The loading dialog for when the WMAT has a large amount of updates for the classifier's table.
 * @author Holly LeBlond
 */
public class LCLoadingBarWindow extends PamDialog {
	
	private int totalToUpdate;
	private int totalCounted;
	private JProgressBar loadingBar;
	
	public LCLoadingBarWindow(Window parentFrame, int totalToUpdate) {
		super(parentFrame, "MIRFEE Live Classifier", false);
		this.totalToUpdate = totalToUpdate;
		totalCounted = 0;

		this.getOkButton().setVisible(false);
		this.getCancelButton().setVisible(false);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints b = new PamGridBagContraints();
		b.anchor = b.WEST;
		b.fill = b.HORIZONTAL;
		mainPanel.add(new JLabel("Loading WMAT updates into Live Classifier..."), b);
		b.gridy++;
		loadingBar = new JProgressBar();
		loadingBar.setValue(0);
		loadingBar.setStringPainted(true);
		loadingBar.setString("0/"+String.valueOf(totalToUpdate)+" (0.0%)");
		mainPanel.add(loadingBar, b);
		
		setDialogComponent(mainPanel);
	}
	
	/**
	 * Adds the input value to the loading bar.
	 */
	public void updateLoadingBar(int addition) {
		totalCounted += addition;
		loadingBar.setValue((int) Math.floor(100*(((double) totalCounted)/totalToUpdate)));
		loadingBar.setString(String.valueOf(totalCounted)+"/"+String.valueOf(totalToUpdate)+" ("+
				String.format("%.1f", (float) 100*((double) totalCounted)/totalToUpdate)+"%)");
	}

	@Override
	public boolean getParams() {
		// Does nothing (button disabled).
		return false;
	}

	@Override
	public void cancelButtonPressed() {
		// Does nothing (button disabled).
	}

	@Override
	public void restoreDefaultSettings() {
		// Does nothing (button disabled).
	}
	
}