package wmnt;

import java.awt.*;
import java.awt.List;
import java.awt.event.*;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.*;
import javax.swing.table.*; //
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import javax.swing.filechooser.*;
import java.util.*; //
import java.text.*; //
import java.io.PrintWriter;

import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;
import PamView.PamTable;
import PamView.dialog.PamButton; //
import PamView.dialog.PamTextField; //

import binaryFileStorage.*;
import PamguardMVC.DataUnitBaseData;
import pamScrollSystem.*;
import PamUtils.PamFileChooser;

import java.util.TimeZone;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The panel where the GUI components of the WMNT are written.
 * Contains the JTable and is connected to most functions of the module.
 * @author Holly LeBlond
 */
public class WMNTPanel {

	private WMNTControl wmntControl;
	
	private PamPanel mainPanel;
	
	private PamFileChooser fc;
	
	private PamTextField fileField;
	private PamButton fileButton;
	private PamButton importButton;
	private PamButton exportButton;
	public PamTable ttable;
	private DefaultTableModel dtmodel;
	public DefaultComboBoxModel<String> speciesModel;
	public JComboBox<String> speciesBox;
	public DefaultComboBoxModel<String> calltypeModel;
	public JComboBox<String> calltypeBox;
	private PamTextField commentField;
	private PamButton speciesButton;
	private PamButton calltypeButton;
	private PamButton commentButton;
	private PamButton allButton;
	private PamButton selectAllButton;
	private PamButton clearSelectionButton;
	private PamButton selectStartButton;
	private PamButton searchButton;
	private PamButton connectButton;
	protected PamButton checkButton;
	protected PamButton commitButton;
	private PamButton scrollButton;
	private PamButton undoButton;
	
	public Object[][] originalTable;
	public boolean[] tableChangeLog;
	
	private WMNTSearchDialog searchDialog;
	
	private int currformat;
	
	static private final int textLength = 35;
	
	private WMNTBinaryReader reader;
	protected WMNTMarkControl marker;
	private List dataDates;
	//public int startInterval;
	
	private String defaultloc;
	private File[] files;
	
	private WMNTSQLLogging testLogger;
	
	private int[] backupIndexes;
	private Object[][] backupValues;
	
	private WMNTBinaryLoadingBarWindow loadingBarWindow;
	private LoadingBarThread loadingBarThread;
	
	public WMNTPanel(WMNTControl wmntControl) {
		this.wmntControl = wmntControl;
		currformat = 6;
		//startInterval = 2000;
		testLogger = null;
		backupIndexes = null;
		backupValues = null;
		
		mainPanel = new PamPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		PamPanel memPanel = new PamPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		memPanel.setBorder(new TitledBorder("Whistle and Moan Navigation Tool"));
		
		fileField = new PamTextField(textLength);
		fileField.setEditable(false);
		c.gridwidth = 3;
		memPanel.add(fileField, c);
		
		c.gridx = 3;
		c.gridwidth = 2;
		fileButton = new PamButton("Load binary data");
		OpenListener openListener = new OpenListener();
		fileButton.addActionListener(openListener);
		memPanel.add(fileButton, c);
		
		PamPanel dataPanel = new PamPanel();
		dataPanel.setLayout(new GridLayout(0, 3, 5, 5));
		connectButton = new PamButton("Connect to database");
		ConnectListener connectListener = new ConnectListener();
		connectButton.addActionListener(connectListener);
		dataPanel.add(connectButton);
		checkButton = new PamButton("Check database for alignment");
		CheckListener checkListener = new CheckListener();
		checkButton.addActionListener(checkListener);
		checkButton.setEnabled(false);
		dataPanel.add(checkButton);
		commitButton = new PamButton("Commit to database");
		CommitListener commitListener = new CommitListener();
		commitButton.addActionListener(commitListener);
		commitButton.setEnabled(false);
		dataPanel.add(commitButton);
		importButton = new PamButton("Import table");
		ImportListener importListener = new ImportListener();
		importButton.addActionListener(importListener);
		dataPanel.add(importButton);
		exportButton = new PamButton("Export table");
		ExportListener exportListener = new ExportListener();
		exportButton.addActionListener(exportListener);
		dataPanel.add(exportButton);
		
		
		c.gridy++;
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 5;
		memPanel.add(dataPanel, c);
		
		
		String[] columnNames = {"UID", "Date/Time (UTC)", "LF", "HF", "Dur.", "Amp.", "Species", "Call type", "Comment"};
		
		//Kudos to this: https://stackoverflow.com/questions/9090974/problems-with-jtable-sorting-of-integer-values/9091438
		dtmodel = new DefaultTableModel(columnNames,0) {
			Class[] types = {Long.class, String.class, Integer.class, Integer.class, Integer.class, Integer.class,
					String.class, String.class, String.class};
			boolean[] canEdit = {false, false, false, false, false, false,
					true, true, true};
			
			@Override
			public Class getColumnClass(int index) {
				return this.types[index];
			}
			
			@Override
			public boolean isCellEditable(int row, int column) {
				return this.canEdit[column];
			}
			
		};
		ttable = new PamTable(dtmodel);
		ttable.getTableHeader().setReorderingAllowed(false);
		ttable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		ttable.setAutoCreateRowSorter(true);
		ttable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		ttable.getColumnModel().getColumn(0).setMaxWidth(100);
		ttable.getColumnModel().getColumn(1).setMaxWidth(140);
		ttable.getColumnModel().getColumn(1).setPreferredWidth(140);
		ttable.getColumnModel().getColumn(2).setMaxWidth(40);
		ttable.getColumnModel().getColumn(2).setPreferredWidth(40);
		ttable.getColumnModel().getColumn(3).setMaxWidth(40);
		ttable.getColumnModel().getColumn(3).setPreferredWidth(40);
		ttable.getColumnModel().getColumn(4).setMaxWidth(40);
		ttable.getColumnModel().getColumn(4).setPreferredWidth(40);
		ttable.getColumnModel().getColumn(5).setMaxWidth(40);
		ttable.getColumnModel().getColumn(5).setPreferredWidth(40);
		ttable.getColumnModel().getColumn(6).setMaxWidth(60);
		ttable.getColumnModel().getColumn(6).setPreferredWidth(60);
		ttable.getColumnModel().getColumn(7).setMaxWidth(60);
		ttable.getColumnModel().getColumn(7).setPreferredWidth(60);
		
		JTextField f20 = new JTextField();
		f20.setDocument(new LimitedPlainDocument(20));
		ttable.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(f20));
		ttable.getColumnModel().getColumn(7).setCellEditor(new DefaultCellEditor(f20));
		JTextField f400 = new JTextField();
		f400.setDocument(new LimitedPlainDocument(400));
		ttable.getColumnModel().getColumn(8).setCellEditor(new DefaultCellEditor(f400));

