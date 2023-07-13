package mirrfTrainingSetBuilder;

import java.awt.Frame;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import PamView.PamTabPanel;

/**
 * The Training Set Builder's PamTabPanel.
 * The actual GUI components are in TSBPanel.
 * @author Holly LeBlond
 */
public class TSBTabPanel implements PamTabPanel {
	
	TSBControl tsbControl;
	private TSBPanel tsbPanel;
	
	public TSBTabPanel(TSBControl tsbControl) {
		super();
		this.tsbControl = tsbControl;
		this.tsbPanel = new TSBPanel(tsbControl);
	}
	
	@Override
	public JMenu createMenu(Frame parentFrame) {
		return null;
	}

	@Override
	public TSBPanel getPanel() {
		return tsbPanel;
	}

	@Override
	public JToolBar getToolBar() {
		return null;
	}
	
}