package mirfeeLiveClassifier;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import PamView.PamTable;
import PamView.dialog.PamDialog;
import PamView.panel.PamPanel;

/**
 * Dialog for displaying Python output after the "List features by usefulness" button is pressed.
 * @author Holly LeBlond
 */
public class LCBestFeaturesDialog extends PamDialog {
	
	protected LCControl lcControl;
	
	protected DefaultTableModel dtm;
	protected PamTable featureTable;
	
	public LCBestFeaturesDialog(Window parentFrame, LCControl lcControl, String pythonOutput) {
		super(parentFrame, lcControl.getUnitName(), true);
		this.lcControl = lcControl;
		
		this.getCancelButton().setVisible(false);
		this.getDefaultButton().setVisible(false);
		
		PamPanel mainPanel = new PamPanel(new FlowLayout());
		
		String[] columnNames = {"Feature name", "F", "p", "Most corr. feat.", "Abs. correlation"};
		dtm = new DefaultTableModel(columnNames,0) {
			Class[] types = {String.class, Double.class, Double.class, String.class, Double.class};
			boolean[] canEdit = {false, false, false, false, false};
			
			@Override
			public Class getColumnClass(int index) {
				return this.types[index];
			}
			
			@Override
			public boolean isCellEditable(int row, int column) {
				return this.canEdit[column];
			}
		};
		featureTable = new PamTable(dtm);
		featureTable.getTableHeader().setReorderingAllowed(false);
		featureTable.setCellSelectionEnabled(true);
		featureTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		featureTable.setAutoCreateRowSorter(true);
		featureTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		featureTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		featureTable.getColumnModel().getColumn(1).setPreferredWidth(50);
		featureTable.getColumnModel().getColumn(2).setPreferredWidth(50);
		featureTable.getColumnModel().getColumn(3).setPreferredWidth(200);
		featureTable.getColumnModel().getColumn(4).setPreferredWidth(50);
		featureTable.getRowSorter().toggleSortOrder(1);
		featureTable.getRowSorter().toggleSortOrder(1); // Doing this twice to make it sort descending.
		JScrollPane sp = new JScrollPane(featureTable);
		sp.setPreferredSize(new Dimension(550,350));
		
		mainPanel.add(sp);
		
		this.setDialogComponent(mainPanel);
		
		String[] tokens = pythonOutput.substring(1, pythonOutput.length()-1).split("\\), \\(");
		for (int i = 0; i < tokens.length; i++) {
			String[] subtokens = tokens[i].split(", ");
			dtm.addRow(new Object[] {subtokens[0], Double.valueOf(String.format("%.1f", Double.valueOf(subtokens[1]))),
					Double.valueOf(subtokens[2]), subtokens[3], Double.valueOf(String.format("%.3f", Double.valueOf(subtokens[4])))});
		}
	}

	@Override
	public boolean getParams() {
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		// Does nothing (no button)
	}

	@Override
	public void restoreDefaultSettings() {
		// Does nothing (no button)
	}
	
}