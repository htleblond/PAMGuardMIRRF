package mirrfTrainingSetBuilder;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.jogamp.newt.Window;

import PamView.PamSidePanel;
import PamView.PamTabPanel;
import PamguardMVC.PamDataBlock;
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
 * The controller class for the MIRRF Training Set Builder.
 * Is a subclass of PamControlledUnit.
 * @author Taylor LeBlond
 */
public class TSBControl extends PamControlledUnit implements PamSettings {
	
	public static final String UNITTYPE = "MIRRFTSB";
	
	protected TSBTabPanel tsbTabPanel;
	protected ArrayList<String> fullClassList;
	protected ArrayList<String> umbrellaClassList;
	protected HashMap<String, String> classMap;
	protected ArrayList<TSBSubset> subsetList;
	protected ArrayList<String> featureList;
	public boolean includeCallType;

	public TSBControl(String unitName) {
		super(UNITTYPE, "MIRRF Training Set Builder");
		
		tsbTabPanel = new TSBTabPanel(this);
		fullClassList = new ArrayList<String>();
		umbrellaClassList = new ArrayList<String>();
		classMap = new HashMap<String, String>();
		
		subsetList = new ArrayList<TSBSubset>();
		featureList = new ArrayList<String>();
		includeCallType = false;
	}
	
	/**
	 * Streamlined error dialog with an editable message.
	 */
	public void SimpleErrorDialog(String inptext, int width) {
		JOptionPane.showMessageDialog(this.getGuiFrame(),
				makeHTML(inptext, width),
			"MIRRF Training Set Builder",
			JOptionPane.ERROR_MESSAGE);
	}
	
	// Kudos to this: https://stackoverflow.com/questions/1842223/java-linebreaks-in-jlabels
		public String makeHTML(String inp, int width) {
			//int width = 150;
			String outp = "<html><p style=\"width:"+Integer.toString(width)+"px\">"+inp+"</p></html>";
			return outp;
		}
	
	public void setFullClassList(ArrayList<String> inp) {
		fullClassList = new ArrayList<String>(inp);
	}
		
	public ArrayList<String> getFullClassList() {
		return fullClassList;
	}
	
	public void setUmbrellaClassList(ArrayList<String> inp) {
		umbrellaClassList = new ArrayList<String>(inp);
	}
	
	public ArrayList<String> getUmbrellaClassList() {
		return umbrellaClassList;
	}
	
	public void setClassMap(HashMap<String, String> inp) {
		classMap = new HashMap<String, String>(inp);
	}
	
	public HashMap<String, String> getClassMap() {
		return classMap;
	}
	
	public void setSubsetList(ArrayList<TSBSubset> inp) {
		subsetList = new ArrayList<TSBSubset>(inp);
	}
	
	public ArrayList<String> getFeatureList() {
		return featureList;
	}
	
	public void setFeatureList(ArrayList<String> inp) {
		featureList = new ArrayList<String>(inp);
	}
	
	public ArrayList<TSBSubset> getSubsetList() {
		return subsetList;
	}
	
	public void resetSubsetList() {
		subsetList = new ArrayList<TSBSubset>();
	}
		
	@Override
	public TSBTabPanel getTabPanel() {
		return tsbTabPanel;
	}

	@Override
	public Serializable getSettingsReference() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getSettingsVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		// TODO Auto-generated method stub
		return false;
	}
	
}