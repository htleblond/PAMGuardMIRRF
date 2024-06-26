package wmnt;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import PamView.dialog.PamButton;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import whistlesAndMoans.ConnectedRegionDataUnit;
import wmnt.WMNTPanel.SpeciesListener;

/**
 * The settings dialog for the WMNT.
 * @author Holly LeBlond
 */
public class WMNTSettingsDialog extends PamDialog {
	
	protected SourcePanel inputSourcePanel;
	protected JTextField speciesField;
	protected DefaultComboBoxModel<String> speciesModel;
	protected JComboBox<String> speciesBox;
	protected JTextField callTypeField;
	protected DefaultComboBoxModel<String> callTypeModel;
	protected JComboBox<String> callTypeBox;
	protected JTextField intervalField;
	protected JTextField scrollBufferField;
	//protected WMNTTimeZonePanel tzPanel;
	protected JCheckBox binaryTZCheck;
	protected JCheckBox databaseTZCheck;
	protected JTextField sqlTableField;
	
	protected WMNTControl wmntControl;
	protected Window parentFrame;
	protected boolean startup;
	
	public WMNTSettingsDialog(Window parentFrame, WMNTControl wmntControl, boolean startup) {
		super(parentFrame, wmntControl.getUnitName(), true);
		this.wmntControl = wmntControl;
		this.parentFrame = parentFrame;
		this.startup = startup;
		
		JTabbedPane tabbedPane = new JTabbedPane();
		
		JPanel p = new JPanel(new BorderLayout());
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints b = new PamGridBagContraints();
		b.gridy = 0;
		b.gridx = 0;
		b.fill = GridBagConstraints.HORIZONTAL;
		b.anchor = GridBagConstraints.WEST;
		
		inputSourcePanel = new SourcePanel(this, "Contour data source (for slice data)", ConnectedRegionDataUnit.class, false, true);
		mainPanel.add(inputSourcePanel.getPanel(), b);
		b.gridy++;
		
		JPanel speciesPanel = new JPanel(new GridBagLayout());
		speciesPanel.setBorder(new TitledBorder("'Species' list"));
		speciesPanel.setAlignmentX(LEFT_ALIGNMENT);
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		speciesField = new JTextField();
		speciesField.setDocument(new JTextFieldLimit(20).getDocument());
		SpeciesAddListener speciesAddListener = new SpeciesAddListener();
		speciesField.addActionListener(speciesAddListener);
		speciesPanel.add(speciesField, c);
		c.gridx++;
		PamButton speciesAddButton = new PamButton("Add");
		speciesAddButton.addActionListener(speciesAddListener);
		speciesPanel.add(speciesAddButton, c);
		c.gridy++;
		c.gridx = 0;
		speciesModel = new DefaultComboBoxModel<String>();
		for (int i = 1; i < wmntControl.getParams().speciesList.size(); i++) {
			speciesModel.addElement(wmntControl.getParams().speciesList.get(i));
		}
		speciesBox = new JComboBox<String>(speciesModel);
		speciesBox.setPrototypeDisplayValue("aaaaaaaaaaaaaaaaaaaaaaaaa");
		speciesPanel.add(speciesBox, c);
		c.gridx++;
		PamButton speciesRemoveButton = new PamButton("Remove");
		SpeciesRemoveListener speciesRemoveListener = new SpeciesRemoveListener();
		speciesRemoveButton.addActionListener(speciesRemoveListener);
		speciesPanel.add(speciesRemoveButton, c);
		mainPanel.add(speciesPanel, b);
		
		b.gridy++;
		JPanel callTypePanel = new JPanel(new GridBagLayout());
		callTypePanel.setBorder(new TitledBorder("'Call type' list"));
		c = new PamGridBagContraints();
		c.gridy = 0;
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		callTypeField = new JTextField();
		callTypeField.setDocument(new JTextFieldLimit(20).getDocument());
		CallTypeAddListener callTypeAddListener = new CallTypeAddListener();
		callTypeField.addActionListener(callTypeAddListener);
		callTypePanel.add(callTypeField, c);
		c.gridx++;
		PamButton callTypeAddButton = new PamButton("Add");
		callTypeAddButton.addActionListener(callTypeAddListener);
		callTypePanel.add(callTypeAddButton, c);
		c.gridy++;
		c.gridx = 0;
		callTypeModel = new DefaultComboBoxModel<String>();
		for (int i = 1; i < wmntControl.getParams().callTypeList.size(); i++) {
			callTypeModel.addElement(wmntControl.getParams().callTypeList.get(i));
		}
		callTypeBox = new JComboBox<String>(callTypeModel);
		callTypeBox.setPrototypeDisplayValue("aaaaaaaaaaaaaaaaaaaaaaaaa");
		callTypePanel.add(callTypeBox, c);
		c.gridx++;
		PamButton callTypeRemoveButton = new PamButton("Remove");
		CallTypeRemoveListener callTypeRemoveListener = new CallTypeRemoveListener();
		callTypeRemoveButton.addActionListener(callTypeRemoveListener);
		callTypePanel.add(callTypeRemoveButton, c);
		mainPanel.add(callTypePanel, b);
		
		b.gridy++;
		JPanel intervalPanel = new JPanel(new GridBagLayout());
		intervalPanel.setBorder(new TitledBorder("Button parameters"));
		c = new PamGridBagContraints();
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 2;
		String intervalText = "Everything within the specified time interval after the start of each file is selected when "
				+ "'Select within start interval' is pressed.";
		String html = "<html><body style='width: %1spx'>%1s";
		intervalPanel.add(new JLabel(String.format(html, 250, intervalText)), c);
		c.gridy++;
		c.gridwidth = 1;
		intervalPanel.add(new JLabel("Interval (in milliseconds): "), c);
		c.gridx++;
		c.fill = GridBagConstraints.NONE;
		intervalField = new JTextField(5);
		intervalField.setDocument(JNumFilter());
		intervalField.setText(String.valueOf(wmntControl.getParams().startBuffer));
		intervalPanel.add(intervalField, c);
		c.gridy++;
		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 2;
		String scrollBufferText = "When 'Scroll to selection on spectrogram' is pressed, the spectrogram scrolls to the time "
				+ "stamp of the selected contour minus the specified buffer length.";
		intervalPanel.add(new JLabel(String.format(html, 250, scrollBufferText)), c);
		c.gridy++;
		c.gridwidth = 1;
		intervalPanel.add(new JLabel("Buffer (in milliseconds): "), c);
		c.gridx++;
		c.fill = GridBagConstraints.NONE;
		scrollBufferField = new JTextField(5);
		scrollBufferField.setDocument(JNumFilter());
		scrollBufferField.setText(String.valueOf(wmntControl.getParams().scrollBuffer));
		intervalPanel.add(scrollBufferField, c);
		mainPanel.add(intervalPanel, b);
		
		b.gridy++;
	/*	tzPanel = new WMNTTimeZonePanel(true, true, true, true);
		tzPanel.setAudioTimeZone(wmntControl.getParams().audioTZ);
		tzPanel.setBinaryTimeZone(wmntControl.getParams().binaryTZ);
		tzPanel.setDatabaseTimeZone(wmntControl.getParams().databaseTZ); */
		JPanel tzPanel = new JPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		c.anchor = c.WEST;
		c.fill = c.HORIZONTAL;
		tzPanel.setBorder(new TitledBorder("Time zone conversion"));
		tzPanel.add(new JLabel("Convert from system time zone to UTC:"), c);
		c.gridy++;
		tzPanel.add(binaryTZCheck = new JCheckBox("Binary file time stamps"), c);
		binaryTZCheck.setSelected(wmntControl.getParams().binaryIsInLocalTime);
		c.gridy++;
		tzPanel.add(databaseTZCheck = new JCheckBox("Database UTC column"), c);
		databaseTZCheck.setSelected(wmntControl.getParams().databaseUTCColumnIsInLocalTime);
		mainPanel.add(tzPanel, b);
		
		b.gridy++;
		JPanel sqlPanel = new JPanel(new GridBagLayout());
		sqlPanel.setBorder(new TitledBorder("SQL table name"));
		c = new PamGridBagContraints();
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		String sqlText = "Name of the database table being read from and written into. Table must include all columns from a "
				+ "typical Whistle and Moan Detector database table.\n";
		html = "<html><body style='width: %1spx'>%1s";
		sqlPanel.add(new JLabel(String.format(html, 250, sqlText)), c);
		c.gridy++;
		sqlTableField = new JTextField();
		sqlTableField.setDocument(JSQLFilter(sqlTableField));
		sqlTableField.setText(wmntControl.getParams().sqlTableName);
		sqlPanel.add(sqlTableField, c);
		mainPanel.add(sqlPanel, b);
		
		p.add(BorderLayout.NORTH, mainPanel);
		
		tabbedPane.add("Navigation Tool", p);
		
		setDialogComponent(tabbedPane);
		
	}
	
