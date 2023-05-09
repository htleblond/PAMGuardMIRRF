package mirrfTrainingSetBuilder;

import java.awt.BorderLayout;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.Comparator;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
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
import javax.swing.ListCellRenderer;
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
import mirrfFeatureExtractor.FEControl;
import PamView.dialog.GroupedSourcePanel;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import cepstrum.CepstrumProcess;

public class TSBSubsetDialog extends PamDialog {
	
	public TSBControl tsbControl;
	private Window parentFrame;
	private boolean editing;
	private String oldID = "";
	
	private PamPanel mainPanel;
	
	private JTextField featuresCSVField;
	private JButton featuresCSVButton;
	private JTextField wmntCSVField;
	private JButton wmntCSVButton;
	private JButton reloadButton;
	private JComboBox<String> idDigit1Box;
	private JComboBox<String> idDigit2Box;
	private JTextField locationField;
	private JTextField startField;
	private JTextField endField;
	private JTextField totalField;
	private DefaultListModel dlmodel;
	//private JList<JCheckBox> checkList;
	private JList<String> checkList;
	
	private ArrayList<String[]> featuresEntriesList;
	private ArrayList<String[]> wmntEntriesList;
	private ArrayList<String> classList;
	private ArrayList<ArrayList<TSBDetection>> validEntriesList;
	private ArrayList<String> featureList;
	
