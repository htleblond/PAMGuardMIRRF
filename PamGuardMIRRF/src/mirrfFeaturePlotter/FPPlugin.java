package mirrfFeaturePlotter;

import PamModel.PamDependency;
import PamModel.PamPluginInterface;
import mirrf.MIRRFInfo;

/**
 * Music Information Retrieval Random Forest (MIRRF) Classifier for PamGuard - Feature Plotter
 * @author Holly LeBlond (Fisheries and Oceans Canada)
 */
public class FPPlugin implements PamPluginInterface {
	
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
		return "mirrfFeaturePlotter.FPControl";
	}

	@Override
	public String getDefaultName() {
		return "MIRRF Feature Plotter";
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
