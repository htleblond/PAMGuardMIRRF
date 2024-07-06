package wmnt;

import java.awt.*;
import java.awt.List;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.*;
import javax.swing.table.*; //
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import PamController.PamControlledUnit;

import javax.swing.filechooser.*;
import java.util.*; //
import java.text.*; //
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.swing.border.TitledBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;
import PamView.PamTable;
import PamView.dialog.PamButton; //
import PamView.dialog.PamTextField; //

import binaryFileStorage.*;
import fftManager.FFTDataBlock;
import mirrf.MIRRFControlledUnit;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataBlock;
import pamScrollSystem.*;
import whistlesAndMoans.ConnectedRegionDataBlock;
import whistlesAndMoans.ConnectedRegionDataUnit;
import whistlesAndMoans.WhistleToneConnectProcess;
import PamUtils.PamFileChooser;

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

	protected WMNTControl wmntControl;
	
	protected PamPanel mainPanel;
	
	protected PamFileChooser fc;
	
	protected PamTextField fileField;
	protected PamButton fileButton;
	protected PamButton importButton;
	protected PamButton exportButton;
	protected PamButton connectButton;
	protected PamButton checkButton;
	protected PamButton updateButton;
	protected PamButton countButton;
	public PamTable ttable;
	protected DefaultTableModel dtmodel;
	public DefaultComboBoxModel<String> speciesModel;
	public JComboBox<String> speciesBox;
	public DefaultComboBoxModel<String> calltypeModel;
	public JComboBox<String> calltypeBox;
	protected PamTextField commentField;
	protected PamButton speciesButton;
	protected PamButton calltypeButton;
	protected PamButton commentButton;
	protected PamButton allButton;
	//protected PamButton selectAllButton;
	protected PamButton selectFirstUnlabelledButton;
	//protected PamButton clearSelectionButton;
	protected PamButton selectWithinViewButton;
	protected PamButton selectStartButton;
	protected PamButton searchButton;
	protected PamButton scrollButton;
	protected PamButton undoButton;
	
	protected BackupCellEditor speciesCellEditor;
	protected BackupCellEditor callTypeCellEditor;
	protected BackupCellEditor commentCellEditor;
	
	public Object[][] originalTable;
	public boolean[] tableChangeLog;
	
	protected HashMap<String, ConnectedRegionDataUnit> crduMap;
	
	protected WMNTSearchDialog searchDialog;
	
	protected int currformat;
	
	static protected final int textLength = 35;
	
	protected WMNTBinaryReader reader;
	protected WMNTMarkControl marker;
	protected List dataDates;
	
	protected String defaultloc;
	//protected File[] files;
	
	protected WMNTSQLLogging testLogger;
	
	protected int[] backupIndexes;
	protected Object[][] backupValues;
	
	protected WMNTBinaryLoadingBarWindow binaryLoadingBarWindow;
	protected BinaryLoadingBarThread binaryLoadingBarThread;
	
	protected boolean warningForMultipleScrollersTriggeredThisSession = false;
	
	public WMNTPanel(WMNTControl wmntControl) {
		this.wmntControl = wmntControl;
		currformat = 6;
		this.crduMap = new HashMap<String, ConnectedRegionDataUnit>();
		testLogger = new WMNTSQLLogging(wmntControl);
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
		updateButton = new PamButton("Execute database updates");
		UpdateListener updateListener = new UpdateListener();
		updateButton.addActionListener(updateListener);
		updateButton.setEnabled(false);
		dataPanel.add(updateButton);
		importButton = new PamButton("Import table");
		ImportListener importListener = new ImportListener();
		importButton.addActionListener(importListener);
		dataPanel.add(importButton);
		exportButton = new PamButton("Export table");
		ExportListener exportListener = new ExportListener();
		exportButton.addActionListener(exportListener);
		dataPanel.add(exportButton);
		countButton = new PamButton("Label counts");
		CountListener countListener = new CountListener();
		countButton.addActionListener(countListener);
		dataPanel.add(countButton);
		
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
			boolean[] canEdit = {false, false, false, false, false, false, true, true, true};
			
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
		
		JTextField fSpecies = new JTextField();
		fSpecies.setDocument(new CommaRemovingDocument(WMNTSQLLogging.SPECIES_CHAR_LENGTH));
		ttable.getColumnModel().getColumn(6).setCellEditor(speciesCellEditor = new BackupCellEditor(fSpecies));
		JTextField fCallType = new JTextField();
		fCallType.setDocument(new CommaRemovingDocument(WMNTSQLLogging.CALLTYPE_CHAR_LENGTH));
		ttable.getColumnModel().getColumn(7).setCellEditor(callTypeCellEditor = new BackupCellEditor(fCallType));
		JTextField fComment = new JTextField();
		fComment.setDocument(new CommaRemovingDocument(WMNTSQLLogging.COMMENT_NVARCHAR_LENGTH));
		ttable.getColumnModel().getColumn(8).setCellEditor(commentCellEditor = new BackupCellEditor(fComment));
		
		//ttable.getColumnModel().getColumn(6).set
		//System.out.println("Class name: "+ttable.getColumnModel().getColumn(6).getCellEditor().getClass().toString());
		ttable.getColumnModel().getColumn(6).getCellEditor().addCellEditorListener(new CellEditorLoggingListener());
		ttable.getColumnModel().getColumn(7).getCellEditor().addCellEditorListener(new CellEditorLoggingListener());
		ttable.getColumnModel().getColumn(8).getCellEditor().addCellEditorListener(new CellEditorLoggingListener());
		

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
		undoButton = new PamButton("Undo");
		UndoListener undoListener = new UndoListener();
		undoButton.addActionListener(undoListener);
		undoButton.setEnabled(false);
		buttonPanel.add(undoButton);
	/*	selectAllButton = new PamButton("Select all");
		SelectAllListener selectAllListener = new SelectAllListener();
		selectAllButton.addActionListener(selectAllListener);
		buttonPanel.add(selectAllButton); */
		selectFirstUnlabelledButton = new PamButton("Select first unlabelled detection");
		SelectFirstUnlabelledListener selectFirstUnlabelledListener = new SelectFirstUnlabelledListener();
		selectFirstUnlabelledButton.addActionListener(selectFirstUnlabelledListener);
		buttonPanel.add(selectFirstUnlabelledButton);
	/*	clearSelectionButton = new PamButton("Clear selection");
		ClearSelectionListener clearSelectionListener = new ClearSelectionListener();
		clearSelectionButton.addActionListener(clearSelectionListener);
		buttonPanel.add(clearSelectionButton); */
		selectWithinViewButton = new PamButton("Select all in spectrogram view");
		SelectWithinViewListener selectWithinViewListener = new SelectWithinViewListener();
		selectWithinViewButton.addActionListener(selectWithinViewListener);
		buttonPanel.add(selectWithinViewButton);
		scrollButton = new PamButton("Scroll to selection on spectrogram");
		ScrollListener scrollListener = new ScrollListener();
		scrollButton.addActionListener(scrollListener);
		buttonPanel.add(scrollButton);
		
		c.gridy++;
		c.fill = GridBagConstraints.HORIZONTAL;
		memPanel.add(buttonPanel, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		speciesModel = new DefaultComboBoxModel<String>();
		for (int i = 0; i < wmntControl.getParams().speciesList.size(); i++)
			speciesModel.addElement(wmntControl.getParams().speciesList.get(i));
		speciesBox = new JComboBox<String>(speciesModel);
		speciesBox.setPrototypeDisplayValue("aaaaaaaaaaaaaaaaaaaaaaaaa");
		memPanel.add(speciesBox, c);
		
		c.gridx++;
		speciesButton = new PamButton("Species");
		SpeciesListener speciesListener = new SpeciesListener();
		speciesButton.addActionListener(speciesListener);
		memPanel.add(speciesButton, c);
		
		c.gridx++;
		calltypeModel = new DefaultComboBoxModel<String>();
		for (int i = 0; i < wmntControl.getParams().callTypeList.size(); i++)
			calltypeModel.addElement(wmntControl.getParams().callTypeList.get(i));
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
		commentField.setDocument(new JTextFieldLimit(WMNTSQLLogging.COMMENT_NVARCHAR_LENGTH).getDocument());
		commentField.addActionListener(commentListener);
		memPanel.add(commentField, c);
		
		c.gridx = 3;
		c.gridwidth = 1;
		commentButton = new PamButton("Comment");
		commentButton.addActionListener(commentListener);
		memPanel.add(commentButton, c);
		
		mainPanel.add(memPanel);
		
		this.marker = new WMNTMarkControl("WMNT Contour Selector", this);
		
		updateHotkeys();
	}
	
	public void updateHotkeys() {
		//Kudos: https://stackoverflow.com/questions/15313469/java-keyboard-keycodes-list
		mainPanel.getInputMap(mainPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(81, java.awt.event.InputEvent.ALT_DOWN_MASK), "altQ");
		mainPanel.getActionMap().put("altQ", new SelectFirstUnlabelledHotkey());
		mainPanel.getInputMap(mainPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(87, java.awt.event.InputEvent.ALT_DOWN_MASK), "altW");
		mainPanel.getActionMap().put("altW", new ScrollHotkey());
		mainPanel.getInputMap(mainPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(69, java.awt.event.InputEvent.ALT_DOWN_MASK), "altE");
		mainPanel.getActionMap().put("altE", new SelectWithinViewHotkey());
		mainPanel.getInputMap(mainPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(90, java.awt.event.InputEvent.CTRL_DOWN_MASK), "ctrlZ");
		mainPanel.getActionMap().put("ctrlZ", new UndoHotkey());
		for (int i = 0; i < 10; i++) {
			mainPanel.getInputMap(mainPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(48+i, java.awt.event.InputEvent.ALT_DOWN_MASK), "alt"+String.valueOf(i));
			mainPanel.getActionMap().put("alt"+String.valueOf(i), new NumHotkey(i));
		}
	}
	
	interface HotkeyAction extends Action {
		@Override
		public default Object getValue(String key) {return null;}
		@Override
		public default void putValue(String key, Object value) {}
		@Override
		public default void setEnabled(boolean b) {}
		@Override
		public default void addPropertyChangeListener(PropertyChangeListener listener) {}
		@Override
		public default void removePropertyChangeListener(PropertyChangeListener listener) {}
	}
	
	public void stopCellEditing() {
		speciesCellEditor.publicFireEditingStopped();
		callTypeCellEditor.publicFireEditingStopped();
		commentCellEditor.publicFireEditingStopped();
	}
	
	class SelectFirstUnlabelledHotkey implements HotkeyAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			stopCellEditing();
			selectFirstUnlabelledButton.doClick();
		}
		@Override
		public boolean isEnabled() {
			return wmntControl.getParams().hotkeyQEnabled;
		}
	}
	
	class ScrollHotkey implements HotkeyAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			stopCellEditing();
			scrollButton.doClick();
		}
		@Override
		public boolean isEnabled() {
			return wmntControl.getParams().hotkeyWEnabled;
		}
	}
	
	class SelectWithinViewHotkey implements HotkeyAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			stopCellEditing();
			selectWithinViewButton.doClick();
		}
		@Override
		public boolean isEnabled() {
			return wmntControl.getParams().hotkeyEEnabled;
		}
	}
	
	class UndoHotkey implements HotkeyAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			stopCellEditing();
			undoButton.doClick();
		}
		@Override
		public boolean isEnabled() {
			return wmntControl.getParams().hotkeyZEnabled;
		}
	}
	
	class NumHotkey implements HotkeyAction {
		
		int num;
		
		public NumHotkey(int num) {
			this.num = num;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			stopCellEditing();
			String[] vals = wmntControl.getParams().hotkeyNumLabels[num];
			if (vals[0].equals("<clear>")) {
				speciesBox.setSelectedItem("");
			} else if (!vals[0].equals("<skip>")) {
				speciesBox.setSelectedItem(vals[0]);
			}
			if (vals[1].equals("<clear>")) {
				calltypeBox.setSelectedItem("");
			} else if (!vals[1].equals("<skip>")) {
				calltypeBox.setSelectedItem(vals[1]);
			}
			if ((vals[0].equals((String) speciesBox.getSelectedItem()) || vals[0].equals("<skip>") || vals[0].equals("<clear>")) &&
					(vals[1].equals((String) calltypeBox.getSelectedItem()) || vals[1].equals("<skip>") || vals[1].equals("<clear>"))) {
				if (!vals[0].equals("<skip>")) speciesButton.doClick();
				if (!vals[1].equals("<skip>")) calltypeButton.doClick();
				return;
			}
			wmntControl.SimpleErrorDialog("Label(s) assigned to hotkey are no longer in list(s).");
		}
		@Override
		public boolean isEnabled() {
			return wmntControl.getParams().hotkeyNumEnabled[num];
		}
	}
	
	/**
	 * Removes all elements from the JTable.
	 */
	public void clearTable() {
		dtmodel.setRowCount(0);
	}
	
	/**
	 * The listener for the 'Select all' button.
	 */
