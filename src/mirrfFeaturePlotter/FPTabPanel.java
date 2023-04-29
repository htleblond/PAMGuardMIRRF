package mirrfFeaturePlotter;

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

public class FPTabPanel implements PamTabPanel {
	
	FPControl fpControl;
	private FPPanel fpPanel;
	
	public FPTabPanel(FPControl fpControl) {
		super();
		this.fpControl = fpControl;
		this.fpPanel = new FPPanel(fpControl);
	}
	
	@Override
	public JMenu createMenu(Frame parentFrame) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FPPanel getPanel() {
		return fpPanel;
	}

	@Override
	public JToolBar getToolBar() {
		// TODO Auto-generated method stub
		return null;
	}
	
}