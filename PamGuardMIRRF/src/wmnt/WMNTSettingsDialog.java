package wmnt;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
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

/**
 * The settings dialog for the WMNT.
 * @author Taylor LeBlond
 */
public class WMNTSettingsDialog extends PamDialog {
	
	protected JTextField speciesField;
	protected DefaultComboBoxModel<String> speciesModel;
	protected JComboBox<String> speciesBox;
	protected JTextField callTypeField;
	protected DefaultComboBoxModel<String> callTypeModel;
	protected JComboBox<String> callTypeBox;
	protected JTextField intervalField;
	protected WMNTTimeZonePanel tzPanel;
	protected String originalAudioTZ;
	protected String originalBinaryTZ;
	protected String originalDatabaseTZ;
	
	protected WMNTControl wmntControl;
	protected Window parentFrame;
	
	public WMNTSettingsDialog(Window parentFrame, WMNTControl wmntControl) {
		super(parentFrame, wmntControl.getUnitName(), true);
		this.wmntControl = wmntControl;
		this.parentFrame = parentFrame;
		
		this.getDefaultButton().setVisible(false);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		
		JPanel p = new JPanel(new BorderLayout());
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints b = new PamGridBagContraints();
		b.gridy = 0;
		b.gridx = 0;
		b.fill = GridBagConstraints.HORIZONTAL;
		b.anchor = GridBagConstraints.WEST;
		
		JPanel speciesPanel = new JPanel(new GridBagLayout());
		speciesPanel.setBorder(new TitledBorder("'Species' list"));
		speciesPanel.setAlignmentX(LEFT_ALIGNMENT);
		GridBagConstraints c = new PamGridBagContraints();
		c.gridy = 0;
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
		for (int i = 0; i < wmntControl.getSidePanel().getWMNTPanel().speciesModel.getSize(); i++) {
			String item = wmntControl.getSidePanel().getWMNTPanel().speciesModel.getElementAt(i).toString();
			if (!(item.equals(""))) {
				speciesModel.addElement(wmntControl.getSidePanel().getWMNTPanel().speciesModel.getElementAt(i));
			}
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
		for (int i = 0; i < wmntControl.getSidePanel().getWMNTPanel().calltypeModel.getSize(); i++) {
			String item = wmntControl.getSidePanel().getWMNTPanel().calltypeModel.getElementAt(i).toString();
			if (!(item.equals(""))) {
				callTypeModel.addElement(wmntControl.getSidePanel().getWMNTPanel().calltypeModel.getElementAt(i));
			}
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
		intervalPanel.setBorder(new TitledBorder("Selection within start interval"));
		c = new PamGridBagContraints();
		c.gridy = 0;
		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 2;
		String intervalText = "Everything within the specified time interval after the start of each file is selected when "
				+ "'Select within start interval' is pressed.\n";
		String html = "<html><body style='width: %1spx'>%1s";
		intervalPanel.add(new JLabel(String.format(html, 250, intervalText)), c);
		c.gridy++;
		c.gridwidth = 1;
		intervalPanel.add(new JLabel("Interval (in milliseconds): "), c);
		c.gridx++;
		c.fill = GridBagConstraints.NONE;
		intervalField = new JTextField(5);
		intervalField.setText(String.valueOf(wmntControl.getSidePanel().getWMNTPanel().startInterval));
		intervalField.setDocument(JNumFilter());
		intervalPanel.add(intervalField, c);
		mainPanel.add(intervalPanel, b);
		
		b.gridy++;
		tzPanel = new WMNTTimeZonePanel(true, true, true, true);
		tzPanel.setAudioTimeZone(wmntControl.audioTZ);
		tzPanel.setBinaryTimeZone(wmntControl.binaryTZ);
		tzPanel.setDatabaseTimeZone(wmntControl.databaseTZ);
		originalAudioTZ = wmntControl.audioTZ;
		originalBinaryTZ = wmntControl.binaryTZ;
		originalDatabaseTZ = wmntControl.databaseTZ;
		mainPanel.add(tzPanel, b);
		
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
	
	@Override
	public boolean getParams() {
		if (!tzPanel.getAudioTimeZone().equals(originalAudioTZ) ||
			!tzPanel.getBinaryTimeZone().equals(originalBinaryTZ) ||
			!tzPanel.getDatabaseTimeZone().equals(originalDatabaseTZ)) {
			int result = JOptionPane.showConfirmDialog(parentFrame,
					"Time zones have been changed, therefore the binary data should be re-loaded."
					+ "\n\nProceed with settings changes?",
					wmntControl.getUnitName(),
					JOptionPane.OK_CANCEL_OPTION);
			if (result != JOptionPane.OK_OPTION) return false;
			wmntControl.audioTZ = tzPanel.getAudioTimeZone();
			wmntControl.binaryTZ = tzPanel.getBinaryTimeZone();
			wmntControl.databaseTZ = tzPanel.getDatabaseTimeZone();
		}
		wmntControl.getSidePanel().getWMNTPanel().speciesModel = new DefaultComboBoxModel<String>();
		wmntControl.getSidePanel().getWMNTPanel().speciesModel.addElement("");
		for (int i = 0; i < speciesModel.getSize(); i++) {
			wmntControl.getSidePanel().getWMNTPanel().speciesModel.addElement(speciesModel.getElementAt(i));
		}
		wmntControl.getSidePanel().getWMNTPanel().speciesBox.setModel(wmntControl.getSidePanel().getWMNTPanel().speciesModel);
		wmntControl.getSidePanel().getWMNTPanel().calltypeModel = new DefaultComboBoxModel<String>();
		wmntControl.getSidePanel().getWMNTPanel().calltypeModel.addElement("");
		for (int i = 0; i < callTypeModel.getSize(); i++) {
			wmntControl.getSidePanel().getWMNTPanel().calltypeModel.addElement(callTypeModel.getElementAt(i));
		}
		wmntControl.getSidePanel().getWMNTPanel().calltypeBox.setModel(wmntControl.getSidePanel().getWMNTPanel().calltypeModel);
		if (intervalField.getText().length() > 0) {
			wmntControl.getSidePanel().getWMNTPanel().startInterval = Integer.valueOf(intervalField.getText());
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		
	}

	@Override
	public void restoreDefaultSettings() {
		// Never called (removed).
	}
}