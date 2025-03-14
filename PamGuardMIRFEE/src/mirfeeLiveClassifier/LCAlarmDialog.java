package mirfeeLiveClassifier;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import whistlesAndMoans.alarm.WMAlarmDialog;
import whistlesAndMoans.alarm.WMAlarmParameters;

public class LCAlarmDialog extends PamDialog {
	
	private static LCAlarmDialog singleInstance;
	
	protected LCControl lcControl;
	protected LCAlarmParameters alarmParams;
	
	protected JCheckBox[] labelChecks;
	protected JTextField minDetectionsField;
	protected JComboBox<String> minCertaintyBox;
	
	protected LCAlarmDialog(Window parentFrame, LCControl lcControl, LCAlarmParameters alarmParams) {
		super(parentFrame, lcControl.getUnitName()+" alarm options", true);
		this.lcControl = lcControl;
		this.alarmParams = alarmParams;
		LCParameters moduleParams = lcControl.getParams();
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints b = new PamGridBagContraints();
		b.fill = b.HORIZONTAL;
		JPanel firstPanel = new JPanel(new GridLayout(0,1,0,0));
		firstPanel.setBorder(new TitledBorder("Select labels"));
		labelChecks = new JCheckBox[moduleParams.labelOrder.length];
		for (int i = 0; i < moduleParams.labelOrder.length; i++) {
			labelChecks[i] = new JCheckBox(moduleParams.labelOrder[i]);
			labelChecks[i].setSelected(alarmParams.selectedLabelsMap.containsKey(moduleParams.labelOrder[i]) && 
										alarmParams.selectedLabelsMap.get(moduleParams.labelOrder[i]));
			firstPanel.add(labelChecks[i]);
		}
		mainPanel.add(firstPanel, b);
		
		b.gridy++;
		JPanel secondPanel = new JPanel(new GridBagLayout());
		secondPanel.setBorder(new TitledBorder(""));
		GridBagConstraints c = new PamGridBagContraints();
		c.anchor = c.WEST;
		secondPanel.add(new JLabel("Min. detections in cluster"), c);
		c.gridx++;
		minDetectionsField = new JTextField(4);
		minDetectionsField.setDocument(JIntFilter());
		minDetectionsField.addFocusListener(new FocusAdapter() {
		    public void focusLost(FocusEvent e) {
				if (minDetectionsField.getText().length() < 1 || Integer.valueOf(minDetectionsField.getText()) < 1)
					minDetectionsField.setText("1");
		    }
		});
		minDetectionsField.setText(String.valueOf(alarmParams.minDetections));
		secondPanel.add(minDetectionsField, c);
		c.gridy++;
		c.gridx = 0;
		secondPanel.add(new JLabel("Min. certainty"), c);
		c.gridx++;
		minCertaintyBox = new JComboBox<String>(new String[] {"Very low", "Low", "Average", "High", "Very high"});
		minCertaintyBox.setSelectedItem(alarmParams.minCertainty);
		secondPanel.add(minCertaintyBox, c);
		mainPanel.add(secondPanel, b);
		
		setDialogComponent(mainPanel);
	}
	
	public static LCAlarmParameters showDialog(Window frame, LCControl lcControl, LCAlarmParameters params) {
		if (singleInstance == null || singleInstance.getOwner() != frame) {
			singleInstance = new LCAlarmDialog(frame, lcControl, params);
		}
		//singleInstance.alarmParams = alarmParams.clone();
		//singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.alarmParams;
	}
	
	/**
	 * Limits entry in text field to numbers only.
	 */
	public PlainDocument JIntFilter() {
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
		for (int i = 0; i < labelChecks.length; i++)
			alarmParams.selectedLabelsMap.put(labelChecks[i].getText(), labelChecks[i].isSelected());
		alarmParams.minDetections = Integer.valueOf(minDetectionsField.getText());
		alarmParams.minCertainty = (String) minCertaintyBox.getSelectedItem();
		return true;
	}

	@Override
	public void cancelButtonPressed() {}

	@Override
	public void restoreDefaultSettings() {
		for (int i = 0; i < labelChecks.length; i++)
			labelChecks[i].setSelected(false);
		minDetectionsField.setText("1");
		minCertaintyBox.setSelectedItem("Very low");
	}
	
}