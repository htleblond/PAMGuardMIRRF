/* 
 * MIRRF Live Classifier
 * Version 1.01 
 * By Holly LeBlond
 * Fisheries and Oceans Canada
 * November 2022
 */

package mirrfLiveClassifier;

import PamModel.PamDependency;
import PamModel.PamPluginInterface;
//import mirrf.MIRRFPlugin;
//import mirrfFeatureExtractor.FEPlugin;

/**
 * Music Information Retrieval Random Forest (MIRRF) Classifier - Live Classifier
 * (Write description here.)
 * @author Holly LeBlond
 */
public class LCPlugin implements PamPluginInterface {
	
	String jarFile;
	
	@Override
	public String getDescription() {
		return getDefaultName();
	}

	@Override
	public void setJarFile(String jarFile) {
		this.jarFile = jarFile;
	}

	@Override
	public String getJarFile() {
		return jarFile;
	}

	@Override
	public String getDeveloperName() {
		return "Holly LeBlond";
	}

	@Override
	public String getContactEmail() {
		return "wtleblond@gmail.com";
	}

	@Override
	public String getVersion() {
		return "0.02b";
	}

	@Override
	public String getPamVerDevelopedOn() {
		return "2.02.09";
	}

	@Override
	public String getPamVerTestedOn() {
		return "2.02.09";
	}

	@Override
	public String getClassName() {
		return "mirrfLiveClassifier.LCControl";
	}

	@Override
	public String getDefaultName() {
		return "MIRRF Live Classifier";
	}

	@Override
	public String getMenuGroup() {
		return "Classifiers";
	}

	@Override
	public String getToolTip() {
		return "TBA";
	}

	@Override
	public PamDependency getDependency() {
		return null;
	}

	@Override
	public int getMinNumber() {
		return 0;
	}

	@Override
	public int getMaxNumber() {
		return 1;
	}

	@Override
	public int getNInstances() {
		return 1;
	}

	@Override
	public boolean isItHidden() {
		return false;
	}

	@Override
	public String getHelpSetName() {
		return null;
	}

	@Override
	public String getAboutText() {
		String desc = "TBA";
		return desc;
	}

	/* (non-Javadoc)
	 * @see PamModel.PamPluginInterface#allowedModes()
	 */
	@Override
	public int allowedModes() {
		return PamPluginInterface.ALLMODES;
	}

}
