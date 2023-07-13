package mirrfTestClassifier;

import javax.swing.JComponent;

import PamView.PamSidePanel;
import mirrfFeatureExtractor.FEPanel;

/**
 * The Test Classifier's PamSidePanel.
 * The actual GUI stuff is in TCSidePanelPanel, not here.
 * @author Holly LeBlond
 */
public class TCSidePanel implements PamSidePanel {

	protected TCControl tcControl;
	protected TCSidePanelPanel tcSidePanelPanel;
	
	public TCSidePanel(TCControl tcControl) {
		super();
		this.tcControl = tcControl;
		tcSidePanelPanel = new TCSidePanelPanel(tcControl);
	}
	
	@Override
	public JComponent getPanel() {
		return tcSidePanelPanel.getComponent();
	}
	
	public TCSidePanelPanel getTCSidePanelPanel() {
		return tcSidePanelPanel;
	}
	
	/**
	 * Currently does nothing.
	 */
	@Override
	public void rename(String newName) {}
	
}