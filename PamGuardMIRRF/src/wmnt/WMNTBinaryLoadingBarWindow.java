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

public class WMNTBinaryLoadingBarWindow extends PamDialog {
	
	private int totalToUpdate;
	private int totalCounted = 0;
	private JProgressBar loadingBar;

	public WMNTBinaryLoadingBarWindow(Window parentFrame, int totalToUpdate) {
		super(parentFrame, "Whistle and Moan Navigation Tool", false);
		this.totalToUpdate = totalToUpdate;
		totalCounted = 0;

		this.getOkButton().setVisible(false);
		this.getCancelButton().setVisible(false);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints b = new PamGridBagContraints();
		b.anchor = b.WEST;
		b.fill = b.HORIZONTAL;
		mainPanel.add(new JLabel("Reading binary files..."), b);
		b.gridy++;
		loadingBar = new JProgressBar();
		loadingBar.setValue(0);
		loadingBar.setStringPainted(true);
		loadingBar.setString("0/"+String.valueOf(totalToUpdate)+" (0.0%)");
		mainPanel.add(loadingBar, b);
		
		setDialogComponent(mainPanel);
	}
	
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