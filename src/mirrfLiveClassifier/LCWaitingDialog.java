package mirrfLiveClassifier;

import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import PamView.dialog.PamDialog;

public class LCWaitingDialog extends PamDialog {
	
	protected LCControl lcControl;
	protected String message;

	public LCWaitingDialog(Window parentFrame, LCControl lcControl, String message) {
		super(parentFrame, lcControl.getUnitName(), false);
		this.lcControl = lcControl;
		this.message = message;
		
		this.getOkButton().setVisible(false);
		this.getCancelButton().setVisible(false);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		// Kudos: https://stackoverflow.com/questions/17957854/add-space-around-a-jpanel-in-a-jdialog
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		mainPanel.add(new JLabel(message));
		
		setDialogComponent(mainPanel);
	}

	@Override
	public boolean getParams() {
		return true;
	}

	@Override
	public void cancelButtonPressed() {}

	@Override
	public void restoreDefaultSettings() {}
	
}