	public TSBSubsetDialog(Window parentFrame, TSBControl tsbControl, boolean editing) {
		super(parentFrame, "MIRRF Training Set Builder", false);
		this.tsbControl = tsbControl;
		this.parentFrame = parentFrame;
		this.editing = editing;
		
		this.featuresEntriesList = new ArrayList<String[]>();
		this.wmntEntriesList = new ArrayList<String[]>();
		this.classList = new ArrayList<String>();
		this.validEntriesList = new ArrayList<ArrayList<TSBDetection>>();
		this.featureList = new ArrayList<String>();
		
		mainPanel = new PamPanel(new GridBagLayout());
		GridBagConstraints b = new PamGridBagContraints();
		mainPanel.setBorder(new TitledBorder(""));
		
		JPanel topLeftPanel = new JPanel(new BorderLayout());
		//topLeftPanel.setBorder(new TitledBorder(""));
		JPanel topLeftSubPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 1;
		c.anchor = c.EAST;
		c.fill = c.NONE;
		topLeftSubPanel.add(new JLabel("Feature Extractor output:"), c);
		c.gridx++;
		featuresCSVField = new JTextField(30);
		featuresCSVField.setEnabled(false);
		topLeftSubPanel.add(featuresCSVField, c);
		c.gridx++;
		c.anchor = c.WEST;
		c.fill = c.HORIZONTAL;
		featuresCSVButton = new JButton("Select .csv");
		featuresCSVButton.addActionListener(new CSVListener(true));
		topLeftSubPanel.add(featuresCSVButton, c);
		c.gridy++;
		c.gridx = 0;
		c.anchor = c.EAST;
		c.fill = c.NONE;
		topLeftSubPanel.add(new JLabel("WMNT output:"), c);
		c.gridx++;
		wmntCSVField = new JTextField(30);
		wmntCSVField.setEnabled(false);
		topLeftSubPanel.add(wmntCSVField, c);
		c.gridx++;
		c.anchor = c.WEST;
		c.fill = c.HORIZONTAL;
		wmntCSVButton = new JButton("Select .csv");
		wmntCSVButton.addActionListener(new CSVListener(false));
		topLeftSubPanel.add(wmntCSVButton, c);
		topLeftPanel.add(topLeftSubPanel);
		c.gridy++;
		reloadButton = new JButton("Reload");
		reloadButton.addActionListener(new ReloadListener());
		reloadButton.setEnabled(false);
		topLeftSubPanel.add(reloadButton, c);
		topLeftPanel.add(topLeftSubPanel);
		b.gridy = 0;
		b.gridx = 0;
		b.gridwidth = 1;
		b.anchor = b.SOUTH;
		b.fill = b.HORIZONTAL;
		mainPanel.add(topLeftPanel, b);
		
		JPanel bottomLeftPanel = new JPanel(new BorderLayout());
		//bottomLeftPanel.setBorder(new TitledBorder(""));
		JPanel bottomLeftSubPanel = new JPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 2;
		c.anchor = c.WEST;
		c.fill = c.NONE;
		bottomLeftSubPanel.add(new JLabel("ID"), c);
		c.gridx += 2;
		c.gridwidth = 1;
		bottomLeftSubPanel.add(new JLabel("Location"), c);
		c.gridx++;
		bottomLeftSubPanel.add(new JLabel("Start"), c);
		c.gridx++;
		bottomLeftSubPanel.add(new JLabel("End"), c);
		c.gridx++;
		bottomLeftSubPanel.add(new JLabel("Total"), c);
		c.gridy++;
		c.gridx = 0;
		String[] box1Digits = new String[] {"1","2","3","4","5","6","7","8","9","0",
				"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
		idDigit1Box = new JComboBox<String>(box1Digits);
		bottomLeftSubPanel.add(idDigit1Box, c);
		c.gridx++;
		String[] box2Digits = new String[] {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z",
				"1","2","3","4","5","6","7","8","9","0"};
		idDigit2Box = new JComboBox<String>(box2Digits);
		bottomLeftSubPanel.add(idDigit2Box, c);
		c.gridx++;
		locationField = new JTextField(14);
		bottomLeftSubPanel.add(locationField, c);
		c.gridx++;
		startField = new JTextField(14);
		startField.setEnabled(false);
		bottomLeftSubPanel.add(startField, c);
		c.gridx++;
		endField = new JTextField(14);
		endField.setEnabled(false);
		bottomLeftSubPanel.add(endField, c);
		c.gridx++;
		totalField = new JTextField(5);
		totalField.setEnabled(false);
		totalField.setText("0");
		bottomLeftSubPanel.add(totalField, c);
		bottomLeftPanel.add(bottomLeftSubPanel);
		b.gridy++;
		b.anchor = b.CENTER;
		mainPanel.add(bottomLeftPanel, b);
		
		JPanel rightPanel = new JPanel(new BorderLayout());
		JPanel rightSubPanel = new JPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 1;
		c.anchor = c.WEST;
		c.fill = c.NONE;
		rightSubPanel.add(new JLabel("Classes found:"), c);
		c.gridy++;
		c.fill = c.BOTH;
		dlmodel = new DefaultListModel();
		//checkList = new JList<JCheckBox>(dlmodel);
		//checkList.setCellRenderer(new CheckboxListCellRenderer());
		checkList = new JList<String>(dlmodel);
		// Copied from: https://stackoverflow.com/questions/2404546/select-multiple-items-in-jlist-without-using-the-ctrl-command-key
		checkList.setSelectionModel(new DefaultListSelectionModel() {
		    @Override
		    public void setSelectionInterval(int index0, int index1) {
		        if(super.isSelectedIndex(index0)) {
		            super.removeSelectionInterval(index0, index1);
		        }
		        else {
		            super.addSelectionInterval(index0, index1);
		        }
		    }
		});
		checkList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateFields();
			}
		});
		checkList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane sp = new JScrollPane(checkList);
		rightSubPanel.add(sp, c);
		rightPanel.add(rightSubPanel);
		b.gridy = 0;
		b.gridx = 1;
		b.gridheight = 2;
		b.fill = b.VERTICAL;
		mainPanel.add(rightPanel, b);
		
		setDialogComponent(mainPanel);
		
		if (editing) {
			int rowIndex = tsbControl.getTabPanel().getPanel().getSubsetTable().getSelectedRow();
			if (rowIndex > -1) {
				String currID = (String) tsbControl.getTabPanel().getPanel().getSubsetTable().getValueAt(rowIndex, 0);
				TSBSubset currSubset = null;
				for (int i = 0; i < tsbControl.subsetList.size(); i++) {
					if (currID.equals(tsbControl.subsetList.get(i).id)) {
						currSubset = tsbControl.subsetList.get(i);
						break;
					}
				}
				if (currSubset != null) {
					this.oldID = currID;
					if (currSubset.featurePath.length() > 0 && currSubset.wmntPath.length() > 0) {
						reloadButton.setEnabled(true);
					}
					featuresCSVField.setText(currSubset.featurePath);
					wmntCSVField.setText(currSubset.wmntPath);
					idDigit1Box.setSelectedItem(currSubset.id.substring(0, 1));
					idDigit2Box.setSelectedItem(currSubset.id.substring(1));
					locationField.setText(currSubset.location);
					startField.setText(currSubset.start);
					endField.setText(currSubset.end);
					classList = new ArrayList<String>(currSubset.classList);
					validEntriesList = new ArrayList<ArrayList<TSBDetection>>(currSubset.validEntriesList);
					DefaultListModel dlm = new DefaultListModel();
					for (int i = 0; i < classList.size(); i++) {
						dlm.addElement(classList.get(i));
					}
					checkList.setModel(dlm);
					checkList.setSelectedIndices(currSubset.selectionArray);
					updateFields();
				}
			}
		}
	}
	
	class CSVListener implements ActionListener {
		
		private boolean featuresNotWMNT;
		
		public CSVListener(boolean featuresNotWMNT) {
			this.featuresNotWMNT = featuresNotWMNT;
		}
		
		public void actionPerformed(ActionEvent e) {
			PamFileChooser fc = new PamFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setMultiSelectionEnabled(false);
			fc.addChoosableFileFilter(new FileNameExtensionFilter("Comma-separated values file (*.csv)","csv"));
			int returnVal = fc.showOpenDialog(parentFrame);
			if (returnVal == fc.APPROVE_OPTION) {
				File f = getSelectedFileWithExtension(fc);
				if (f.exists()) {
					Scanner sc;
					try {
						sc = new Scanner(f);
						if (sc.hasNextLine()) {
							if (featuresNotWMNT) {
								ArrayList<String[]> currFEList = new ArrayList<String[]>();
								ArrayList<String> currFeatureList = new ArrayList<String>();
								String[] firstLine = sc.nextLine().split(",");
								if (firstLine.length > 6) {
									if (firstLine[0].equals("cluster") && firstLine[1].equals("uid") && firstLine[2].equals("date") &&
											firstLine[3].equals("duration") && firstLine[4].equals("lf") && firstLine[5].equals("hf")) {
										SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss+SSS");
										df.setTimeZone(TimeZone.getTimeZone("UTC"));
										if (tsbControl.getFeatureList().size() > 0) {
											if (firstLine.length-6 == tsbControl.getFeatureList().size()) {
												for (int i = 6; i < firstLine.length; i++) {
													System.out.println(firstLine[i]+" -> "+tsbControl.getFeatureList().get(i-6));
													if (!firstLine[i].equals(tsbControl.getFeatureList().get(i-6))) {
														tsbControl.SimpleErrorDialog("The features of the selected file do not match those "
																+ "of subsets already loaded into the table. To add this subset, either "
																+ "clear the table or re-run the subset through the Feature Extractor with "
																+ "the correct features.", 300);
														sc.close();
														return;
													}
												}
											} else {
												tsbControl.SimpleErrorDialog("The features of the selected file do not match those "
														+ "of subsets already loaded into the table. To add this subset, either "
														+ "clear the table or re-run the subset through the Feature Extractor with "
														+ "the correct features.", 300);
												sc.close();
												return;
											}
										} else {
											for (int i = 6; i < firstLine.length; i++) {
												currFeatureList.add(firstLine[i]);
											}
										}
										//boolean printFirst = true;
										while (sc.hasNextLine()) {
											String[] nextLine = sc.nextLine().split(",");
											try {
												Long.valueOf(nextLine[1]);
												df.parse(nextLine[2]);
												for (int i = 3; i < firstLine.length; i++) {
													nextLine[i] = String.valueOf(Double.valueOf(nextLine[i]));
												}
												currFEList.add(nextLine);
											} catch (Exception e2){
												// TODO
											}
										}
										if (currFEList.size() > 0) {
											featuresEntriesList = currFEList;
											featuresCSVField.setText(f.getPath());
											if (tsbControl.getFeatureList().size() > 0) {
												featureList = new ArrayList<String>(tsbControl.getFeatureList());
											} else {
												featureList = new ArrayList<String>(currFeatureList);
											}
										} else {
											tsbControl.SimpleErrorDialog("Selected file contained no valid entries.", 150);
											sc.close();
											return;
										}
									} else {
										tsbControl.SimpleErrorDialog("Selected file not formatted like Feature Extractor output.", 150);
										sc.close();
										return;
									}
								} else {
									tsbControl.SimpleErrorDialog("Selected file not formatted like Feature Extractor output.", 150);
									sc.close();
									return;
								}
							} else {
								ArrayList<String[]> currWMNTList = new ArrayList<String[]>();
								String[] firstLine = sc.nextLine().split(",");
								if (firstLine.length >= 8) {
									if (firstLine[0].equals("uid") && firstLine[1].equals("datetime") && firstLine[2].equals("lf")
											 && firstLine[3].equals("hf") && firstLine[4].equals("duration") && firstLine[5].equals("amplitude")
											 && firstLine[6].equals("species") && firstLine[7].equals("calltype")) {
										SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss+SSS");
										df.setTimeZone(TimeZone.getTimeZone("UTC"));
										while (sc.hasNextLine()) {
											String[] nextLine = sc.nextLine().split(",");
											try {
												Long.valueOf(nextLine[0]);
												df.parse(nextLine[1]);
												Integer.valueOf(nextLine[2]);
												Integer.valueOf(nextLine[3]);
												Integer.valueOf(nextLine[4]);
												Integer.valueOf(nextLine[5]);
												if (tsbControl.includeCallType && nextLine.length > 7) {
													if (nextLine[6].length() > 0 || nextLine[7].length() > 0) {
														currWMNTList.add(nextLine);
													}
												} else {
													if (nextLine[6].length() > 0) {
														currWMNTList.add(nextLine);
													}
												}
											} catch (Exception e2){
												// TODO
											}
										}
										if (currWMNTList.size() > 0) {
											wmntEntriesList = currWMNTList;
											wmntCSVField.setText(f.getPath());
										} else {
											tsbControl.SimpleErrorDialog("Selected file contained no valid entries.", 150);
											sc.close();
											return;
										}
									} else {
										tsbControl.SimpleErrorDialog("Selected file not formatted like WMNT output.", 150);
										sc.close();
										return;
									}
								} else {
									tsbControl.SimpleErrorDialog("Selected file not formatted like WMNT output.", 150);
									sc.close();
									return;
								}
							}
							//System.out.println(featuresEntriesList.size());
							//System.out.println(wmntEntriesList.size());
							if (featuresEntriesList.size() > 0 && wmntEntriesList.size() > 0) {
								classList = new ArrayList<String>();
								for (int i = 0; i < wmntEntriesList.size(); i++) {
									String[] curr = wmntEntriesList.get(i);
									String className = "";
									if (tsbControl.includeCallType) {
										if (curr.length > 7) {
											if (curr[6].length() > 0 && curr[7].length() > 0) {
												className = curr[6]+" ("+curr[7]+")";
											} else if (curr[6].length() > 0) {
												className = curr[6];
											} else if (curr[7].length() > 0) {
												className = curr[7];
											}
										}
									} else {
										className = curr[6];
									}
									if (className.length() > 0 && !classList.contains(className)) {
										classList.add(className);
									}
									wmntEntriesList.get(i)[6] = className;
								}
								Collections.sort(classList);
								
								validEntriesList = new ArrayList<ArrayList<TSBDetection>>();
								//boolean printFirst = true;
								for (int i = 0; i < classList.size(); i++) {
									ArrayList<TSBDetection> currDetectionList = new ArrayList<TSBDetection>();
									for (int j = 0; j < wmntEntriesList.size(); j++) {
										String[] currWMNT = wmntEntriesList.get(j);
										String className = "";
										if (tsbControl.includeCallType) {
											if (currWMNT.length > 7) {
												if (currWMNT[6].length() > 0 && currWMNT[7].length() > 0) {
													className = currWMNT[6]+" ("+currWMNT[7]+")";
												} else if (currWMNT[6].length() > 0) {
													className = currWMNT[6];
												} else if (currWMNT[7].length() > 0) {
													className = currWMNT[7];
												}
											}
										} else {
											className = currWMNT[6];
										}
										if (className.equals(classList.get(i))) {
											for (int k = 0; k < featuresEntriesList.size(); k++) {
												String[] currFE = featuresEntriesList.get(k);
												if (currWMNT[0].equals(currFE[1]) && currWMNT[1].equals(currFE[2])) {
												/*	String[] outp = new String[currFE.length + 1];
													outp[0] = currFE[0];
													outp[1] = currFE[1];
													outp[2] = currFE[2];
													outp[3] = currFE[3];
													outp[4] = currFE[4];
													outp[5] = currFE[5];
													outp[6] = className;
													for (int l = 6; l < currFE.length; l++) {
														outp[l+1] = currFE[l];
													} */
													try {
														TSBDetection outp = new TSBDetection(tsbControl, featureList.size(),
																currFE[0], currWMNT, Arrays.copyOfRange(currFE, 6, currFE.length));
														currDetectionList.add(outp);
													} catch (AssertionError | Exception e2) {
														e2.printStackTrace();
													}
												/*	if (printFirst) {
														System.out.print(outp[0]);
														for (int l = 1; l < outp.length; l++) {
															System.out.print(", "+outp[l]);
														}
														System.out.println();
														printFirst = false;
													} */
													break;
												}
											}
										}
									}
									if (currDetectionList.size() > 0) {
										//currDetectionList.sort(Comparator.comparing(a -> a[2]));
										Collections.sort(currDetectionList, Comparator.comparingLong(TSBDetection::getDateTimeAsLong));
										validEntriesList.add(currDetectionList);
									} else {
										classList.remove(i);
										i--;
									}
								}
								checkList.removeAll();
								if (validEntriesList.size() > 0) {
									DefaultListModel dlm = new DefaultListModel();
									for (int i = 0; i < classList.size(); i++) {
										dlm.addElement(classList.get(i));
									}
									checkList.setModel(dlm);
									for (int i = 0; i < checkList.getModel().getSize(); i++) {
										if (tsbControl.getFullClassList().contains(checkList.getModel().getElementAt(i))) {
											checkList.setSelectedIndex(i);
										}
									}
									updateFields();
									reloadButton.setEnabled(true);
								} else {
									tsbControl.SimpleErrorDialog("Selected files contain no matching valid data.", 150);
								}
							}
						} else {
							tsbControl.SimpleErrorDialog("Selected file is blank.", 150);
							sc.close();
							return;
						}
						sc.close();
					} catch (Exception e2) {
						tsbControl.SimpleErrorDialog("Could not parse through selected file.", 150);
						return;
					}
				} else {
					tsbControl.SimpleErrorDialog("Selected file does not exist.", 150);
					return;
				}
			}
		}
	}
	
	class ReloadListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (featuresCSVField.getText().length() == 0 || wmntCSVField.getText().length() == 0) {
				tsbControl.SimpleErrorDialog("Both feature data and WMNT data files need to be selected.", 150);
				reloadButton.setEnabled(false);
				return;
			}
			File f = new File(featuresCSVField.getText());
			if (f.exists()) {
				Scanner sc = null;
				try {
					sc = new Scanner(f);
					if (sc.hasNextLine()) {
						ArrayList<String[]> currFEList = new ArrayList<String[]>();
						ArrayList<String> currFeatureList = new ArrayList<String>();
						String[] firstLine = sc.nextLine().split(",");
						if (firstLine.length > 3) {
							if (firstLine[0].equals("cluster") && firstLine[1].equals("uid") && firstLine[2].equals("date") &&
									firstLine[3].equals("duration") && firstLine[4].equals("lf") && firstLine[5].equals("hf")) {
								SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss+SSS");
								df.setTimeZone(TimeZone.getTimeZone("UTC"));
								if (tsbControl.getFeatureList().size() > 0) {
									if (firstLine.length-6 == tsbControl.getFeatureList().size()) {
										for (int i = 6; i < firstLine.length; i++) {
											if (!firstLine[i].equals(tsbControl.getFeatureList().get(i-6))) {
												tsbControl.SimpleErrorDialog("The features of the selected file do not match those "
														+ "of subsets already loaded into the table. To add this subset, either "
														+ "clear the table or re-run the subset through the Feature Extractor with "
														+ "the correct features.", 300);
												sc.close();
												return;
											}
										}
									} else {
										tsbControl.SimpleErrorDialog("The features of the selected file do not match those "
												+ "of subsets already loaded into the table. To add this subset, either "
												+ "clear the table or re-run the subset through the Feature Extractor with "
												+ "the correct features.", 300);
										sc.close();
										return;
									}
								} else {
									for (int i = 6; i < firstLine.length; i++) {
										currFeatureList.add(firstLine[i]);
									}
								}
								while (sc.hasNextLine()) {
									String[] nextLine = sc.nextLine().split(",");
									try {
										Long.valueOf(nextLine[1]);
										df.parse(nextLine[2]);
										for (int i = 3; i < firstLine.length; i++) {
											Double.valueOf(nextLine[i]);
										}
										currFEList.add(nextLine);
									} catch (Exception e2){
										// TODO
									}
								}
								if (currFEList.size() > 0) {
									featuresEntriesList = currFEList;
									featuresCSVField.setText(f.getPath());
									if (tsbControl.getFeatureList().size() > 0) {
										featureList = new ArrayList<String>(tsbControl.getFeatureList());
									} else {
										featureList = new ArrayList<String>(currFeatureList);
									}
								} else {
									tsbControl.SimpleErrorDialog("Selected feature data file contained no valid entries.", 150);
									sc.close();
									//reloadButton.setEnabled(false);
									return;
								}
							} else {
								tsbControl.SimpleErrorDialog("Selected feature data file not formatted like Feature Extractor output.", 150);
								sc.close();
								//reloadButton.setEnabled(false);
								return;
							}
						} else {
							tsbControl.SimpleErrorDialog("Selected feature data file not formatted like Feature Extractor output.", 150);
							sc.close();
							//reloadButton.setEnabled(false);
							return;
						}
					} else {
						tsbControl.SimpleErrorDialog("Selected feature data file is blank.", 150);
						sc.close();
						//reloadButton.setEnabled(false);
						return;
					}
				} catch (Exception e2) {
					tsbControl.SimpleErrorDialog("Exception thrown when parsing through feature data file.", 150);
					e2.printStackTrace();
					if (sc != null) {
						sc.close();
					}
					//reloadButton.setEnabled(false);
					return;
				}
			} else {
				tsbControl.SimpleErrorDialog("Feature data file at specified path does not exist.", 150);
				//reloadButton.setEnabled(false);
				return;
			}
			f = new File(wmntCSVField.getText());
			if (f.exists()) {
				Scanner sc = null;
				try {
					sc = new Scanner(f);
					if (sc.hasNextLine()) {
						ArrayList<String[]> currWMNTList = new ArrayList<String[]>();
						String[] firstLine = sc.nextLine().split(",");
						if (firstLine.length >= 8) {
							if (firstLine[0].equals("uid") && firstLine[1].equals("datetime") && firstLine[2].equals("lf")
									 && firstLine[3].equals("hf") && firstLine[4].equals("duration") && firstLine[5].equals("amplitude")
									 && firstLine[6].equals("species") && firstLine[7].equals("calltype")) {
								SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss+SSS");
								df.setTimeZone(TimeZone.getTimeZone("UTC"));
								while (sc.hasNextLine()) {
									String[] nextLine = sc.nextLine().split(",");
									try {
										Long.valueOf(nextLine[0]);
										df.parse(nextLine[1]);
										Integer.valueOf(nextLine[2]);
										Integer.valueOf(nextLine[3]);
										Integer.valueOf(nextLine[4]);
										Integer.valueOf(nextLine[5]);
										if (tsbControl.includeCallType && nextLine.length > 7) {
											if (nextLine[6].length() > 0 || nextLine[7].length() > 0) {
												currWMNTList.add(nextLine);
											}
										} else {
											if (nextLine[6].length() > 0) {
												currWMNTList.add(nextLine);
											}
										}
									} catch (Exception e2){
										// TODO
									}
								}
								if (currWMNTList.size() > 0) {
									wmntEntriesList = currWMNTList;
									wmntCSVField.setText(f.getPath());
								} else {
									tsbControl.SimpleErrorDialog("Selected file contained no valid entries.", 150);
									sc.close();
									return;
								}
							} else {
								tsbControl.SimpleErrorDialog("Selected file not formatted like WMNT output.", 150);
								sc.close();
								return;
							}
						} else {
							tsbControl.SimpleErrorDialog("Selected file not formatted like WMNT output.", 150);
							sc.close();
							return;
						}
					} else {
						tsbControl.SimpleErrorDialog("Selected WMNT data file is blank.", 150);
						sc.close();
						//reloadButton.setEnabled(false);
						return;
					}
				} catch (Exception e2) {
					tsbControl.SimpleErrorDialog("Exception thrown when parsing through WMNT data file.", 150);
					e2.printStackTrace();
					if (sc != null) {
						sc.close();
					}
					//reloadButton.setEnabled(false);
					return;
				}
			} else {
				tsbControl.SimpleErrorDialog("WMNT data file at specified path does not exist.", 150);
				//reloadButton.setEnabled(false);
				return;
			}
			if (featuresEntriesList.size() > 0 && wmntEntriesList.size() > 0) {
				classList = new ArrayList<String>();
				for (int i = 0; i < wmntEntriesList.size(); i++) {
					String[] currWMNT = wmntEntriesList.get(i);
					String className = "";
					if (tsbControl.includeCallType) {
						if (currWMNT.length > 7) {
							if (currWMNT[6].length() > 0 && currWMNT[7].length() > 0) {
								className = currWMNT[6]+" ("+currWMNT[7]+")";
							} else if (currWMNT[6].length() > 0) {
								className = currWMNT[6];
							} else if (currWMNT[7].length() > 0) {
								className = currWMNT[7];
							}
						}
					} else {
						className = currWMNT[6];
					}
					if (className.length() > 0 && !classList.contains(className)) {
						classList.add(className);
					}
					wmntEntriesList.get(i)[6] = className;
				}
				Collections.sort(classList);
				
				validEntriesList = new ArrayList<ArrayList<TSBDetection>>();
				for (int i = 0; i < classList.size(); i++) {
					ArrayList<TSBDetection> currDetectionList = new ArrayList<TSBDetection>();
					for (int j = 0; j < wmntEntriesList.size(); j++) {
						String[] currWMNT = wmntEntriesList.get(j);
						String className = "";
						if (tsbControl.includeCallType) {
							if (currWMNT.length > 7) {
								if (currWMNT[6].length() > 0 && currWMNT[7].length() > 0) {
									className = currWMNT[6]+" ("+currWMNT[7]+")";
								} else if (currWMNT[6].length() > 0) {
									className = currWMNT[6];
								} else if (currWMNT[7].length() > 0) {
									className = currWMNT[7];
								}
							}
						} else {
							className = currWMNT[6];
						}
						if (className.equals(classList.get(i))) {
							for (int k = 0; k < featuresEntriesList.size(); k++) {
								String[] currFE = featuresEntriesList.get(k);
								if (currWMNT[0].equals(currFE[1]) && currWMNT[1].equals(currFE[2])) {
								/*	String[] outp = new String[currFE.length + 1];
									outp[0] = currFE[0];
									outp[1] = currFE[1];
									outp[2] = currFE[2];
									outp[3] = currFE[3];
									outp[4] = currFE[4];
									outp[5] = currFE[5];
									outp[6] = className;
									for (int l = 6; l < currFE.length; l++) {
										outp[l+1] = currFE[l];
									} */
									try {
										TSBDetection outp = new TSBDetection(tsbControl, featureList.size(),
												currFE[0], currWMNT, Arrays.copyOfRange(currFE, 6, currFE.length));
										currDetectionList.add(outp);
									} catch (AssertionError | Exception e2) {
										e2.printStackTrace();
									}
									break;
								}
							}
						}
					}
					if (currDetectionList.size() > 0) {
						//currDetectionList.sort(Comparator.comparing(a -> a[2]));
						Collections.sort(currDetectionList, Comparator.comparingLong(TSBDetection::getDateTimeAsLong));
						validEntriesList.add(currDetectionList);
					} else {
						classList.remove(i);
						i--;
					}
				}
				checkList.removeAll();
				if (validEntriesList.size() > 0) {
					DefaultListModel dlm = new DefaultListModel();
					for (int i = 0; i < classList.size(); i++) {
						dlm.addElement(classList.get(i));
					}
					checkList.setModel(dlm);
					for (int i = 0; i < checkList.getModel().getSize(); i++) {
						if (tsbControl.getFullClassList().contains(checkList.getModel().getElementAt(i))) {
							checkList.setSelectedIndex(i);
						}
					}
					updateFields();
				} else {
					tsbControl.SimpleErrorDialog("Selected files contain no matching valid data.", 150);
					//reloadButton.setEnabled(false);
				}
			} else {
				tsbControl.SimpleErrorDialog("Selected files contain no matching valid data.", 150);
				//reloadButton.setEnabled(false);
			}
		}
	}
	