		JScrollPane sp = new JScrollPane(ttable);
		sp.setPreferredSize(new Dimension(300,300));
		
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 5;
		memPanel.add(sp, c);
		
		PamPanel buttonPanel = new PamPanel();
		buttonPanel.setLayout(new GridLayout(0, 3, 5, 5));
		selectStartButton = new PamButton("Select within start interval");
		SelectStartListener selectStartListener = new SelectStartListener();
		selectStartButton.addActionListener(selectStartListener);
		buttonPanel.add(selectStartButton);
		searchButton = new PamButton("Select by search");
		SearchListener searchListener = new SearchListener();
		searchButton.addActionListener(searchListener);
		buttonPanel.add(searchButton);
		scrollButton = new PamButton("Scroll to selection on spectrogram");
		ScrollListener scrollListener = new ScrollListener();
		scrollButton.addActionListener(scrollListener);
		buttonPanel.add(scrollButton);
		selectAllButton = new PamButton("Select all");
		SelectAllListener selectAllListener = new SelectAllListener();
		selectAllButton.addActionListener(selectAllListener);
		buttonPanel.add(selectAllButton);
		clearSelectionButton = new PamButton("Clear selection");
		ClearSelectionListener clearSelectionListener = new ClearSelectionListener();
		clearSelectionButton.addActionListener(clearSelectionListener);
		buttonPanel.add(clearSelectionButton);
		undoButton = new PamButton("Undo");
		UndoListener undoListener = new UndoListener();
		undoButton.addActionListener(undoListener);
		undoButton.setEnabled(false);
		buttonPanel.add(undoButton);
		
		c.gridy++;
		c.fill = GridBagConstraints.HORIZONTAL;
		memPanel.add(buttonPanel, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		String[] speciesString = {"", "False positive", "KW", "HW", "CSL", "PWSD", "KW/HW?", "KW/PWSD?", "Fish", "Vessel", "Mooring",
									"Unk", "Unk-Anthro", "Unk-Odontocete", "Unk-Mysticete", "Unk-Cetacean", "Deployment", "Aliens"};
		speciesModel = new DefaultComboBoxModel<String>(speciesString);
		speciesBox = new JComboBox<String>(speciesModel);
		speciesBox.setPrototypeDisplayValue("aaaaaaaaaaaaaaaaaaaaaaaaa");
		memPanel.add(speciesBox, c);
		
		c.gridx++;
		speciesButton = new PamButton("Species");
		SpeciesListener speciesListener = new SpeciesListener();
		speciesButton.addActionListener(speciesListener);
		memPanel.add(speciesButton, c);
		
		c.gridx++;
		String[] callTypeString = {"","n/a","N01i","N01ii","N01iii","N01iv","N01v","N02","N03",
				"N04","N05i","N05ii","N07i","N07ii","N07iii","N07iv","N08i","N08ii","N08iii","N09i","N09ii",
				"N09iii","N10","N11","N12","N13","N16i","N16ii","N16iii","N16iv","N17","N18","N20","N21",
				"N27","N47","Unnamed Aclan","Unnamed AAsubclan","Unnamed ABsubclan","N23i","N23ii","N24i","N24ii","N25","N26",
				"N28","N29","N30","N39","N40","N41","N44","N45","N46","N48","Unnamed Gclan","Unnamed GGsubclan","Unnamed GIsubclan",
				"N32i","N32ii","N33","N34","N42","N43","N50","N51","N52","Unnamed Rclan","S01","S02i","S02ii","S02iii","S03","S04",
				"S05","S06","S07","S08i","S08ii","S09","S10","S12","S13i","S13ii","S14","S16","S17","S18","S19","S22","S31","S33",
				"S36","S37i","S37ii","S40","S41","S42","S44","Unnamed Jclan"};
		calltypeModel = new DefaultComboBoxModel<String>(callTypeString);
		calltypeBox = new JComboBox<String>(calltypeModel);
		calltypeBox.setPrototypeDisplayValue("aaaaaaaaaaaaaaaaaaaaaaaaa");
		memPanel.add(calltypeBox, c);
		
		c.gridx++;
		calltypeButton = new PamButton("Call type");
		CalltypeListener calltypeListener = new CalltypeListener();
		calltypeButton.addActionListener(calltypeListener);
		memPanel.add(calltypeButton, c);
		
		CommentListener commentListener = new CommentListener();
		
		c.gridx++;
		c.gridheight = 2;
		c.fill = GridBagConstraints.BOTH;
		allButton = new PamButton("Enter\n all");
		AllListener allListener = new AllListener();
		allButton.addActionListener(allListener);
		memPanel.add(allButton, c);
		
		c.gridx = 0;
		c.gridy++;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridheight = 1;
		c.gridwidth = 3;
		commentField = new PamTextField(textLength);
		commentField.setDocument(new JTextFieldLimit(400).getDocument());
		commentField.addActionListener(commentListener);
		memPanel.add(commentField, c);
		
		c.gridx = 3;
		c.gridwidth = 1;
		commentButton = new PamButton("Comment");
		commentButton.addActionListener(commentListener);
		memPanel.add(commentButton, c);
		
		mainPanel.add(memPanel);
		
		this.marker = new WMNTMarkControl("WMNT Contour Selector", this);
		
	/*	String html = "<html><body style='width: %1spx'>%1s";
		int htmllen = 400;
		String message = String.format(html, htmllen, 
				"If this is your first time using this plug-in, it is strongly recommended that you read the manual first. "
				+ "It should be noted that this program is inconveniently still named 'Whistle and Moan Detector' in some parts of PAMGuard, "
				+ "as changing this would result in the plug-in not reading the correct table in MySQL. "
				+ "If you don't have a copy of the manual, or would like to report a bug, the developer can be contacted at "
				+ "wtleblond@gmail.com.\n\n"
				//+ "Version 1.03 updates (August, 2021):\n"
				//+ "� Fixed bug where the search dialog would crash when attempting to read UID values larger than 2^31.\n"
				//+ "� Table column widths changed slightly.\n"
				//+ "� Added SRKW call types to default drop-down list.\n\n"
				//+ "Version 1.04 updates (October, 2021):\n"
				//+ "� Plugin renamed \"Whistle and Moan Navigation Tool\" (WMNT).\n"
				//+ "� Added feature for exporting the table to a .csv or .txt file.\n"
				//+ "� Duration and amplitude, in milliseconds and sound pressure spectrum level, respectively, added as table columns.\n"
				//+ "(NOTE that these are loaded in through MySQL, not the binary files, so a placeholder value of -1 fills these columns\n"
				//+ "until the database has been connected to.)\n"
				//+ "� A few new \"species\" added to defult drop-down list.\n\n"
				+ "Version 1.05 updates (May, 2022):\n"
				+ "� Table now displays date/time values in UTC as opposed to local time.\n"
				+ "� Proper time zones have been implemented, replacing manual time offset; this should fix DST issues.\n"
				+ "� SQL logger now prints results to console when committing entries to the database.\n"
				+ "� Redundant WMD settings removed from settings dialog.\n\n"
				+ "Version 1.06 updates (May, 2022):\n"
				+ "� SQL logger will now only attempt to update entries that were actually modified, significantly reducing the amount of\n"
				+ "time a commit takes (unless the entire table is modified in one go).\n\n"
				+ "Version 1.07 updates (October, 2022):\n"
				+ "� \"Import table\" button added.\n"
				+ "� Dialog windows now set to correct parent frame.");
		JOptionPane.showMessageDialog(wmntControl.getGuiFrame(),
				message,
				"Whistle and Moan Navigation Tool",
				JOptionPane.INFORMATION_MESSAGE); */
	}
	
