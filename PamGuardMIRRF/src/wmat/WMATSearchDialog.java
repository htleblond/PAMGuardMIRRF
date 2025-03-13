package wmat;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;

import PamView.dialog.*;
import PamView.panel.PamPanel;
import PamView.*;

/**
 * Opens the dialog window when 'Select by search' is pressed.
 * @author Holly LeBlond
 */
public class WMATSearchDialog extends PamDialog {

	private WMATControl wmatControl;
	
	private PamPanel mainPanel;
	
	private JCheckBox uidCheck;
	private JCheckBox dateCheck;
	private JCheckBox freqCheck;
	private JCheckBox durCheck;
	private JCheckBox ampCheck;
	private JCheckBox speciesCheck;
	private JCheckBox classCheck;
	private JCheckBox commentCheck;
	
	private JTextField uidField1;
	private JTextField uidField2;
	
	private JTextField yearField1; //size 4
	private JTextField monthField1; //size 2
	private JTextField dayField1; //size 2
	private JTextField hourField1; //size 2
	private JTextField minuteField1; //size 2
	private JTextField secondField1; //size 2
	private JTextField msField1; //size 3
	private JTextField yearField2; //size 4
	private JTextField monthField2; //size 2
	private JTextField dayField2; //size 2
	private JTextField hourField2; //size 2
	private JTextField minuteField2; //size 2
	private JTextField secondField2; //size 2
	private JTextField msField2; //size 3
	
	private JTextField lfField1;
	private JTextField lfField2;
	private JTextField hfField1;
	private JTextField hfField2;
	
	private JTextField durField1;
	private JTextField durField2;
	
	private JTextField ampField1;
	private JTextField ampField2;
	
	private JComboBox<String> speciesBox;
	
	private JComboBox<String> classBox;
	
	private JTextField commentField;
	
	private PamTable outpTable;
	
	private int scrollint;
	
