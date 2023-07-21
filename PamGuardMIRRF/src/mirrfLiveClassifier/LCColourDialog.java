package mirrfLiveClassifier;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import wmnt.WMNTDataBlock;
import wmnt.WMNTDataUnit;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;

/**
 * The dialog for assigning colours to labels.
 * Also replaces the settings dialog in Viewer Mode.
 * @author Holly LeBlond
 */
public class LCColourDialog extends PamDialog {
	
	private static LCColourDialog singleInstance;
	
	private LCControl lcControl;
	private Window parentFrame;
	private LCSettingsDialog dialog;
	private HashMap<String, Color> currentColours;
	
	protected JLabel[] labelColumn;
	protected JButton[] buttonColumn;
	
	protected JComboBox<String> tzBox;
	
	//protected SourcePanel updateSourcePanel;

	//public LCColourDialog(Window parentFrame, LCControl lcControl, LCSettingsDialog dialog, boolean includeWMNTOption) {
	public LCColourDialog(Window parentFrame, LCControl lcControl, LCSettingsDialog dialog) {
		super(parentFrame, "MIRRF Live Classifier", true);
		this.lcControl = lcControl;
		this.dialog = dialog;
		this.currentColours = dialog.getCurrentColours();
		
		String[] labels = new String[dialog.labelList.getModel().getSize()];
		for (int i = 0; i < labels.length; i++) {
			labels[i] = dialog.labelList.getModel().getElementAt(i);
		}
		
		JPanel p0 = new JPanel(new GridBagLayout());
		GridBagConstraints b = new PamGridBagContraints();
		JPanel mainPanel0 = new JPanel(new GridLayout(labels.length,2,20,5));
		mainPanel0.setBorder(new TitledBorder("Label colours"));
		
		labelColumn = new JLabel[labels.length];
		buttonColumn = new JButton[labels.length];
		
		for (int i = 0; i < labels.length; i++) {
			labelColumn[i] = new JLabel(labels[i]);
			labelColumn[i].setOpaque(true);
			Color curr = currentColours.get(labels[i]);
			labelColumn[i].setBackground(curr);
			if (curr.getRed() >= 200 || curr.getGreen() >= 200) {
				labelColumn[i].setForeground(Color.BLACK);
			} else {
				labelColumn[i].setForeground(Color.WHITE);
			}
			labelColumn[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
			mainPanel0.add(labelColumn[i]);
			
			buttonColumn[i] = new JButton("Change...");
			buttonColumn[i].addActionListener(new ColourPickerListener(i, this));
			mainPanel0.add(buttonColumn[i]);
		}
		
		b.fill = b.HORIZONTAL;
		b.anchor = b.NORTH;
		p0.add(mainPanel0, b);
		
	/*	if (includeWMNTOption) {
			JPanel mainPanel1 = new JPanel(new FlowLayout(FlowLayout.CENTER));
			mainPanel1.setBorder(new TitledBorder("Time zone"));
			
			String[] tz_list = TimeZone.getAvailableIDs();
			tzBox = new JComboBox<String>(tz_list);
			tzBox.setSelectedItem(lcControl.getParams().timeZone);
			tzBox.setSize(200, tzBox.getHeight());
			mainPanel1.add(tzBox);
			
			b.gridy++;
			p0.add(mainPanel1, b);
			
			updateSourcePanel = new SourcePanel(this, "Annotation update data source", WMNTDataUnit.class, false, true);
			b.gridy++;
			p0.add(updateSourcePanel.getPanel(), b);
		} */
		
		setDialogComponent(p0);
	}
	
	/**
	 * The ActionListener for each button in buttonColumn.
	 */
	class ColourPickerListener implements ActionListener {
		private int index;
		private LCColourDialog cDialog;
		
		public ColourPickerListener(int index, LCColourDialog cDialog) {
			this.index = index;
			this.cDialog = cDialog;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			LCColourPickerDialog cpDialog = new LCColourPickerDialog(parentFrame, lcControl, cDialog, labelColumn[index].getText());
			cpDialog.setVisible(true);
		}
	}
	
	/**
	 * @return A hash map matching species to the current selection of colours.
	 */
	public HashMap<String, Color> getCurrentColours() {
		return currentColours;
	}
	
	/**
	 * Sets the currentColours hash map.
	 * Also automatically adjusts the text colour for each label to either white or black for visibility.
	 */
	public void setCurrentColours(HashMap<String, Color> inp) {
		currentColours = inp;
		for (int i = 0; i < labelColumn.length; i++) {
			Color curr = currentColours.get(labelColumn[i].getText());
			labelColumn[i].setBackground(curr);
			if (curr.getRed() >= 200 || curr.getGreen() >= 200) {
				labelColumn[i].setForeground(Color.BLACK);
			} else {
				labelColumn[i].setForeground(Color.WHITE);
			}
		}
	}

	@Override
	public boolean getParams() {
	/*	if (lcControl.isViewer()) {
			lcControl.getParams().labelColours = currentColours;
			lcControl.getParams().timeZone = (String) tzBox.getSelectedItem();
			if (updateSourcePanel.getSourceName() != null) {
				lcControl.getParams().updateProcessName = updateSourcePanel.getSourceName();
			} else {
				lcControl.getParams().updateProcessName = "";
			}
			if (lcControl.getParams().updateProcessName.length() > 0) {
				WMNTDataBlock db = (WMNTDataBlock) updateSourcePanel.getSource();
				lcControl.getUpdateProcess().setParentDataBlock(db);
				db.updateLC(true);
			}
		} else {
			dialog.setCurrentColours(currentColours);
		} */
		dialog.setCurrentColours(currentColours);
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		
	}

	@Override
	public void restoreDefaultSettings() {
		setCurrentColours(dialog.getCurrentColours());
	}
	
}