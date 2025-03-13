package mirfeeFeatureExtractor;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import javax.swing.JMenuItem;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import mirfee.MIRFEEControlledUnit;

/**
 * The controller class for the MIRFEE Feature Extractor.
 * @author Holly LeBlond
 */
public class FEControl extends MIRFEEControlledUnit implements PamSettings {
	
	protected FEParameters feParameters = new FEParameters();
	protected FESidePanel feSidePanel;
	protected FESettingsDialog feSettingsDialog;
	protected FEProcess feProcess;
	protected FEPythonThreadManager threadManager;
	
	public static final String UNITTYPE = "MIRFEEFE";
	
	public FEControl(String unitName) {
		super(UNITTYPE, "MIRFEE Feature Extractor");
		
		PamSettingManager.getInstance().registerSettings(this);
		
		feSidePanel = new FESidePanel(this);
		setSidePanel(feSidePanel);
		
		feSettingsDialog = null;
		
		runTempFolderDialogLoop("MIRFEE Feature Extractor", "Feature Extractor", feParameters);
		
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
	
	public static HashMap<String, String> findFESettingsInFile(File f) {
		HashMap<String, String> outp = new HashMap<String, String>();
		Scanner sc;
		try {
			sc = new Scanner(f);
			if (!sc.hasNextLine() || !(sc.nextLine().equals("EXTRACTOR PARAMS START") && sc.hasNextLine())) {
				sc.close();
				return null;
			}
			while (sc.hasNextLine()) {
				String next = sc.nextLine();
				if (next.equals("EXTRACTOR PARAMS END")) break;
				String[] tokens = next.split("=");
				if (tokens.length >= 2) outp.put(tokens[0], tokens[1]);
			}
			sc.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return outp;
	}
	
	/**
	 * Searches for feature names within an input .mfe or .mtsf file.
	 * @return An ArrayList of found feature names. Otherwise, returns null if the file format is incorrect or no header line could be found.
	 */
	public static ArrayList<String> findFeaturesInFile(File f) {
		if (!(f.getPath().endsWith(".mirrffe") || f.getPath().endsWith(".mirrfts") ||
				f.getPath().endsWith(".mfe") || f.getPath().endsWith(".mtsf"))) return null;
		ArrayList<String> outp = new ArrayList<String>();
		Scanner sc;
		try {
			sc = new Scanner(f);
			while (sc.hasNextLine()) {
				String next = sc.nextLine();
				if ((f.getPath().endsWith("fe") && next.startsWith("cluster,uid,date,duration,lf,hf,")) ||
						(!f.getPath().endsWith("fe") && next.startsWith("cluster,uid,location,date,duration,lf,hf,label,"))) {
					String[] tokens = next.split(",");
					int startindex = 6;
					if (!f.getPath().endsWith("fe")) startindex = 8;
					for (int i = startindex; i < tokens.length; i++) outp.add(tokens[i]);
					sc.close();
					return outp;
				}
			}
			sc.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * The same as findFeaturesInFile, but only includes header or slice data features that can't be calculated if they otherwise weren't present in the input .mtsf file:
	 * amplitude, freqsd, freqsdd1, freqsdd2, freqsdslope, freqsdelbow, wcfreqs, wcslopes, wccurves
	 */
	public static ArrayList<String> findProblematicFeaturesInFile(File f) {
		ArrayList<String> foundFeatures = findFeaturesInFile(f);
		if (foundFeatures == null) return null;
		for (int j = 0; j < foundFeatures.size(); j++) {
			String[] tokens = foundFeatures.get(j).split("_");
			if (tokens[0].equals("amplitude") || tokens[0].equals("freqsd") || tokens[0].equals("freqsdd1") ||
					tokens[0].equals("freqsdd2") || tokens[0].equals("freqsdslope") || tokens[0].equals("freqsdelbow") ||
					tokens[0].equals("wcfreqs") || tokens[0].equals("wcslopes") || tokens[0].equals("wccurves"))
				continue;
			foundFeatures.remove(j);
			j--;
		}
		return foundFeatures;
	}
	
	/**
	 * Checks if two files contain the exact same FE settings and features.
	 * Files must be either .mfe or .mtsf (or their old counterparts).
	 * @return True if settings and features match. Otherwise, false.
	 */
	public boolean fileSettingsMatch(File f1, File f2) {
		HashMap<String, String> f1settings = findFESettingsInFile(f1);
		HashMap<String, String> f2settings = findFESettingsInFile(f2);
		if (f1settings == null || f2settings == null || f1settings.size() != f2settings.size())
			return false;
		Iterator<String> it = f1settings.keySet().iterator();
		while (it.hasNext()) {
			String next = it.next();
			if (!f2settings.containsKey(next) || !f1settings.get(next).equals(f2settings.get(next)))
				return false;
		}
		ArrayList<String> f1features = findFeaturesInFile(f1);
		ArrayList<String> f2features = findFeaturesInFile(f2);
		if (f1features == null || f2features == null || f1features.size() == 0 || f2features.size() == 0 || f1features.size() != f2features.size())
			return false;
		for (int i = 0; i < f1features.size(); i++) {
			if (!f1features.get(i).equals(f2features.get(i))) return false;
		}
		return true;
	}
	
	@Override
	public FESidePanel getSidePanel() {
		return feSidePanel;
	}
	
	public FEParameters getParams() {
		return feParameters;
	}
	
	public void setParams(FEParameters inp) {
		feParameters = inp;
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
	protected void settingsDialog(Frame parentFrame) {
		FESettingsDialog settingsDialog = new FESettingsDialog(this.getPamView().getGuiFrame(), this);
		settingsDialog.setVisible(true);
	}
}