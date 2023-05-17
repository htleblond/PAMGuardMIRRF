package wmnt;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamView.PamTable;
import PamView.dialog.PamGridBagContraints;
import generalDatabase.DBControl;
import generalDatabase.DBSystem;

/**
 * Used to read from and write to the 'whistle_and_moan_detector' table in the database.
 * @author Taylor LeBlond
 */
public class WMNTSQLLogging {
	
	private WMNTControl wmntControl;
	private DBControl dbControl;
	private DBSystem mySystem;
	private boolean isMySQL;
	private PamTable ttable;
	
	private Statement stmt;
	private ResultSet rs;
	private ResultSet rsColumns;
	private boolean loaded;
	private Set<String> tableSet;
	private Set<String> dbSet;
	private List<String> tableList;
	private List<String> dbList;
	
	private Object[][] originalTable;
	private boolean[] tableChangeLog;
	
	protected volatile WMNTSQLLoadingBarWindow loadingBarWindow;
	protected volatile LoadingBarThread loadingBarThread;
	
	public WMNTSQLLogging(WMNTControl wmntControl, boolean overwrite) {
		this.wmntControl = wmntControl;
		ttable = wmntControl.getSidePanel().getWMNTPanel().ttable;
		dbControl = (DBControl) PamController.getInstance().findControlledUnit(DBControl.getDbUnitType());
		mySystem = dbControl.getDatabaseSystem();
		System.out.println("DB Name: "+mySystem.getDatabaseName());
		isMySQL = !mySystem.getDatabaseName().contains(".sqlite");
		
		try {
			mySystem.getConnection().getConnection().setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
			wmntControl.SimpleErrorDialog();
		}		
		
		//Shoutout to this: https://stackoverflow.com/questions/5809239/query-a-mysql-db-using-java
		
		loaded = true;
		try {
			if (isMySQL) {
				stmt = mySystem.getConnection().getConnection().createStatement();
				rsColumns = stmt.executeQuery("SHOW COLUMNS FROM whistle_and_moan_detector;");
			} else {
				if (!mySystem.getConnection().getConnection().isClosed()) {
					mySystem.getConnection().getConnection().close();
				}
				stmt = mySystem.getConnection().getConnection().createStatement();
				rsColumns = stmt.executeQuery("PRAGMA table_info('whistle_and_moan_detector');");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			wmntControl.getSidePanel().getWMNTPanel().checkButton.setEnabled(false);
			wmntControl.getSidePanel().getWMNTPanel().commitButton.setEnabled(false);
			JOptionPane.showMessageDialog(wmntControl.getGuiFrame(),
				"Could not connect to database - see console for details.\nIf this problem persists, try restarting PamGuard.",
			    "Database error",
			    JOptionPane.ERROR_MESSAGE);
			loaded = false;
		}
		
		
		if (loaded == true) {
			
			tableSet = new HashSet<String>();
			tableList = new ArrayList<String>();
			for (int i = 0; i < ttable.getRowCount(); i++) {
				String entry = ttable.getValueAt(i, 0).toString() + " - "
						+ ttable.getValueAt(i, 1).toString().substring(0, 19);
				tableSet.add(entry);
				tableList.add(entry);
			}
			
			boolean boo1 = false;
			boolean boo2 = false;
			boolean boo3 = false;
			try {
				while (rsColumns.next()) {
					if (rsColumns.getString(1).equals("species")) {
						boo1 = true;
					} else if (rsColumns.getString(1).equals("callType")) {
						boo2 = true;
					} else if (rsColumns.getString(1).equals("comment")) {
						boo3 = true;
					}
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				wmntControl.SimpleErrorDialog();
			}
			
			if (boo1 == false) {
				try {
					stmt.executeUpdate("ALTER TABLE whistle_and_moan_detector\r\n" + 
							"ADD COLUMN species CHAR(20);");
				} catch(SQLException e) {
					e.printStackTrace();
					wmntControl.SimpleErrorDialog();
				}
			}
			if (boo2 == false) {
				try {
					stmt.executeUpdate("ALTER TABLE whistle_and_moan_detector\r\n" + 
							"ADD COLUMN callType CHAR(20);");
				} catch(SQLException e) {
					e.printStackTrace();
					wmntControl.SimpleErrorDialog();
				}
			}
			if (boo3 == false) {
				try {
					stmt.executeUpdate("ALTER TABLE whistle_and_moan_detector\r\n" + 
							"ADD COLUMN comment NVARCHAR(400);");
				} catch(SQLException e) {
					e.printStackTrace();
					wmntControl.SimpleErrorDialog();
				}
			}
			
			try {
				rs = stmt.executeQuery("SELECT * FROM whistle_and_moan_detector;");
				dbSet = new HashSet<String>();
				dbList = new ArrayList<String>();
				int rownum = -1;
				while(rs.next()) {
					String conv = convertDate(String.valueOf(rs.getTimestamp("UTC")).substring(0, 19),
							rs.getShort("UTCMilliseconds"), false);
					//System.out.println(conv);
					String entry = String.valueOf(rs.getLong("UID")) + " - " + conv;
					dbSet.add(entry);
					dbList.add(entry);
					rownum = findRow(String.valueOf(rs.getLong("UID")), conv);
					WMNTAnnotationInfo ai = new WMNTAnnotationInfo();
					if (rownum != -1 && overwrite == true) {
						if (rs.getInt("startSample") > -1 && rs.getDouble("startSeconds") > -1 && rs.getInt("duration") > -1) {
							int sr = (int)(rs.getInt("startSample") / rs.getDouble("startSeconds"));
							int dur = (int)(1000 * (double)rs.getInt("duration") / sr);
							ttable.setValueAt(dur, rownum, 4);
						} else {
							ttable.setValueAt(-1, rownum, 4);
						}
						if (rs.getDouble("amplitude") > -1) {
							ttable.setValueAt((int)rs.getDouble("amplitude"), rownum, 5);
						} else {
							ttable.setValueAt(-1, rownum, 5);
						}
						if (rs.getString("species") != null) {
							ttable.setValueAt(rs.getString("species"), rownum, 6);
							ai.species = rs.getString("species");
							if (wmntControl.getSidePanel().getWMNTPanel().speciesModel.getIndexOf(rs.getString("species")) == -1) {
								wmntControl.getSidePanel().getWMNTPanel().speciesModel.addElement(rs.getString("species"));
							}
						} else {
							ttable.setValueAt("", rownum, 6);
						}
						if (rs.getString("callType") != null) {
							ttable.setValueAt(rs.getString("callType"), rownum, 7);
							ai.callType = rs.getString("callType");
							if (wmntControl.getSidePanel().getWMNTPanel().calltypeModel.getIndexOf(rs.getString("callType")) == -1) {
								wmntControl.getSidePanel().getWMNTPanel().calltypeModel.addElement(rs.getString("callType"));
							}
						} else {
							ttable.setValueAt("", rownum, 7);
						}
						if (rs.getString("comment") != null) {
							ttable.setValueAt(rs.getString("comment"), rownum, 8);
							ai.comment = rs.getString("comment");
						} else {
							ttable.setValueAt("", rownum, 8);
						}
					}
				}
				wmntControl.getSidePanel().getWMNTPanel().checkButton.setEnabled(true);
				wmntControl.getSidePanel().getWMNTPanel().commitButton.setEnabled(true);
			} catch(SQLException e) {
				e.printStackTrace();
				wmntControl.SimpleErrorDialog();
			}
		}
		
	}

	/**
	 * Finds the index of the row in the table with matching UID and date values.
	 * Returns -1 if such row is not present.
	 * @author Taylor LeBlond
	 */
	protected int findRow(String uid, String date) {
		for (int i = 0; i < ttable.getRowCount(); i++) {
			if (ttable.getValueAt(i, 0).toString().equals(uid) &&
					ttable.getValueAt(i, 1).toString().substring(0, 19).equals(date)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Fixes the rounding discrepancy for date/time between the binary files and database
	 * and converts time zones.
	 * Unfortunately, the binary files appear to have the correct time, not the database.
	 * @param inpdate - Date being converted (String).
	 * @param mill - The milliseconds attached to the date (Short).
	 * @param writing - False removes a second and converts to UTC. True adds a second and converts from UTC.
	 * @return The converted date (String).
	 * @author Taylor LeBlond
	 */
	protected String convertDate(String inpdate, short mill, boolean writing) {
		String conv;
		if (!writing) {
			conv = wmntControl.convertBetweenTimeZones(wmntControl.databaseTZ, "UTC", inpdate.substring(0, 19), false);
		} else {
			conv = wmntControl.convertBetweenTimeZones("UTC", wmntControl.databaseTZ, inpdate.substring(0, 19), false);
		}
		if (mill < 500) {
			return conv;
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(df.parse(conv));
			if (!writing) { //DB to table
				cal.add(Calendar.SECOND, -1);
			} else { //Table to DB
				cal.add(Calendar.SECOND, 1);
			}
			return df.format(cal.getTime());
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	/**
	 * For when the 'Check database for alignment' button is pressed.
	 * @param outpTable
	 * @author Taylor LeBlond
	 */
	public void checkAlignment(PamTable outpTable) {
		DefaultListModel<String> tableModel = new DefaultListModel<>();
		JList<String> tableList2 = new JList<String>(tableModel);
		DefaultListModel<String> dbModel = new DefaultListModel<>();
		JList<String> dbList2 = new JList<String>(dbModel);
		for (int i = 0; i < tableList.size(); i++) {
			if (!(dbSet.contains(tableList.get(i)))){
				String entry = tableList.get(i);
				tableModel.addElement(entry);
			}
		}
		for (int i = 0; i < dbList.size(); i++) {
			if (!(tableSet.contains(dbList.get(i)))){
				String entry = dbList.get(i);
				dbModel.addElement(entry);
			}
		}
		
		//Shoutout to this: https://stackoverflow.com/questions/7861724/is-there-a-word-wrap-property-for-jlabel/7861833#7861833
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		String introstr = "The following lists show detections present in either the binary files or the database that are not present in the other.";
		String html = "<html><body style='width: %1spx'>%1s";
		JLabel intro = new JLabel(String.format(html, 300, introstr));
		c.gridwidth = 2;
		mainPanel.add(intro, c);
		c.gridwidth = 1;
		c.gridy++;
		JPanel tablePanel = new JPanel(new GridBagLayout());
		tablePanel.setBorder(new TitledBorder("Present only in binary"));
		JScrollPane sp1 = new JScrollPane(tableList2);
		sp1.setPreferredSize(new Dimension(200,300));
		tablePanel.add(sp1);
		mainPanel.add(tablePanel, c);
		c.gridx++;
		JPanel dbPanel = new JPanel(new GridBagLayout());
		dbPanel.setBorder(new TitledBorder("Present only in database"));
		JScrollPane sp2 = new JScrollPane(dbList2);
		sp2.setPreferredSize(new Dimension(200,300));
		dbPanel.add(sp2);
		mainPanel.add(dbPanel, c);
		JOptionPane.showMessageDialog(wmntControl.getGuiFrame(),
				mainPanel,
				"Checking alignment",
				JOptionPane.INFORMATION_MESSAGE /*,
				new ImageIcon(ClassLoader.getSystemResource("Resources/pamguardIcon.png")).getImage() */);
	}
	
	/**
	 * Thread used for running the loading bar window.
	 * @author Taylor LeBlond
	 */
	protected class LoadingBarThread extends Thread {
		protected LoadingBarThread() {}
		@Override
		public void run() {
			loadingBarWindow.setVisible(true);
		}
	}
	
	/**
	 * Commits to the database.
	 * @param outpTable
	 * @author Taylor LeBlond
	 */
	public void commit(PamTable outpTable, Object[][] origTable, boolean[] changeLog) {
		int totalChanges = 0;
		for (int i = 0; i < changeLog.length; i++) {
			if (changeLog[i] == true) {
				totalChanges++;
			}
		}
		loadingBarWindow = new WMNTSQLLoadingBarWindow(wmntControl.getGuiFrame(), totalChanges);
		loadingBarThread = new LoadingBarThread();
		loadingBarThread.start();
		int y = 0;
		int n = 0;
		System.out.println("\nCOMMITTING TO DATABASE: "+mySystem.getDatabaseName());
		for (int i = 0; i < outpTable.getRowCount(); i++) {
			if (changeLog[getOriginalIndex(i,outpTable,origTable)] == true) {
				String outpSql = "UPDATE whistle_and_moan_detector SET species = ";
				if (outpTable.getValueAt(i, 6).toString().length() > 0) {
					outpSql = outpSql + "\"" + outpTable.getValueAt(i, 6).toString() + "\", callType = ";
				} else {
					outpSql = outpSql + "null, callType = ";
				}
				if (outpTable.getValueAt(i, 7).toString().length() > 0) {
					outpSql = outpSql + "\"" + outpTable.getValueAt(i, 7).toString() + "\", comment = ";
				} else {
					outpSql = outpSql + "null, comment = ";
				}
				if (outpTable.getValueAt(i, 8).toString().length() > 0) {
					outpSql = outpSql + "\"" + outpTable.getValueAt(i, 8).toString() + "\" WHERE UID = ";
				} else {
					outpSql = outpSql + "null WHERE UID = ";
				}
				outpSql = outpSql + outpTable.getValueAt(i, 0).toString() + " AND UTC = '";
				String conv = convertDate(outpTable.getValueAt(i, 1).toString().substring(0, 19),
						Short.valueOf(outpTable.getValueAt(i, 1).toString().substring(20, 23)), true);
				outpSql = outpSql + conv + "';";
				
				boolean success = commitEntry(outpSql);
				if (success == true) {
					y++;
					System.out.println("Change "+(y+n)+" of "+totalChanges+": SUCCESS ("
							+ outpTable.getValueAt(i, 0).toString()+", "+outpTable.getValueAt(i, 1).toString()+", "+outpTable.getValueAt(i, 6).toString()
							+ ", "+outpTable.getValueAt(i, 7).toString()+", "+outpTable.getValueAt(i, 8).toString()+")");
					for (int j = 6; j < outpTable.getModel().getColumnCount(); j++) {
						origTable[getOriginalIndex(i,outpTable,origTable)][j] = outpTable.getValueAt(i, j);
					}
					changeLog[getOriginalIndex(i,outpTable,origTable)] = false;
				} else {
					n++;
					System.out.println("Change "+(y+n)+" of "+totalChanges+": FAILURE ("
							+ outpTable.getValueAt(i, 0).toString()+", "+outpTable.getValueAt(i, 1).toString()+", "+outpTable.getValueAt(i, 6).toString()
							+ ", "+outpTable.getValueAt(i, 7).toString()+", "+outpTable.getValueAt(i, 8).toString()+")");
				}
				loadingBarWindow.updateLoadingBar(success);
			}
		}
		originalTable = origTable;
		tableChangeLog = changeLog;
	/*	JOptionPane.showMessageDialog(wmntControl.getGuiFrame(),
			    "Write successes: " + String.valueOf(y) + "\nWrite failures: " + String.valueOf(n),
			    "Database results",
			    JOptionPane.PLAIN_MESSAGE); */
	}
	
	/**
	 * Executes the SQL for commit().
	 * @param sqlEntry - The SQL update statement. (String)
	 * @return boolean - Whether or not the SQL succeeded or not.
	 * @author Taylor LeBlond
	 */
	protected boolean commitEntry(String sqlEntry) {
		try {
			stmt.execute(sqlEntry);
			if (stmt.getUpdateCount() < 1) {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	protected int getOriginalIndex(int rowIndex, PamTable ttable, Object[][] originalTable) {
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
	
	public Object[][] getOriginalTable(){
		return originalTable;
	}
	
	public boolean[] getChangeLog() {
		return tableChangeLog;
	}
}