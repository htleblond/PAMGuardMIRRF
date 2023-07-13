package mirrfTestClassifier;

import PamModel.PamDependency;
import PamModel.PamPluginInterface;
import mirrf.MIRRFInfo;

/**
 * Music Information Retrieval Random Forest (MIRRF) Classifier for PamGuard - Test Classifier
 * @author Holly LeBlond (Fisheries and Oceans Canada)
 */
public class TCPlugin implements PamPluginInterface {
	
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
		return MIRRFInfo.getDeveloperName();
	}

	@Override
	public String getContactEmail() {
		return MIRRFInfo.getContactEmail();
	}

	@Override
	public String getVersion() {
		return MIRRFInfo.getVersion();
	}

	@Override
	public String getPamVerDevelopedOn() {
		return MIRRFInfo.getPamVerDevelopedOn();
	}

	@Override
	public String getPamVerTestedOn() {
		return MIRRFInfo.getPamVerTestedOn();
	}

	@Override
	public String getClassName() {
		return "mirrfTestClassifier.TCControl";
	}

	@Override
	public String getDefaultName() {
		return "MIRRF Test Classifier";
	}

	@Override
	public String getMenuGroup() {
		return "Classifiers";
	}

	@Override
	public String getToolTip() {
		return "Classifies Whistle and Moan Detector data from pre-existing training sets.";
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
		return "A modified version of the MIRRF Live Classifier that runs tests on pre-exisiting "
				+ "training sets through cross-validation as opposed to live data from the Feature Extractor.";
	}

	/* (non-Javadoc)
	 * @see PamModel.PamPluginInterface#allowedModes()
	 */
	@Override
	public int allowedModes() {
		return PamPluginInterface.ALLMODES;
	}

}
