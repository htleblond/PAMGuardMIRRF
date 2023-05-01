/* 
 * MIRRF Training Set Builder
 * Version 1.01 
 * By Taylor LeBlond
 * Fisheries and Oceans Canada
 * July 2022
 */

package mirrfTrainingSetBuilder;

import PamModel.PamDependency;
import PamModel.PamPluginInterface;

/**
 * Music Information Retrieval Random Forest (MIRRF) Classifier - Feature Extractor
 * (Write description here.)
 * @author Taylor LeBlond
 */
public class TSBPlugin implements PamPluginInterface {
	
	String jarFile;

	@Override
	public String getClassName() {
		return "mirrfTrainingSetBuilder.TSBControl";
	}

	@Override
	public String getDefaultName() {
		return "MIRRF Training Set Builder";
	}

	@Override
	public String getDescription() {
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
	public void setJarFile(String jarFile) {
		this.jarFile = jarFile;
	}

	@Override
	public String getJarFile() {
		return jarFile;
	}

	@Override
	public String getDeveloperName() {
		return "Taylor LeBlond";
	}

	@Override
	public String getContactEmail() {
		return "wtleblond@gmail.com";
	}

	@Override
	public String getVersion() {
		return "1.01";
	}

	@Override
	public String getPamVerDevelopedOn() {
		return "2.01.05da";
	}

	@Override
	public String getPamVerTestedOn() {
		return "2.02.02";
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
