package mirrfTestClassifier;

import mirrfLiveClassifier.LCTabPanel;

/**
 * The Test Classifier's PamTabPanel.
 * Not that actual GUI panel - that's TCPanel.
 * Subclass of the Live Classifier's PamTabPanel.
 * @author Holly LeBlond
 */
public class TCTabPanel extends LCTabPanel {

	public TCTabPanel(TCControl control) {
		super(control);
		this.panel = new TCPanel(control);
	}
	
	@Override
	public TCPanel getPanel() {
		return (TCPanel) panel;
	}
	
}