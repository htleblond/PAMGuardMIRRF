package wmnt;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.TimeZone;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;

/**
 * Streamlined panel containing time zone options for audio, binary files, and the database, if desired.
 */
@Deprecated
public class WMNTTimeZonePanel extends JPanel {
	
	protected JComboBox<String> audioBox;
	protected JComboBox<String> binaryBox;
	protected JComboBox<String> databaseBox;
	
	public WMNTTimeZonePanel(boolean includeAudio, boolean includeBinary, boolean includeDatabase, boolean addBorder) {
		super(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.anchor = c.WEST;
		c.fill = c.HORIZONTAL;
		if (addBorder) this.setBorder(new TitledBorder("Time zones"));
		if (includeAudio) {
			this.add(new JLabel("Audio:"), c);
			c.gridx += c.gridwidth;
			audioBox = new JComboBox<String>(TimeZone.getAvailableIDs());
			audioBox.setSelectedItem("UTC");
			this.add(audioBox, c);
			c.gridy++;
			c.gridx = 0;
		}
		if (includeBinary) {
			this.add(new JLabel("Binary files:"), c);
			c.gridx += c.gridwidth;
			binaryBox = new JComboBox<String>(TimeZone.getAvailableIDs());
			binaryBox.setSelectedItem("UTC");
			this.add(binaryBox, c);
			c.gridy++;
			c.gridx = 0;
		} if (includeDatabase) {
			this.add(new JLabel("Database:"), c);
			c.gridx += c.gridwidth;
			databaseBox = new JComboBox<String>(TimeZone.getAvailableIDs());
			databaseBox.setSelectedItem("UTC");
			this.add(databaseBox, c);
		}
	}
	
	private boolean setTimeZone(JComboBox<String> box, String inp) {
		if (box == null) return false;
		box.setSelectedItem(inp);
		return true;
	}
	
	public boolean setAudioTimeZone(String inp) {
		return setTimeZone(audioBox, inp);
	}
	
	public boolean setBinaryTimeZone(String inp) {
		return setTimeZone(binaryBox, inp);
	}
	
	public boolean setDatabaseTimeZone(String inp) {
		return setTimeZone(databaseBox, inp);
	}
	
	private String getTimeZone(JComboBox<String> box) {
		if (box == null) return null;
		return (String) box.getSelectedItem();
	}
	
	public String getAudioTimeZone() {
		return getTimeZone(audioBox);
	}
	
	public String getBinaryTimeZone() {
		return getTimeZone(binaryBox);
	}
	
	public String getDatabaseTimeZone() {
		return getTimeZone(databaseBox);
	}
}