package wmnt;

import javax.swing.JComponent;

import PamView.PamSidePanel;

/**
 * The side panel for WMNT.
 * Note that virtually all the GUI stuff of the side panel is actually in WMNTPanel.
 * @author Holly LeBlond
 */
public class WMNTSidePanel implements PamSidePanel {

	private WMNTControl wmntControl;
	private WMNTPanel wmntPanel;
	
	public WMNTSidePanel(WMNTControl wmntControl) {
		super();
		this.wmntControl = wmntControl;
		wmntPanel = new WMNTPanel(wmntControl);
	}

	/**
	 * The thing you're trying to do is probably getWMNTPanel() instead.
	 */
	@Override
	public JComponent getPanel() {
		return wmntPanel.getComponent();
	}
	
	/**
	 * Getter function for the WMNTPanel.
	 * @return WMNTPanel
	 * @author Holly LeBlond
	 */
	public WMNTPanel getWMNTPanel() {
		return wmntPanel;
	}

	/**
	 * Currently does nothing.
	 */
	@Override
	public void rename(String newName) {
		
	}

}