	/**
	 * Removes all elements from the JTable.
	 * @author Holly LeBlond
	 */
	public void ClearTable() {
		dtmodel.setRowCount(0);
	}
	
	/**
	 * The listener for the 'Select all' button.
	 * @author Holly LeBlond
	 */
	class SelectAllListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			ttable.selectAll();
		}
	}
	
	/**
	 * The listener for the 'Clear all' button.
	 * @author Holly LeBlond
	 */
	class ClearSelectionListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			ttable.clearSelection();
		}
	}
	
	/**
	 * The listener for the 'Select within start interval' button.
	 * @author Holly LeBlond
	 */
	class SelectStartListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			ttable.clearSelection();
			for (int i = 0; i < ttable.getRowCount(); i++) {
				try {
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss+SSS");
					Calendar cal = Calendar.getInstance();
					cal.setTime(df.parse(dataDates.getItem(i)));
					cal.add(Calendar.MILLISECOND, wmntControl.getParams().startBuffer);
					String limdate = df.format(cal.getTime());
					if (ttable.getValueAt(i, 1).toString().compareTo(limdate) < 0) {
						ttable.addRowSelectionInterval(i, i);
					}
				} catch (ParseException e1) {
					e1.printStackTrace();
					SimpleErrorDialog();
				}
			}
		}
	}
	
	/**
	 * The listener for the 'Select by search' button.
	 * @author Holly LeBlond
	 */
	class SearchListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if (ttable.getRowCount() <= 0) {
				JOptionPane.showMessageDialog(wmntControl.getGuiFrame(),
					    "No data loaded into table.",
					    "Selection by search",
					    JOptionPane.WARNING_MESSAGE);
			} else {
				searchDialog = new WMNTSearchDialog(wmntControl.getGuiFrame(), wmntControl, ttable);
				searchDialog.setVisible(true);
			}
		}
	}
	
	/**
	 * The listener for the 'Species' button.
	 * @author Holly LeBlond
	 */
	class SpeciesListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			createBackup();
			String species = speciesBox.getSelectedItem().toString();
			int[] selrows = ttable.getSelectedRows();
			if (selrows.length == 0) {
				return;
			}
			for(int i = 0; i < selrows.length; i++) {
				ttable.setValueAt(species, selrows[i], 6);
				fixChangeLog(selrows[i], getOriginalIndex(selrows[i]));
			}
			updateFromSelectedRows();
		}
	}
	
	/**
	 * The listener for the 'Call type' button.
	 * @author Holly LeBlond
	 */
	class CalltypeListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			createBackup();
			String classification = calltypeBox.getSelectedItem().toString();
			int[] selrows = ttable.getSelectedRows();
			if (selrows.length == 0) {
				return;
			}
			for(int i = 0; i < selrows.length; i++) {
				ttable.setValueAt(classification, selrows[i], 7);
				fixChangeLog(selrows[i], getOriginalIndex(selrows[i]));
			}
			updateFromSelectedRows();
		}
	}
	
	/**
	 * The listener for the 'Comment' button and text field.
	 * @author Holly LeBlond
	 */
	class CommentListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			createBackup();
			String comment = commentField.getText();
			int[] selrows = ttable.getSelectedRows();
			if (selrows.length == 0) {
				return;
			}
			for(int i = 0; i < selrows.length; i++) {
				ttable.setValueAt(comment, selrows[i], 8);
				fixChangeLog(selrows[i], getOriginalIndex(selrows[i]));
			}
			updateFromSelectedRows();
		}
	}
	
	/**
	 * The listener for the 'Enter all' button.
	 * @author Holly LeBlond
	 */
	class AllListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			createBackup();
			String species = speciesBox.getSelectedItem().toString();
			String classification = calltypeBox.getSelectedItem().toString();
			String comment = commentField.getText();
			int[] selrows = ttable.getSelectedRows();
			if (selrows.length == 0) {
				return;
			}
			for(int i = 0; i < selrows.length; i++) {
				ttable.setValueAt(species, selrows[i], 6);
				ttable.setValueAt(classification, selrows[i], 7);
				ttable.setValueAt(comment, selrows[i], 8);
				fixChangeLog(selrows[i], getOriginalIndex(selrows[i]));
			}
			updateFromSelectedRows();
		}
	}
	
	/**
	 * The listener for the 'Connect to database' button.
	 * @author Holly LeBlond
	 */
	class ConnectListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			int res = JOptionPane.showConfirmDialog(wmntControl.getGuiFrame(),
				    "This will turn on AutoCommit.\nProceed?",
				    "Connect to database",
				    JOptionPane.YES_NO_OPTION);
			if (res == JOptionPane.YES_OPTION) {
				res = JOptionPane.showConfirmDialog(wmntControl.getGuiFrame(),
					    "Read data from database into table?\n(Data currently in the table will be overwritten.)\n"
					    + "This may freeze PamGuard for a few seconds.",
					    "Connect to database",
					    JOptionPane.YES_NO_CANCEL_OPTION);
				if (res != JOptionPane.CANCEL_OPTION) {
					fileField.setText("Connecting to database...");
					checkButton.setEnabled(false);
					commitButton.setEnabled(false);
					backupIndexes = null;
					backupValues = null;
					undoButton.setEnabled(false);
					try {
						if (res == JOptionPane.YES_OPTION) {
							testLogger = new WMNTSQLLogging(wmntControl, true);
						} else if (res == JOptionPane.NO_OPTION) {
							testLogger = new WMNTSQLLogging(wmntControl, false);
						}
						updateFromFullTable();
					} catch (Exception e2) {
						testLogger = null;
						e2.printStackTrace();
						SimpleErrorDialog();
					}
					fileField.setText(defaultloc);
				}
			}
		}
	}
	
	/**
	 * The listener for the 'Load data' button.
	 * @author Holly LeBlond
	 */
	class OpenListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			OpenerThread openerThread = new OpenerThread();
			openerThread.start();
		}
	}
	
	/**
	 * Thread used for running the loading bar window.
	 * @author Holly LeBlond
	 */
	protected class LoadingBarThread extends Thread {
		protected LoadingBarThread() {}
		@Override
		public void run() {
			loadingBarWindow.setVisible(true);
		}
	}
	
	/**
	 * Thread used for loading binary file data into the table, 
	 * and to allow the loading bar window to function properly.
	 * @author Holly LeBlond
	 */
	protected class OpenerThread extends Thread {
		protected OpenerThread() {}
		@Override
		public void run() {
			if (ttable.getRowCount() > 0) {
				int res = JOptionPane.showConfirmDialog(wmntControl.getGuiFrame(),
					    "Are you sure?\n(All new uncommitted entries will be lost.)",
					    "Opening data",
					    JOptionPane.YES_NO_OPTION);
				if (res != JOptionPane.YES_OPTION) {
					return;
				}
			}
			
		/*	String[] tz_list = TimeZone.getAvailableIDs();
			String tz = (String)JOptionPane.showInputDialog(wmntControl.getGuiFrame(),
					"Select a time zone.\n"
					+ "(NOTE: This will convert dates/times from the binary files FROM the\n"
					+ "selected time zone to UTC. Therefore, you should select the time zone\n"
					+ "of the computer that processed the audio through the WMD.)", 
	                "Select time zone", JOptionPane.QUESTION_MESSAGE, null, tz_list, wmntControl.getTimezone());
			if (tz == null) {
				return;
			}
			wmntControl.setTimezone(tz); */
			
			try {
				backupIndexes = null;
				backupValues = null;
				undoButton.setEnabled(false);
				defaultloc = findBinaryStorePath();
				fileField.setText("Loading data from binary files...");
				File defFolder = new File(defaultloc);
				FilenameFilter pgdfFilter = new FilenameFilter() {
					@Override
					public boolean accept(File f, String name) {
						return name.endsWith(".pgdf");
					}
				};
				files = defFolder.listFiles(pgdfFilter);
				
				ClearTable();
				
				loadingBarWindow = new WMNTBinaryLoadingBarWindow(wmntControl.getGuiFrame(), files.length);
				loadingBarThread = new LoadingBarThread();
				loadingBarThread.start();
				
				dataDates = new List();
				for (int i = 0; i < files.length; i++) {
					try {
						PopulateTable(files[i]);
					} catch (Exception e2) {
						System.out.println("Error while parsing "+files[i].getName()+".");
						e2.printStackTrace();
					}
					loadingBarWindow.addOneToLoadingBar();
				}
				loadingBarWindow.setVisible(false);
				fileField.setText(defaultloc);
				
			} catch (Exception e2) {
				fileField.setText("Load error - see console.");
				System.out.println(e2);
				System.out.println("Error - tried to open at: " + defaultloc);
			}
			//updateFromFullTable(); // Seems to make updating not work at all for some reason.
			checkButton.setEnabled(false);
			commitButton.setEnabled(false);
			
			if(!(fileField.getText().equals("Load error - see console."))) {
				int res = JOptionPane.showConfirmDialog(wmntControl.getGuiFrame(),
					    "Connect to database as well? (Recommended)\n(This will turn on AutoCommit. PamGuard may also freeze for a few seconds.)",
					    "Opening data",
					    JOptionPane.YES_NO_OPTION);
				if (res == JOptionPane.YES_OPTION) {
					try {
						testLogger = new WMNTSQLLogging(wmntControl, true);
						updateFromFullTable();
					} catch (Exception e2) {
						testLogger = null;
						e2.printStackTrace();
						SimpleErrorDialog();
					}
				}
			}
			originalTable = getTableRowsAsArray();
			tableChangeLog = resetChangeLog(originalTable.length);
		}
	}
	
	/**
	 * Reads from a .pgdf file that contains Whistle and Moan Detector contours and loads them into the JTable.
	 * @param inpfile - The File being read from.
	 * @author Holly LeBlond
	 */
	protected void PopulateTable(File inpfile) {
		try {
			reader = new WMNTBinaryReader(defaultloc + inpfile.getName());
		} catch (Exception e2){
			fileField.setText("Load error - see console.");
			System.out.println("Could not find file: " + inpfile.getName());
			return;
		}
		if (reader.bh.getModuleType().equals("WhistlesMoans") && reader.bh.getStreamName().equals("Contours")) {
			currformat = reader.bh.getHeaderFormat();
			BinaryObjectData curr = reader.nextData(currformat);
			int num = 0;
			while(true){
				num++;
				curr = reader.nextData(currformat);
				DataUnitBaseData dubd = null;
				if (currformat >= 3) {
					dubd = curr.getDataUnitBaseData();
				}
				if (curr.getObjectType() != -4){
					long detectionTime = curr.getTimeMilliseconds();
					long fileTime = reader.bh.getDataDate();
					Date detectionDate = new Date(detectionTime);
					Date fileDate = new Date(fileTime);
					
					String date_format = "yyyy-MM-dd HH:mm:ss+SSS";
					SimpleDateFormat currdateformat = new SimpleDateFormat(date_format);
					String detectionDateString = currdateformat.format(detectionDate);
					String fileDateString = currdateformat.format(fileDate);
					
					detectionDateString = wmntControl.convertBetweenTimeZones(wmntControl.getParams().binaryTZ, "UTC", detectionDateString, true);
					fileDateString = wmntControl.convertBetweenTimeZones(wmntControl.getParams().binaryTZ, "UTC", fileDateString, true);
					if (detectionDateString == null || fileDateString == null) continue;
				/*	LocalDateTime ldt = LocalDateTime.parse(currdate, DateTimeFormatter.ofPattern(date_format));
					LocalDateTime ldt2 = LocalDateTime.parse(datadate, DateTimeFormatter.ofPattern(date_format));
					ZoneId localZoneId = ZoneId.of(wmntControl.getTimezone());
					ZoneId utcZoneId = ZoneId.of("UTC");
					ZonedDateTime localZonedDateTime = ldt.atZone(localZoneId);
					ZonedDateTime localZonedDateTime2 = ldt2.atZone(localZoneId);
					ZonedDateTime utcDateTime = localZonedDateTime.withZoneSameInstant(utcZoneId);
					ZonedDateTime utcDateTime2 = localZonedDateTime2.withZoneSameInstant(utcZoneId);
					DateTimeFormatter dtformat = DateTimeFormatter.ofPattern(date_format);
					currdate = dtformat.format(utcDateTime);
					datadate = dtformat.format(utcDateTime2); */
					dataDates.add(fileDateString);
					if (!(dubd.getUID() <= 0 && detectionDateString.equals("1970-01-01 00:00:00+000"))) {
						if (currformat > 3) {
							double[] freqs = dubd.getFrequency();
							//double dur = dubd.getSampleDuration(); //Millisecond version doesn't work
							//double amp = dubd.getCalculatedAmlitudeDB(); //THIS DOESN'T ACTUALLY WORK
							// THE ABOVE TWO VARIABLES ARE LOADED IN THROUGH THE DATABASE INSTEAD
							dtmodel.addRow(new Object[]{dubd.getUID(), detectionDateString,
									(int)freqs[0], (int)freqs[1], -1, -1, "", "", ""});
						} else  if (currformat == 3){
							dtmodel.addRow(new Object[]{dubd.getUID(), detectionDateString,
									-1, -1, -1, -1, "", "", ""});
						} else {
							dtmodel.addRow(new Object[]{-1, detectionDateString,
									-1, -1, -1, -1, "", "", ""});
						}
					}
				} else {
					break;
				}
			}
		}
		reader.closeReader();
	}
	
	private void updateFromSelectedRows() {
		WMNTDataBlock db = (WMNTDataBlock) wmntControl.getProcess().getOutputDataBlock(0);
		db.updateLC(false);
	}
	
	private void updateFromFullTable() {
		WMNTDataBlock db = (WMNTDataBlock) wmntControl.getProcess().getOutputDataBlock(0);
		db.updateLC(true);
	}
	
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
	
	/**
	 * The listener for the 'Import table' button.
	 * @author Holly LeBlond
	 */
	class ImportListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if (ttable.getRowCount() == 0) {
				SimpleErrorDialog(String.format("<html><body style='width: %1spx'>%1s", 300,
						"Binary data must be loaded in first."
						+ "\n(The purpose of this feature is to provide a backup for any labelling work "
						+ "in case the database connection ends, not to be a replacement for binary files or the database.)"));
				return;
			}
			int res = JOptionPane.showConfirmDialog(wmntControl.getGuiFrame(),
					String.format("<html><body style='width: %1spx'>%1s", 300,
					"This will match labelling data from .csv or .txt files that have been exported from the WMNT. "
					+ "Any entries found in the selected file will be matched to binary data entries currently loaded into the table "
					+ "and will overwrite species, call type and comment data in the matched table entries. "
					+ "It will have no effect on any entries in the table that aren't present in the selected file."
					+ "\nProceed?"),
					"Whistle and Moan Navigation Tool",
					JOptionPane.YES_NO_OPTION);
			if (res == JOptionPane.NO_OPTION) {
				return;
			}
			fc = new PamFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setMultiSelectionEnabled(false);
			fc.addChoosableFileFilter(new FileNameExtensionFilter("WMNT table export file (*.wmnt)","wmnt"));
			fc.addChoosableFileFilter(new FileNameExtensionFilter("Comma-separated values file (*.csv)","csv"));
			fc.addChoosableFileFilter(new FileNameExtensionFilter("Text file (*.txt)","txt"));
			int returnVal = fc.showOpenDialog(wmntControl.getGuiFrame());
			if (returnVal == JFileChooser.CANCEL_OPTION) {
				return;
			}
			File f = getSelectedFileWithExtension(fc);
			if (!f.exists()) {
				SimpleErrorDialog("Selected file does not exist.");
				return;
			}
			int matched = 0;
			int notInTable = 0;
			int invalids = 0;
			Scanner sc;
			try {
				sc = new Scanner(f);
				if (f.getName().endsWith(".csv") || f.getName().endsWith(".wmnt")) {
					sc.nextLine();
				}
				while (sc.hasNextLine()) {
					boolean found = false;
					String[] tokens;
					if (f.getName().endsWith(".csv") || f.getName().endsWith(".wmnt")) {
						tokens = sc.nextLine().split(",");
					} else {
						tokens = sc.nextLine().split("\t");
					}
					if (tokens.length >= 6) {
						for (int i = 0; i < ttable.getRowCount(); i++) {
							if (tokens[0].equals(String.valueOf(ttable.getValueAt(i, 0))) &&
									tokens[1].equals(String.valueOf(ttable.getValueAt(i, 1)))) {
								ttable.setValueAt("", i, 6);
								ttable.setValueAt("", i, 7);
								ttable.setValueAt("", i, 8);
								if (tokens.length >= 7) {
									ttable.setValueAt(tokens[6], i, 6);
									if (tokens.length >= 8) {
										ttable.setValueAt(tokens[7], i, 7);
										if (tokens.length >= 9) {
											ttable.setValueAt(tokens[8], i, 8);
										}
									}
								}
								if (!found) {
									matched++;
								}
								found = true;
							}
						}
						if (!found) {
							notInTable++;
						}
					} else {
						invalids++;
					}
				}
			} catch (Exception e2) {
				e2.printStackTrace();
				SimpleErrorDialog("Parsing error encountered while reading file.");
			}
			updateFromFullTable();
			JOptionPane.showMessageDialog(wmntControl.getGuiFrame(),
					String.format("<html><body style='width: %1spx'>%1s", 300,
					"Matched entries: "+String.valueOf(matched)
					+"\nUnmatched entries in table: "+String.valueOf(ttable.getRowCount()-matched)
					+"\nUnmatched entries in file: "+String.valueOf(notInTable)
					+"\nInvalid entries in file: "+String.valueOf(invalids)),
					"Whistle and Moan Navigation Tool",
					JOptionPane.INFORMATION_MESSAGE);
		}	
	}
	
	/**
	 * The listener for the 'Export table' button.
	 * @author Holly LeBlond
	 */
	class ExportListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if (ttable.getRowCount() > 0) {
				fc = new PamFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setMultiSelectionEnabled(false);
				fc.addChoosableFileFilter(new FileNameExtensionFilter("WMNT table export file (*.wmnt)","wmnt"));
				fc.addChoosableFileFilter(new FileNameExtensionFilter("Comma-separated values file (*.csv)","csv"));
				fc.addChoosableFileFilter(new FileNameExtensionFilter("Text file (*.txt)","txt"));
				int returnVal = fc.showSaveDialog(wmntControl.getGuiFrame());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File f = getSelectedFileWithExtension(fc);
					f.setWritable(true, false);
					if (f.exists() == true) {
						int res = JOptionPane.showConfirmDialog(wmntControl.getGuiFrame(),
								"Overwrite selected file?",
								"Whistle and Moan Navigation Tool",
								JOptionPane.YES_NO_OPTION);
						if (res == JOptionPane.YES_OPTION) {
							f.setExecutable(true);
						} else {
							return;
						}
					} else {
						try {
							f.createNewFile();
						} catch (Exception e2) {
							System.out.println(e2);
							SimpleErrorDialog("Could not create new file.\nSee console for details.");
							return;
						}
					}
					String fn = f.getName();
					System.out.println(fn);
					if (fn.endsWith(".csv") || fn.endsWith(".wmnt")) {
						try {
							PrintWriter pw = new PrintWriter(f);
							StringBuilder sb = new StringBuilder();
							sb.append("uid,datetime,lf,hf,duration,amplitude,species,calltype,comment\n");
							pw.write(sb.toString());
							for (int i = 0; i < ttable.getRowCount(); i++) {
								sb = new StringBuilder();
								for (int j = 0; j < ttable.getColumnCount(); j++) {
									sb.append(ttable.getValueAt(i, j).toString());
									if (j < ttable.getColumnCount() - 1) {
										sb.append(",");
									} else if (i < ttable.getRowCount() - 1) {
										sb.append("\n");
									}
								}
								pw.write(sb.toString());
								pw.flush();
							}
							pw.close();
							JOptionPane.showMessageDialog(wmntControl.getGuiFrame(),
									"Table successfully written to file.",
									"Whistle and Moan Navigation Tool",
									JOptionPane.INFORMATION_MESSAGE);
						} catch (Exception e2) {
							System.out.println(e2);
							SimpleErrorDialog();
						}
					} else if (fn.endsWith(".txt")) {
						try {
							PrintWriter pw = new PrintWriter(f);
							StringBuilder sb = new StringBuilder();
							for (int i = 0; i < ttable.getRowCount(); i++) {
								sb = new StringBuilder();
								for (int j = 0; j < ttable.getColumnCount(); j++) {
									sb.append(ttable.getValueAt(i, j).toString());
									if (j < ttable.getColumnCount() - 1) {
										sb.append("\t");
									} else if (i < ttable.getRowCount() - 1) {
										sb.append("\n");
									}
								}
								pw.write(sb.toString());
								pw.flush();
							}
							pw.close();
							JOptionPane.showMessageDialog(wmntControl.getGuiFrame(),
									"Table successfully written to file.",
									"Whistle and Moan Navigation Tool",
									JOptionPane.INFORMATION_MESSAGE);
						} catch (Exception e2) {
							System.out.println(e2);
							SimpleErrorDialog();
						}
					} else {
						SimpleErrorDialog("Invalid file name or extension.");
					}
				}
			} else {
				SimpleErrorDialog("Table is currently empty.");
			}
		}
	}
	
	/**
	 * The listener for the 'Check database for alignment' button.
	 * @author Holly LeBlond
	 */
	class CheckListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (testLogger != null) {
				testLogger.checkAlignment(ttable);
			}
		}
	}
	
	protected class CommitThread extends Thread {
		protected CommitThread() {}
		@Override
		public void run() {
			if (testLogger != null) {
				int res = JOptionPane.showConfirmDialog(wmntControl.getGuiFrame(),
					    "Are you sure?\n\n(This may freeze PamGuard for a few minutes.\n"
					    + "Progress can be viewed in console.)",
					    "Commit to database",
					    JOptionPane.YES_NO_OPTION);
				if (res == JOptionPane.YES_OPTION) {
					fileField.setText("Committing to database...");
					testLogger.commit(ttable, originalTable, tableChangeLog);
					originalTable = testLogger.getOriginalTable();
					tableChangeLog = testLogger.getChangeLog();
					fileField.setText(defaultloc);
				}
			} else {
				wmntControl.SimpleErrorDialog("Error: SQL logging function was not created.");
			}
		}
	}
	
	/**
	 * The listener for the 'Commit to database' button.
	 * @author Holly LeBlond
	 */
	class CommitListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			CommitThread commitThread = new CommitThread();
			commitThread.start();
		}
	}
	
	/**
	 * The listener for the 'Scroll to selection on spectrogram' button.
	 * @author Holly LeBlond
	 */
	class ScrollListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (ttable.getRowCount() > 0) {
				if (ttable.getSelectedRow() > -1) {
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss+SSS");
					df.setTimeZone(TimeZone.getTimeZone("UTC"));
					String fromTable = ttable.getValueAt(ttable.getSelectedRow(), 1).toString();
					fromTable = wmntControl.convertBetweenTimeZones("UTC", wmntControl.getParams().audioTZ, fromTable, true);
					try {
						Date date = df.parse(fromTable);
						long outpTime = date.getTime();
						ViewerScrollerManager vsm = (ViewerScrollerManager) AbstractScrollManager.getScrollManager();
						for (int i = 0; i < vsm.getPamScrollers().size(); i++) {
							AbstractPamScroller scroller = vsm.getPamScrollers().get(i);
							long duration = scroller.getMaximumMillis() - scroller.getMinimumMillis();
							scroller.anotherScrollerMovedOuter(outpTime-1000, outpTime+duration-1000);
						}
						vsm.loadData(true);
					} catch (ParseException e1) {
						e1.printStackTrace();
						SimpleErrorDialog();
					}
					return;
				}
			}
			JOptionPane.showMessageDialog(wmntControl.getGuiFrame(),
					"No contour has been selected from the table.",
				    "Whistle and Moan Navigation Tool",
				    JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * The listener for the 'Undo' button.
	 * @author Holly LeBlond
	 */
	class UndoListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			undo();
		}
	}
	
	/**
	 * Saves the selected values in 'backupIndexes' and their index numbers in 'backupValues'. 
	 * @author Holly LeBlond
	 */
	private void createBackup() {
		if (ttable.getRowCount() > 0) {
			if (ttable.getSelectedRowCount() > 0) {
				backupIndexes = ttable.getSelectedRows();
				backupValues = new Object[backupIndexes.length][ttable.getModel().getColumnCount()];
				for (int i = 0; i < backupIndexes.length; i++) {
					for (int j = 0; j < ttable.getModel().getColumnCount(); j++) {
						backupValues[i][j] = ttable.getValueAt(backupIndexes[i], j);
					}
				}
				undoButton.setEnabled(true);
			}
		}
	}
	
	/**
	 * Undoes the last change to the table. Currently only goes back once.
	 * @author Holly LeBlond
	 */
	private void undo() {
		if (backupIndexes != null && backupValues != null) {
			int[] oldIndexes = backupIndexes.clone();
			// TODO NOTE: THIS WILL NOT WORK CORRECTLY IF THE TABLE IS SORTED BY A COLUMN VALUE BEFORE PRESSING UNDO
			Object[][] oldValues = backupValues.clone();
			oldIndexes = new int[oldIndexes.length];
			for (int i = 0; i < oldIndexes.length; i++) {
				for (int j = 0; j < ttable.getModel().getRowCount(); j++) {
					if (ttable.getValueAt(j,0).equals(oldValues[i][0]) &&
					    ttable.getValueAt(j,1).equals(oldValues[i][1])) {
						oldIndexes[i] = j;
						break;
					}
				}
			}
			createBackup();
			ttable.clearSelection();
			for (int i = 0; i < oldIndexes.length; i++) {
				ttable.setValueAt(oldValues[i][6], oldIndexes[i], 6);
				ttable.setValueAt(oldValues[i][7], oldIndexes[i], 7);
				ttable.setValueAt(oldValues[i][8], oldIndexes[i], 8);
				ttable.addRowSelectionInterval(oldIndexes[i], oldIndexes[i]);
				fixChangeLog(oldIndexes[i], getOriginalIndex(oldIndexes[i]));
			}
			updateFromSelectedRows();
		}
		undoButton.setEnabled(false);
	}
	
	/**
	 * Streamlined error dialog.
	 * @author Holly LeBlond
	 */
	public void SimpleErrorDialog() {
		JOptionPane.showMessageDialog(wmntControl.getGuiFrame(),
			"An error has occured.\nSee console for details.",
			"",
			JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Streamlined error dialog with an editable message.
	 * @author Holly LeBlond
	 */
	public void SimpleErrorDialog(String inptext) {
		JOptionPane.showMessageDialog(wmntControl.getGuiFrame(),
			inptext,
			"",
			JOptionPane.ERROR_MESSAGE);
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
	 * Sets a limit to the number of characters allowed in a JTable cell.
	 * Copied from: https://stackoverflow.com/questions/28779236/how-to-make-jtable-cell-character-length
	 * Author page: https://stackoverflow.com/users/4617078/bigminimus
	 */
	class LimitedPlainDocument extends javax.swing.text.PlainDocument {
		  private int maxLen = -1;  
		  public LimitedPlainDocument() {}
		  public LimitedPlainDocument(int maxLen) { this.maxLen = maxLen; }
		  public void insertString(int param, String str, 
		                           javax.swing.text.AttributeSet attributeSet) 
		                      throws javax.swing.text.BadLocationException {
		    if (str != null && maxLen > 0 && this.getLength() + str.length() > maxLen) {
		      java.awt.Toolkit.getDefaultToolkit().beep();
		      return;
		    }
		    super.insertString(param, str, attributeSet);
		  }
		}
	
	/**
	 * Finds the path to the binary store.
	 * Copied from PamController as it's private in the library for some reason.
	 * @return The path to the binary store (String).
	 * @author Doug Gillespie
	 */
	public String findBinaryStorePath() {
		BinaryStore binaryControl = BinaryStore.findBinaryStoreControl();
		if (binaryControl == null) {
			return null;
		}
		String storeLoc = binaryControl.getBinaryStoreSettings().getStoreLocation();
		if (storeLoc == null) {
			return "";
		}
		if (storeLoc.endsWith(File.separator) == false) {
			storeLoc += File.separator;
		}
		return storeLoc;
	}
	
	/**
	 * Returns all rows in the table as a 2D Object array.
	 * @return The table as a 2D Object array (Object[][]).
	 * @author Holly LeBlond
	 */
	public Object[][] getTableRowsAsArray(){
		Object[][] outp = new Object[ttable.getModel().getRowCount()][ttable.getModel().getColumnCount()];
		for (int i = 0; i < ttable.getModel().getRowCount(); i++) {
			for (int j = 0; j < ttable.getModel().getColumnCount(); j++) {
				outp[i][j] = ttable.getModel().getValueAt(i, j);
			}
		}
		return outp;
	}
	
	/**
	 * Returns original index of row in table.
	 * @param rowIndex - The current index of the row in the table.
	 * @return The original index of the row in the initial table before any sorting (Integer).
	 * @author Holly LeBlond
	 */
	public int getOriginalIndex(int rowIndex) {
		Object[] row = new Object[2];
		row[0] = ttable.getValueAt(rowIndex, 0);
		row[1] = ttable.getValueAt(rowIndex, 1);
		for (int i = 0; i < originalTable.length; i++) {
			if (originalTable[i][0].equals(row[0]) && originalTable[i][1].equals(row[1])) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Returns an array of false booleans.
	 * @param len - Desired length of array.
	 * @return An array of false booleans (boolean[]).
	 * @author Holly LeBlond
	 */
	public boolean[] resetChangeLog(int len) {
		boolean[] outp = new boolean[len];
		for (int i = 0; i < len; i++) {
			outp[i] = false;
		}
		return outp;
	}
	
	/**
	 * Compares the current values of a row in the table against the values of the same row in the database.
	 * If all values are identical, its respective boolean in tableChangeLog is set to false; otherwise it is set to true.
	 * @param currIndex - Index of row in current table (in case of sorting).
	 * @param origIndex - Index of row in initial table.
	 * @author Holly LeBlond
	 */
	public void fixChangeLog(int currIndex, int origIndex) {
		if (currIndex > -1 && origIndex > -1) {
			if (ttable.getValueAt(currIndex, 6).equals(originalTable[origIndex][6]) &&
				ttable.getValueAt(currIndex, 7).equals(originalTable[origIndex][7]) &&
				ttable.getValueAt(currIndex, 8).equals(originalTable[origIndex][8])) {
				tableChangeLog[origIndex] = false;
			} else {
				tableChangeLog[origIndex] = true;
			}
		} else {
			System.out.println("Indices out of bounds.");
		}
	}
	
	/**
	 * Returns the current format of the last loaded binary file. Default is 6.
	 * @return int - The current format of the loaded binary files.
	 * @author Holly LeBlond
	 */
	public int getCurrFormat() {
		return currformat;
	}
	
	/**
	 * @return mainPanel
	 */
	public JComponent getComponent() {
		return mainPanel;
	}

}
