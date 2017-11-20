/*
 * $Id$
 *
 * Copyright 2015 Valentyn Kolesnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ass.file;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import ass.file.importer.FileImporter;
import ass.keyboard.macro.ListenForMacroChanges;
import ass.keyboard.macro.MacroAction;
import ass.keyboard.macro.MacroEditor;
import ass.keyboard.macro.MacroLoader;
import constants.Constants;
import file.ObjectSerializer;
import icons.Icons;
import logger.Logger;

/**
 * Credits :
 * 
 * The skeleton for this class comes from Andrew Thompson, and the Right Click menu comes from codejava.net/
 * Codejava.net : http://www.codejava.net/java-se/swing/jtable-popup-menu-example
 *
 */
public class Manager extends JPanel implements ActionListener, ListenForMacroChanges {

	//

	private ObjectSerializer<ArrayList<File>> importedFilesSerialiser = new ObjectSerializer<ArrayList<File>>("imported.ser");

	/** Directory listing */
	private JTable table;

	//

	/** Table model for File[]. */
	private FileTableModel fileTableModel;
	private ListSelectionListener listSelectionListener;
	private boolean cellSizesSet = false;
	private int rowIconPadding = 6;

	/** Popup menu on right click **/
	private JPopupMenu popupMenu;

	//

	public Visualiser fileVisualiser;
	private FileImporter fileImporter;

	//

	public Manager() {

		fileImporter = new FileImporter(this);
		fileVisualiser = new Visualiser();

		/* :) */

		setLayout(new BorderLayout(3, 3));
		setBorder(new EmptyBorder(5, 5, 5, 5));

		// Popup menu

		/**
		 * Popup menu
		 * @see macroChanged(ArrayList<MacroAction> newMacros)
		 */
		popupMenu = new JPopupMenu();

		//End popup menu

		/* Provides nice icons and names for files. */
		FileSystemView fileSystemView = FileSystemView.getFileSystemView();

		JPanel detailView = new JPanel(new BorderLayout(3, 3));

		table = new JTable();
		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setAutoCreateRowSorter(true);
		table.setShowVerticalLines(false);
		table.setSelectionBackground(Constants.SICK_PURPLE);
		table.setSelectionForeground(Color.WHITE);

		table.setComponentPopupMenu(popupMenu);

		fileTableModel = new FileTableModel();
		table.setModel(fileTableModel);

		//set font bold for column 1 (see CellRenderer at the bottom of this class)
		table.getColumnModel().getColumn(1).setCellRenderer(new CellRenderer());

		listSelectionListener = new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();

				boolean isAdjusting = e.getValueIsAdjusting();

				if (!lsm.isSelectionEmpty()) {
					// Find out which indexes are selected.
					int minIndex = lsm.getMinSelectionIndex();
					int maxIndex = lsm.getMaxSelectionIndex();

					ArrayList<File> selectedFiles = new ArrayList<File>();

					for (int i = minIndex; i <= maxIndex; i++) {
						if (lsm.isSelectedIndex(i)) {

							//Fixes row being incorrect after sortings
							int row = table.convertRowIndexToModel(i);

							selectedFiles.add(fileTableModel.getFile(row));
						}
					}

					if (!isAdjusting) {
						if (selectedFiles.size() == 1) { //amount of selected files == 1

							File selecteFiled = selectedFiles.get(0);

							fileVisualiser.setSelectedFile(selecteFiled);

						} else if (selectedFiles.size() > 1) { //more than 1 selected file

							fileVisualiser.setSelectedFiles(selectedFiles);

						}

						//Update toolbar and menu
						tellSelectedFilesChanged();

					}

				}

			}

		};
		table.getSelectionModel().addListSelectionListener(listSelectionListener);

		JScrollPane tableScroll = new JScrollPane(table);
		Dimension d = tableScroll.getPreferredSize();
		tableScroll.setPreferredSize(new Dimension((int) d.getWidth(), (int) d.getHeight() / 2));
		detailView.add(tableScroll, BorderLayout.CENTER);

		add(detailView, BorderLayout.CENTER);

		JPanel simpleOutput = new JPanel(new BorderLayout(3, 3));
		JProgressBar progressBar = new JProgressBar();
		simpleOutput.add(progressBar, BorderLayout.EAST);
		progressBar.setVisible(false);

		add(simpleOutput, BorderLayout.SOUTH);

		//Import files stored in serialiser (saves from session to session imported files)
		if (!importedFilesSerialiser.isNull()) {
			importFiles(importedFilesSerialiser.get());
		}

	}

	@Override
	public void actionPerformed(ActionEvent event) {

		if (event.getSource() instanceof FileMenuItem) {
			FileMenuItem menu = (FileMenuItem) event.getSource();
			System.out.println(
					"Hey, its me, menu! I've been clicked on. My master is way too busy fixing important shit, so he left this little message here to remind himself that he hasnt broken this yet.");
			//menu.updateAndPerform(fileTableModel.getFiles().size() == 1);
		}

	}

	/*private void removeAllRows() {
	
		System.out.println("Add new row");
		tableModel.addRow(new String[0]);
	
		System.out.println("Remove current row");
		int selectedRow = table.getSelectedRow();
		tableModel.removeRow(selectedRow);
	
		System.out.println("Add new row");
		int rowCount = tableModel.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			tableModel.removeRow(0);
		}
	}*/

	//Used by FileImporter and importedFileSerialise
	public void importFiles(ArrayList<File> filesToImport) {

		final ArrayList<File> oldFile = fileTableModel.getFiles();

		ArrayList<File> newFile = new ArrayList<File>();
		newFile.addAll(oldFile);

		for (File file : filesToImport) {
			if (!oldFile.contains(file) && file.exists()) {
				newFile.add(file);
			} else {
				String error = "";

				if (oldFile.contains(file)) {
					error = "is a duplicate file";
				} else {
					error = "doesn't exist at this path '" + file.getAbsolutePath() + "'";
				}

				String TAG = "FileManager";
				Logger.logWarning(TAG, "Warning : File '" + file.getName() + "' " + error + ".");

			}
		}

		setTableData(newFile);

	}

	/** Update the table on the EDT */
	private void setTableData(ArrayList<File> files) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				table.getSelectionModel().removeListSelectionListener(listSelectionListener);
				fileTableModel.setFiles(files);
				table.getSelectionModel().addListSelectionListener(listSelectionListener);
				if (!cellSizesSet) {

					int rowHeight = 32;

					// size adjustment to better account for icons
					table.setRowHeight(rowHeight); //TODO make this an editeable property

					setColumnWidth(0, -1);
					// setColumnWidth(3, 60);
					// table.getColumnModel().getColumn(3).setMaxWidth(120);
					// setColumnWidth(4, -1);

					cellSizesSet = true;
				}
			}
		});

		importedFilesSerialiser.set(files);
	}

	private void setColumnWidth(int column, int width) {
		TableColumn tableColumn = table.getColumnModel().getColumn(column);
		if (width < 0) {
			// use the preferred width of the header..
			JLabel label = new JLabel((String) tableColumn.getHeaderValue());
			Dimension preferred = label.getPreferredSize();
			// altered 10->14 as per camickr comment.
			width = (int) preferred.getWidth() + 14;
		}
		tableColumn.setPreferredWidth(width);
		tableColumn.setMaxWidth(width);
		tableColumn.setMinWidth(width);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception weTried) {
					weTried.printStackTrace();
				}
				JFrame f = new JFrame(Constants.SOFTWARE_NAME);
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				Manager fileManager = new Manager();
				f.setContentPane(fileManager);

				f.setIconImage(Icons.SOFTWARE_ICON.getImage());

				f.pack();
				f.setLocationByPlatform(true);
				f.setMinimumSize(f.getSize());
				f.setVisible(true);
			}
		});
	}

	public void showFileImporter() {
		fileImporter.setVisible(true);
	}

	@Override
	public void macroChanged(ArrayList<MacroAction> newMacros) {

		System.out.println("macroChanged");

		popupMenu.removeAll();

		for (MacroAction ma : newMacros) { //populate JMenuItem

			if (ma.showInMenu) {
				JMenuItem item = new FileMenuItem(ma);
				item.addActionListener(this);
				popupMenu.add(item);
			}
		}
	}

	//Waiting for selected files change (used by MacroLoader to update weither MacroAction is enabled based on policy)

	private ArrayList<ListenForSelectedFilesChanges> waitingForChanges = new ArrayList<>();

	public void registerWaitingForFileChanges(ListenForSelectedFilesChanges f) {
		waitingForChanges.add(f);
	}

	public void tellSelectedFilesChanged() {

		System.out.println(fileVisualiser.getSelectedFiles().size());

		for (ListenForSelectedFilesChanges l : waitingForChanges) {

			l.filesChanged(fileVisualiser.getSelectedFiles().size());
		}

	}

}

