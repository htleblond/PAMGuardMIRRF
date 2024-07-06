package wmnt;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import PamView.PamTable;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

/**
 * @author Holly LeBlond
 */
public class WMNTCountDialog extends PamDialog {
	
	protected WMNTControl wmntControl;
	
	protected HashMap<String, ArrayList<String>> speciesToCallTypeMap;
	protected HashMap<String, Integer> combinedLabelCountMap;
	protected DefaultTableModel speciesModel;
	protected PamTable speciesTable;
	protected DefaultTableModel callTypeModel;
	protected PamTable callTypeTable;
	
	public WMNTCountDialog(Window parentFrame, WMNTControl wmntControl, PamTable ttable) {
		super(parentFrame, wmntControl.getUnitName(), false);
		this.getCancelButton().setVisible(false);
		
		int noSpeciesLabelCount = 0;
		speciesToCallTypeMap = new HashMap<String, ArrayList<String>>();
		combinedLabelCountMap = new HashMap<String, Integer>();
		for (int i = 0; i < ttable.getRowCount(); i++) {
			String currSpecies = (String) ttable.getValueAt(i, 6);
			String currCallType = (String) ttable.getValueAt(i, 7);
			if (currSpecies.length() == 0) {
				noSpeciesLabelCount++;
				continue;
			}
			if (!speciesToCallTypeMap.containsKey(currSpecies))
				speciesToCallTypeMap.put(currSpecies, new ArrayList<String>());
			if (!speciesToCallTypeMap.get(currSpecies).contains(currCallType)) {
				ArrayList<String> currList = speciesToCallTypeMap.get(currSpecies);
				currList.add(currCallType);
				speciesToCallTypeMap.put(currSpecies, currList);
				combinedLabelCountMap.put(currSpecies+", "+currCallType, 0);
			}
			int val = combinedLabelCountMap.get(currSpecies+", "+currCallType);
			combinedLabelCountMap.put(currSpecies+", "+currCallType, val+1);
		}
		
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints b = new PamGridBagContraints();
		b.fill = GridBagConstraints.HORIZONTAL;
		b.anchor = GridBagConstraints.NORTHWEST;
		
		JPanel topPanel = new JPanel(new GridBagLayout());
		topPanel.setBorder(new TitledBorder("Overall counts"));
		GridBagConstraints c = new PamGridBagContraints();
		c.anchor = c.WEST;
		c.ipadx = 20;
		topPanel.add(new JLabel("Labelled detections"), c);
		c.gridx++;
		topPanel.add(new JLabel(String.valueOf(ttable.getRowCount() - noSpeciesLabelCount)), c);
		c.gridy++;
		c.gridx = 0;
		topPanel.add(new JLabel("Unlabelled detections"), c);
		c.gridx++;
		topPanel.add(new JLabel(String.valueOf(noSpeciesLabelCount)), c);
		c.gridy++;
		c.gridx = 0;
		topPanel.add(new JLabel("Total detections"), c);
		c.gridx++;
		topPanel.add(new JLabel(String.valueOf(ttable.getRowCount())), c);
		mainPanel.add(topPanel, b);
		
		b.gridy++;
		JPanel midPanel = new JPanel(new GridBagLayout());
		midPanel.setBorder(new TitledBorder("Species counts"));
		c = new PamGridBagContraints();
		c.fill = c.HORIZONTAL;
		speciesModel = new DefaultTableModel(new String[] {"Label", "Count"}, 0) {
			Class[] types = {String.class, Integer.class};
			boolean[] canEdit = {false, false};
			
			@Override
			public Class getColumnClass(int index) {
				return this.types[index];
			}
			
			@Override
			public boolean isCellEditable(int row, int column) {
				return this.canEdit[column];
			}
		};
		speciesTable = new PamTable(speciesModel);
		speciesTable.getTableHeader().setReorderingAllowed(false);
		speciesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		speciesTable.setAutoCreateRowSorter(true);
		speciesTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		speciesTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		speciesTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		speciesTable.getRowSorter().toggleSortOrder(1);
		speciesTable.getRowSorter().toggleSortOrder(1); // Doing this twice to make it sort descending.
		JScrollPane speciesScrollPane = new JScrollPane(speciesTable);
		speciesScrollPane.setPreferredSize(new Dimension(250,200));
		midPanel.add(speciesScrollPane, c);
		mainPanel.add(midPanel, b);
		
		b.gridy++;
		JPanel bottomPanel = new JPanel(new GridBagLayout());
		bottomPanel.setBorder(new TitledBorder("Call type counts (of species selected above)"));
		c = new PamGridBagContraints();
		c.fill = c.HORIZONTAL;
		callTypeModel = new DefaultTableModel(new String[] {"Label", "Count"}, 0) {
			Class[] types = {String.class, Integer.class};
			boolean[] canEdit = {false, false};
			
			@Override
			public Class getColumnClass(int index) {
				return this.types[index];
			}
			
			@Override
			public boolean isCellEditable(int row, int column) {
				return this.canEdit[column];
			}
		};
		callTypeTable = new PamTable(callTypeModel);
		callTypeTable.getTableHeader().setReorderingAllowed(false);
		callTypeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		callTypeTable.setAutoCreateRowSorter(true);
		callTypeTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		callTypeTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		callTypeTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		callTypeTable.getRowSorter().toggleSortOrder(1);
		callTypeTable.getRowSorter().toggleSortOrder(1); // Doing this twice to make it sort descending.
		JScrollPane callTypeScrollPane = new JScrollPane(callTypeTable);
		callTypeScrollPane.setPreferredSize(new Dimension(250,200));
		bottomPanel.add(callTypeScrollPane, c);
		mainPanel.add(bottomPanel, b);
		
		setDialogComponent(mainPanel);
		
		Iterator<String> it1 = speciesToCallTypeMap.keySet().iterator();
		while (it1.hasNext()) {
			String key = it1.next();
			ArrayList<String> currList = speciesToCallTypeMap.get(key);
			int currCount = 0;
			for (int i = 0; i < currList.size(); i++) {
				currCount += combinedLabelCountMap.get(key+", "+currList.get(i));
			}
			speciesModel.addRow(new Object[] {key, currCount});
		}
		
		speciesTable.getSelectionModel().addListSelectionListener(new SpeciesTableSelectionListener());
	}
	
	class SpeciesTableSelectionListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			callTypeModel.setRowCount(0);
			if (speciesTable.getSelectedRow() == -1) return;
			String key = (String) speciesTable.getValueAt(speciesTable.getSelectedRow(), 0);
			ArrayList<String> currList = speciesToCallTypeMap.get(key);
			for (int i = 0; i < currList.size(); i++) {
				String currCallType = currList.get(i);
				Integer val = combinedLabelCountMap.get(key+", "+currCallType);
				if (val == null) continue;
				if (currCallType.length() == 0) currCallType = "<none>";
				callTypeModel.addRow(new Object[] {currCallType, val});
			}
		}
	}

	@Override
	public boolean getParams() {
		return true;
	}

	@Override
	public void cancelButtonPressed() {}

	@Override
	public void restoreDefaultSettings() {}
	
}