	public WMATSearchDialog(Window parentFrame, WMATControl wmatControl, PamTable ttable) {
		super(parentFrame, "Selection by search", true);
		this.wmatControl = wmatControl;
		this.outpTable = ttable;
		
		this.getDefaultButton().setVisible(false);
		
		scrollint = -1;
		
		mainPanel = new PamPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		PamPanel fpanel1 = new PamPanel(new FlowLayout(FlowLayout.LEFT));
		PamPanel panel1 = new PamPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		fpanel1.setBorder(new TitledBorder("Search by UID"));
		uidCheck = new JCheckBox();
		uidCheck.addItemListener(new CheckListener(1));
		uidField1 = new JTextField(20);
		uidField2 = new JTextField(20);
		uidField1.setHorizontalAlignment(SwingConstants.RIGHT);
		uidField2.setHorizontalAlignment(SwingConstants.RIGHT);
		uidField1.setDocument(JNumFilter());
		uidField2.setDocument(JNumFilter());
		panel1.add(uidCheck, c);
		c.gridx++;
		panel1.add(uidField1, c);
		c.gridx++;
		panel1.add(new JLabel(" - "), c);
		c.gridx++;
		panel1.add(uidField2, c);
		fillUIDs(ttable);
		fpanel1.add(panel1);
		mainPanel.add(fpanel1);
		
		PamPanel fpanel2 = new PamPanel(new FlowLayout(FlowLayout.LEFT));
		PamPanel panel2 = new PamPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		fpanel2.setBorder(new TitledBorder("Search by date"));
		dateCheck = new JCheckBox();
		dateCheck.addItemListener(new CheckListener(2));
		yearField1 = new JTextField(4);
		monthField1 = new JTextField(2);
		dayField1 = new JTextField(2);
		hourField1 = new JTextField(2);
		minuteField1 = new JTextField(2);
		secondField1 = new JTextField(2);
		msField1 = new JTextField(3);
		yearField2 = new JTextField(4);
		monthField2 = new JTextField(2);
		dayField2 = new JTextField(2);
		hourField2 = new JTextField(2);
		minuteField2 = new JTextField(2);
		secondField2 = new JTextField(2);
		msField2 = new JTextField(3);
		yearField1.setHorizontalAlignment(SwingConstants.RIGHT);
		monthField1.setHorizontalAlignment(SwingConstants.RIGHT);
		dayField1.setHorizontalAlignment(SwingConstants.RIGHT);
		hourField1.setHorizontalAlignment(SwingConstants.RIGHT);
		minuteField1.setHorizontalAlignment(SwingConstants.RIGHT);
		secondField1.setHorizontalAlignment(SwingConstants.RIGHT);
		msField1.setHorizontalAlignment(SwingConstants.RIGHT);
		yearField2.setHorizontalAlignment(SwingConstants.RIGHT);
		monthField2.setHorizontalAlignment(SwingConstants.RIGHT);
		dayField2.setHorizontalAlignment(SwingConstants.RIGHT);
		hourField2.setHorizontalAlignment(SwingConstants.RIGHT);
		minuteField2.setHorizontalAlignment(SwingConstants.RIGHT);
		secondField2.setHorizontalAlignment(SwingConstants.RIGHT);
		msField2.setHorizontalAlignment(SwingConstants.RIGHT);
		yearField1.setDocument(new JTextFieldLimit(4).getDocument());
		monthField1.setDocument(new JTextFieldLimit(2).getDocument());
		dayField1.setDocument(new JTextFieldLimit(2).getDocument());
		hourField1.setDocument(new JTextFieldLimit(2).getDocument());
		minuteField1.setDocument(new JTextFieldLimit(2).getDocument());
		secondField1.setDocument(new JTextFieldLimit(2).getDocument());
		msField1.setDocument(new JTextFieldLimit(3).getDocument());
		yearField2.setDocument(new JTextFieldLimit(4).getDocument());
		monthField2.setDocument(new JTextFieldLimit(2).getDocument());
		dayField2.setDocument(new JTextFieldLimit(2).getDocument());
		hourField2.setDocument(new JTextFieldLimit(2).getDocument());
		minuteField2.setDocument(new JTextFieldLimit(2).getDocument());
		secondField2.setDocument(new JTextFieldLimit(2).getDocument());
		msField2.setDocument(new JTextFieldLimit(3).getDocument());
		panel2.add(dateCheck, c);
		c.gridx++;
		panel2.add(yearField1, c);
		c.gridx++;
		panel2.add(new JLabel("-"), c);
		c.gridx++;
		panel2.add(monthField1, c);
		c.gridx++;
		panel2.add(new JLabel("-"), c);
		c.gridx++;
		panel2.add(dayField1, c);
		c.gridx++;
		panel2.add(new JLabel(" "), c);
		c.gridx++;
		panel2.add(hourField1, c);
		c.gridx++;
		panel2.add(new JLabel(":"), c);
		c.gridx++;
		panel2.add(minuteField1, c);
		c.gridx++;
		panel2.add(new JLabel(":"), c);
		c.gridx++;
		panel2.add(secondField1, c);
		c.gridx++;
		panel2.add(new JLabel("+"), c);
		c.gridx++;
		panel2.add(msField1, c);
		c.gridy++;
		c.gridx = 1;
		panel2.add(yearField2, c);
		c.gridx++;
		panel2.add(new JLabel("-"), c);
		c.gridx++;
		panel2.add(monthField2, c);
		c.gridx++;
		panel2.add(new JLabel("-"), c);
		c.gridx++;
		panel2.add(dayField2, c);
		c.gridx++;
		panel2.add(new JLabel(" "), c);
		c.gridx++;
		panel2.add(hourField2, c);
		c.gridx++;
		panel2.add(new JLabel(":"), c);
		c.gridx++;
		panel2.add(minuteField2, c);
		c.gridx++;
		panel2.add(new JLabel(":"), c);
		c.gridx++;
		panel2.add(secondField2, c);
		c.gridx++;
		panel2.add(new JLabel("+"), c);
		c.gridx++;
		panel2.add(msField2, c);
		fillDates(ttable);
		fpanel2.add(panel2);
		mainPanel.add(fpanel2);
		
		PamPanel fpanel3 = new PamPanel(new FlowLayout(FlowLayout.LEFT));
		PamPanel panel3 = new PamPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		fpanel3.setBorder(new TitledBorder("Search by frequency"));
		freqCheck = new JCheckBox();
		freqCheck.addItemListener(new CheckListener(3));
		lfField1 = new JTextField(6);
		lfField2 = new JTextField(6);
		hfField1 = new JTextField(6);
		hfField2 = new JTextField(6);
		lfField1.setHorizontalAlignment(SwingConstants.RIGHT);
		lfField2.setHorizontalAlignment(SwingConstants.RIGHT);
		hfField1.setHorizontalAlignment(SwingConstants.RIGHT);
		hfField2.setHorizontalAlignment(SwingConstants.RIGHT);
		lfField1.setDocument(JNumFilter());
		lfField2.setDocument(JNumFilter());
		hfField1.setDocument(JNumFilter());
		hfField2.setDocument(JNumFilter());
		panel3.add(freqCheck, c);
		c.gridx++;
		panel3.add(new JLabel("Low: "), c);
		c.gridx++;
		panel3.add(lfField1, c);
		c.gridx++;
		panel3.add(new JLabel(" - "), c);
		c.gridx++;
		panel3.add(lfField2, c);
		c.gridy++;
		c.gridx = 1;
		panel3.add(new JLabel("High: "), c);
		c.gridx++;
		panel3.add(hfField1, c);
		c.gridx++;
		panel3.add(new JLabel(" - "), c);
		c.gridx++;
		panel3.add(hfField2, c);
		fillFreqs(ttable);
		fpanel3.add(panel3);
		mainPanel.add(fpanel3);
		
		PamPanel fpanel4 = new PamPanel(new FlowLayout(FlowLayout.LEFT));
		PamPanel panel4 = new PamPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		fpanel4.setBorder(new TitledBorder("Search by duration"));
		durCheck = new JCheckBox();
		durCheck.addItemListener(new CheckListener(4));
		durField1 = new JTextField(6);
		durField2 = new JTextField(6);
		durField1.setHorizontalAlignment(SwingConstants.RIGHT);
		durField2.setHorizontalAlignment(SwingConstants.RIGHT);
		panel4.add(durCheck, c);
		c.gridx++;
		panel4.add(durField1, c);
		c.gridx++;
		panel4.add(new JLabel(" - "), c);
		c.gridx++;
		panel4.add(durField2, c);
		fillDurs(ttable);
		fpanel4.add(panel4);
		mainPanel.add(fpanel4);
		
		PamPanel fpanel5 = new PamPanel(new FlowLayout(FlowLayout.LEFT));
		PamPanel panel5 = new PamPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		fpanel5.setBorder(new TitledBorder("Search by amplitude"));
		ampCheck = new JCheckBox();
		ampCheck.addItemListener(new CheckListener(5));
		ampField1 = new JTextField(6);
		ampField2 = new JTextField(6);
		ampField1.setHorizontalAlignment(SwingConstants.RIGHT);
		ampField2.setHorizontalAlignment(SwingConstants.RIGHT);
		panel5.add(ampCheck, c);
		c.gridx++;
		panel5.add(ampField1, c);
		c.gridx++;
		panel5.add(new JLabel(" - "), c);
		c.gridx++;
		panel5.add(ampField2, c);
		fillAmps(ttable);
		fpanel5.add(panel5);
		mainPanel.add(fpanel5);
		
		PamPanel fpanel6 = new PamPanel(new FlowLayout(FlowLayout.LEFT));
		PamPanel panel6 = new PamPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		fpanel6.setBorder(new TitledBorder("Search by species"));
		speciesCheck = new JCheckBox();
		speciesCheck.addItemListener(new CheckListener(6));
		speciesBox = new JComboBox<String>();
		speciesBox.setPrototypeDisplayValue("aaaaaaaaaaaaaaaaaaaa");
		panel6.add(speciesCheck, c);
		c.gridx++;
		panel6.add(speciesBox, c);
		fillSpecies(ttable);
		fpanel6.add(panel6);
		mainPanel.add(fpanel6);
		
		PamPanel fpanel7 = new PamPanel(new FlowLayout(FlowLayout.LEFT));
		PamPanel panel7 = new PamPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		fpanel7.setBorder(new TitledBorder("Search by call type"));
		classCheck = new JCheckBox();
		classCheck.addItemListener(new CheckListener(7));
		classBox = new JComboBox<String>();
		classBox.setPrototypeDisplayValue("aaaaaaaaaaaaaaaaaaaa");
		panel7.add(classCheck, c);
		c.gridx++;
		panel7.add(classBox, c);
		fillClass(ttable);
		fpanel7.add(panel7);
		mainPanel.add(fpanel7);
		
		PamPanel fpanel8 = new PamPanel(new FlowLayout(FlowLayout.LEFT));
		PamPanel panel8 = new PamPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		fpanel8.setBorder(new TitledBorder("Search by comment"));
		commentCheck = new JCheckBox();
		commentCheck.addItemListener(new CheckListener(8));
		commentField = new JTextField(25);
		panel8.add(commentCheck, c);
		c.gridx++;
		panel8.add(commentField, c);
		fpanel8.add(panel8);
		mainPanel.add(fpanel8);
		
		onSwitch(0, false);
		
		this.add(mainPanel);
		this.pack();
		
		
	}
	
