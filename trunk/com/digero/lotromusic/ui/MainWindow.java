/* Copyright (c) 2008 Ben Howell
 * This software is licensed under the MIT License
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

package com.digero.lotromusic.ui;

import info.clearthought.layout.TableLayout;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Transmitter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import com.digero.lotromusic.LotroMusicMain;
import com.digero.lotromusic.Version;
import com.digero.lotromusic.abc.MidiToAbc;
import com.digero.lotromusic.keyboard.Instrument;
import com.digero.lotromusic.midi.LotroMidiReceiver;
import com.digero.lotromusic.midi.TrackMetaInfo;
import com.digero.lotromusic.midi.TransposeInfo;
import com.digero.lotromusic.ui.events.SongPositionEvent;
import com.digero.lotromusic.ui.events.SongPositionListener;
import com.digero.lotromusic.ui.events.TrackMuteEvent;
import com.digero.lotromusic.ui.events.TrackMuteListener;
import com.digero.lotromusic.windows.WinApi;

@SuppressWarnings("serial")
public class MainWindow extends JFrame implements SongPositionListener, TrackMuteListener {
	private final double fill = TableLayout.FILL;
	private final double pref = TableLayout.PREFERRED;

	private final int MIN_WIDTH = 340;
	private final int MIN_HEIGHT = 385;

	private JFileChooser fileChooser = new JFileChooser();
	private JFileChooser abcSaveChooser = new JFileChooser();
	private Preferences prefs = Preferences.userNodeForPackage(LotroMusicMain.class);
	private Preferences windowPrefs = prefs.node("window");
	private Preferences songPrefs = null;

	private Sequencer sequencer;
	private LotroMidiReceiver lotroReceiver;
	private Transmitter transmitter;
	private TransposeInfo transposeInfo;
	private File songFile;

	private TrackListPanel trackPanel;
	private SongPositionBar positionBar;
	private JButton playButton, stopButton;
	private ImageIcon playIcon, pauseIcon, stopIcon;
	private JTextField titleText;
	private JLabel timerLabel;
	private JPanel contentPane;
	private JTextField typeTextField;
	private JButton sendTextButton;
	private Timer updateTimer;

	private JCheckBox localPreviewCheckbox;
	private JSpinner transposeSpinner;
	private JButton bestTransposeButton;
	private JLabel notesMissedLabel;
	private JSpinner tempoSpinner;
	private JComboBox instrumentComboBox;
	private JCheckBox bkgdSoundCheckBox;
	private Timer soundTimer;

	private JMenuItem exportAbcMenuItem;

	private ExternalDeviceFrame midiPianoFrame = null;

	private Version version = new Version(1, 1, 0, 1);

	public static void printThreadInfo() {
		if (false) {
			StackTraceElement element = new Throwable().getStackTrace()[1];
			String className = element.getClassName();
			int dot = className.lastIndexOf('.');
			if (dot > 0) {
				className = className.substring(dot + 1);
			}
			String methodName = element.getMethodName();
			System.out.println(Thread.currentThread().getName() + ": "
					+ Thread.currentThread().getPriority() + " (" + className + "." + methodName
					+ ")");
		}
	}

	public MainWindow() {
		this(null);
	}

	public MainWindow(File fileToOpen) {
		super("LotRO MIDI Player");
		MainWindow.printThreadInfo();
		try {
			sequencer = MidiSystem.getSequencer(false);
			sequencer.open();
			transmitter = sequencer.getTransmitter();
			lotroReceiver = new LotroMidiReceiver();
			transmitter.setReceiver(lotroReceiver);
		}
		catch (MidiUnavailableException e) {
			error("Failed to initialize MIDI sequencer.\nThe program will now exit.",
					"Failed to initialize MIDI sequencer.");
			System.exit(1);
			return;
		}

		Runtime.getRuntime().addShutdownHook(new CleanupThread());

		try {
			List<Image> icons = new ArrayList<Image>();
			icons.add(ImageIO.read(MainWindow.class.getResource("icn_16.png")));
			icons.add(ImageIO.read(MainWindow.class.getResource("icn_32.png")));
			setIconImages(icons);
		}
		catch (Exception ex) {
			// Ignore
		}

		initializeWindowBounds();

		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				windowPrefs.putInt("width", getWidth());
				windowPrefs.putInt("height", getHeight());
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				windowPrefs.putInt("x", getX());
				windowPrefs.putInt("y", getY());
			}
		});

		setupMenu();

		String userHome = System.getProperty("user.home", ".");
		boolean isVista = System.getProperty("os.name", "").contains("Vista");

		String musicPath;
		if (isVista) {
			musicPath = userHome + "\\Music";
		}
		else {
			musicPath = userHome + "\\My Documents\\My Music";
		}

		File fileChooserDirectory = new File(prefs.get("fileChooserDirectory", musicPath));
		if (!fileChooserDirectory.exists()) {
			fileChooserDirectory = new File(musicPath);
		}
		if (fileChooserDirectory.exists()) {
			fileChooser.setCurrentDirectory(fileChooserDirectory);
		}

		String lotroPath;
		if (isVista) {
			lotroPath = userHome + "\\Documents\\The Lord of the Rings Online";
		}
		else {
			lotroPath = userHome + "\\My Documents\\The Lord of the Rings Online";
		}
		File abcDirectory = new File(prefs.get("abcDirectory", lotroPath + "\\Music"));
		if (!abcDirectory.exists()) {
			File lotroDocsDirectory = new File(lotroPath);
			File lotroMusicDirectory = new File(lotroPath + "\\Music");
			if (lotroDocsDirectory.exists()) {
				if (!lotroMusicDirectory.exists()) {
					lotroMusicDirectory.mkdir();
				}
				abcDirectory = lotroMusicDirectory;
			}
		}
		if (abcDirectory.exists()) {
			abcSaveChooser.setCurrentDirectory(abcDirectory);
		}
		abcSaveChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		abcSaveChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				int dot = f.getName().lastIndexOf('.');
				if (dot < 0)
					return false;
				String ext = f.getName().substring(dot).toLowerCase();
				return ext.equals(".abc") || ext.equals(".txt");
			}

			public String getDescription() {
				return "ABC Files (*.abc, *.txt)";
			}
		});

		titleText = new JTextField();
		titleText.setToolTipText("Title used in exported ABC files");
		titleText.setEnabled(false);
		titleText.setFont(titleText.getFont().deriveFont(Font.BOLD, 13f));
		titleText.setBackground(SystemColor.control);
		titleText.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				if (titleText.isEnabled())
					titleText.setBackground(SystemColor.window);
			}

			public void mouseExited(MouseEvent e) {
				if (!titleText.hasFocus())
					titleText.setBackground(SystemColor.control);
			}
		});
		titleText.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				titleText.setBackground(SystemColor.window);
			}

			@Override
			public void focusLost(FocusEvent e) {
				titleText.setBackground(SystemColor.control);
			}
		});
		titleText.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				if (songPrefs != null)
					songPrefs.put("title", titleText.getText());
			}

			public void insertUpdate(DocumentEvent e) {
				if (songPrefs != null)
					songPrefs.put("title", titleText.getText());
			}

			public void removeUpdate(DocumentEvent e) {
				if (songPrefs != null)
					songPrefs.put("title", titleText.getText());
			}
		});
		titleText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					typeTextField.requestFocus();
				}
			}
		});
		titleText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				typeTextField.requestFocus();
			}
		});
		// titleLabel = new JLabel(" ");
		// titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));

		trackPanel = new TrackListPanel();
		trackPanel.setFocusable(true);
		JScrollPane trkScrollPane = new JScrollPane(trackPanel,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		trkScrollPane.getVerticalScrollBar().setUnitIncrement(TrackListPanel.ROW_HEIGHT);
		trackPanel.addTrackMuteListener(this);

		playIcon = new ImageIcon(MainWindow.class.getResource("play.png"));
		pauseIcon = new ImageIcon(MainWindow.class.getResource("pause.png"));
		stopIcon = new ImageIcon(MainWindow.class.getResource("stop.png"));

		playButton = new JButton(playIcon);
		playButton.setEnabled(false);
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				printThreadInfo();
				playPause();
			}
		});

		stopButton = new JButton(stopIcon);
		stopButton.setEnabled(false);
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				printThreadInfo();
				stop();
			}
		});

		localPreviewCheckbox = new JCheckBox("MIDI preview mode");
		localPreviewCheckbox.setToolTipText("Check to play music using the computer's "
				+ "MIDI synthesizer, rather than playing in-game.");
		localPreviewCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!localPreviewCheckbox.isSelected() && sequencer.isRunning()) {
					if (!lotroReceiver.resetHWnd()) {
						localPreviewCheckbox.setSelected(true);
						showCantFindWindowError();
					}
					else if (!lotroReceiver.getKeyMap().isLoaded()) {
						localPreviewCheckbox.setSelected(true);
						error(lotroReceiver.getKeyMap().getLastReadError(),
								"Failed to load key mappings");
					}
				}
				lotroReceiver.setLocalPreviewMode(localPreviewCheckbox.isSelected());
			}
		});

		notesMissedLabel = new JLabel();
		transposeSpinner = new JSpinner(new SpinnerNumberModel(0, -36, 36, 1));
		transposeSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (songPrefs != null) {
					int transpose = (Integer) transposeSpinner.getValue();
					lotroReceiver.setTranspose(transpose);
					updateTransposeText();

					bestTransposeButton.setEnabled(transpose != transposeInfo.getBestTranspose());

					songPrefs.putBoolean("autoTranspose", false);
					songPrefs.putInt("transpose", transpose);
				}
			}
		});
		transposeSpinner.setEnabled(false);

		bestTransposeButton = new JButton("Best");
		bestTransposeButton.setHorizontalAlignment(JButton.LEADING);
		bestTransposeButton.setMargin(new Insets(2, 5, 2, 5));
		bestTransposeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (transposeInfo != null) {
					int bestTranspose = transposeInfo.getBestTranspose();
					transposeSpinner.setValue(bestTranspose);
					lotroReceiver.setTranspose(bestTranspose);
					bestTransposeButton.setEnabled(false);
					transposeSpinner.requestFocus();
				}
			}
		});
		bestTransposeButton.setEnabled(false);

		tempoSpinner = new JSpinner(new SpinnerNumberModel(100, 20, 500, 5));
		tempoSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (songPrefs != null) {
					float tempo = ((Integer) tempoSpinner.getValue()) / 100.0f;
					sequencer.setTempoFactor(tempo);
					songPrefs.putDouble("tempo", tempo);
					updateTimerLabel();
				}
			}
		});
		tempoSpinner.setEnabled(false);

		instrumentComboBox = new JComboBox(Instrument.INSTRUMENTS);
		instrumentComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Instrument sel = (Instrument) instrumentComboBox.getSelectedItem();
				if (sel == null) {
					sel = Instrument.LUTE;
				}
				refreshTransposeInfo();
				lotroReceiver.setLowestNote(sel.lowestPlayable);
				lotroReceiver.setHighestNote(sel.highestPlayable);
			}
		});

		// TODO
		soundTimer = new Timer(1000, new ActionListener() {
			long lastResetMillis = 0;

			public void actionPerformed(ActionEvent e) {
				// Reset the HWND every 15s
				if (System.currentTimeMillis() - lastResetMillis > 15000) {
					lastResetMillis = System.currentTimeMillis();
					lotroReceiver.resetHWnd();
				}
				if (lotroReceiver.isValidHWnd()) {
					WinApi.SendFocusMessage(lotroReceiver.getHWnd(), true);
				}
			}
		});
		soundTimer.stop();
		soundTimer.setInitialDelay(0);

		bkgdSoundCheckBox = new JCheckBox("Always play game sound in background");
		bkgdSoundCheckBox.setToolTipText("'Tricks' LotRO into thinking it's the active window.");
		bkgdSoundCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (bkgdSoundCheckBox.isSelected()) {
					soundTimer.start();
				}
				else {
					if (lotroReceiver.isValidHWnd()) {
						WinApi.SendFocusMessage(lotroReceiver.getHWnd(), false);
					}
					soundTimer.stop();
				}
				prefs.putBoolean("bkgdSound", bkgdSoundCheckBox.isSelected());
			}
		});
		bkgdSoundCheckBox.setSelected(prefs.getBoolean("bkgdSound", false));
		if (bkgdSoundCheckBox.isSelected()) {
			soundTimer.start();
		}

		positionBar = new SongPositionBar();
		positionBar.addSongPositionListener(this);

		timerLabel = new JLabel("0:00/0:00");

		updateTimer = new Timer(200, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!sequencer.isRunning()) {
					// XXX
					System.out.println("Stopping...");
					stop();
					System.out.println("Stopped");
				}

				updateTimerLabel();
			}
		});

		ActionListener sendTextListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!lotroReceiver.resetHWnd()) {
					showCantFindWindowError();
					return;
				}
				WinApi.SendKeyString(lotroReceiver.getHWnd(), "~" + typeTextField.getText() + "~",
						!lotroReceiver.isPlaySoundInactive());
				typeTextField.setText("");
			}
		};

		sendTextButton = new JButton("\u2500\u25BA");
		sendTextButton.setMargin(new Insets(2, 8, 2, 8));
		sendTextButton.addActionListener(sendTextListener);

		typeTextField = new JTextField();
		typeTextField.addActionListener(sendTextListener);

		// Play/Pause controls
		TableLayout controlsLayout = new TableLayout( //
				new double[] { 0, 0.50, pref, pref, 0.50, 60, 0 }, //
				new double[] { pref, pref });
		controlsLayout.setHGap(6);
		controlsLayout.setVGap(6);
		JPanel controlsPanel = new JPanel(controlsLayout);
		controlsPanel.add(positionBar, "1, 0, 4, 0, f, c");
		controlsPanel.add(timerLabel, "5, 0, r, c");
		controlsPanel.add(playButton, "2, 1");
		controlsPanel.add(stopButton, "3, 1");
		controlsPanel.add(localPreviewCheckbox, "4, 1, 5, 1, l, c");

		// Transpose/Speed settings
		TableLayout settingsLayout = new TableLayout( //
				new double[] { pref, pref, pref, fill }, //
				new double[] { 0.50, 2, pref, 4, 0.50, 4, pref, 4, pref });
		settingsLayout.setHGap(8);
		JPanel settingsPanel = new JPanel(settingsLayout);
		settingsPanel.setBorder(new TitledBorder("Play Settings"));

		settingsPanel.add(new JLabel("Transpose:"), "0, 0");
		settingsPanel.add(transposeSpinner, "1, 0");
		settingsPanel.add(bestTransposeButton, "2, 0");
		settingsPanel.add(notesMissedLabel, "3, 0");

		settingsPanel.add(new JLabel("Play speed:"), "0, 4");
		settingsPanel.add(tempoSpinner, "1, 4");

		settingsPanel.add(new JLabel("Instrument:"), "0, 6");
		settingsPanel.add(instrumentComboBox, "1, 6, 3, 6, l, c");

		settingsPanel.add(new JLabel("Game sound:"), "0, 8");
		settingsPanel.add(bkgdSoundCheckBox, "1, 8, 3, 8, l, c");

		// TODO
		// JPanel soundModePanel = new JPanel(new BorderLayout());
		// soundModePanel.add(new JLabel("Play game sounds in background: "), BorderLayout.WEST);
		// soundModePanel.add(soundComboBox, BorderLayout.CENTER);
		// settingsPanel.add(soundModePanel, "0, 8, 3, 8, l, c");

		// Type text to the game
		JPanel typeTextPanel = new JPanel(new TableLayout( //
				new double[] { fill, 2, pref }, new double[] { pref }));

		typeTextPanel.setBorder(new TitledBorder("Type text in the game:"));
		typeTextPanel.add(typeTextField, "0, 0");
		typeTextPanel.add(sendTextButton, "2, 0");

		TableLayout contentPaneLayout = new TableLayout( //
				new double[] { 4, fill, 4 }, //
				new double[] { 0, // 
						pref, // 1 Title
						fill, // 2 Track List
						pref, // 3 Controls
						pref, // 4 Settings
						pref, // 5 Type Text
				});
		contentPaneLayout.setVGap(8);

		setContentPane(contentPane = new JPanel(contentPaneLayout));
		contentPane.add(titleText, "1, 1");
		contentPane.add(trkScrollPane, "0, 2, 2, 2");
		contentPane.add(controlsPanel, "1, 3");
		contentPane.add(settingsPanel, "1, 4");
		contentPane.add(typeTextPanel, "1, 5");

		new DropTarget(this, new MyDropListener());

		// TODO
		if (fileToOpen != null) {
			if (fileToOpen.exists()) {
				openSong(fileToOpen);
			}
			else {
				error("File not found:\n" + fileToOpen.getAbsolutePath(), "File not found");
			}
		}
		else {
			String lastSongPath = prefs.get("lastSongPath", null);
			if (lastSongPath != null) {
				File lastSongFile = new File(lastSongPath);
				if (lastSongFile.exists()) {
					openSong(lastSongFile);
				}
			}
		}

		addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				playButton.requestFocus();

				// Check if the version has been updated
				Version lastVersion = Version.parseVersion(prefs.get("lastVersion", null));
				if (lastVersion != null && lastVersion.compareTo(version) < 0) {
					ImageIcon aboutIcon = new ImageIcon(MainWindow.class.getResource("icn_96.png"));
					JLabel updateMessage = new JLabel(
							"<html>LotRO MIDI Player was updated to version " + version + "!<br>"
									+ "Visit the website to see what's new.<br>"
									+ "<a href='http://lotro.acasylum.com/midi/changelog.html'>"
									+ "http://lotro.acasylum.com/midi/changelog.html</a></html>");
					updateMessage.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					updateMessage.addMouseListener(new MouseAdapter() {
						public void mouseClicked(MouseEvent e) {
							if (e.getButton() == MouseEvent.BUTTON1) {
								openURL("http://lotro.acasylum.com/midi/changelog.html");
							}
						}
					});
					String aboutTitle = "LotRO MIDI Player has been updated";
					JOptionPane.showMessageDialog(MainWindow.this, updateMessage, aboutTitle,
							JOptionPane.INFORMATION_MESSAGE, aboutIcon);
				}

				prefs.put("lastVersion", version.toString());
			}
		});
	}

	private void initializeWindowBounds() {
		Dimension mainScreen = Toolkit.getDefaultToolkit().getScreenSize();

		int width = windowPrefs.getInt("width", 350);
		int height = windowPrefs.getInt("height", 450);
		int x = windowPrefs.getInt("x", (mainScreen.width - width) / 2);
		int y = windowPrefs.getInt("y", (mainScreen.height - height) / 2);

		// Handle the case where the window was last saved on
		// a screen that is no longer connected
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		Rectangle onScreen = null;
		for (int i = 0; i < gs.length; i++) {
			Rectangle monitorBounds = gs[i].getDefaultConfiguration().getBounds();
			if (monitorBounds.intersects(x, y, width, height)) {
				onScreen = monitorBounds;
				break;
			}
		}
		if (onScreen == null) {
			x = (mainScreen.width - width) / 2;
			y = (mainScreen.height - height) / 2;
		}
		else {
			if (x < onScreen.x)
				x = onScreen.x;
			else if (x + width > onScreen.x + onScreen.width)
				x = onScreen.x + onScreen.width - width;

			if (y < onScreen.y)
				y = onScreen.y;
			else if (y + height > onScreen.y + onScreen.height)
				y = onScreen.y + onScreen.height - height;
		}

		setBounds(x, y, width, height);
	}

	private class MyDropListener implements DropTargetListener {
		private File draggingFile = null;

		public void dragEnter(DropTargetDragEvent dtde) {
			draggingFile = getMidiFile(dtde.getTransferable());
			if (draggingFile != null) {
				dtde.acceptDrag(DnDConstants.ACTION_COPY);
			}
			else {
				dtde.rejectDrag();
			}
		}

		public void dragExit(DropTargetEvent dte) {
		}

		public void dragOver(DropTargetDragEvent dtde) {
		}

		public void drop(DropTargetDropEvent dtde) {
			if (draggingFile != null) {
				dtde.acceptDrop(DnDConstants.ACTION_COPY);
				openSong(draggingFile);
				draggingFile = null;
			}
			else {
				dtde.rejectDrop();
			}
		}

		public void dropActionChanged(DropTargetDragEvent dtde) {
			draggingFile = getMidiFile(dtde.getTransferable());
			if (draggingFile != null) {
				dtde.acceptDrag(DnDConstants.ACTION_COPY);
			}
			else {
				dtde.rejectDrag();
			}
		}

		@SuppressWarnings("unchecked")
		private File getMidiFile(Transferable t) {
			if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				List<File> files;
				try {
					files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
				}
				catch (Exception e) {
					return null;
				}
				if (files.size() >= 1) {
					String name = files.get(0).getName();
					int dot = name.lastIndexOf('.');
					if (dot > 0) {
						String ext = name.substring(dot).toLowerCase();
						if (ext.equals(".mid") || ext.equals(".midi")) {
							return files.get(0);
						}
					}
				}
			}
			return null;
		}
	}

	private void setupMenu() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu fileMenu = menuBar.add(new JMenu(" File "));
		fileMenu.setMnemonic(KeyEvent.VK_F);

		JMenuItem openMenuItem = fileMenu.add(new JMenuItem("Open MIDI File...", KeyEvent.VK_O));
		openMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int result = fileChooser.showOpenDialog(MainWindow.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					openSong(fileChooser.getSelectedFile());
				}
			}
		});

		exportAbcMenuItem = fileMenu.add(new JMenuItem("Export to ABC...", KeyEvent.VK_E));
		exportAbcMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportAbc();
			}
		});
		exportAbcMenuItem.setEnabled(false);

		JMenuItem midiPianoMenuItem = fileMenu.add(new JMenuItem("Connect MIDI Piano...",
				KeyEvent.VK_M));
		midiPianoMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (midiPianoFrame == null) {
					midiPianoFrame = new ExternalDeviceFrame();
				}
				midiPianoFrame.centerOn(getBounds());
				midiPianoFrame.setVisible(true);
			}
		});

		fileMenu.addSeparator();

		JMenuItem exitMenuItem = fileMenu.add(new JMenuItem("Exit", KeyEvent.VK_X));
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		JMenu editMenu = menuBar.add(new JMenu(" Edit "));
		editMenu.setMnemonic(KeyEvent.VK_E);

		JMenuItem reloadKeymap = editMenu.add(new JMenuItem("Reload Key Mappings", KeyEvent.VK_R));
		reloadKeymap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (lotroReceiver.getKeyMap().load()) {
					JOptionPane.showMessageDialog(MainWindow.this,
							"The key map has been reloaded from the game's settings file");
				}
				else {
					String message = lotroReceiver.getKeyMap().getLastReadError();
					error(message, "Error loading key mappings");
				}
			}
		});

		JMenu helpMenu = menuBar.add(new JMenu(" Help "));
		helpMenu.setMnemonic(KeyEvent.VK_H);

		JMenuItem about = helpMenu.add(new JMenuItem("About", KeyEvent.VK_A));
		about.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ImageIcon aboutIcon = new ImageIcon(MainWindow.class.getResource("icn_96.png"));
				JLabel aboutMessage = new JLabel("<html>Lord of the Rings Online MIDI Player<br>"
						+ "Version " + version + "<br>" + "Created by Digero of Landroval<br>"
						+ "<a href='http://lotro.acasylum.com/midi'>"
						+ "http://lotro.acasylum.com/midi</a><br>"
						+ "&copy; 2008 acasylum.com</html>");
				aboutMessage.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				aboutMessage.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (e.getButton() == MouseEvent.BUTTON1) {
							openURL("http://lotro.acasylum.com/midi");
						}
					}
				});
				String aboutTitle = "About LotRO MIDI Player";
				JOptionPane.showMessageDialog(MainWindow.this, aboutMessage, aboutTitle,
						JOptionPane.INFORMATION_MESSAGE, aboutIcon);
			}
		});
	}

	private static boolean openURL(String url) {
		try {
			if (System.getProperty("os.name").startsWith("Windows")) {
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
				return true;
			}
		}
		catch (Exception e) {
		}
		return false;
	}

	private void updateTimerLabel() {
		long cur;
		if (positionBar.isMouseDragging()) {
			cur = (long) (positionBar.getPointerPosition() / sequencer.getTempoFactor());
		}
		else {
			cur = (long) (sequencer.getMicrosecondPosition() / sequencer.getTempoFactor());
		}
		long len = (long) (sequencer.getMicrosecondLength() / sequencer.getTempoFactor());
		positionBar.setSongPosition(sequencer.getMicrosecondPosition());
		timerLabel.setText(formatDuration(cur) + "/" + formatDuration(len));
	}

	private void playPause() {
		if (!sequencer.isRunning()) {
			if (!lotroReceiver.isLocalPreviewMode()) {
				if (!lotroReceiver.resetHWnd()) {
					showCantFindWindowError();
					return;
				}
				if (!lotroReceiver.getKeyMap().isLoaded()) {
					error(lotroReceiver.getKeyMap().getLastReadError(),
							"Failed to load key mappings");
					return;
				}
			}
			if (sequencer.getMicrosecondPosition() >= sequencer.getMicrosecondLength()) {
				sequencer.setMicrosecondPosition(0);
			}
			sequencer.start();
			// playButton.setText("Pause");
			playButton.setIcon(pauseIcon);
			stopButton.setEnabled(true);
			updateTimer.start();
		}
		else {
			sequencer.stop();
			lotroReceiver.releaseAllKeys();
			// playButton.setText("Play");
			playButton.setIcon(playIcon);
			updateTimer.stop();
			updateTimerLabel();
		}
	}

	private void stop() {
		// XXX
		// System.out.println(" Stopping sequencer");
		sequencer.stop();
		// System.out.println(" Yielding...");
		Thread.yield();
		// System.out.println(" Resetting sequencer");
		sequencer.setMicrosecondPosition(0);
		// System.out.println(" Releasing keys");
		lotroReceiver.releaseAllKeys();
		// System.out.println(" Disabling stop button");
		stopButton.setEnabled(false);
		// System.out.println(" Changing play icon");
		playButton.setIcon(playIcon);
		// System.out.println(" Stopping update timer");
		updateTimer.stop();
		// System.out.println(" Updating label");
		updateTimerLabel();
	}

	public void openSong(File songFile) {
		stop();

		Sequence song;
		try {
			song = MidiSystem.getSequence(songFile);
			sequencer.setSequence(song);
		}
		catch (InvalidMidiDataException e) {
			error("The file is not a valid MIDI file:\n" + songFile.getAbsolutePath() + "\n\n"
					+ e.getMessage(), "Invalid MIDI File");
			return;
		}
		catch (IOException e) {
			error(e.getMessage(), "Error Reading File");
			return;
		}

		this.songFile = songFile;

		prefs.put("fileChooserDirectory", fileChooser.getCurrentDirectory().getAbsolutePath());
		prefs.put("lastSongPath", songFile.getAbsolutePath());
		TrackMetaInfo[] trackInfo = TrackMetaInfo.analyzeTracks(sequencer);

		songPrefs = prefs.node("songs/" + songFile.getName().toLowerCase());
		for (int i = 0; i < trackInfo.length; i++) {
			boolean mute = songPrefs.getBoolean("Track " + i, trackInfo[i].hasDrums);
			sequencer.setTrackMute(i, mute);
		}

		transposeSpinner.setEnabled(true);
		tempoSpinner.setEnabled(true);
		bestTransposeButton.setEnabled(true);
		exportAbcMenuItem.setEnabled(true);

		String songName = songFile.getName();
		int dot = songName.lastIndexOf('.');
		if (dot > 0) {
			songName = songName.substring(0, dot);
		}
		songName = songName.replace('_', ' ');
		titleText.setEnabled(true);
		titleText.setText(songPrefs.get("title", songName));

		abcSaveChooser.setSelectedFile(new File(abcSaveChooser.getCurrentDirectory() + "\\"
				+ songPrefs.get("abcFileName", songName.replace(' ', '_'))));

		trackPanel.songChanged(sequencer, trackInfo);
		transposeInfo = new TransposeInfo(sequencer, (Instrument) instrumentComboBox
				.getSelectedItem());

		int transpose = songPrefs.getInt("transpose", transposeInfo.getBestTranspose());
		transposeSpinner.setValue(transpose);
		bestTransposeButton.setText("Best: " + transposeInfo.getBestTranspose());
		bestTransposeButton.setEnabled(transpose != transposeInfo.getBestTranspose());

		lotroReceiver.setTranspose(transpose);
		updateTransposeText();

		double tempo = songPrefs.getDouble("tempo", 1.0);
		tempoSpinner.setValue((int) (tempo * 100));
		sequencer.setTempoFactor((float) tempo);

		positionBar.setSongPosition(sequencer.getMicrosecondPosition());
		positionBar.setSongLength(sequencer.getMicrosecondLength());

		playButton.setEnabled(true);
		stopButton.setEnabled(false);

		updateTimerLabel();
	}

	private void exportAbc() {
		int res = abcSaveChooser.showSaveDialog(this);
		if (res != JFileChooser.APPROVE_OPTION)
			return;
		prefs.put("abcDirectory", abcSaveChooser.getCurrentDirectory().getAbsolutePath());
		songPrefs.put("abcFileName", abcSaveChooser.getSelectedFile().getName());

		File saveTo = abcSaveChooser.getSelectedFile();
		if (saveTo.getName().indexOf('.') < 0) {
			saveTo = new File(saveTo.getParent() + File.separator + saveTo.getName() + ".abc");
		}
		if (saveTo.exists()) {
			res = JOptionPane.showConfirmDialog(this, "File " + saveTo.getName()
					+ " already exists. Overwrite?", "Confirm overwrite file",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

			if (res == JOptionPane.CANCEL_OPTION) {
				exportAbc();
				return;
			}
		}

		try {
			PrintStream out = new PrintStream(saveTo);
			MidiToAbc.convert(sequencer, titleText.getText(), songFile.getName(), lotroReceiver,
					out);
			JOptionPane.showMessageDialog(this, "<html>Song successfully exported to ABC.<br><br>" //
					+ "Title: <b>" + titleText.getText() + "</b><br>" //
					+ "File name: <b>" + saveTo.getName() + "</b></html>", "Export success",
					JOptionPane.INFORMATION_MESSAGE);
			out.close();
		}
		catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this, "Export failed:\n" + e.getMessage(),
					"Export Failed", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void refreshTransposeInfo() {
		transposeInfo = new TransposeInfo(sequencer, (Instrument) instrumentComboBox
				.getSelectedItem());
		bestTransposeButton.setText("Best: " + transposeInfo.getBestTranspose());
		bestTransposeButton.setEnabled(transposeInfo.getBestTranspose() != lotroReceiver
				.getTranspose());
		// XXX
		updateTransposeText();
	}

	private void updateTransposeText() {
		int notes = transposeInfo.getNotesMissed(lotroReceiver.getTranspose());
		if (notes == 1) {
			notesMissedLabel.setText("1 note out of range");
		}
		else {
			notesMissedLabel.setText(notes + " notes out of range");
		}
	}

	public void trackMuteChanged(TrackMuteEvent e) {
		sequencer.setTrackMute(e.getTrack(), e.isMuted());
		lotroReceiver.releaseAllKeys();
		refreshTransposeInfo();
		songPrefs.putBoolean("Track " + e.getTrack(), e.isMuted());
	}

	public void songPositionChanged(SongPositionEvent e) {
		sequencer.setMicrosecondPosition(e.getSongPosition());
	}

	public void songPositionDrag(SongPositionEvent e) {
		updateTimerLabel();
	}

	private static String formatDuration(long micros) {
		StringBuilder s = new StringBuilder(5);

		int t = (int) (micros / (1000 * 1000));

		int hr = t / (60 * 60);
		t %= 60 * 60;
		int min = t / 60;
		t %= 60;
		int sec = t;

		if (hr > 0) {
			s.append(hr).append(':');
			if (min < 10) {
				s.append('0');
			}
		}
		s.append(min).append(':');
		if (sec < 10) {
			s.append('0');
		}
		s.append(sec);

		return s.toString();
	}

	private void showCantFindWindowError() {
		error("Unable to find Lord of the Rings Online window.\n"
				+ "Make sure that the game is running.",
				"Unable to find Lord of the Rings Online Window");
	}

	private void error(String message, String title) {
		JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
	}

	private class CleanupThread extends Thread {
		public CleanupThread() {
			super("LotRO MIDI Player Cleanup Thread");
		}

		@Override
		public void run() {
			try {
				if (sequencer != null) {
					if (sequencer.isRunning()) {
						sequencer.stop();
					}
					if (sequencer.isOpen()) {
						sequencer.close();
					}
				}
			}
			catch (Exception ex) {
			}

			try {
				if (transmitter != null) {
					transmitter.close();
				}
			}
			catch (Exception ex) {
			}

			try {
				if (lotroReceiver != null) {
					lotroReceiver.close();
				}
			}
			catch (Exception ex) {
			}

			try {
				if (midiPianoFrame != null) {
					midiPianoFrame.stop();
				}
			}
			catch (Exception ex) {
			}
		}
	}
}
