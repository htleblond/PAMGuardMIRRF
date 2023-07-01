/* 
 * MIRRF Training Set Builder
 * Version 1.01 
 * By Holly LeBlond
 * Fisheries and Oceans Canada
 * July 2022
 */

package mirrfTrainingSetBuilder;

import PamModel.PamDependency;
import PamModel.PamPluginInterface;
//import mirrfFeatureExtractor.FEPlugin;
//import mirrf.MIRRFPlugin;

/**
 * Music Information Retrieval Random Forest (MIRRF) Classifier - Feature Extractor
 * (Write description here.)
 * @author Holly LeBlond
 */
public class TSBPlugin implements PamPluginInterface {
	
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
		return "mirrfTrainingSetBuilder.TSBControl";
	}

	@Override
	public String getDefaultName() {
		return "MIRRF Training Set Builder";
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
