package GUI.soundpanel;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import GUI.Sorter;
import constants.Constants;
import property.Properties;
import util.file.FileSizeToString;

public class SoundPanel extends JPanel {

	private static final Color SELECTED = Constants.SICK_PURPLE;
	private static final Color UNSELECTED = Color.LIGHT_GRAY;
	private static final Color UNSELECTED_LABEL = new Color(18, 18, 24); // Eigengrau :D

	private static final long serialVersionUID = 1L;

	Sorter sorter;

	public Sound sound;

	private JLabel lblFilename;
	private JLabel lblFileSize;

	private SoundPanel me = this;

	public SoundPanel(Sound sound, Sorter s) {
		this.sorter = s;
		this.sound = sound;

		setBackground(UNSELECTED);

		setPreferredSize(new Dimension(579, 48));
		setLayout(null);

		JPanel containerPanel = new JPanel();
		containerPanel.setBackground(Color.WHITE); //always white, the other panel changes
		containerPanel.setBounds(1, 1, 576, 44);
		add(containerPanel);
		containerPanel.setLayout(null);

		lblFilename = new JLabel();
		lblFilename.setBounds(12, 5, 552, 16);
		containerPanel.add(lblFilename);
		lblFilename.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblFilename.setForeground(UNSELECTED_LABEL);

		lblFileSize = new JLabel("File Size");
		lblFileSize.setBounds(12, 22, 552, 16);
		containerPanel.add(lblFileSize);
		lblFileSize.setForeground(UNSELECTED_LABEL);
		updateFilenameLabel();

		addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {

				if (SwingUtilities.isLeftMouseButton(e)) {
					sorter.soundPanelIsLeftClicked(me);
				} else if (SwingUtilities.isRightMouseButton(e)) {
					sorter.soundPanelIsRightClicked(me);
				}

			}
		});

		DefaultContextMenu.addDefaultContextMenu(this);

	}

	public void updateFilenameLabel() {
		lblFilename.setText(sound.getName(Properties.DISPLAY_SOUND_SUFFIXES.getValueAsBoolean()));
		lblFileSize.setText(FileSizeToString.getFileSizeAsString(sound.file));
	}

	//Dont call this directly, use Sorter. setSelected instead
	
	/**
	 * @param isSelected true = select, false = unselect
	 * @param play true = playOrPause()
	 */
	public void setSelected(boolean isSelected, boolean play) {
		if (isSelected) {

			setBackground(SELECTED);
			lblFilename.setForeground(SELECTED);
			lblFileSize.setForeground(SELECTED);

			if (play) {
				sound.playOrPause();
			}

		} else {

			setBackground(UNSELECTED);
			lblFilename.setForeground(UNSELECTED_LABEL);
			lblFileSize.setForeground(UNSELECTED_LABEL);

			sound.pause();
		}
	}

	public File getFile() {
		return sound.file;
	}

	@Override
	public String toString() {
		return "SoundPanel [sound=" + sound + ", lblFilename=" + lblFilename + ", lblFileSize=" + lblFileSize + "]";
	}

	public void openFileLocation() {
		try {
			Desktop.getDesktop().open(getFile().getParentFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}