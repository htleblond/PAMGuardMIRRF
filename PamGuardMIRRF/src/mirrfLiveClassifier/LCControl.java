package mirrfLiveClassifier;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamguardMVC.PamDataBlock;

/**
 * The controller class for the MIRRF Live Classifier.
 * Is a subclass of PamControlledUnit.
 * @author Taylor LeBlond
 */
public class LCControl extends PamControlledUnit implements PamSettings {
	
	public static final String UNITTYPE = "MIRRFLC";
	
	protected LCParameters parameters = new LCParameters();
	protected LCTabPanel tabPanel;
	protected LCPythonThreadManager threadManager;
	protected LCProcess process;
	protected LCUpdateProcess updateProcess;
	protected LCMarkControl markControl;
	
	//protected String trainPath;
	//protected String[] featureList;
	protected LCTrainingSetInfo loadedTrainingSetInfo;
	protected volatile boolean trainingSetLoaded;
	protected volatile boolean modelFittingFinished;
	
	public LCControl(String unitName) {
		super(UNITTYPE, unitName);
		init();
	}
	
	protected void init() {
		System.out.println("LC: Init happened");
		PamSettingManager.getInstance().registerSettings(this);
		
		//trainPath = "";
		//featureList = new String[0];
		loadedTrainingSetInfo = new LCTrainingSetInfo("");
		trainingSetLoaded = false;
		modelFittingFinished = true;
		
		tabPanel = new LCTabPanel(this);
		if (parameters.tempFolder.length() == 0) {
			LCTempFolderDialog tfDialog = new LCTempFolderDialog(this.getGuiFrame(), this, "MIRRF Live Classifier", "Live Classifier");
			tfDialog.setVisible(true);
		} else {
			File testFile = new File(parameters.tempFolder);
			if (!testFile.exists()) {
				LCTempFolderDialog tfDialog = new LCTempFolderDialog(this.getGuiFrame(), this, "MIRRF Live Classifier", "Live Classifier");
				tfDialog.setVisible(true);
			}
		}
		//if (!this.isViewer()) {
		if (true) {
			threadManager = new LCPythonThreadManager(this);
		}
		process = new LCProcess(this);
		addPamProcess(process);
		
		updateProcess = null;
		if (this.isViewer()) {
			updateProcess = new LCUpdateProcess(this);
			markControl = new LCMarkControl(this, this.getTabPanel().getPanel());
		}
		
		if (this.getParams().timeZone == null) {
			showTimeZoneDialog();
		}
	}
	
	public void showTimeZoneDialog() {
		String[] tz_list = TimeZone.getAvailableIDs();
		String tz = (String) JOptionPane.showInputDialog(this.getGuiFrame(),
				"Select a time zone.\n"
				+ "(NOTE: Dates and times are written into the binary files in\n"
				+ "local time for whatever reason. For both the table of results\n"
				+ "and annotation purposes, dates and times will be converted\n"
				+ "FROM the selected time zone to UTC.)", 
                this.getUnitName(), JOptionPane.QUESTION_MESSAGE, null, tz_list, "Canada/Pacific");
		if (tz == null) {
			tz = "UTC";
		}
		getParams().timeZone = tz;
	}
	
	public String convertLocalLongToUTC(long inp) {
		Date date = new Date(inp);
		String date_format = "yyyy-MM-dd HH:mm:ss+SSS";
		SimpleDateFormat currdateformat = new SimpleDateFormat(date_format);
		String currdate = currdateformat.format(date);
		LocalDateTime ldt = LocalDateTime.parse(currdate, DateTimeFormatter.ofPattern(date_format));
		ZoneId localZoneId = ZoneId.of(this.getParams().timeZone);
		ZoneId utcZoneId = ZoneId.of("UTC");
		ZonedDateTime localZonedDateTime = ldt.atZone(localZoneId);
		ZonedDateTime utcDateTime = localZonedDateTime.withZoneSameInstant(utcZoneId);
		DateTimeFormatter dtformat = DateTimeFormatter.ofPattern(date_format);
		return dtformat.format(utcDateTime);
	}
	
	public long convertDateStringToLong(String inp) {
		// Kudos: https://stackoverflow.com/questions/12473550/how-to-convert-a-string-date-to-long-millseconds
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss+SSS");
		try {
		    Date d = f.parse(inp);
		    return d.getTime();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * Streamlined error dialog with an editable message.
	 */
	public void SimpleErrorDialog(String inptext, int width) {
		JOptionPane.showMessageDialog(this.getGuiFrame(),
				makeHTML(inptext, width),
			this.getUnitName(),
			JOptionPane.ERROR_MESSAGE);
	}
	
	// Kudos to this: https://stackoverflow.com/questions/1842223/java-linebreaks-in-jlabels
	public String makeHTML(String inp, int width) {
		//int width = 150;
		String outp = "<html><p style=\"width:"+Integer.toString(width)+"px\">"+inp+"</p></html>";
		return outp;
	}
		
	@Override
	public LCTabPanel getTabPanel() {
		return tabPanel;
	}
	
	public LCPythonThreadManager getThreadManager() {
		return threadManager;
	}
	
	public LCProcess getProcess() {
		return process;
	}
	
	public LCUpdateProcess getUpdateProcess() {
		return updateProcess;
	}
	
/*	public String getTrainPath() {
		return trainPath;
	}
	
	public void setTrainPath(String inp) {
		trainPath = inp;
	}
	
	public String[] getFeatureList() {
		return featureList;
	}
	
	public void setFeatureList(String[] inp) {
		featureList = inp;
	} */
	
	public LCTrainingSetInfo getTrainingSetInfo() {
		return loadedTrainingSetInfo;
	}
	
	public void setTrainingSetInfo(LCTrainingSetInfo inp) {
		loadedTrainingSetInfo = inp;
	}
	
	public String getTrainPath() {
		return loadedTrainingSetInfo.pathName;
	}
	
	public ArrayList<String> getFeatureList() {
		return loadedTrainingSetInfo.featureList;
	}
	
	public HashMap<String, Integer> getLabelCounts() {
		return loadedTrainingSetInfo.labelCounts;
	}
	
	public HashMap<String, Integer> getSubsetCounts() {
		return loadedTrainingSetInfo.subsetCounts;
	}
	
	public boolean isTrainingSetLoaded() {
		return trainingSetLoaded;
	}
	
	public void setTrainingSetStatus(boolean inp) {
		trainingSetLoaded = inp;
	}
	
	public boolean isModelFittingFinished() {
		return modelFittingFinished;
	}
	
	public void setModelFittingStatus(boolean inp) {
		modelFittingFinished = inp;
	}
	
/*	public ArrayList<TCCallCluster> getCallClusterList() {
		return callClusterList;
	} */
	
	public LCParameters getParams() {
		return parameters;
	}
	
	public void setParams(LCParameters inp) {
		parameters = inp;
	}

	@Override
	public Serializable getSettingsReference() {
		return parameters;
	}

	@Override
	public long getSettingsVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		parameters = ((LCParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}
	
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName());
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
		LCSettingsDialog settingsDialog = new LCSettingsDialog(this.getPamView().getGuiFrame(), this);
		if (this.isViewer()) {
			LCColourDialog colourDialog = new LCColourDialog(this.getPamView().getGuiFrame(), this, settingsDialog);
			colourDialog.setVisible(true);
		} else {
			settingsDialog.setVisible(true);
		}
	}
}