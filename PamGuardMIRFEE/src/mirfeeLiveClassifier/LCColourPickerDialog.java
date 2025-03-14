package mirfeeLiveClassifier;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.HashMap;

import javax.swing.JColorChooser;
import javax.swing.JPanel;

import PamView.dialog.PamDialog;

/**
 * Just brings up a JColorChooser for LCColourDialog.
 * @author Holly LeBlond
 */
public class LCColourPickerDialog extends PamDialog {
	
	private LCControl lcControl;
	private Window parentFrame;
	private LCColourDialog dialog;
	private HashMap<String, Color> currColours;
	private String key;
	protected JColorChooser colourChooser;
	
	public LCColourPickerDialog(Window parentFrame, LCControl lcControl, LCColourDialog dialog, String key) {
		super(parentFrame, "MIRFEE Live Classifier", true);
		this.lcControl = lcControl;
		this.parentFrame = parentFrame;
		this.dialog = dialog;
		this.currColours = dialog.getCurrentColours();
		this.key = key;
		
		JPanel p0 = new JPanel(new FlowLayout(FlowLayout.CENTER));
		colourChooser = new JColorChooser(currColours.get(key));
		p0.add(colourChooser);
		
		setDialogComponent(p0);
	}

	@Override
	public boolean getParams() {
		currColours.put(key, colourChooser.getColor());
		dialog.setCurrentColours(currColours);
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		
	}

	@Override
	public void restoreDefaultSettings() {
		colourChooser.setColor(currColours.get(key));
	}
	
}