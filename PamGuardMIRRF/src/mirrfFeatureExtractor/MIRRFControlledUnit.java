package mirrfFeatureExtractor;

import java.io.File;

import javax.swing.JOptionPane;

import PamController.PamControlledUnit;

public abstract class MIRRFControlledUnit extends PamControlledUnit {

	public MIRRFControlledUnit(String unitType, String unitName) {
		super(unitType, unitName);
	}
	
	protected void runTempFolderDialogLoop(String unitName, String subfolderName, MIRRFParameters params) {
		boolean preExistingFile = false;
		if (params.tempKey > -1) {
			int result = JOptionPane.showConfirmDialog(this.getGuiFrame(),
					makeHTML("In this configuration, the following temporary folder path was found:"
							+ "\n\n"+params.tempFolder+"\n\n"
							+ "Would you like to change the folder?\n\n"
							+ "(WARNING: If another instance of PAMGuard is running the "+subfolderName+" with\n"
							+ "this folder, SELECT YES, otherwise that instance will most likely crash.)", 300),
					unitName,
					JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				String toRemove = subfolderName+"\\"+String.format("%09d", params.tempKey)+"\\";
				if (params.tempFolder.endsWith(toRemove)) {
					params.tempFolder = params.tempFolder.substring(0, params.tempFolder.length()-toRemove.length());
					preExistingFile = true;
				}
				params.tempKey = -1;
			}
		}
		
		if (params.tempFolder.length() == 0 || params.tempKey < 0) {
			do {
				MIRRFTempFolderDialog tfDialog = new MIRRFTempFolderDialog(this.getGuiFrame(), this, unitName,
						subfolderName, params, preExistingFile);
				tfDialog.setVisible(true);
				File testFile = new File(params.tempFolder);
				if (!testFile.exists()) {
					params.tempFolder = "";
				}
			} while (params.tempFolder.length() == 0 || params.tempKey < 0);
		}
		System.out.println("tempFolder: "+params.tempFolder);
	}
	
	public String makeHTML(String inp, int width) {
		return String.format("<html><body style='width: %1spx'>%1s", width, inp);
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
	 * Streamlined error dialog with an editable message and length.
	 */
	public void SimpleErrorDialog(String inptext, int width) {
		JOptionPane.showMessageDialog(this.getGuiFrame(),
				makeHTML(inptext, width),
			this.getUnitName(),
			JOptionPane.ERROR_MESSAGE);
	}
}