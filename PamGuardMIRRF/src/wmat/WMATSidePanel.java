package wmat;

import javax.swing.JComponent;

import PamView.PamSidePanel;

/**
 * The side panel for WMAT.
 * Note that virtually all the GUI stuff of the side panel is actually in WMATPanel.
 * @author Holly LeBlond
 */
public class WMATSidePanel implements PamSidePanel {

	private WMATControl wmatControl;
	private WMATPanel wmatPanel;
	
	public WMATSidePanel(WMATControl wmatControl) {
		super();
		this.wmatControl = wmatControl;
		wmatPanel = new WMATPanel(wmatControl);
	}

	/**
	 * The thing you're trying to do is probably getWMATPanel() instead.
	 */
	@Override
	public JComponent getPanel() {
		return wmatPanel.getComponent();
	}
	
	/**
	 * Getter function for the WMATPanel.
	 * @return WMATPanel
	 */
	public WMATPanel getWMATPanel() {
		return wmatPanel;
	}

	/**
	 * Currently does nothing.
	 */
	@Override
	public void rename(String newName) {
		
	}

}