	/**
	 * Sets a limit to the number of characters allowed in a JTextField.
	 * Used with JTextField.setDocument(new JTextFieldLimit(int limit).getDocument());
	 * Copied from https://stackoverflow.com/questions/3519151/how-to-limit-the-number-of-characters-in-jtextfield
	 * Author page: https://stackoverflow.com/users/1866109/francisco-j-g%c3%bcemes-sevilla
	 */
	public class JTextFieldLimit extends JTextField {
	    private int limit;

	    public JTextFieldLimit(int limit) {
	        super();
	        this.limit = limit;
	    }
	    @Override
	    protected Document createDefaultModel() {
	        return new LimitDocument();
	    }
	    private class LimitDocument extends PlainDocument {

	        @Override
	        public void insertString( int offset, String  str, AttributeSet attr ) throws BadLocationException {
	            if (str == null) return;

	            if ((getLength() + str.length()) <= limit) {
	                super.insertString(offset, str, attr);
	            }
	        }       
	    }
	}
	
	/**
	 * PlainDocument that filters out non-numeric characters being typed in.
	 * Based off of this: https://stackoverflow.com/questions/11890774/how-to-use-a-filter-for-textfields-in-java-swing.
	 * Author page: https://stackoverflow.com/users/883780/aymeric
	 * @return PlainDocument
	 */
	public PlainDocument JNumFilter() {
		PlainDocument d = new PlainDocument() {
			@Override
	        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
	            char c = str.charAt(0);
	            if (getLength() == 0) {
	            	if (c == '-' || (c >= '0' && c <= '9')) {
		                super.insertString(offs, str, a);
		            }
	            } else {
	            	if (c >= '0' && c <= '9') {
		                super.insertString(offs, str, a);
		            }
	            }
	        }
		};
		return d;
	}
	
	/**
	 * Enables text fields and drop-down lists in a panel when its box is checked.
	 * @param panelNum - Which panel is being enabled/disabled. (int)
	 * @param boo - Enable or disable. (boolean)
	 */
	protected void onSwitch(int panelNum, boolean boo) {
		if (panelNum == 0 || panelNum == 1) {
			uidField1.setEnabled(boo);
			uidField2.setEnabled(boo);
		}
		if (panelNum == 0 || panelNum == 2) {
			yearField1.setEnabled(boo);
			monthField1.setEnabled(boo);
			dayField1.setEnabled(boo);
			hourField1.setEnabled(boo);
			minuteField1.setEnabled(boo);
			secondField1.setEnabled(boo);
			msField1.setEnabled(boo);
			yearField2.setEnabled(boo);
			monthField2.setEnabled(boo);
			dayField2.setEnabled(boo);
			hourField2.setEnabled(boo);
			minuteField2.setEnabled(boo);
			secondField2.setEnabled(boo);
			msField2.setEnabled(boo);
		}
		if (panelNum == 0 || panelNum == 3) {
			lfField1.setEnabled(boo);
			lfField2.setEnabled(boo);
			hfField1.setEnabled(boo);
			hfField2.setEnabled(boo);
		}
		if (panelNum == 0 || panelNum == 4) {
			durField1.setEnabled(boo);
			durField2.setEnabled(boo);
		}
		if (panelNum == 0 || panelNum == 5) {
			ampField1.setEnabled(boo);
			ampField2.setEnabled(boo);
		}
		if (panelNum == 0 || panelNum == 6) {
			speciesBox.setEnabled(boo);
		}
		if (panelNum == 0 || panelNum == 7) {
			classBox.setEnabled(boo);
		}
		if (panelNum == 0 || panelNum == 8) {
			commentField.setEnabled(boo);
		}
	}
	
	/**
	 * Automatically fills the UID fields with the lowest and highest UIDs in the table, respectively.
	 * @param ttable
	 */
	protected void fillUIDs(PamTable ttable) {
		if (ttable.getRowCount() <= 0) {
			System.out.println("HARP SEAL A");
			return;
		}
		long startUID = Long.valueOf(ttable.getValueAt(0, 0).toString());
		long endUID = Long.valueOf(ttable.getValueAt(0, 0).toString());
		for (int i = 1; i < ttable.getRowCount(); i++) {
			long uid1 = Long.valueOf(ttable.getValueAt(i, 0).toString());
			if (uid1 < startUID) {
				startUID = uid1;
			}
			if (uid1 > endUID) {
				endUID = uid1;
			}
		}
		uidField1.setText(Long.toString(startUID));
		uidField2.setText(Long.toString(endUID));
	}
	
	/**
	 * Automatically fills the date/time fields with the earliest and latest times, respectively.
	 * @param ttable
	 */
	protected void fillDates(PamTable ttable) {
		if (ttable.getRowCount() <= 0) {
			System.out.println("SEA LION A");
			return;
		}
		String startDate = ttable.getValueAt(0, 1).toString();
		String endDate = ttable.getValueAt(0, 1).toString();
		if (startDate.length() != 23 || endDate.length() != 23) {
			System.out.println("SEA LION B");
			return;
		}
		for (int i = 1; i < ttable.getRowCount(); i++) {
			String s1 = ttable.getValueAt(i, 1).toString();
			if (s1.length() != 23) {
				System.out.println("SEA LION C");
				return;
			}
			if (s1.compareTo(startDate) < 0) {
				startDate = s1;
			}
			if (s1.compareTo(endDate) > 0) {
				endDate = s1;
			}
		}
		yearField1.setText(startDate.substring(0, 4));
		monthField1.setText(startDate.substring(5, 7));
		dayField1.setText(startDate.substring(8, 10));
		hourField1.setText(startDate.substring(11, 13));
		minuteField1.setText(startDate.substring(14, 16));
		secondField1.setText(startDate.substring(17, 19));
		msField1.setText(startDate.substring(20, 23));
		yearField2.setText(endDate.substring(0, 4));
		monthField2.setText(endDate.substring(5, 7));
		dayField2.setText(endDate.substring(8, 10));
		hourField2.setText(endDate.substring(11, 13));
		minuteField2.setText(endDate.substring(14, 16));
		secondField2.setText(endDate.substring(17, 19));
		msField2.setText(endDate.substring(20, 23));
	}
	
	/**
	 * Automatically fills the low and high frequency fields with the lowest and highest instances in the table.
	 * @param ttable
	 */
	protected void fillFreqs(PamTable ttable) {
		if (ttable.getRowCount() <= 0) {
			System.out.println("RING SEAL A");
			return;
		}
		int lowLF = Integer.valueOf(ttable.getValueAt(0, 2).toString());
		int highLF = Integer.valueOf(ttable.getValueAt(0, 2).toString());
		int lowHF = Integer.valueOf(ttable.getValueAt(0, 3).toString());
		int highHF = Integer.valueOf(ttable.getValueAt(0, 3).toString());
		for (int i = 1; i < ttable.getRowCount(); i++) {
			int lf1 = Integer.valueOf(ttable.getValueAt(i, 2).toString());
			if (lf1 < lowLF) {
				lowLF = lf1;
			}
			if (lf1 > highLF) {
				highLF = lf1;
			}
			int hf1 = Integer.valueOf(ttable.getValueAt(i, 3).toString());
			if (hf1 < lowHF) {
				lowHF = hf1;
			}
			if (hf1 > highHF) {
				highHF = hf1;
			}
		}
		lfField1.setText(Integer.toString(lowLF));
		lfField2.setText(Integer.toString(highLF));
		hfField1.setText(Integer.toString(lowHF));
		hfField2.setText(Integer.toString(highHF));
	}
	
	/**
	 * Automatically fills the low and high duration fields with the lowest and highest instances in the table.
	 * @param ttable
	 */
	protected void fillDurs(PamTable ttable) {
		if (ttable.getRowCount() <= 0) {
			System.out.println("RING SEAL B");
			return;
		}
		int lowDur = Integer.valueOf(ttable.getValueAt(0, 4).toString());
		int highDur = Integer.valueOf(ttable.getValueAt(0, 4).toString());
		for (int i = 1; i < ttable.getRowCount(); i++) {
			int dur1 = Integer.valueOf(ttable.getValueAt(i, 4).toString());
			if (dur1 < lowDur) {
				lowDur = dur1;
			}
			if (dur1 > highDur) {
				highDur = dur1;
			}
		}
		durField1.setText(Integer.toString(lowDur));
		durField2.setText(Integer.toString(highDur));
	}
	
	/**
	 * Automatically fills the low and high amplitude fields with the lowest and highest instances in the table.
	 * @param ttable
	 */
	protected void fillAmps(PamTable ttable) {
		if (ttable.getRowCount() <= 0) {
			System.out.println("RING SEAL C");
			return;
		}
		int lowAmp = Integer.valueOf(ttable.getValueAt(0, 5).toString());
		int highAmp = Integer.valueOf(ttable.getValueAt(0, 5).toString());
		for (int i = 1; i < ttable.getRowCount(); i++) {
			int amp1 = Integer.valueOf(ttable.getValueAt(i, 5).toString());
			if (amp1 < lowAmp) {
				lowAmp = amp1;
			}
			if (amp1 > highAmp) {
				highAmp = amp1;
			}
		}
		ampField1.setText(Integer.toString(lowAmp));
		ampField2.setText(Integer.toString(highAmp));
	}
	
	/**
	 * Fills the species drop-down list with values that exist in the table.
	 * @param ttable
	 */
	protected void fillSpecies(PamTable ttable) {
		if (ttable.getRowCount() <= 0) {
			System.out.println("SEA OTTER A");
			return;
		}
		speciesBox.removeAllItems();
		List<String> slist = new ArrayList<String>();
		for (int i = 0; i < ttable.getRowCount(); i++) {
			String s1 = ttable.getValueAt(i, 6).toString();
			if (!(slist.contains(s1))) {
				slist.add(s1);
				speciesBox.addItem(s1);
			}
		}
	}
	
	/**
	 * Fills the call type drop-down list with values that exist in the table.
	 * @param ttable
	 */
	protected void fillClass(PamTable ttable) {
		if (ttable.getRowCount() <= 0) {
			System.out.println("ELEPHANT SEAL A");
			return;
		}
		classBox.removeAllItems();
		List<String> slist = new ArrayList<String>();
		for (int i = 0; i < ttable.getRowCount(); i++) {
			String s1 = ttable.getValueAt(i, 7).toString();
			if (!(slist.contains(s1))) {
				slist.add(s1);
				classBox.addItem(s1);
			}
		}
	}
	
	/**
	 * The listener for each checkbox.
	 */
	class CheckListener implements ItemListener{
		private int panelNum;
		
		public CheckListener(int panelNum) {
			this.panelNum = panelNum;
		}
		
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange() == ItemEvent.SELECTED) {
	            onSwitch(panelNum, true);
	        } else {
	        	onSwitch(panelNum, false);
	        }
		}
	}
	
	/**
	 * Performs the search and returns the indexes to select.
	 * @param ttable
	 * @return Array of integers being the indexes in the table for selecting.
	 */
	public int[] performSearch(PamTable ttable) {
		int[] selectArr = new int[ttable.getRowCount()];
		if (selectArr.length <= 0) {
			System.out.println("BLUE WHALE A");
			return selectArr;
		}
		if (!(uidCheck.isSelected() || dateCheck.isSelected() || freqCheck.isSelected() || durCheck.isSelected() || ampCheck.isSelected() ||
				speciesCheck.isSelected() || classCheck.isSelected() || commentCheck.isSelected())) {
			for (int i = 0; i < selectArr.length; i++) {
				selectArr[i] = 0;
			}
			return selectArr;
		}
		for (int i = 0; i < selectArr.length; i++) {
			selectArr[i] = 1;
		}
		String uids1 = uidField1.getText();
		String uids2 = uidField2.getText();
		long uid1 = 0;
		long uid2 = 0;
		if (!(uids1.equals("-") || uids1.length() == 0)) {
			uid1 = Long.valueOf(uids1);
		}
		if (!(uids2.equals("-") || uids2.length() == 0)) {
			uid2 = Long.valueOf(uids2);
		}
		String[] dates = fillOutDateString();
		String date1 = dates[0];
		String date2 = dates[1];
		String lfs1 = lfField1.getText();
		String lfs2 = lfField2.getText();
		String hfs1 = hfField1.getText();
		String hfs2 = hfField2.getText();
		String durs1 = durField1.getText();
		String durs2 = durField2.getText();
		String amps1 = ampField1.getText();
		String amps2 = ampField2.getText();
		int lf1 = 0;
		int lf2 = 0;
		int hf1 = 0;
		int hf2 = 0;
		int dur1 = 0;
		int dur2 = 0;
		int amp1 = 0;
		int amp2 = 0;
		if (!(lfs1.equals("-") || lfs1.length() == 0)) {
			lf1 = Integer.valueOf(lfs1);
		}
		if (!(lfs2.equals("-") || lfs2.length() == 0)) {
			lf2 = Integer.valueOf(lfs2);
		}
		if (!(hfs1.equals("-") || hfs1.length() == 0)) {
			hf1 = Integer.valueOf(hfs1);
		}
		if (!(hfs2.equals("-") || hfs2.length() == 0)) {
			hf2 = Integer.valueOf(hfs2);
		}
		if (!(durs1.equals("-") || durs1.length() == 0)) {
			dur1 = Integer.valueOf(durs1);
		}
		if (!(durs2.equals("-") || durs2.length() == 0)) {
			dur2 = Integer.valueOf(durs2);
		}
		if (!(amps1.equals("-") || amps1.length() == 0)) {
			amp1 = Integer.valueOf(amps1);
		}
		if (!(amps2.equals("-") || amps2.length() == 0)) {
			amp2 = Integer.valueOf(amps2);
		}
		for (int i = 0; i < selectArr.length; i++) {
			if (uidCheck.isSelected()) {
				long uid0 = Long.valueOf(ttable.getValueAt(i, 0).toString());
				if (uids1.length() != 0 && uids2.length() != 0) {
					if (uid0 < uid1 || uid0 > uid2) {
						selectArr[i] = 0;
					}
				} else if (uids1.length() != 0) {
					if (uid0 < uid1) {
						selectArr[i] = 0;
					}
				} else if (uids2.length() != 0) {
					if (uid0 > uid2) {
						selectArr[i] = 0;
					}
				}
			}
			if (dateCheck.isSelected()) {
				String date0 = ttable.getValueAt(i, 1).toString();
				if (date0.compareTo(date1) < 0 || date0.compareTo(date2) > 0) {
					selectArr[i] = 0;
				}
			}
			if (freqCheck.isSelected()) {
				int lf0 = Integer.valueOf(ttable.getValueAt(i, 2).toString());
				if (lfs1.length() != 0 && lfs2.length() != 0) {
					if (lf0 < lf1 || lf0 > lf2) {
						selectArr[i] = 0;
					}
				} else if (lfs1.length() != 0) {
					if (lf0 < lf1) {
						selectArr[i] = 0;
					}
				} else if (lfs2.length() != 0) {
					if (lf0 > lf2) {
						selectArr[i] = 0;
					}
				}
				int hf0 = Integer.valueOf(ttable.getValueAt(i, 3).toString());
				if (hfs1.length() != 0 && hfs2.length() != 0) {
					if (hf0 < hf1 || hf0 > hf2) {
						selectArr[i] = 0;
					}
				} else if (hfs1.length() != 0) {
					if (hf0 < hf1) {
						selectArr[i] = 0;
					}
				} else if (hfs2.length() != 0) {
					if (hf0 > hf2) {
						selectArr[i] = 0;
					}
				}
			}
			if (durCheck.isSelected()) {
				int dur0 = Integer.valueOf(ttable.getValueAt(i, 4).toString());
				if (durs1.length() != 0 && durs2.length() != 0) {
					if (dur0 < dur1 || dur0 > dur2) {
						selectArr[i] = 0;
					}
				} else if (durs1.length() != 0) {
					if (dur0 < dur1) {
						selectArr[i] = 0;
					}
				} else if (durs2.length() != 0) {
					if (dur0 > dur2) {
						selectArr[i] = 0;
					}
				}
			}
			if (ampCheck.isSelected()) {
				int amp0 = Integer.valueOf(ttable.getValueAt(i, 5).toString());
				if (amps1.length() != 0 && amps2.length() != 0) {
					if (amp0 < amp1 || amp0 > amp2) {
						selectArr[i] = 0;
					}
				} else if (amps1.length() != 0) {
					if (amp0 < amp1) {
						selectArr[i] = 0;
					}
				} else if (amps2.length() != 0) {
					if (amp0 > amp2) {
						selectArr[i] = 0;
					}
				}
			}
			if (speciesCheck.isSelected()) {
				if (!(ttable.getValueAt(i, 6).toString().equals(speciesBox.getSelectedItem().toString()))) {
					selectArr[i] = 0;
				}
			}
			if (classCheck.isSelected()) {
				if (!(ttable.getValueAt(i, 7).toString().equals(classBox.getSelectedItem().toString()))) {
					selectArr[i] = 0;
				}
			}
			if (commentCheck.isSelected()) {
				if (!(ttable.getValueAt(i, 8).toString().contains(commentField.getText()))) {
					selectArr[i] = 0;
				}
			}
		}
		return selectArr;
	}
	
	/**
	 * Makes the selections in the table following results from performSearch.
	 * @param selections
	 * @param ttable
	 */
	public void confirmSearch(int[] selections, PamTable ttable) {
		ttable.clearSelection();
		for (int i = 0; i < ttable.getRowCount(); i++) {
			if (selections[i] == 1) {
				if (scrollint == -1) {
					scrollint = i;
				}
				ttable.addRowSelectionInterval(i, i);
			}
		}
	}
	/**
	 * Formats the input into the date/time fields as parsable strings.
	 * @return String[] - Array containing the Strings for the lower limit and the higher limit.
	 */
	public String[] fillOutDateString() {
		String s1 = "";
		s1 = s1 + ("0000" + yearField1.getText()).substring(yearField1.getText().length()) + "-";
		s1 = s1 + ("00" + monthField1.getText()).substring(monthField1.getText().length()) + "-";
		s1 = s1 + ("00" + dayField1.getText()).substring(dayField1.getText().length()) + " ";
		s1 = s1 + ("00" + hourField1.getText()).substring(hourField1.getText().length()) + ":";
		s1 = s1 + ("00" + minuteField1.getText()).substring(minuteField1.getText().length()) + ":";
		s1 = s1 + ("00" + secondField1.getText()).substring(secondField1.getText().length()) + "+";
		s1 = s1 + ("000" + msField1.getText()).substring(msField1.getText().length());
		String s2 = "";
		s2 = s2 + ("0000" + yearField2.getText()).substring(yearField2.getText().length()) + "-";
		s2 = s2 + ("00" + monthField2.getText()).substring(monthField2.getText().length()) + "-";
		s2 = s2 + ("00" + dayField2.getText()).substring(dayField2.getText().length()) + " ";
		s2 = s2 + ("00" + hourField2.getText()).substring(hourField2.getText().length()) + ":";
		s2 = s2 + ("00" + minuteField2.getText()).substring(minuteField2.getText().length()) + ":";
		s2 = s2 + ("00" + secondField2.getText()).substring(secondField2.getText().length()) + "+";
		s2 = s2 + ("000" + msField2.getText()).substring(msField2.getText().length());
		String[] outp = {s1, s2};
		return outp;
	}
	
	@Override
	public boolean getParams() {
		confirmSearch(performSearch(outpTable), outpTable);
		if (scrollint != -1) {
			outpTable.scrollRectToVisible(outpTable.getCellRect(outpTable.getSelectedRowCount()-1, 0, true));
			outpTable.scrollRectToVisible(outpTable.getCellRect(scrollint, 0, true));
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		
	}

	@Override
	public void restoreDefaultSettings() {
		
	}
}