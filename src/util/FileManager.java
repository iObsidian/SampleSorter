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
package util;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import constants.Constants;
import constants.Icons;
import util.file.FileSizeToString;

/**
Credits : 
@author Andrew Thompson
@version 2011-06-01
*/
public class FileManager {

	/** Title of the application */
	public static final String APP_TITLE = "FileManager";
	
	/** Used to open/edit/print files. */
	private Desktop desktop;
	
	/** Provides nice icons and names for files. */
	private FileSystemView fileSystemView;

	/** currently selected File. */
	private File currentFile;

	/** Main GUI container */
	private JPanel gui;

	/** Directory listing */
	private JTable table;
	private JProgressBar progressBar;
	
	/** Table model for File[]. */
	private FileTableModel fileTableModel;
	private ListSelectionListener listSelectionListener;
	private boolean cellSizesSet = false;
	private int rowIconPadding = 6;

	/* File controls. */
	private JButton openFile;
	private JButton printFile;
	private JButton editFile;
	/* File details. */
	private JLabel fileName;
	private JTextField path;
	private JLabel date;
	private JLabel size;
	private JRadioButton isDirectory;
	private JRadioButton isFile;

	public Container getGui() {
		if (gui == null) {
			gui = new JPanel(new BorderLayout(3, 3));
			gui.setBorder(new EmptyBorder(5, 5, 5, 5));

			fileSystemView = FileSystemView.getFileSystemView();
			desktop = Desktop.getDesktop();

			JPanel detailView = new JPanel(new BorderLayout(3, 3));
			//fileTableModel = new FileTableModel();

			table = new JTable();

			table.setRowSelectionAllowed(true);
			table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			table.setAutoCreateRowSorter(true);
			table.setShowVerticalLines(false);

			table.setSelectionBackground(Constants.SICK_PURPLE);

			listSelectionListener = new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent lse) {
					int row = table.getSelectionModel().getLeadSelectionIndex();
					setFileDetails(((FileTableModel) table.getModel()).getFile(row));
				}
			};
			table.getSelectionModel().addListSelectionListener(listSelectionListener);
			JScrollPane tableScroll = new JScrollPane(table);
			Dimension d = tableScroll.getPreferredSize();
			tableScroll.setPreferredSize(new Dimension((int) d.getWidth(), (int) d.getHeight() / 2));
			detailView.add(tableScroll, BorderLayout.CENTER);

			// details for a File
			JPanel fileMainDetails = new JPanel(new BorderLayout(4, 2));
			fileMainDetails.setBorder(new EmptyBorder(0, 6, 0, 6));

			JPanel fileDetailsLabels = new JPanel(new GridLayout(0, 1, 2, 2));
			fileMainDetails.add(fileDetailsLabels, BorderLayout.WEST);

			JPanel fileDetailsValues = new JPanel(new GridLayout(0, 1, 2, 2));
			fileMainDetails.add(fileDetailsValues, BorderLayout.CENTER);

			fileDetailsLabels.add(new JLabel("File", JLabel.TRAILING));
			fileName = new JLabel();
			fileDetailsValues.add(fileName);
			fileDetailsLabels.add(new JLabel("Path/name", JLabel.TRAILING));
			path = new JTextField(5);
			path.setEditable(false);
			fileDetailsValues.add(path);
			fileDetailsLabels.add(new JLabel("Last Modified", JLabel.TRAILING));
			date = new JLabel();
			fileDetailsValues.add(date);
			fileDetailsLabels.add(new JLabel("File size", JLabel.TRAILING));
			size = new JLabel();
			fileDetailsValues.add(size);
			fileDetailsLabels.add(new JLabel("Type", JLabel.TRAILING));

			JPanel flags = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
			isDirectory = new JRadioButton("Directory");
			isDirectory.setEnabled(false);
			flags.add(isDirectory);

			isFile = new JRadioButton("File");
			isFile.setEnabled(false);
			flags.add(isFile);
			fileDetailsValues.add(flags);

			int count = fileDetailsLabels.getComponentCount();
			for (int ii = 0; ii < count; ii++) {
				fileDetailsLabels.getComponent(ii).setEnabled(false);
			}

			JToolBar toolBar = new JToolBar();
			// mnemonics stop working in a floated toolbar
			toolBar.setFloatable(false);

			openFile = new JButton("Open");
			openFile.setMnemonic('o');

