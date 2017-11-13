package keybinds.macro.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jnativehook.keyboard.NativeKeyEvent;

import global.icons.IconLoader;
import global.logger.Logger;
import key.KeysToString;
import key.NativeKeyEventToKey;
import keybinds.action.Action;
import keybinds.action.ActionManager;
import keybinds.action.editeable.EditeablePropertyEditor;
import keybinds.keys.Key;
import keybinds.macro.MacroAction;
import keybinds.macro.MacroEditor;

public class MacroEditorUI extends JPanel implements GetIcon {

	private static final String TAG = "LogUI";

	private IconChooser iconChooser = new IconChooser();

	private MacroEditorUI me = this;

	//

	private MacroAction keyBindToEdit;

	//

	private static final Font RESULT_FONT_PLAIN = new Font("Segoe UI Light", Font.PLAIN, 20);
	private static final Font RESULT_FONT_BOLD = new Font("Segoe UI Light", Font.BOLD, 20);

	private boolean isListenningForKeyInputs = false;
	private JTextField keyEditorImputBox;

	private int keysPressedAndNotReleased = 0;

	private boolean newKeyBind;

	private static JPanel columnpanel = new JPanel();
	private static JPanel borderlaoutpanel;

	private static JScrollPane scrollPane;

	private ArrayList<Action> actions = new ArrayList<Action>();
	private ArrayList<MacroActionPanel> macroActionEditPanels = new ArrayList<MacroActionPanel>();

	public EditeablePropertyEditor actionEditor = new EditeablePropertyEditor();
	private JTextField titleEditor;
	private JButton iconButton;

	public void onHide() {
		isListenningForKeyInputs = false;
	}

	public void onShow() {
	}

