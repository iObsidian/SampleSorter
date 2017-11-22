package ass.file;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JPanel;

import ass.file.player.AudioPlayer;
import constants.Constants;
import constants.Properties;
import file.FileTypes;
import logger.Logger;
import ui.FileInformation;

public class FileVisualiser extends FileInformation {

	private static final String TAG = "FileVisualiser";

	private JPanel playerContainer;

	private AudioPlayer audPlayer = new AudioPlayer();

	private static ArrayList<File> selectedFiles = new ArrayList<File>();

	public FileVisualiser() {
		super();

		playerContainer = new JPanel();
		playerContainer.setBackground(Constants.SICK_PURPLE);
		playerContainer.setLayout(new BorderLayout(0, 0));
		playerContainer.add(audPlayer.getVisualizer().analyzer, BorderLayout.CENTER);
	}

	public void setSelectedFile(File file) {

		selectedFiles.clear();
		selectedFiles.add(file);

		setFileDetails(file);

		if (FileTypes.AUDIO_FILES.accept(file)) {

			if (Properties.PLAY_ON_CLICK.getValueAsBoolean()) {
				audPlayer.play(file);
			}

		} else if (FileTypes.VIDEO_FILES.accept(file)) {
			Logger.logError(TAG, "Video files are not currently supported!");
		} else if (FileTypes.PICTURE_FILES.accept(file)) {
			Logger.logError(TAG, "Picture files are not currently supported!");
		} else if (FileTypes.TEXT_FILES.accept(file)) {
			Logger.logError(TAG, "Text files are not currently supported!");
		} else {
			Logger.logError(TAG, "Unknown file Type " + file.getName());
		}

	}

	public void setSelectedFiles(ArrayList<File> files) {
		selectedFiles = files;
		setFilesDetails(files);
	}

	public AudioPlayer getAudioPlayer() {
		return audPlayer;
	}

	public JPanel getPlayer() {
		return playerContainer;
	}

}