/** A TableModel to hold File[]. */
class FileTableModel extends AbstractTableModel {

	private static final String TAG = null;
	private ArrayList<File> files;

	private ArrayList<File> selectedFiles;

	private FileSystemView fileSystemView = FileSystemView.getFileSystemView();
	private String[] columns = { "Icon", "File", "Path", "Size", "Last Modified" };

	FileTableModel() {
		files = new ArrayList<File>();
	}

	public Object getValueAt(int row, int column) {
		File file = files.get(row);
		switch (column) {
		case 0:
			return fileSystemView.getSystemIcon(file);
		case 1:
			return fileSystemView.getSystemDisplayName(file);
		case 2:
			return file.getPath();
		case 3:
			return file.length();
		case 4:
			return file.lastModified();
		default:
			System.err.println("Logic Error");
		}
		return "";
	}

	public int getColumnCount() {
		return columns.length;
	}

	public Class<?> getColumnClass(int column) {
		switch (column) {
		case 0:
			return ImageIcon.class;
		case 3:
			return Long.class;
		case 4:
			return Date.class;
		}
		return String.class;
	}

	public String getColumnName(int column) {
		return columns[column];
	}

	public int getRowCount() {
		return files.size();
	}

	public File getFile(int row) {
		return files.get(row);
	}

	public void setFiles(ArrayList<File> files) {
		this.files = files;
		fireTableDataChanged();
	}

	public ArrayList<File> getFiles() {
		return files;
	}

	public void addFile(File file) {
		files.add(file);
		fireTableDataChanged();
	}

}

class CellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		// if (value>17 value<26) {
		this.setValue(table.getValueAt(row, column));
		this.setFont(this.getFont().deriveFont(Font.BOLD));
		//}
		return this;
	}
}

class FileMenuItem extends JMenuItem {

	public FileMenuItem(MacroAction macroAction) {
		super(macroAction.getName());

		setToolTipText(macroAction.getInformationAsHTML());
		setIcon(macroAction.getIcon());
		setEnabled(macroAction.isEnabled);
	}

}