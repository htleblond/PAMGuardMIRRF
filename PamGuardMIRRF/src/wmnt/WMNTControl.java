package wmnt;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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
	
	//private String timezone;
	protected String audioTZ;
	protected String binaryTZ;
	protected String databaseTZ;
	
	protected WMNTProcess wmntProcess;
	
	public WMNTControl(String unitName) {
		super("WMNT", "Whistle and Moan Detector");
		
		wmntSidePanel = new WMNTSidePanel(this);
		setSidePanel(wmntSidePanel);
		
		//timezone = "Canada/Pacific";
		audioTZ = "UTC";
		binaryTZ = "UTC";
		databaseTZ = "UTC";
		
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
/*	public void setTimezone(String inpstr) {
		timezone = inpstr;
	} */
	
	/**
	 * Getter function for the time zone.
	 * @return String - time offset from UTC.
	 * @author Taylor LeBlond
	 */
/*	public String getTimezone() {
		return timezone;
	} */
	
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