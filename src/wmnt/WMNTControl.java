package wmnt;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import PamController.PamControlledUnit;
import whistlesAndMoans.*;

/**
 * The controller class for the Whistle and Moan Navigation Tool.
 * @author Taylor LeBlond
 */
public class WMNTControl extends PamControlledUnit {
	
	WMNTSidePanel wmntSidePanel;
	
	private String timezone;
	
	private WMNTProcess wmntProcess;
	
	public WMNTControl(String unitName) {
		super("WMNT", "Whistle and Moan Detector");
		
		wmntSidePanel = new WMNTSidePanel(this);
		setSidePanel(wmntSidePanel);
		
		timezone = "Canada/Pacific";
		
		this.wmntProcess = new WMNTProcess(this, null);
		this.addPamProcess(wmntProcess);
	}
	
	public WMNTProcess getProcess() {
		return wmntProcess;
	}
	
	/**
	 * Streamlined error dialog.
	 * @author Taylor LeBlond
	 */
	public void SimpleErrorDialog() {
		JOptionPane.showMessageDialog(this.getGuiFrame(),
			"An error has occured.\nSee console for details.",
			"Whistle and Moan Navigation Tool",
			JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Streamlined error dialog with an editable message.
	 * @author Taylor LeBlond
	 */
	public void SimpleErrorDialog(String inptext) {
		JOptionPane.showMessageDialog(this.getGuiFrame(),
			inptext,
			"Whistle and Moan Navigation Tool",
			JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Used to set the time zone for converting binary dates/times from set time zone to UTC.
	 * @param inpstr - Valid time zone name from java.utils.TimeZone. (String)
	 * @author Taylor LeBlond
	 */
	public void setTimezone(String inpstr) {
		timezone = inpstr;
	}
	
	/**
	 * Getter function for the time zone.
	 * @return String - time offset from UTC.
	 * @author Taylor LeBlond
	 */
	public String getTimezone() {
		return timezone;
	}
	
	/**
	 * @return WMNTSidePanel
	 */
	@Override
	public WMNTSidePanel getSidePanel() {
		return wmntSidePanel;
	}
	
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem("Whistle and Moan Navigation Tool");
		menuItem.addActionListener(new DetectionSettings(parentFrame));
		return menuItem;
	}
	
	class DetectionSettings implements ActionListener {

		private Frame parentFrame;

		public DetectionSettings(Frame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			settingsDialog(parentFrame);	
		}

	}
	
	/**
	 * Opens the settings dialog.
	 * @param parentFrame
	 */
	protected void settingsDialog(Frame parentFrame) {
		WMNTSettingsDialog settingsDialog = new WMNTSettingsDialog(this.getPamView().getGuiFrame(), this);
		settingsDialog.setVisible(true);
	}
}