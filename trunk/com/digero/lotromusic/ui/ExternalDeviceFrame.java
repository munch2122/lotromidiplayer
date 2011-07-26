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
import info.clearthought.layout.TableLayoutConstants;

import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.digero.lotromusic.midi.LotroMidiReceiver;
import com.digero.lotromusic.windows.NativeApi;
import com.digero.lotromusic.windows.NativeApiUnavailableException;

@SuppressWarnings("serial")
public class ExternalDeviceFrame extends JFrame implements TableLayoutConstants {
	private JComboBox deviceSelector;
	private JButton refreshButton;
	private JLabel descriptionLabel;
	private JSpinner transposeSpinner;

	private ButtonGroup outOfRangeGroup;
	private JRadioButton transposeOctave;
	private JRadioButton dontPlay;

	private ImageIcon playIcon, stopIcon;
	private JButton playButton;

	private MidiDevice deviceInUse = null;
	private LotroMidiReceiver lotroReceiver;

	private Preferences prefs = Preferences.userNodeForPackage(ExternalDeviceFrame.class);

	private boolean isRunning = false;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {}
		ExternalDeviceFrame edf = new ExternalDeviceFrame();
		edf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		edf.setVisible(true);
	}

	public ExternalDeviceFrame() {
		super("Connect MIDI Piano");

		try {
			List<Image> icons = new ArrayList<Image>();
			icons.add(ImageIO.read(MainWindow.class.getResource("icn_16.png")));
			icons.add(ImageIO.read(MainWindow.class.getResource("icn_32.png")));
			setIconImages(icons);
		}
		catch (Exception ex) {
			// Ignore
		}

		int transpose = prefs.getInt("transpose", 0);
		lotroReceiver = new LotroMidiReceiver(transpose);

		deviceSelector = new JComboBox();
		deviceSelector.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object obj = deviceSelector.getSelectedItem();
				if (obj instanceof DeviceWrapper) {
					DeviceWrapper item = (DeviceWrapper) obj;
					if (deviceInUse != item.device) {
						stop();
					}
					deviceInUse = item.device;
					descriptionLabel.setText(item.getDescription());
				}
			}
		});

		refreshButton = new JButton("Refresh");
		refreshButton.setToolTipText("If clicking refresh doesn't make your "
				+ "device appear, try restarting the program.");
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshDevices();
			}
		});

		descriptionLabel = new JLabel(" ");

		transposeSpinner = new JSpinner(new SpinnerNumberModel(transpose, -36, 36, 1));
		transposeSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int transpose = (Integer) transposeSpinner.getValue();
				lotroReceiver.setTranspose(transpose);
				prefs.putInt("transpose", transpose);
			}
		});

		ActionListener outOfRangeActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lotroReceiver.setTransposeNotesOutOfRange(transposeOctave.isSelected());
				prefs.putBoolean("transposeOutOfRange", transposeOctave.isSelected());
			}
		};

		transposeOctave = new JRadioButton("Transpose it up or down by an octave");
		transposeOctave.setSelected(prefs.getBoolean("transposeOutOfRange", true));
		transposeOctave.addActionListener(outOfRangeActionListener);

		dontPlay = new JRadioButton("Don't play the note");
		dontPlay.setSelected(!transposeOctave.isSelected());
		dontPlay.addActionListener(outOfRangeActionListener);

		outOfRangeGroup = new ButtonGroup();
		outOfRangeGroup.add(transposeOctave);
		outOfRangeGroup.add(dontPlay);

		playIcon = new ImageIcon(MainWindow.class.getResource("play.png"));
		stopIcon = new ImageIcon(MainWindow.class.getResource("stop.png"));

		playButton = new JButton(playIcon);
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (isRunning)
					stop();
				else
					start();
			}
		});

		addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				stop();
			}
		});

		double[] cols = new double[] {
				4, PREFERRED, 150, PREFERRED, FILL, 4
		};
		double[] rows = new double[] {
				4, PREFERRED, PREFERRED, PREFERRED, 4, PREFERRED, PREFERRED, 4
		};
		TableLayout layout = new TableLayout(cols, rows);
		layout.setHGap(4);
		layout.setVGap(4);

		JPanel cp = new JPanel(layout);
		setContentPane(cp);

		cp.add(new JLabel("Device:"), "1, 1");
		cp.add(deviceSelector, "2, 1");
		cp.add(refreshButton, "3, 1");

		cp.add(descriptionLabel, "2, 2, 4, 2");

		JPanel tpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		tpPanel.add(transposeSpinner);
		cp.add(new JLabel("Transpose:"), "1, 3");
		cp.add(tpPanel, "2, 3");

		cols = new double[] {
				25, FILL
		};
		rows = new double[] {
				PREFERRED, PREFERRED, PREFERRED
		};
		JPanel rangePanel = new JPanel(new TableLayout(cols, rows));
		rangePanel.add(new JLabel("If a note is out of range:"), "0, 0, 1, 0");
		rangePanel.add(transposeOctave, "1, 1");
		rangePanel.add(dontPlay, "1, 2");

		cp.add(rangePanel, "1, 5, 4, 5");

		JPanel playPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2));
		playPanel.add(playButton);
		cp.add(playPanel, "1, 6, 5, 6");

		refreshDevices();

		pack();
		setResizable(false);
	}

	@Override
	protected void finalize() throws Throwable {
		stop();
		super.finalize();
	}

	public void centerOn(Rectangle bounds) {
		int x = bounds.x + bounds.width / 2 - getWidth() / 2;
		int y = bounds.y + bounds.height / 2 - getHeight() / 2;
		setLocation(x, y);
	}

	public void start() {
		if (isRunning)
			return;

		if (deviceInUse == null) {
			error("No device is selected. Please select a device.", "No device selected");
			return;
		}

		try {
			if (!lotroReceiver.resetHWnd()) {
				error("Unable to find Lord of the Rings Online window.\n" + "Make sure that the game is running.",
						"Unable to find Lord of the Rings Online Window");
				return;
			}
		}
		catch (NativeApiUnavailableException e1) {
			error(NativeApi.ERROR_MESSAGE, NativeApi.ERROR_TITLE);
		}

		try {
			deviceInUse.open();
			deviceInUse.getTransmitter().setReceiver(lotroReceiver);
			playButton.setIcon(stopIcon);
			isRunning = true;
		}
		catch (MidiUnavailableException e) {
			error("Failed to open device: " + deviceInUse.getDeviceInfo().getName() + "\n" + e.getMessage(),
					"Failed to open device");
		}
	}

	public void stop() {
		playButton.setIcon(playIcon);
		isRunning = false;
		if (deviceInUse != null && deviceInUse.isOpen()) {
			deviceInUse.close();
		}
	}

	private void refreshDevices() {
		stop();

		deviceSelector.removeAllItems();

		int deviceIndex = -1;

		for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
			try {
				MidiDevice device = MidiSystem.getMidiDevice(info);
				if (device.getMaxTransmitters() != 0) {
					if (deviceInUse == device) {
						deviceIndex = deviceSelector.getItemCount();
					}
					deviceSelector.addItem(new DeviceWrapper(device));
				}
			}
			catch (MidiUnavailableException e) {
				continue;
			}
		}

		if (deviceIndex == -1) {
			stop();
		}
		else {
			deviceSelector.setSelectedIndex(deviceIndex);
		}

		if (deviceSelector.getItemCount() == 0) {
			deviceSelector.addItem("No devices found");
			descriptionLabel.setText(" ");
			deviceSelector.setEnabled(false);
			playButton.setEnabled(false);
		}
		else {
			deviceSelector.setEnabled(true);
			playButton.setEnabled(true);
		}
	}

	private void error(String message, String title) {
		JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
	}

	private class DeviceWrapper {
		public final MidiDevice device;

		public DeviceWrapper(MidiDevice device) {
			this.device = device;
		}

		public String getDescription() {
			return device.getDeviceInfo().getDescription();
		}

		@Override
		public String toString() {
			return device.getDeviceInfo().getName();
		}
	}
}
