package mirrfTrainingSetBuilder;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

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
 * @author Holly LeBlond
 */
public class TSBControl extends PamControlledUnit implements PamSettings {
	
	public static final String UNITTYPE = "MIRRFTSB";
	
	protected TSBTabPanel tsbTabPanel;
	protected ArrayList<String> fullClassList;
	protected ArrayList<String> umbrellaClassList;
	protected HashMap<String, String> classMap;
	protected ArrayList<TSBSubset> subsetList;
	protected ArrayList<String> featureList;
	protected HashMap<String, String> feParamsMap;
	public boolean includeCallType;

	public TSBControl(String unitName) {
		super(UNITTYPE, "MIRRF Training Set Builder");
		
		tsbTabPanel = new TSBTabPanel(this);
		fullClassList = new ArrayList<String>();
		umbrellaClassList = new ArrayList<String>();
		classMap = new HashMap<String, String>();
		
		subsetList = new ArrayList<TSBSubset>();
		featureList = new ArrayList<String>();
		feParamsMap = new HashMap<String, String>();
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
	
	public long convertDateStringToLong(String inp) {
		// Kudos: https://stackoverflow.com/questions/12473550/how-to-convert-a-string-date-to-long-millseconds
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss+SSS");
		try {
		    Date d = f.parse(inp);
		    return d.getTime();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		return -1;
	}
	
	public String makeHTML(String inp, int width) {
		return String.format("<html><body style='width: %1spx'>%1s", width, inp);
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
	
	public ArrayList<String> getOutputClassLabels() {
		ArrayList<String> outp = new ArrayList<String>();
		Iterator<String> it = getClassMap().values().iterator();
		while (it.hasNext()) {
			String next = it.next();
			if (!outp.contains(next)) outp.add(next);
		}
		return outp;
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
	
	public HashMap<String, String> getFEParamsMap() {
		return feParamsMap;
	}
	
	public void setFEParamsMap(HashMap<String, String> inp) {
		feParamsMap = inp;
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