	class SpeciesAddListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (speciesModel.getIndexOf(speciesField.getText()) == -1 && !(speciesField.getText().equals(""))) {
				speciesModel.addElement(speciesField.getText());
			}
			speciesField.setText("");
		}
	}
	
	class SpeciesRemoveListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (speciesModel.getSize() > 0) {
				speciesModel.removeElement(speciesModel.getSelectedItem());
			}
		}
	}
	
	class CallTypeAddListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (callTypeModel.getIndexOf(callTypeField.getText()) == -1 && !(callTypeField.getText().equals(""))) {
				callTypeModel.addElement(callTypeField.getText());
			}
			callTypeField.setText("");
		}
	}
	
	class CallTypeRemoveListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (callTypeModel.getSize() > 0) {
				callTypeModel.removeElement(callTypeModel.getSelectedItem());
			}
		}
	}
	
	/**
	 * Sets a limit to the number of characters allowed in a JTextField.
	 * Used with JTextField.setDocument(new JTextFieldLimit(int limit).getDocument());
	 * Copied from https://stackoverflow.com/questions/3519151/how-to-limit-the-number-of-characters-in-jtextfield
	 * Author page: https://stackoverflow.com/users/1866109/francisco-j-g%c3%bcemes-sevilla
	 * (Also modified to filter out commas.)
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
	            if (str.length() == 1 && str.charAt(0) == ',') return;
	            if ((getLength() + str.length()) <= limit) super.insertString(offset, str, attr);
	        }       
	    }
	}
	
	/**
	 * Limits entry in text field to numbers only.
	 * @return PlainDocument
	 */
	public PlainDocument JNumFilter() {
		PlainDocument d = new PlainDocument() {
			@Override
	        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
	            char c = str.charAt(0);
	            if (c >= '0' && c <= '9') {
	            	super.insertString(offs, str, a);
		        }
	        }
		};
		return d;
	}
	
	/**
	 * Limits entry in text field to miniscule letters, digits and underscores.
	 * Capital letters are automatically un-capitalized, and digits are blocked from being the first character.
	 * @return PlainDocument
	 */
	public PlainDocument JSQLFilter(JTextField field) {
		PlainDocument d = new PlainDocument() {
			@Override
	        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
				if (str.length() > 1) {
					super.insertString(offs, str, a);
					return;
				}
	            char c = str.charAt(0);
	            if (c >= 'A' && c <= 'Z') {
	            	c += 32;
	            } else if (!((c >= '0' && c <= '9' && field.getText().length() > 0)
	            		    || (c >= 'a' && c <= 'z')
	            		    || c == '_')) {
	            	return;
		        }
	            super.insertString(offs, String.valueOf(c), a);
	        }
		};
		return d;
	}
	
	@Override
	public boolean getParams() {
		WMNTParameters newParams = new WMNTParameters();
		if (sqlTableField.getText().length() == 0) {
			wmntControl.SimpleErrorDialog("SQL table must have a name.");
			return false;
		}
		
		if (!sqlTableField.getText().equals(wmntControl.getParams().sqlTableName)) {
			if (!startup) {
				int result = JOptionPane.showConfirmDialog(parentFrame,
						"The SQL table name has been changed, therefore the database should be manually re-connected to."
						+ "\n\nProceed with settings changes?",
						wmntControl.getUnitName(),
						JOptionPane.OK_CANCEL_OPTION);
				if (result != JOptionPane.OK_OPTION) return false;
			}
			newParams.sqlTableName = sqlTableField.getText();
		}
		
		if (binaryTZCheck.isSelected() != wmntControl.getParams().binaryIsInLocalTime ||
			databaseTZCheck.isSelected() != wmntControl.getParams().databaseUTCColumnIsInLocalTime) {
			if (!startup) {
				int result = JOptionPane.showConfirmDialog(parentFrame,
						"Time zones have been changed, therefore the binary data should be manually re-loaded."
						+ "\n\nProceed with settings changes?",
						wmntControl.getUnitName(),
						JOptionPane.OK_CANCEL_OPTION);
				if (result != JOptionPane.OK_OPTION) return false;
			}
			newParams.binaryIsInLocalTime = binaryTZCheck.isSelected();
			newParams.databaseUTCColumnIsInLocalTime = databaseTZCheck.isSelected();
		}
		
		newParams.inputProcessName = inputSourcePanel.getSourceName();
		newParams.speciesList = new ArrayList<String>();
		newParams.speciesList.add("");
		wmntControl.getSidePanel().getWMNTPanel().speciesModel = new DefaultComboBoxModel<String>();
		wmntControl.getSidePanel().getWMNTPanel().speciesModel.addElement("");
		for (int i = 0; i < speciesModel.getSize(); i++) {
			newParams.speciesList.add(speciesModel.getElementAt(i));
			wmntControl.getSidePanel().getWMNTPanel().speciesModel.addElement(speciesModel.getElementAt(i));
		}
		wmntControl.getSidePanel().getWMNTPanel().speciesBox.setModel(wmntControl.getSidePanel().getWMNTPanel().speciesModel);
		
		newParams.callTypeList = new ArrayList<String>();
		newParams.callTypeList.add("");
		wmntControl.getSidePanel().getWMNTPanel().calltypeModel = new DefaultComboBoxModel<String>();
		wmntControl.getSidePanel().getWMNTPanel().calltypeModel.addElement("");
		for (int i = 0; i < callTypeModel.getSize(); i++) {
			newParams.callTypeList.add(callTypeModel.getElementAt(i));
			wmntControl.getSidePanel().getWMNTPanel().calltypeModel.addElement(callTypeModel.getElementAt(i));
		}
		wmntControl.getSidePanel().getWMNTPanel().calltypeBox.setModel(wmntControl.getSidePanel().getWMNTPanel().calltypeModel);
		
		if (intervalField.getText().length() > 0)
			newParams.startBuffer = Integer.valueOf(intervalField.getText());
		if (scrollBufferField.getText().length() > 0)
			newParams.scrollBuffer = Integer.valueOf(scrollBufferField.getText());
		
		wmntControl.setParams(newParams);
		return true;
	}

	@Override
	public void cancelButtonPressed() {}

	@Override
	public void restoreDefaultSettings() {
		// (Confirm dialog apparently comes built-in.)
		// NOT meant to immediately replace the settings, just setting the values in the boxes to default.
		WMNTParameters newParams = new WMNTParameters();
		speciesModel.removeAllElements();
		for (int i = 1; i < newParams.speciesList.size(); i++) speciesModel.addElement(newParams.speciesList.get(i));
		callTypeModel.removeAllElements();
		for (int i = 1; i < newParams.callTypeList.size(); i++) callTypeModel.addElement(newParams.callTypeList.get(i));
		intervalField.setText(String.valueOf(newParams.startBuffer));
		scrollBufferField.setText(String.valueOf(newParams.scrollBuffer));
	/*	tzPanel.setAudioTimeZone(newParams.audioTZ);
		tzPanel.setBinaryTimeZone(newParams.binaryTZ);
		tzPanel.setDatabaseTimeZone(newParams.databaseTZ); */
		binaryTZCheck.setSelected(newParams.binaryIsInLocalTime);
		databaseTZCheck.setSelected(newParams.databaseUTCColumnIsInLocalTime);
		sqlTableField.setText(newParams.sqlTableName);
	}
}