/*	class SelectAllListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			ttable.selectAll();
		}
	} */
	
	class SelectFirstUnlabelledListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			for (int i = 0; i < ttable.getRowCount(); i++) {
				String val = (String) ttable.getValueAt(i, 6);
				if (val.length() == 0) {
					ttable.clearSelection();
					ttable.addRowSelectionInterval(i, i);
					//ttable.scrollRectToVisible(ttable.getCellRect(ttable.getSelectedRowCount()-1, 0, true));
					ttable.scrollRectToVisible(ttable.getCellRect(i, 0, true));
					return;
				}
			}
			JOptionPane.showMessageDialog(wmntControl.getGuiFrame(),
				    "All detections in table have been labelled.",
				    wmntControl.getUnitName(),
				    JOptionPane.WARNING_MESSAGE);
		}
	}
	
	/**
	 * The listener for the 'Clear all' button.
	 */
/*	class ClearSelectionListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			ttable.clearSelection();
		}
	} */
	
	class SelectWithinViewListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ViewerScrollerManager vsm = (ViewerScrollerManager) AbstractScrollManager.getScrollManager();
			if (vsm.getPamScrollers().size() == 0) {
				JOptionPane.showMessageDialog(wmntControl.getGuiFrame(),
					    "No spectrogram display has been added.",
					    wmntControl.getUnitName(),
					    JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (vsm.getPamScrollers().size() > 1 && !warningForMultipleScrollersTriggeredThisSession) {
				JOptionPane.showMessageDialog(wmntControl.getGuiFrame(),
					    "Multiple scrollers found — only the first one found will be used. If the wrong values are selected, the "
					    + "other displays with scrollers may need to be removed. (This warning should only appear once per session.)",
					    wmntControl.getUnitName(),
					    JOptionPane.WARNING_MESSAGE);
				warningForMultipleScrollersTriggeredThisSession = true;
			}
			AbstractPamScroller scroller = vsm.getPamScrollers().get(0);
			String startTime = MIRRFControlledUnit.convertDateLongToString(scroller.getMinimumMillis());
			startTime = wmntControl.convertBetweenTimeZones(MIRRFControlledUnit.getLocalTimeZoneName(), "UTC", startTime, true);
			String endTime = MIRRFControlledUnit.convertDateLongToString(scroller.getMaximumMillis());
			endTime = wmntControl.convertBetweenTimeZones(MIRRFControlledUnit.getLocalTimeZoneName(), "UTC", endTime, true);
			//System.out.println("startTime: "+startTime);
			//System.out.println("endTime: "+endTime);
			
			ttable.clearSelection();
			int firstScroll = -1;
			int lastScroll = -1;
			for (int i = 0; i < ttable.getRowCount(); i++) {
				String val = ttable.getValueAt(i, 1).toString();
				if (startTime.compareTo(val) <= 0 && endTime.compareTo(val) > 0) {
					ttable.addRowSelectionInterval(i, i);
					if (firstScroll == -1) firstScroll = i;
					lastScroll = i;
				}
			}
			if (firstScroll != -1) {
				ttable.scrollRectToVisible(ttable.getCellRect(lastScroll, 0, true));
				ttable.scrollRectToVisible(ttable.getCellRect(firstScroll, 0, true));
			}
		}
	}
	
	/**
	 * The listener for the 'Select within start interval' button.
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
	 */
	class ConnectListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			ConnectThread connectThread = new ConnectThread();
			connectThread.start();
		}
	}
	
	protected class ConnectThread extends Thread {
		protected ConnectThread() {}
		@Override
		public void run() {
			int res = JOptionPane.showConfirmDialog(wmntControl.getGuiFrame(),
				    "This will turn on AutoCommit.\nProceed?",
				    "Connect to database",
				    JOptionPane.YES_NO_OPTION);
			if (res == JOptionPane.YES_OPTION) {
				res = JOptionPane.showConfirmDialog(wmntControl.getGuiFrame(),
					    "Read data from database into table?\n(Data currently in the table will be overwritten.)\n",
					    "Connect to database",
					    JOptionPane.YES_NO_CANCEL_OPTION);
				if (res != JOptionPane.CANCEL_OPTION) {
					fileField.setText("Connecting to database...");
					checkButton.setEnabled(false);
					updateButton.setEnabled(false);
					backupIndexes = null;
					backupValues = null;
					undoButton.setEnabled(false);
					try {
						//testLogger.openConnection();
						testLogger.createColumnsAndFillTable(res == JOptionPane.YES_OPTION);
					/*	if (res == JOptionPane.YES_OPTION) {
							//testLogger = new WMNTSQLLogging(wmntControl, true);
							testLogger.createColumnsAndFillTable(true);
						} else if (res == JOptionPane.NO_OPTION) {
							//testLogger = new WMNTSQLLogging(wmntControl, false);
							testLogger.createColumnsAndFillTable(false);
						} */
						updateFromFullTable();
						//testLogger.closeConnection();
					} catch (Exception e2) {
						//testLogger.closeConnection();
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
	 */
	class OpenListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			OpenerThread openerThread = new OpenerThread();
			openerThread.start();
		}
	}
	
	/**
	 * Thread used for running the loading bar window.
	 */
	protected class BinaryLoadingBarThread extends Thread {
		protected BinaryLoadingBarThread() {}
		@Override
		public void run() {
			binaryLoadingBarWindow.setVisible(true);
		}
	}
	
	/**
	 * Thread used for loading binary file data into the table, 
	 * and to allow the loading bar window to function properly.
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
			
			try {
				WhistleToneConnectProcess wmDetector = null;
				if (wmntControl.getParams().inputProcessName.length() != 0) {
					PamDataBlock db = wmntControl.getPamController().getDetectorDataBlock(wmntControl.getParams().inputProcessName);
					if (db != null) wmDetector = (WhistleToneConnectProcess) db.getParentProcess();
				} else {
					wmntControl.SimpleErrorDialog("A Whistle and Moan Detector contour source should be selected in the settings first.");
					return;
				}
				if (wmDetector == null) {
					wmntControl.SimpleErrorDialog("No actual Whistle and Moan Detector module appears to be in this configuration. "
							+ "You should add one and ensure the sampling rate, FFT length and FFT hop match those that the contours "
							+ "in the binary files were processed with.");
					return;
				}
				
				backupIndexes = null;
				backupValues = null;
				undoButton.setEnabled(false);
				defaultloc = findBinaryStorePath();
				fileField.setText("Loading data from binary files...");
				File defFolder = new File(defaultloc);
			/*	FilenameFilter pgdfFilter = new FilenameFilter() {
					@Override
					public boolean accept(File f, String name) {
						return name.endsWith(".pgdf");
					}
				}; */
				
				//files = defFolder.listFiles(pgdfFilter);
				ArrayList<File> fileList = new ArrayList<File>();
				ArrayList<File> dirQueue = new ArrayList<File>();
				dirQueue.add(defFolder);
				
				//loadingBarWindow = new WMNTBinaryLoadingBarWindow(wmntControl.getGuiFrame(), fileList.size());
				binaryLoadingBarWindow = new WMNTBinaryLoadingBarWindow(wmntControl.getGuiFrame());
				binaryLoadingBarThread = new BinaryLoadingBarThread();
				binaryLoadingBarThread.start();
				
				while (dirQueue.size() > 0) {
					File currDir = dirQueue.remove(0);
					File[] files = currDir.listFiles();
					for (int i = 0; i < files.length; i++) {
						if (files[i].isDirectory()) {
							//if (checkBinaryStoreSubfolderOption()) dirQueue.add(files[i]);
							dirQueue.add(files[i]); // Disabling it isn't even an option in Viewer Mode for some reason.
						} else if (files[i].getPath().endsWith(".pgdf")) {
							fileList.add(files[i]);
							binaryLoadingBarWindow.addOneToTotalFileCount();
						}
					}
				}
				
				if (fileList.size() == 0) {
					binaryLoadingBarWindow.setVisible(false);
				/*	if (checkBinaryStoreSubfolderOption())
						wmntControl.SimpleErrorDialog("No Whistle and Moan Detector binary files were found in the specified "
								+ "binary file folder.");
					else
						wmntControl.SimpleErrorDialog("No Whistle and Moan Detector binary files were found in the specified "
								+ "binary file folder or any of its subfolders."); */
					wmntControl.SimpleErrorDialog("No Whistle and Moan Detector binary files were found in the specified "
							+ "binary file folder or any of its subfolders.");
					return;
				}
				
				clearTable();
				
				// moved
			/*	loadingBarWindow = new WMNTBinaryLoadingBarWindow(wmntControl.getGuiFrame(), fileList.size());
				loadingBarThread = new LoadingBarThread();
				loadingBarThread.start(); */
				
				dataDates = new List();
				
				for (int i = 0; i < fileList.size(); i++) {
					try {
						PopulateTable(fileList.get(i), wmDetector);
					} catch (Exception e2) {
						System.out.println("Error while parsing "+fileList.get(i).getName()+".");
						e2.printStackTrace();
					}
					binaryLoadingBarWindow.addOneToLoadingBar();
				}
				binaryLoadingBarWindow.setVisible(false);
				fileField.setText(defaultloc);
				
			} catch (Exception e2) {
				fileField.setText("Load error - see console.");
				e2.printStackTrace();
				System.out.println("Error - tried to open at: " + defaultloc);
			}
			//updateFromFullTable(); // Seems to make updating not work at all for some reason.
			checkButton.setEnabled(false);
			updateButton.setEnabled(false);
			
			if(!(fileField.getText().equals("Load error - see console."))) {
				int res = JOptionPane.showConfirmDialog(wmntControl.getGuiFrame(),
					    "Connect to database as well? (Recommended)\n(This will turn on AutoCommit.)",
					    "Opening data",
					    JOptionPane.YES_NO_OPTION);
				if (res == JOptionPane.YES_OPTION) {
					try {
						testLogger.createColumnsAndFillTable(true);
						updateFromFullTable();
					} catch (Exception e2) {
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
	 */
	protected void PopulateTable(File inpfile, WhistleToneConnectProcess wmDetector) {
		try {
			reader = new WMNTBinaryReader(wmntControl, inpfile.getPath(), wmDetector);
		} catch (Exception e2){
			fileField.setText("Load error - see console.");
			System.out.println("Could not find file: " + inpfile.getPath());
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
					
					detectionDateString = wmntControl.convertBetweenTimeZones(MIRRFControlledUnit.getLocalTimeZoneName(), "UTC", detectionDateString, true);
					fileDateString = wmntControl.convertBetweenTimeZones(MIRRFControlledUnit.getLocalTimeZoneName(), "UTC", fileDateString, true);
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
						crduMap.put(String.valueOf(dubd.getUID())+", "+detectionDateString,
								reader.getDataAsDataUnit(curr, reader.bh, reader.bh.getHeaderFormat()));
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
					"This will match labelling data from .wmnt, .csv or .txt files that have been exported from the WMNT. "
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
								fixChangeLog(i, getOriginalIndex(i));
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
	 */
	class ExportListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if (ttable.getRowCount() == 0) {
				SimpleErrorDialog("Table is currently empty.");
			}	
			fc = new PamFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setMultiSelectionEnabled(false);
			//fc.addChoosableFileFilter(new FileNameExtensionFilter("Experimental WMNT table export file (*.wmnte)","wmnte"));
			fc.addChoosableFileFilter(new FileNameExtensionFilter("WMNT table export file (*.wmnt)","wmnt"));
			fc.addChoosableFileFilter(new FileNameExtensionFilter("Comma-separated values file (*.csv)","csv"));
			fc.addChoosableFileFilter(new FileNameExtensionFilter("Text file (*.txt)","txt"));
			int returnVal = fc.showSaveDialog(wmntControl.getGuiFrame());
			if (returnVal != JFileChooser.APPROVE_OPTION) return;
			File f = getSelectedFileWithExtension(fc);
			f.setWritable(true, false);
			if (f.exists()) {
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
					e2.printStackTrace();
					SimpleErrorDialog("Could not create new file.\nSee console for details.");
					return;
				}
			}
			String fn = f.getName();
			if (fn.endsWith(".csv") || fn.endsWith(".wmnt")) {
				try {
					PrintWriter pw = new PrintWriter(f);
					StringBuilder sb = new StringBuilder();
					sb.append("uid,datetime,lf,hf,duration,amplitude,species,calltype,comment,slicedata\n");
					pw.write(sb.toString());
					for (int i = 0; i < ttable.getRowCount(); i++) {
						sb = new StringBuilder();
						for (int j = 0; j < ttable.getColumnCount(); j++) {
							sb.append(ttable.getValueAt(i, j).toString());
							if (j < ttable.getColumnCount() - 1) {
								sb.append(",");
								continue;
							}
							if (fn.endsWith(".wmnt")) {
								ConnectedRegionDataUnit crdu = 
										crduMap.get(ttable.getValueAt(i, 0).toString()+", "
												+ttable.getValueAt(i, 1).toString());
								WhistleToneConnectProcess wmDetector = null;
								if (wmntControl.getParams().inputProcessName.length() != 0) {
									PamDataBlock db = 
											wmntControl.getPamController().getDetectorDataBlock(
													wmntControl.getParams().inputProcessName);
									if (db != null) {
										wmDetector = (WhistleToneConnectProcess) db.getParentProcess();
										FFTDataBlock fftDB = (FFTDataBlock) wmDetector.getParentDataBlock();
										WMNTSliceDataCalculator calc =
												new WMNTSliceDataCalculator(crdu.getConnectedRegion().getSliceData(),
												(int) wmDetector.getSampleRate(), fftDB.getFftLength());
										double[] freqs = calc.getFreqs();
										for (int k = 0; k < freqs.length; k++)
											sb.append(","+String.valueOf(calc.sliceDataArr[k].getStartSample())+
													">"+String.valueOf(freqs[k]));
										//System.out.println(ttable.getValueAt(i, 0).toString()+": "+
										//		String.valueOf(calc.calculateFreqElbowAngle()));
									}
								}
							}
							sb.append("\n");
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
					e2.printStackTrace();
					SimpleErrorDialog();
					return;
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
			}
		}
	}
	
	/**
	 * The listener for the 'Check database for alignment' button.
	 */
	class CheckListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (testLogger != null) {
				testLogger.checkAlignment(ttable);
			}
		}
	}
	
	class CountListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			WMNTCountDialog countDialog = new WMNTCountDialog(wmntControl.getGuiFrame(), wmntControl, ttable);
			countDialog.setVisible(true);
		}
	}
	
	protected class UpdateThread extends Thread {
		protected UpdateThread() {}
		@Override
		public void run() {
			if (testLogger != null) {
				int res = JOptionPane.showConfirmDialog(wmntControl.getGuiFrame(),
					    MIRRFControlledUnit.makeHTML("Would you like to commit all changes now?\n"
							    + "(This includes database changes made by other modules. If you press \"no\", the SQL update commands\n"
							    + "will be executed, but won't be committed until you press \"File > Save Data\". If AutoCommit was\n"
							    + "turned on at startup, the commit will happen automatically regardless.)", 350),
					    wmntControl.getUnitName(),
					    JOptionPane.YES_NO_CANCEL_OPTION);
				if (res == JOptionPane.CANCEL_OPTION) return;
				fileField.setText("Committing to database...");
				//testLogger.openConnection();
				testLogger.executeUpdates(ttable, originalTable, tableChangeLog, res == JOptionPane.YES_OPTION);
				originalTable = testLogger.getOriginalTable();
				tableChangeLog = testLogger.getChangeLog();
				//testLogger.closeConnection();
				fileField.setText(defaultloc);
			} else {
				wmntControl.SimpleErrorDialog("Error: SQL logging function was not created.");
			}
		}
	}
	
	/**
	 * The listener for the 'Commit to database' button.
	 */
	class UpdateListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			UpdateThread updateThread = new UpdateThread();
			updateThread.start();
		}
	}
	
	/**
	 * The listener for the 'Scroll to selection on spectrogram' button.
	 */
	class ScrollListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (ttable.getRowCount() > 0) {
				if (ttable.getSelectedRow() > -1) {
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss+SSS");
					df.setTimeZone(TimeZone.getTimeZone("UTC"));
					String fromTable = ttable.getValueAt(ttable.getSelectedRow(), 1).toString();
					//fromTable = wmntControl.convertBetweenTimeZones("UTC", MIRRFControlledUnit.getLocalTimeZoneName(), fromTable, true);
					try {
						Date date = df.parse(fromTable);
						long outpTime = date.getTime();
						ViewerScrollerManager vsm = (ViewerScrollerManager) AbstractScrollManager.getScrollManager();
						for (int i = 0; i < vsm.getPamScrollers().size(); i++) {
							AbstractPamScroller scroller = vsm.getPamScrollers().get(i);
							long duration = scroller.getMaximumMillis() - scroller.getMinimumMillis();
							int buffer = wmntControl.getParams().scrollBuffer;
							scroller.anotherScrollerMovedOuter(outpTime-buffer, outpTime+duration-buffer);
							//System.out.println(MIRRFControlledUnit.convertDateLongToString(scroller.getMinimumMillis()));
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
	 */
	class UndoListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			undo();
		}
	}
	
	/**
	 * Saves the selected values in 'backupIndexes' and their index numbers in 'backupValues'. 
	 */
	private void createBackup() {
		if (ttable.getRowCount() > 0 && ttable.getSelectedRowCount() > 0) {
			// NOTE: getSelectedRows() does NOT actually cause any issues if the rows are re-arranged.
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
	
	/**
	 * Undoes the last change to the table. Currently only goes back once.
	 */
	private void undo() {
		if (backupIndexes != null && backupValues != null) {
			int[] oldIndexes = backupIndexes.clone();
			// NOTE: This still works even if you sort by a column.
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
	 */
	public void SimpleErrorDialog() {
		JOptionPane.showMessageDialog(wmntControl.getGuiFrame(),
			"An error has occured.\nSee console for details.",
			"",
			JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Streamlined error dialog with an editable message.
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
	 * Extension of the LimitedPlainDocument below that prevents commas from being input,
	 * in addition to keeping input strings below a certain length.
	 */
	protected class CommaRemovingDocument extends LimitedPlainDocument {
		public CommaRemovingDocument(int maxLen) {
			this.maxLen = maxLen; 
		}
		
		@Override
		public void insertString(int param, String str, javax.swing.text.AttributeSet attributeSet) 
				 throws javax.swing.text.BadLocationException {
			if (str == null) return;
			super.insertString(param, str.replaceAll(",", ""), attributeSet);
		}
	}
	
	protected class BackupCellEditor extends DefaultCellEditor {

		public BackupCellEditor(JTextField textField) {
			super(textField);
		}
		
		public void publicFireEditingStopped() {
			fireEditingStopped();
		}
		
		@Override
		public boolean stopCellEditing() {
			createBackup();
			return super.stopCellEditing();
		}
		
	}
	
	protected class CellEditorLoggingListener implements CellEditorListener {
		
		@Override
		public void editingStopped(ChangeEvent e) {
			// Backup done through BackupCellEditor
			int[] selrows = ttable.getSelectedRows();
			if (selrows.length == 0) return;
			for(int i = 0; i < selrows.length; i++) // Only because it's possible for multiple cells to be selected while you're only editing one
				fixChangeLog(selrows[i], getOriginalIndex(selrows[i]));
			updateFromSelectedRows();
		}

		@Override
		public void editingCanceled(ChangeEvent e) {}
		
	}
	
	/**
	 * Sets a limit to the number of characters allowed in a JTable cell.
	 * Copied from: https://stackoverflow.com/questions/28779236/how-to-make-jtable-cell-character-length
	 * Author page: https://stackoverflow.com/users/4617078/bigminimus
	 */
	private class LimitedPlainDocument extends javax.swing.text.PlainDocument {
		 protected int maxLen = -1;  
		 public LimitedPlainDocument() {}
		 public LimitedPlainDocument(int maxLen) { this.maxLen = maxLen; }
		 public void insertString(int param, String str, javax.swing.text.AttributeSet attributeSet) 
				 throws javax.swing.text.BadLocationException {
			 if (str != null && maxLen > 0 && this.getLength() + str.length() > maxLen) {
				 //java.awt.Toolkit.getDefaultToolkit().beep();
				 return;
			 }
			 super.insertString(param, str, attributeSet);
		 }
	}
	
	/**
	 * Finds the path to the binary store.
	 * Copied from PamController as it's private in the library for some reason.
	 * @return The path to the binary store (String).
	 * @author Doug Gillespie (in PamController)
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
	
	public boolean checkBinaryStoreSubfolderOption() {
		BinaryStore binaryControl = BinaryStore.findBinaryStoreControl();
		if (binaryControl == null) {
			return false;
		}
		return binaryControl.getBinaryStoreSettings().datedSubFolders;
	}
	
	/**
	 * Returns all rows in the table as a 2D Object array.
	 * @return The table as a 2D Object array (Object[][]).
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
	 */
	public boolean[] resetChangeLog(int len) {
		boolean[] outp = new boolean[len];
		for (int i = 0; i < len; i++) {
			outp[i] = false;
		}
		return outp;
	}
	
	/**
	 * Map of ConnectedRegionDataUnits that match table entries via
	 * "[uid], [timestamp]" as the key. Mainly meant for dealing with
	 * slice data.
	 */
	public HashMap<String, ConnectedRegionDataUnit> getCRDUMap(){
		return crduMap;
	}
	
	/**
	 * Compares the current values of a row in the table against the values of the same row in the database.
	 * If all values are identical, its respective boolean in tableChangeLog is set to false; otherwise it is set to true.
	 * @param currIndex - Index of row in current table (in case of sorting).
	 * @param origIndex - Index of row in initial table.
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
