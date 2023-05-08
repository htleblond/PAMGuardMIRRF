package mirrfFeatureExtractor;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;

/**
 * The controller class for the MIRRF Feature Extractor.
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
		
		if (feParameters.tempFolder.length() == 0 || feParameters.tempKey < 0) {
			do {
				//FETempFolderDialog tfDialog = new FETempFolderDialog(this.getGuiFrame(), this);
				MIRRFTempFolderDialog tfDialog = new MIRRFTempFolderDialog(this.getGuiFrame(), this,
						"MIRRF Feature Extractor", "Feature Extractor", feParameters);
				tfDialog.setVisible(true);
				File testFile = new File(feParameters.tempFolder);
				if (!testFile.exists()) {
					feParameters.tempFolder = "";
				}
			} while (feParameters.tempFolder.length() == 0 || feParameters.tempKey < 0);
		}
		System.out.println("tempFolder: "+feParameters.tempFolder);
		
		if (!this.isViewer()) {
			this.threadManager = new FEPythonThreadManager(this);
		}
		
		this.feProcess = new FEProcess(this);
		addPamProcess(feProcess);
		
	}
	
	/**
	 * Calls the function in FEPanel that adds 1 to the respective counter.
	 * @param i - Number determining the chosen counter:
	 * <br> 0 = Success
	 * <br> 1 = Failure
	 * <br> 2 = Ignore
	 * <br> 3 = Pending
	 * @param uid - The contour's UID, for printing purposes.
	 */
	public void addOneToCounter(int i, String uid) {
		this.getSidePanel().getFEPanel().addOneToCounter(i, uid);
	}
	
	/**
	 * Calls the function in FEPanel that subtracts 1 from the pending counter in the FEPanel.
	 * @return True if the counter is above 0. False otherwise.
	 */
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
	
	/**
	 * The object that handles the Python scripts that perform the feature extraction.
	 */
	public FEPythonThreadManager getThreadManager() {
		return threadManager;
	}

	@Override
	public Serializable getSettingsReference() {
		return feParameters;
	}

	@Override
	public long getSettingsVersion() {
		// TODO
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