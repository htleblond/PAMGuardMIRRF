package mirrfTrainingSetBuilder;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;

import pamMaths.HistogramDisplay;
import whistleClassifier.WhistleClassifierControl;
import PamView.PamTabPanel;
import PamView.panel.CornerLayoutContraint;
import PamView.panel.PamBorderPanel;

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TSBPanel getPanel() {
		return tsbPanel;
	}

	@Override
	public JToolBar getToolBar() {
		// TODO Auto-generated method stub
		return null;
	}
	
}