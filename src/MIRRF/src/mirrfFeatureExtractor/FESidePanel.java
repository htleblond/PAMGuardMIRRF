package mirrfFeatureExtractor;

import javax.swing.JComponent;

import PamView.PamSidePanel;

/**
 * The side panel for the Feature Extractor.
 * Note that virtually all the GUI stuff of the side panel is actually in FEPanel.
 * @author Taylor LeBlond
 */
public class FESidePanel implements PamSidePanel {

	private FEControl feControl;
	private FEPanel fePanel;
	
	public FESidePanel(FEControl feControl) {
		super();
		this.feControl = feControl;
		fePanel = new FEPanel(feControl);
	}

	/**
	 * The thing you're trying to do is probably getFEPanel() instead.
	 */
	@Override
	public JComponent getPanel() {
		return fePanel.getComponent();
	}
	
	/**
	 * Getter function for the FEPanel.
	 * @return FEPanel
	 */
	public FEPanel getFEPanel() {
		return fePanel;
	}

	/**
	 * Currently does nothing.
	 */
	@Override
	public void rename(String newName) {
		// TODO Auto-generated method stub	
	}
}