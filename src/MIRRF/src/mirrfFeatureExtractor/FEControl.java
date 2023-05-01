package mirrfFeatureExtractor;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import java.awt.Window;

import PamView.PamSidePanel;
import PamguardMVC.PamDataBlock;
import mirrfLiveClassifier.LCColourDialog;
import mirrfLiveClassifier.LCSettingsDialog;
import whistlesAndMoans.*;
//import whistlesAndMoans.WhistleMoanControl.DetectionSettings;
import Layout.DisplayPanelProvider;
import Layout.DisplayProviderList;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import userDisplay.*;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;

/**
 * The controller class for the MIRRF Feature Extractor.
 * Is a subclass of PamControlledUnit.
 * @author Taylor LeBlond
 */
public class FEControl extends PamControlledUnit implements PamSettings{
	
	protected FEParameters feParameters = new FEParameters();
	protected FESidePanel feSidePanel;
	protected FESettingsDialog feSettingsDialog;
	protected FEProcess feProcess;
	protected FEPythonThreadManager threadManager;
	
	public static final String UNITTYPE = "MIRRFFE";
	
	public FEControl(String unitName) {
		super(UNITTYPE, "MIRRF Feature Extractor");
		
		PamSettingManager.getInstance().registerSettings(this);
		
		feSidePanel = new FESidePanel(this);
		setSidePanel(feSidePanel);
		
		feSettingsDialog = null;
		
		if (feParameters.tempFolder.length() == 0) {
			do {
				FETempFolderDialog tfDialog = new FETempFolderDialog(this.getGuiFrame(), this);
				tfDialog.setVisible(true);
				File testFile = new File(feParameters.tempFolder);
				if (!testFile.exists()) {
					feParameters.tempFolder = "";
				}
			} while (feParameters.tempFolder.length() == 0);
		}
		System.out.println("tempFolder: "+feParameters.tempFolder);
		
		if (!this.isViewer()) {
			this.threadManager = new FEPythonThreadManager(this);
		}
		
		this.feProcess = new FEProcess(this);
		addPamProcess(feProcess);
		
	/*	System.out.println("/////////");
		ArrayList<PamDataBlock> blockList = this.getPamController().getDataBlocks();
		for (int i = 0; i < blockList.size(); i++) {
			PamDataBlock block = blockList.get(i);
			System.out.println(block.getDataName());
			int j = 0;
			while (true) {
				try {
					System.out.println("\t"+block.getPamObserver(j).getObserverName());
				} catch (Exception e) {
					break;
				}
				j++;
			}
		}
		System.out.println("/////////"); */
		
	}
	
	public void addOneToCounter(int i, String uid) {
		this.getSidePanel().getFEPanel().addOneToCounter(i, uid);
	}
	
	public boolean subtractOneFromPendingCounter() {
		return this.getSidePanel().getFEPanel().subtractOneFromPendingCounter();
	}
	
	/**
	 * Streamlined error dialog.
	 */
	public void SimpleErrorDialog() {
		JOptionPane.showMessageDialog(this.getGuiFrame(),
			"An error has occured.\nSee console for details.",
			"MIRRF Feature Extractor",
			JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Streamlined error dialog with an editable message.
	 */
	public void SimpleErrorDialog(String inptext) {
		JOptionPane.showMessageDialog(this.getGuiFrame(),
			inptext,
			"MIRRF Feature Extractor",
			JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * @return FESidePanel
	 */
	@Override
	public FESidePanel getSidePanel() {
		return feSidePanel;
	}
	
	public FEParameters getParams() {
		return feParameters;
	}
	
	public FESettingsDialog getSettingsDialog() {
		return feSettingsDialog;
	}
	
	public void setSettingsDialog(FESettingsDialog settingsDialog) {
		feSettingsDialog = settingsDialog;
	}
	
	public FEPythonThreadManager getThreadManager() {
		return threadManager;
	}
/*
	public WhistleToneParameters getWhistleToneParams() {
		return this.whistleToneParameters;
	}
*/

	@Override
	public Serializable getSettingsReference() {
		return feParameters;
	}

	@Override
	public long getSettingsVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		feParameters = ((FEParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}	


	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName());
		menuItem.addActionListener(new DetectionSettings(parentFrame));
		return menuItem;
	}

	class DetectionSettings implements ActionListener {

		private Frame parentFrame;

		public DetectionSettings(Frame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			settingsDialog(parentFrame);	
		}
	}

	
	/**
	 * Opens the settings dialog.
	 * @param parentFrame
	 */
	/**
	 * Opens the settings dialog.
	 * @param parentFrame
	 */
	protected void settingsDialog(Frame parentFrame) {
		FESettingsDialog settingsDialog = new FESettingsDialog(this.getPamView().getGuiFrame(), this);
		settingsDialog.setVisible(true);
	}
}