/*	public class CheckListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			for (int i = 0; i < classList.size(); i++) {
				System.out.println("Listener worked!");
				updateFields();
			}
		}
	} */
	
	public void updateFields() {
		String startDate = "";
		String endDate = "";
		int total = 0;
		for (int i = 0; i < checkList.getModel().getSize(); i++) {
			if (checkList.isSelectedIndex(i)) {
				ArrayList<TSBDetection> currList = validEntriesList.get(i);
				if (startDate.length() == 0 || currList.get(0).datetime.compareTo(startDate) < 0) {
					startDate = currList.get(0).datetime;
				}
				if (endDate.length() == 0 || currList.get(currList.size()-1).datetime.compareTo(endDate) > 0) {
					endDate = currList.get(currList.size()-1).datetime;
				}
				total += currList.size();
			}
		}
		startField.setText(startDate);
		endField.setText(endDate);
		totalField.setText(String.valueOf(total));
	}
	
	// Copied from Rene Link at: https://stackoverflow.com/questions/19766/how-do-i-make-a-list-with-checkboxes-in-java-swing?noredirect=1&lq=1
/*	public class CheckboxListCellRenderer extends JCheckBox implements ListCellRenderer {

	    public Component getListCellRendererComponent(JList list, Object value, int index, 
	            boolean isSelected, boolean cellHasFocus) {

	        setComponentOrientation(list.getComponentOrientation());
	        setFont(list.getFont());
	        setBackground(list.getBackground());
	        setForeground(list.getForeground());
	        setSelected(isSelected);
	        setEnabled(list.isEnabled());

	        //setText(value == null ? "" : value.toString());  
	        JCheckBox valueBox = (JCheckBox) value;
	        setText(valueBox.getText());
	        //addActionListener(new CheckListener());

	        return this;
	    }
	} */
	
	/**
	 * Returns the selected file from a JFileChooser, including the extension from
	 * the file filter.
	 * 
	 * Copied from here: https://stackoverflow.com/questions/16846078/jfilechoosershowsavedialog-cant-get-the-value-of-the-extension-file-chosen
	 * Author page: https://stackoverflow.com/users/964243/boann
	 */
	public static File getSelectedFileWithExtension(JFileChooser c) {
	    File file = c.getSelectedFile();
	    if (c.getFileFilter() instanceof FileNameExtensionFilter) {
	        String[] exts = ((FileNameExtensionFilter)c.getFileFilter()).getExtensions();
	        String nameLower = file.getName().toLowerCase();
	        for (String ext : exts) { // check if it already has a valid extension
	            if (nameLower.endsWith('.' + ext.toLowerCase())) {
	                return file; // if yes, return as-is
	            }
	        }
	        // if not, append the first extension from the selected filter
	        file = new File(file.toString() + '.' + exts[0]);
	    }
	    return file;
	}

	@Override
	public boolean getParams() {
		if (checkList.getSelectedIndices().length == 0) {
			tsbControl.SimpleErrorDialog("No classes have been selected.", 150);
			return false;
		}
		TSBSubset outp = new TSBSubset();
		outp.id = idDigit1Box.getItemAt(idDigit1Box.getSelectedIndex());
		outp.id += idDigit2Box.getItemAt(idDigit2Box.getSelectedIndex());
		ArrayList<TSBSubset> sl = tsbControl.getSubsetList();
		for (int i = 0; i < sl.size(); i++) {
			if (sl.get(i).id.equals(outp.id) && !outp.id.equals(oldID)) {
				tsbControl.SimpleErrorDialog("Selected ID has already been taken.", 150);
				return false;
			}
		}
		outp.featurePath = featuresCSVField.getText();
		outp.wmntPath = wmntCSVField.getText();
		outp.location = locationField.getText();
		outp.start = startField.getText();
		outp.end = endField.getText();
		outp.classList = classList;
		outp.selectionArray = checkList.getSelectedIndices();
		outp.validEntriesList = validEntriesList;
		boolean classListChanged = false;
		if (editing) {
			for (int i = 0; i < tsbControl.getSubsetList().size(); i++) {
				if (oldID.equals(tsbControl.getSubsetList().get(i).id)) {
					tsbControl.getSubsetList().set(i, outp);
				}
			}
		} else {
			tsbControl.getSubsetList().add(outp);
		}
		ArrayList<String> outpClassList = tsbControl.getFullClassList();
		HashMap<String, String> outpMap = tsbControl.getClassMap();
		for (int i = 0; i < outp.selectionArray.length; i++) {
			if (!outpClassList.contains(classList.get(outp.selectionArray[i]))) {
				outpClassList.add(classList.get(outp.selectionArray[i]));
				tsbControl.getClassMap().put(classList.get(outp.selectionArray[i]), classList.get(outp.selectionArray[i]));
				classListChanged = true;
			}
		}
		if (classListChanged) {
			String[] columnNames = new String[5 + outpClassList.size()];
			columnNames[0] = "ID";
			columnNames[1] = "Location";
			columnNames[2] = "Start";
			columnNames[3] = "End";
			columnNames[4] = "Total";
			for(int i = 0; i < outpClassList.size(); i++) {
				columnNames[i+5] = outpClassList.get(i);
			}
			Class[] typesPre = new Class[columnNames.length];
			for (int i = 0; i < typesPre.length; i++) {
				if (i < 4) {
					typesPre[i] = String.class;
				} else {
					typesPre[i] = Integer.class;
				}
			}
			boolean[] canEditPre = new boolean[columnNames.length];
			for (int i = 0; i < canEditPre.length; i++) {
				canEditPre[i] = false;
			}
			DefaultTableModel subsetTableModel = new DefaultTableModel(columnNames,0) {
				Class[] types = typesPre;
				boolean[] canEdit = canEditPre;
				
				@Override
				public Class getColumnClass(int index) {
					return this.types[index];
				}
				
				@Override
				public boolean isCellEditable(int row, int column) {
					return this.canEdit[column];
				}
			};
			ArrayList<Object[]> rowList = new ArrayList<Object[]>();
			int rowCount = tsbControl.getTabPanel().getPanel().subsetTable.getRowCount();
			if (!editing) {
				rowCount++;
			}
			for (int i = 0; i < rowCount; i++) {
				TSBSubset curr = null;
				if (editing || (!editing && i < rowCount-1)) {
					String currID = (String) tsbControl.getTabPanel().getPanel().subsetTable.getValueAt(i, 0);
					for (int j = 0; j < tsbControl.getSubsetList().size(); j++) {
						if (currID.equals(tsbControl.getSubsetList().get(j).id) || currID.equals(oldID)) {
							curr = tsbControl.getSubsetList().get(j);
							break;
						}
					}
				} else {
					curr = outp;
				}
				if (curr != null) {
					Object[] row = new Object[columnNames.length];
					row[0] = curr.id;
					row[1] = curr.location;
					row[2] = curr.start;
					row[3] = curr.end;
					int currTotal = 0;
					for (int j = 5; j < columnNames.length; j++) {
						int classTotal = 0;
						for (int k = 0; k < curr.selectionArray.length; k++) {
							if (columnNames[j].equals(curr.classList.get(curr.selectionArray[k]))) {
								classTotal = curr.validEntriesList.get(curr.selectionArray[k]).size();
								currTotal += classTotal;
								break;
							}
						}
						row[j] = classTotal;
					}
					row[4] = currTotal;
					//subsetTableModel.addRow(row);
					rowList.add(row);
				}
			}
			//tsbControl.getTabPanel().getPanel().getSubsetTable().setModel(subsetTableModel);
			tsbControl.getTabPanel().getPanel().setTableModel(subsetTableModel);
			tsbControl.getTabPanel().getPanel().getSubsetTable().getColumnModel().getColumn(0).setPreferredWidth(30);
			tsbControl.getTabPanel().getPanel().getSubsetTable().getColumnModel().getColumn(1).setPreferredWidth(100);
			tsbControl.getTabPanel().getPanel().getSubsetTable().getColumnModel().getColumn(2).setPreferredWidth(150);
			tsbControl.getTabPanel().getPanel().getSubsetTable().getColumnModel().getColumn(3).setPreferredWidth(150);
			for (int i = 4; i < tsbControl.getTabPanel().getPanel().getSubsetTable().getColumnCount(); i++) {
				tsbControl.getTabPanel().getPanel().getSubsetTable().getColumnModel().getColumn(i).setPreferredWidth(50);
			}
			for (int i = 0; i < rowList.size(); i++) {
				tsbControl.getTabPanel().getPanel().subsetTableModel.addRow(rowList.get(i));
			}
		} else {
			Object[] row = new Object[tsbControl.getTabPanel().getPanel().subsetTable.getColumnCount()];
			row[0] = outp.id;
			row[1] = outp.location;
			row[2] = outp.start;
			row[3] = outp.end;
			int currTotal = 0;
			for (int j = 5; j < row.length; j++) {
				int classTotal = 0;
				for (int k = 0; k < outp.selectionArray.length; k++) {
					if (tsbControl.getTabPanel().getPanel().getSubsetTable().getColumnName(j).equals(outp.classList.get(outp.selectionArray[k]))) {
						classTotal = outp.validEntriesList.get(outp.selectionArray[k]).size();
						currTotal += classTotal;
						break;
					}
				}
				row[j] = classTotal;
			}
			row[4] = currTotal;
			//subsetTableModel.addRow(row);
			if (editing) {
				for (int i = 0; i < tsbControl.getTabPanel().getPanel().subsetTable.getColumnCount(); i++) {
					tsbControl.getTabPanel().getPanel().subsetTableModel.setValueAt(row[i],
							tsbControl.getTabPanel().getPanel().subsetTable.getSelectedRow(), i);
				}
			} else {
				tsbControl.getTabPanel().getPanel().subsetTableModel.addRow(row);
			}
		}
		tsbControl.setFullClassList(outpClassList);
		tsbControl.setClassMap(outpMap);
		if (tsbControl.getFeatureList().size() == 0) {
			tsbControl.setFeatureList(featureList);
		}
		tsbControl.getTabPanel().getPanel().clearButton.setEnabled(true);
		tsbControl.getTabPanel().getPanel().saveButton.setEnabled(true);
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}
	
}