	/**
	 * Create the frame.
	 */
	public MacroEditorUI(MacroEditor m) {

		setBounds(0, 0, 355, 271);
		setVisible(true);
		setLayout(null);

		JButton btnAdd = new JButton("Save");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				m.getToolBar().repopulate();

				if (newKeyBind) {
					newKeyBind = false;
					Logger.logInfo(TAG, "Creating new KeyBind");

					keyBindToEdit.actionsToPerform.clear();

					m.macroLoader.addNewMacro(keyBindToEdit);

				}

				keyBindToEdit.actionsToPerform.clear();

				for (Action action : actions) {
					keyBindToEdit.actionsToPerform.add(action);
				}

				m.macroListUI.refreshInfoPanel();

				m.macroLoader.serialise(); //just save

				m.showMacroEditUI();
			}
		});
		btnAdd.setBounds(12, 233, 330, 25);
		add(btnAdd);

		keyEditorImputBox = new JTextField("Click to edit shortcut");
		keyEditorImputBox.setToolTipText("Shortcut");
		keyEditorImputBox.setHorizontalAlignment(SwingConstants.CENTER);
		keyEditorImputBox.setFont(RESULT_FONT_PLAIN);
		keyEditorImputBox.setBackground(Color.decode("#FCFEFF")); //'Ultra Light Cyan'
		keyEditorImputBox.setEditable(false);
		keyEditorImputBox.setBounds(12, 185, 330, 35);
		keyEditorImputBox.setColumns(10);
		add(keyEditorImputBox);

		//Populate the ComboBox

		Logger.logInfo(TAG, "Found " + ActionManager.actions.size() + " actions.");

		JComboBox<Action> comboBox = new JComboBox<Action>();
		comboBox.setToolTipText("Add action");
		for (Action a : ActionManager.actions) {
			comboBox.addItem(a);
		}

		comboBox.setBounds(12, 58, 330, 22);
		add(comboBox);

		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (comboBox.getSelectedItem() != null) {
					Action selectedAction = (Action) comboBox.getSelectedItem();
					addActionAndActionEditPanel(selectedAction);

				}
			}
		});

		scrollPane = new JScrollPane();
		scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 93, 330, 79);
		scrollPane.setAutoscrolls(true);
		// scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(scrollPane);

		borderlaoutpanel = new JPanel();
		scrollPane.setViewportView(borderlaoutpanel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		borderlaoutpanel.setLayout(new BorderLayout(0, 0));

		columnpanel = new JPanel();
		columnpanel.setLayout(new GridLayout(0, 1, 0, 1));
		columnpanel.setBackground(Color.gray);
		borderlaoutpanel.add(columnpanel, BorderLayout.NORTH);

		titleEditor = new JTextField();
		titleEditor.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateName();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateName();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}

			public void updateName() {
				if (keyBindToEdit != null) { //sometimes is triggered on software launch
					keyBindToEdit.name = titleEditor.getText();
				}
			}
		});

		titleEditor.setToolTipText("Click to edit title");
		titleEditor.setFont(new Font("Segoe UI Light", Font.BOLD, 20));
		titleEditor.setText("Title");
		titleEditor.setHorizontalAlignment(SwingConstants.CENTER);
		titleEditor.setBounds(56, 13, 286, 32);

		titleEditor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("New title : " + titleEditor.getText());
				keyBindToEdit.name = titleEditor.getText();
			}
		});

		add(titleEditor);
		titleEditor.setColumns(10);

		//

		iconButton = new JButton();
		iconButton.setIcon(new ImageIcon(MacroEditorUI.class
				.getResource("/com/sun/deploy/uitoolkit/impl/fx/ui/resources/image/graybox_error.png")));

		iconButton.setFocusable(false); //Removes the stupid 'selected' border
		iconButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				iconChooser.getIcon(me);
			}
		});

		//

		iconButton.setBounds(12, 13, 32, 32);
		add(iconButton);

		keyEditorImputBox.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!isListenningForKeyInputs) {
					keyBindToEdit.clearKeys();
					keyEditorImputBox.setText("Press any key");
					Logger.logInfo(TAG, "Now listenning for key inputs");
					isListenningForKeyInputs = true;
					keyEditorImputBox.setFont(RESULT_FONT_PLAIN);
				} else {
					Logger.logInfo(TAG, "Already listenning for key inputs!");
				}
			}
		});

	}

	public void changeKeyBindToEdit(MacroAction keyBindToEdit) {

		//if keyBindToEdit is null, it means MacroListUI wants us to create a new keyBind

		if (keyBindToEdit != null) {
			newKeyBind = false;

			Logger.logInfo(TAG, "Editing existing keybind");

			for (Action a : keyBindToEdit.actionsToPerform) {
				addActionAndActionEditPanel(a);
			}

			titleEditor.setText(keyBindToEdit.name);

			if (keyBindToEdit.iconPath != null) {

				System.out.println("Setting icon : " + keyBindToEdit.iconPath);
				iconButton.setIcon(IconLoader.getIconFromKey(keyBindToEdit.iconPath));

			}

		} else {

			Logger.logInfo(TAG, "Creating new keyBind");

			newKeyBind = true;
			keyBindToEdit = new MacroAction();

			titleEditor.setText("Title");

		}

		this.keyBindToEdit = keyBindToEdit;

		updateImputBoxText();
	}

	private void updateImputBoxText() {
		if (keyBindToEdit.keys.size() > 0) {
			keyEditorImputBox.setText(KeysToString.keysToString("[", keyBindToEdit.keys, "]"));
		} else {
			keyEditorImputBox.setText("Click to edit");
		}
	}

	/**
	 * This is a work-around the previous keyboard register hook thing
	 *  that caused both the JTable and the PropertyEditor to not receive imputs.
	 */
	public void globalKeyBoardImput(NativeKeyEvent ke, boolean isPressed) {

		Key e = NativeKeyEventToKey.getJavaKeyEvent(ke);

		if (isListenningForKeyInputs) {

			if (isPressed) {

				if (!keyBindToEdit.keys.contains(e)) {
					keyBindToEdit.keys.add(e);

					keysPressedAndNotReleased++;

					//Build string for field showing name of keys

					updateImputBoxText();

				}
			} else {
				if (keysPressedAndNotReleased == 1) { //is last key to be released

					Logger.logInfo(TAG, "Stopped listenning for events.");
					isListenningForKeyInputs = false;
					keyEditorImputBox.setFont(RESULT_FONT_BOLD);

				}

				keysPressedAndNotReleased--;

			}
		}
	}

	// TODO : update this when adding fiels
	void addActionAndActionEditPanel(Action action) {

		MacroActionPanel infoPanel = new MacroActionPanel(action, this);

		columnpanel.add(infoPanel);

		actions.add(action);
		macroActionEditPanels.add(infoPanel);

		refreshPanels();

	}

	public void clearActionEditPanels() {
		try {
			for (Iterator<MacroActionPanel> iterator = macroActionEditPanels.iterator(); iterator.hasNext();) {
				MacroActionPanel infoP = iterator.next();

				iterator.remove();
				columnpanel.remove(infoP);
			}

			actions.clear();

			refreshPanels();

		} catch (Exception e) {
			Logger.logError(TAG, "Error in clearActionEditPanels", e);
			e.printStackTrace();
		}
	}

	void refreshPanels() {

		for (MacroActionPanel logPanel : macroActionEditPanels) {
			logPanel.validate();
			logPanel.repaint();
		}

		columnpanel.validate();
		columnpanel.repaint();

		borderlaoutpanel.validate();
		borderlaoutpanel.repaint();

		scrollPane.validate();
		scrollPane.repaint();

	}

	public static Color hex2Rgb(String colorStr) {
		//@formatter:off
		return new Color(Integer.valueOf(colorStr.substring(1, 3), 16), Integer.valueOf(colorStr.substring(3, 5), 16),
				Integer.valueOf(colorStr.substring(5, 7), 16));
		//@formatter:on
	}

	public void removeFromPanels(MacroActionPanel me) {

		actions.remove(me.action);

		columnpanel.remove(me);
		macroActionEditPanels.remove(me);

		refreshPanels();
	}

	@Override
	public void GetResponse(Icon icon, String iconPath) {
		keyBindToEdit.iconPath = iconPath;
		iconButton.setIcon(icon);
	}

}