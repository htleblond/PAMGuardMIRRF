package mirrfFeatureExtractor;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.PrintWriter;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamFileChooser;
import spectrogramNoiseReduction.SpectrogramNoiseDialogPanel;
import spectrogramNoiseReduction.SpectrogramNoiseSettings;
import whistlesAndMoans.AbstractWhistleDataUnit;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import PamView.dialog.GroupedSourcePanel;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import cepstrum.CepstrumProcess;

public class FETempFolderDialog extends PamDialog {
	
	private FEControl feControl;
	private Window parentFrame;
	
	private JTextField fileField;
	private JButton selectButton;
	private boolean hasMirrfName;
	
	public FETempFolderDialog(Window parentFrame, FEControl feControl) {
		super(parentFrame, "MIRRF Feature Extractor", true);
		this.feControl = feControl;
		this.parentFrame = parentFrame;
		this.hasMirrfName = false;
		
		this.getDefaultButton().setVisible(false);
		
		JPanel mainPanel = new JPanel(new FlowLayout());
		JPanel subPanel = new JPanel(new GridBagLayout());
		subPanel.setBorder(new TitledBorder(""));
		GridBagConstraints b = new PamGridBagContraints();
		b.gridy = 0;
		b.gridx = 0;
		b.gridwidth = 2;
		b.fill = GridBagConstraints.HORIZONTAL;
		b.anchor = GridBagConstraints.CENTER;
		subPanel.add(new JLabel(makeHTML("MIRRF requires a folder to temporarily keep sound clips and Python code during processing. "
				+ "Unfortunately, PamGuard's file security configuration doesn't allow the program to create new files or folders within PamGuard's own "
				+ "folders, so a different folder must be created.<br><br>"
				+ "A new folder named \"MIRRF Temp\" will be created in the folder you select (unless the selected folder is named \"MIRRF Temp\" itself). "
				+ "NOTE THAT anything placed inside this folder will be deleted every time the Feature Extractor is run.")), b);
		b.gridy++;
		b.gridwidth = 1;
		fileField = new JTextField(30);
		fileField.setEnabled(false);
		subPanel.add(fileField, b);
		b.gridx++;
		selectButton = new JButton("Select folder");
		selectButton.addActionListener(new FileListener());
		subPanel.add(selectButton, b);
		
		
		
		mainPanel.add(subPanel);
		setDialogComponent(mainPanel);
	}
	
	// Kudos to this: https://stackoverflow.com/questions/1842223/java-linebreaks-in-jlabels
	public String makeHTML(String inp) {
		int width = 300;
		String outp = "<html><p style=\"width:"+Integer.toString(width)+"px\">"+inp+"</p></html>";
		return outp;
	}
	
	/**
	 * The listener for the 'Export table' button.
	 */
	class FileListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			PamFileChooser fc = new PamFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setMultiSelectionEnabled(false);
			int returnVal = fc.showSaveDialog(parentFrame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				//System.out.println(fc.getSelectedFile().getName());
				if (fc.getSelectedFile().getName().equals("MIRRF Temp")) {
					//fileField.setText(fc.getSelectedFile().getPath()+"\\");
					hasMirrfName = true;
				} else {
					//fileField.setText(fc.getSelectedFile().getPath()+"\\MIRRF Temp\\");
					hasMirrfName = false;
				}
				//System.out.println(hasMirrfName);
				fileField.setText(fc.getSelectedFile().getPath()+"\\");
			}
		}
	}
	
	public FEControl getControl() {
		return feControl;
	}
	
	/**
	 * Streamlined error dialog with an editable message.
	 */
	public void SimpleErrorDialog(String inptext) {
		JOptionPane.showMessageDialog(null,
			inptext,
			"Whistle and Moan Navigation Tool",
			JOptionPane.ERROR_MESSAGE);
	}
	
	@Override
	public boolean getParams() {
		String currPath = fileField.getText();
		File testFile = new File(currPath);
		if (!testFile.exists()) {
			SimpleErrorDialog("Selected folder does not exist.");
			return false;
		}
		if (!testFile.isDirectory()) {
			SimpleErrorDialog("Selected object is not a folder.");
			return false;
		}
		if (!hasMirrfName) {
			currPath += "MIRRF Temp\\";
			testFile = new File(currPath);
		}
		if (!testFile.exists()) {
			if (!testFile.mkdir()) {
				SimpleErrorDialog("Could not create MIRRF Temp folder within selected folder.");
				return false;
			}
		}
		currPath += "Feature Extractor\\";
		testFile = new File(currPath);
		if (!testFile.exists()) {
			testFile.mkdir();
		}
		testFile = new File(currPath+"test.txt");
		if (testFile.canRead()) {
			if (!testFile.delete()) {
				SimpleErrorDialog("Folder does not allow file management.");
				return false;
			}
		} else {
			try {
				boolean boo = testFile.createNewFile();
				if (boo) {
					testFile.delete();
				} else {
					SimpleErrorDialog("Folder does not allow file management.");
					return false;
				}
			} catch(Exception e) {
				SimpleErrorDialog("Folder does not allow file management.");
				return false;
			}
		}
		feControl.getParams().tempFolder = currPath;
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		//feControl.removeUnit();
		//feControl.getSidePanel().getFEPanel().fileField.setText("No MIRRF Temp folder selected.");
	}

	@Override
	public void restoreDefaultSettings() {
		return;
	}
}