			openFile.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					try {
						desktop.open(currentFile);
					} catch (Throwable t) {
						showThrowable(t);
					}
					gui.repaint();
				}
			});
			toolBar.add(openFile);

			editFile = new JButton("Edit");
			editFile.setMnemonic('e');
			editFile.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					try {
						desktop.edit(currentFile);
					} catch (Throwable t) {
						showThrowable(t);
					}
				}
			});
			toolBar.add(editFile);

			printFile = new JButton("Print");
			printFile.setMnemonic('p');
			printFile.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					try {
						desktop.print(currentFile);
					} catch (Throwable t) {
						showThrowable(t);
					}
				}
			});
			toolBar.add(printFile);

			// Check the actions are supported on this platform!
			openFile.setEnabled(desktop.isSupported(Desktop.Action.OPEN));
			editFile.setEnabled(desktop.isSupported(Desktop.Action.EDIT));
			printFile.setEnabled(desktop.isSupported(Desktop.Action.PRINT));

			JPanel fileView = new JPanel(new BorderLayout(3, 3));

			fileView.add(toolBar, BorderLayout.NORTH);
			fileView.add(fileMainDetails, BorderLayout.CENTER);

			detailView.add(fileView, BorderLayout.SOUTH);

			gui.add(detailView, BorderLayout.CENTER);

			JPanel simpleOutput = new JPanel(new BorderLayout(3, 3));
			progressBar = new JProgressBar();
			simpleOutput.add(progressBar, BorderLayout.EAST);
			progressBar.setVisible(false);

			gui.add(simpleOutput, BorderLayout.SOUTH);

			File f1 = new File("C:\\Users\\4LDE\\Music\\Music\\Bustin.mp3");
			File f2 = new File("Network");
			File[] f = new File[] { f1, f2 };

			setTableData(f);

		}
		return gui;
	}

	private void showThrowable(Throwable t) {
		t.printStackTrace();
		JOptionPane.showMessageDialog(gui, t.toString(), t.getMessage(), JOptionPane.ERROR_MESSAGE);
		gui.repaint();
	}

	/** Update the table on the EDT */
	private void setTableData(final File[] files) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (fileTableModel == null) {
					fileTableModel = new FileTableModel();
					table.setModel(fileTableModel);
				}
				table.getSelectionModel().removeListSelectionListener(listSelectionListener);
				fileTableModel.setFiles(files);
				table.getSelectionModel().addListSelectionListener(listSelectionListener);
				if (!cellSizesSet) {
					Icon icon = fileSystemView.getSystemIcon(files[0]);

					// size adjustment to better account for icons
					table.setRowHeight(icon.getIconHeight() + rowIconPadding);

					setColumnWidth(0, -1);
					//setColumnWidth(3, 60);
					//table.getColumnModel().getColumn(3).setMaxWidth(120);
					//setColumnWidth(4, -1);

					cellSizesSet = true;
				}
			}
		});
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

	/** Update the File details view with the details of this File. */
	private void setFileDetails(File file) {
		currentFile = file;
		Icon icon = fileSystemView.getSystemIcon(file);
		fileName.setIcon(icon);
		fileName.setText(fileSystemView.getSystemDisplayName(file));
		path.setText(file.getPath());
		date.setText(new Date(file.lastModified()).toString());
		size.setText(FileSizeToString.getFileSizeAsString(file));
		isDirectory.setSelected(file.isDirectory());

		isFile.setSelected(file.isFile());

		JFrame f = (JFrame) gui.getTopLevelAncestor();
		if (f != null) {
			f.setTitle(APP_TITLE + " :: " + fileSystemView.getSystemDisplayName(file));
		}

		gui.repaint();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception weTried) {
					weTried.printStackTrace();
				}
				JFrame f = new JFrame(APP_TITLE);
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				FileManager fileManager = new FileManager();
				f.setContentPane(fileManager.getGui());

				f.setIconImage(Icons.SOFTWARE_ICON.getImage());

				f.pack();
				f.setLocationByPlatform(true);
				f.setMinimumSize(f.getSize());
				f.setVisible(true);
			}
		});
	}
}

/** A TableModel to hold File[]. */
class FileTableModel extends AbstractTableModel {

	private File[] files;
	private FileSystemView fileSystemView = FileSystemView.getFileSystemView();
	private String[] columns = { "Icon", "File", "Path", "Size", "Last Modified" };

	FileTableModel() {
		this(new File[0]);
	}

	FileTableModel(File[] files) {
		this.files = files;
	}

	public Object getValueAt(int row, int column) {
		File file = files[row];
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
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
			return Boolean.class;
		}
		return String.class;
	}

	public String getColumnName(int column) {
		return columns[column];
	}

	public int getRowCount() {
		return files.length;
	}

	public File getFile(int row) {
		return files[row];
	}

	public void setFiles(File[] files) {
		this.files = files;
		fireTableDataChanged();
	}
}

/*
 * table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
 
 after that :
 
 class CustomTableCellRenderer extends DefaultTableCellRenderer {
 

	public static final DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component c = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		if (isSelected) {
			c.setBackground(Color.red);
		} else {
			c.setForeground(Color.black);
			c.setBackground(Color.white);
		}
		return c;
	}
}**/