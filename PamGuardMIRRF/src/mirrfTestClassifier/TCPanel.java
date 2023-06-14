package mirrfTestClassifier;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import mirrfLiveClassifier.LCExportDialog;
import mirrfLiveClassifier.LCPanel;
import mirrfLiveClassifier.LCWaitingDialogThread;

public class TCPanel extends LCPanel {
	
	public TCPanel(TCControl control) {
		super(control, true);
		
		bestFeaturesButton.removeActionListener(null);
	}
	
	@Override
	protected void bestFeaturesButtonAction() {
		if (getControl().getTrainingSetInfo() == null || getControl().getTrainPath().equals("")) {
			control.SimpleErrorDialog("Training and/or testing sets have not been configured yet.", 250);
			return;
		}
		wdThread = new LCWaitingDialogThread(getControl().getGuiFrame(), getControl(), "Waiting for response from Python script...");
		wdThread.start();
		if (getControl().getThreadManager().initializeBestFeaturesSet())
			control.getThreadManager().pythonCommand("tcmBest.printBestFeatureOrder()");
		else wdThread.halt();
	}
	
	@Override
	protected void exportButtonListenerAction() {
		TCExportDialog exportDialog = new TCExportDialog(getControl(), getControl().getPamView().getGuiFrame());
		exportDialog.setVisible(true);
	}
	
	// Only difference is that this one cuts off the bottom two rows of the confusion matrix.
	@Override
	protected void showMatrices() {
		JPanel matricesPanel = new JPanel(new GridLayout(2, 1, 10, 50));
		JPanel cmGridPanel = new JPanel(new GridLayout(jLabelConfMatrix.length-2, jLabelConfMatrix[0].length, 10, 10));
		for (int i = 0; i < jLabelConfMatrix.length-2; i++) {
			for (int j = 0; j < jLabelConfMatrix[i].length; j++) {
				cmGridPanel.add(jLabelConfMatrix[i][j]);
			}
		}
		matricesPanel.add(cmGridPanel);
		JPanel amGridPanel = new JPanel(new GridLayout(jLabelAccuracyMatrix.length, jLabelAccuracyMatrix[0].length, 10, 10));
		for (int i = 0; i < jLabelAccuracyMatrix.length; i++) {
			for (int j = 0; j < jLabelAccuracyMatrix[i].length; j++) {
				amGridPanel.add(jLabelAccuracyMatrix[i][j]);
			}
		}
		matricesPanel.add(amGridPanel);
		cmCardsPanel.add(matricesPanel, "matricesPanel");
		cl.show(cmCardsPanel, "matricesPanel");
	}
	
	protected TCControl getControl() {
		return (TCControl) control;
	}
}