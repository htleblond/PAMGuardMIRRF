package wmat;

import PamModel.PamDependency;
import PamModel.PamPluginInterface;
import mirfee.MIRFEEInfo;

/**
 * Whistle and Moan Annotation Tool (WMAT) - now bundled into mirfee.jar
 * (Formerly named "Whistle and Moan Detector Plus" (WMDP) and "Whistle and Moan Navigation Tool (WMNT).)
 * The intention of this was to add a side panel for streamlined manual data entry into MySQL for the
 * pre-existing Whistle and Moan Detector in PamGuard. However, it is also very useful for simply
 * navigating through the spectrogram for viewing WMD contours. It was originally designed for use by the
 * Marine Mammals Unit at the Pacific Biological Station in Nanaimo, BC as an external plugin.
 * @author Holly LeBlond
 */
public class WMATPlugin implements PamPluginInterface {
	
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
		return MIRFEEInfo.getDeveloperName();
	}

	@Override
	public String getContactEmail() {
		return MIRFEEInfo.getContactEmail();
	}

	@Override
	public String getVersion() {
		return MIRFEEInfo.getVersion();
	}

	@Override
	public String getPamVerDevelopedOn() {
		return MIRFEEInfo.getPamVerDevelopedOn();
	}

	@Override
	public String getPamVerTestedOn() {
		return MIRFEEInfo.getPamVerTestedOn();
	}

	@Override
	public String getClassName() {
		return "wmat.WMATControl";
	}

	@Override
	public String getDefaultName() {
		return "MIRFEE Whistle and Moan Annotation Tool";
	}

	@Override
	public String getMenuGroup() {
		//return "Detectors";
		return "Utilities";
	}

	@Override
	public String getToolTip() {
		return "Viewer-mode-only tool for viewing, analyzing and annotating Whistle and Moan Detector contours.";
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
		return "Viewer-mode-only tool for viewing, analyzing and annotating Whistle and Moan Detector contours.";
	}

	/* (non-Javadoc)
	 * @see PamModel.PamPluginInterface#allowedModes()
	 */
	@Override
	public int allowedModes() {
		return PamPluginInterface.VIEWERONLY;
	}

}
