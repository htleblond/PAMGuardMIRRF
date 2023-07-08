package wmnt;

import java.io.Serializable;
import java.util.ArrayList;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class WMNTParameters implements Serializable, Cloneable, ManagedParameters {
	
	public String inputProcessName;
	
	public ArrayList<String> speciesList;
	public ArrayList<String> callTypeList;
	
	public int startBuffer;
	
	public String audioTZ;
	public String binaryTZ;
	public String databaseTZ;
	
	public String sqlTableName;
	
	public WMNTParameters() {
		inputProcessName = "";
		
		String[] speciesNames = new String[] {"", "2-second glitch", "False positive", "KW", "HW", "CSL", "PWSD", "KW/HW?", "KW/PWSD?", "Fish",
				"Vessel", "Mooring", "Unk", "Unk-Anthro", "Unk-Odontocete", "Unk-Mysticete", "Unk-Cetacean", "Deployment", "Aliens"};
		speciesList = new ArrayList<String>();
		for (int i = 0; i < speciesNames.length; i++) speciesList.add(speciesNames[i]);
		
		String[] callTypeNames = {"","n/a","N01i","N01ii","N01iii","N01iv","N01v","N02","N03",
				"N04","N05i","N05ii","N07i","N07ii","N07iii","N07iv","N08i","N08ii","N08iii","N09i","N09ii",
				"N09iii","N10","N11","N12","N13","N16i","N16ii","N16iii","N16iv","N17","N18","N20","N21",
				"N27","N47","Unnamed Aclan","Unnamed AAsubclan","Unnamed ABsubclan","N23i","N23ii","N24i","N24ii","N25","N26",
				"N28","N29","N30","N39","N40","N41","N44","N45","N46","N48","Unnamed Gclan","Unnamed GGsubclan","Unnamed GIsubclan",
				"N32i","N32ii","N33","N34","N42","N43","N50","N51","N52","Unnamed Rclan","S01","S02i","S02ii","S02iii","S03","S04",
				"S05","S06","S07","S08i","S08ii","S09","S10","S12","S13i","S13ii","S14","S16","S17","S18","S19","S22","S31","S33",
				"S36","S37i","S37ii","S40","S41","S42","S44","Unnamed Jclan"};
		callTypeList = new ArrayList<String>();
		for (int i = 0; i < callTypeNames.length; i++) callTypeList.add(callTypeNames[i]);
		
		startBuffer = 2000;
		
		audioTZ = "UTC";
		binaryTZ = "UTC";
		databaseTZ = "UTC";
		
		sqlTableName = "whistle_and_moan_detector";
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		return PamParameterSet.autoGenerate(this);
	}
}