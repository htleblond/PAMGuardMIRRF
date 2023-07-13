package wmnt;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import mirrfLiveClassifier.LCParameters;
import whistlesAndMoans.*;

/**
 * The controller class for the Whistle and Moan Navigation Tool.
 * @author Holly LeBlond
 */
public class WMNTControl extends PamControlledUnit implements PamSettings {
	
	protected WMNTParameters parameters = new WMNTParameters();
	
	protected WMNTSidePanel wmntSidePanel;
	
	protected WMNTProcess wmntProcess;
	
	public WMNTControl(String unitName) {
		super("WMNT", "Whistle and Moan Detector");
		PamSettingManager.getInstance().registerSettings(this);
		
		wmntSidePanel = new WMNTSidePanel(this);
		setSidePanel(wmntSidePanel);
		
		this.wmntProcess = new WMNTProcess(this, null);
		this.addPamProcess(wmntProcess);
	}
	
	public WMNTProcess getProcess() {
		return wmntProcess;
	}
	
	/**
	 * Streamlined error dialog.
	 */
	public void SimpleErrorDialog() {
		JOptionPane.showMessageDialog(this.getGuiFrame(),
			"An error has occured.\nSee console for details.",
			"Whistle and Moan Navigation Tool",
			JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Streamlined error dialog with an editable message.
	 */
	public void SimpleErrorDialog(String inptext) {
		JOptionPane.showMessageDialog(this.getGuiFrame(),
			inptext,
			"Whistle and Moan Navigation Tool",
			JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Converts a date/time string formatted like yyyy-MM-dd HH:mm:ss(+SSS) to a new time zone.
	 * @param tz1name - The original time zone's name
	 * @param tz2name - The new time zone's name
	 * @param originalDate - The original date/time string
	 * @param includeMilliseconds - Whether or not milliseconds should be appended to the string
	 */
	public String convertBetweenTimeZones(String tz1name, String tz2name, String originalDate, boolean includeMilliseconds) {
		try {
			String date_format = "yyyy-MM-dd HH:mm:ss";
			if (includeMilliseconds) date_format += "+SSS";
			LocalDateTime ldt = LocalDateTime.parse(originalDate, DateTimeFormatter.ofPattern(date_format));
			ZoneId tz1 = ZoneId.of(tz1name);
			ZoneId tz2 = ZoneId.of(tz2name);
			ZonedDateTime originalZDT = ldt.atZone(tz1);
			ZonedDateTime newZDT = originalZDT.withZoneSameInstant(tz2);
			DateTimeFormatter dtformat = DateTimeFormatter.ofPattern(date_format);
			return dtformat.format(newZDT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @return WMNTSidePanel
	 */
	@Override
	public WMNTSidePanel getSidePanel() {
		return wmntSidePanel;
	}
	
	protected WMNTParameters getParams() {
		return parameters;
	}
	
	protected void setParams(WMNTParameters inp) {
		parameters = inp;
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

	@Override
	public Serializable getSettingsReference() {
		return parameters;
	}

	@Override
	public long getSettingsVersion() {
		// TODO
		return 0;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		WMNTParameters newParams = (WMNTParameters) pamControlledUnitSettings.getSettings();
		parameters = newParams;
		//parameters = newParams.clone(); (not sure why this doesn't work - Cloneable should be imported)
		return true;
	}
}