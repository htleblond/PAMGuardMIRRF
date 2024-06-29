package mirrf;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.JOptionPane;

import PamController.PamControlledUnit;

/**
 * Streamlined abstract PamControlledUnit extension for MIRRF modules that use Python scripts.
 * @author Holly LeBlond
 */
public abstract class MIRRFControlledUnit extends PamControlledUnit {

	public MIRRFControlledUnit(String unitType, String unitName) {
		super(unitType, unitName);
	}
	
	protected void runTempFolderDialogLoop(String unitName, String subfolderName, MIRRFParameters params) {
		boolean preExistingFile = false;
		if (params.tempKey > -1) {
			int result = JOptionPane.showConfirmDialog(this.getGuiFrame(),
					makeHTML("In this configuration, the following temporary folder path was found:"
							+ "\n\n"+params.tempFolder+"\n\n"
							+ "Would you like to change the folder?\n\n"
							+ "(WARNING: If another instance of PAMGuard is running the "+subfolderName+" with\n"
							+ "this folder, SELECT YES, otherwise that instance will most likely crash.)", 300),
					unitName,
					JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				String toRemove = subfolderName+"\\"+String.format("%09d", params.tempKey)+"\\";
				if (params.tempFolder.endsWith(toRemove)) {
					params.tempFolder = params.tempFolder.substring(0, params.tempFolder.length()-toRemove.length());
					preExistingFile = true;
				}
				params.tempKey = -1;
			}
		}
		
		if (params.tempFolder.length() == 0 || params.tempKey < 0) {
			do {
				MIRRFTempFolderDialog tfDialog = new MIRRFTempFolderDialog(this.getGuiFrame(), this, unitName,
						subfolderName, params, preExistingFile);
				tfDialog.setVisible(true);
				File testFile = new File(params.tempFolder);
				if (!testFile.exists()) {
					params.tempFolder = "";
				}
			} while (params.tempFolder.length() == 0 || params.tempKey < 0);
		}
		System.out.println("tempFolder: "+params.tempFolder);
	}
	
	/**
	 * Converts date/time strings formatted as yyyy-MM-dd HH:mm:ss+SSS back to longs.
	 */
	public static long convertDateStringToLong(String inp) {
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
	 * Converts a long to a string representing a date/time with the following format: yyyy-MM-dd HH:mm:ss+SSS
	 */
	public static String convertDateLongToString(long inp) {
		Date date = new Date(inp);
		String date_format = "yyyy-MM-dd HH:mm:ss+SSS";
		SimpleDateFormat currdateformat = new SimpleDateFormat(date_format);
		String currdate = currdateformat.format(date);
		LocalDateTime ldt = LocalDateTime.parse(currdate, DateTimeFormatter.ofPattern(date_format));
		DateTimeFormatter dtformat = DateTimeFormatter.ofPattern(date_format);
		//System.out.println(dtformat.format(ldt));
		return dtformat.format(ldt);
	}
	
	/**
	 * Converts a date/time long from one time zone to another.
	 * NOTE: Always assumes Daylight Savings Time is toggled ON.
	 * @return Long representing the input time, or -1 if the input time zones aren't real.
	 */
	public static long convertBetweenTimeZones(long inp, String fromTZ, String toTZ) {
		try {
			ZonedDateTime fromZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(inp), ZoneId.of(fromTZ));
			ZonedDateTime toZonedDateTime = fromZonedDateTime.withZoneSameInstant(ZoneId.of(toTZ));
			String date_format = "yyyy-MM-dd HH:mm:ss+SSS";
			DateTimeFormatter dtformat = DateTimeFormatter.ofPattern(date_format);
			//System.out.println("From: "+dtformat.format(fromZonedDateTime)+" "+fromTZ);
			//System.out.println("To:   "+dtformat.format(toZonedDateTime)+" "+toTZ);
			return convertDateStringToLong(dtformat.format(toZonedDateTime));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public static String getLocalTimeZoneName() {
		return TimeZone.getDefault().getID();
	}
	
	public static String makeHTML(String inp, int width) {
		return String.format("<html><body style='width: %1spx'>%1s", width, inp);
	}
	
	/**
	 * Streamlined error dialog.
	 */
	public void SimpleErrorDialog() {
		JOptionPane.showMessageDialog(this.getGuiFrame(),
			"An error has occured.\nSee console for details.",
			"MIRRF Feature Extractor",
			JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Streamlined error dialog with an editable message.
	 */
	public void SimpleErrorDialog(String inptext) {
		JOptionPane.showMessageDialog(this.getGuiFrame(),
			inptext,
			"MIRRF Feature Extractor",
			JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Streamlined error dialog with an editable message and length.
	 */
	public void SimpleErrorDialog(String inptext, int width) {
		JOptionPane.showMessageDialog(this.getGuiFrame(),
				makeHTML(inptext, width),
			this.getUnitName(),
			JOptionPane.ERROR_MESSAGE);
	}
}