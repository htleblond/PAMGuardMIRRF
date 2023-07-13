package mirrfLiveClassifier;

import java.awt.Frame;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import PamView.PamTabPanel;

/**
 * The Live Classifier's PamTabPanel.
 * Not the actual GUI panel - that's LCPanel.
 * @author Holly LeBlond
 */
public class LCTabPanel implements PamTabPanel {
	
	protected LCControl control;
	protected LCPanel panel;
	
	public LCTabPanel(LCControl control) {
		super();
		this.control = control;
		this.panel = new LCPanel(control, control.isViewer());
	}
	
	@Override
	public JMenu createMenu(Frame parentFrame) {
		return null;
	}

	@Override
	public LCPanel getPanel() {
		return panel;
	}

	@Override
	public JToolBar getToolBar() {
		return null;
	}
	
}