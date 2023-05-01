package mirrfTestClassifier;

import mirrfLiveClassifier.LCPanel;
import mirrfLiveClassifier.LCTabPanel;

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