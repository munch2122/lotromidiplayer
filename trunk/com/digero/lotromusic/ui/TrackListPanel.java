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

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.Sequencer;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.digero.lotromusic.midi.TrackMetaInfo;
import com.digero.lotromusic.ui.events.TrackMuteEvent;
import com.digero.lotromusic.ui.events.TrackMuteListener;

@SuppressWarnings("serial")
public class TrackListPanel extends JPanel implements TableLayoutConstants {
	private Map<Integer, JCheckBox> checkBoxes = new HashMap<Integer, JCheckBox>();
	private String songFileName = "";

	private List<TrackMuteListener> trackMuteListeners = new ArrayList<TrackMuteListener>();

	public TrackListPanel() {
		super(new TableLayout(new double[] { 4, PREFERRED, 8, PREFERRED, 8, PREFERRED, FILL },
				new double[] {}));

		setBackground(Color.WHITE);

		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				requestFocus();
			}
		});
	}

	public void clear() {
		removeAll();
		TableLayout layout = (TableLayout) getLayout();
		for (int i = layout.getNumRow() - 1; i >= 0; i--) {
			layout.deleteRow(i);
		}
		validate2();
	}

	public void addTrackMuteListener(TrackMuteListener l) {
		trackMuteListeners.add(l);
	}

	public void removeTrackMuteListener(TrackMuteListener l) {
		trackMuteListeners.remove(l);
	}

	private void fireTrackMuteEvent(int track, boolean muted) {
		if (trackMuteListeners.size() > 0) {
			TrackMuteEvent evt = new TrackMuteEvent(this, track, muted);
			for (TrackMuteListener l : trackMuteListeners) {
				l.trackMuteChanged(evt);
			}
		}
	}

	public void validate2() {
		invalidate();
		repaint();
		// if (getParent() instanceof JScrollPane) {
		// getParent().validate();
		// }
		// else {
		// super.validate();
		// }
	}

	public void songChanged(Sequencer seq, TrackMetaInfo[] trackInfo) {
		clear();

		TableLayout layout = (TableLayout) getLayout();

		int row = 0;

		String songTitle;
		// if (trackInfo[0].noteCount == 0 && trackInfo[0].name != null) {
		// // Probably the song title
		// songTitle = trackInfo[0].name;
		// }
		// else {
		songTitle = songFileName;
		int idx = songTitle.lastIndexOf('.');
		if (idx > 0) {
			songTitle = songTitle.substring(0, idx);
		}
		// }

		layout.insertRow(0, 4);
		layout.insertRow(1, PREFERRED);
		JLabel title = new JLabel(songTitle);
		title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
		add(title, "1, 1, " + (layout.getNumColumn() - 1) + ", 1");
		layout.insertRow(2, 4);
		row += 3;

		for (int i = 0; i < trackInfo.length; i++) {
			if (trackInfo[i].noteCount != 0) {
				layout.insertRow(row, 18);

				String name = i + ". " + trackInfo[i].getInstrumentNames();
				JCheckBox checkBox = new JCheckBox(name, !seq.getTrackMute(i));
				checkBox.setFont(checkBox.getFont().deriveFont(Font.BOLD));
				checkBox.setBackground(getBackground());
				if (trackInfo[i].hasDrums) {
					checkBox.setForeground(Color.RED);
				}
				checkBox.addActionListener(new TrackChangeActionListener(i));
				checkBoxes.put(i, checkBox);

				JLabel noteCount = new JLabel(trackInfo[i].getNoteCountString());

				JLabel description = new JLabel(trackInfo[i].getDescription());

				add(checkBox, "1, " + row);
				add(noteCount, "3, " + row);
				add(description, "5, " + row);
				row++;
			}
		}
		validate2();
	}

	private class TrackChangeActionListener implements ActionListener {
		private int trackNumber;

		public TrackChangeActionListener(int trackNumber) {
			this.trackNumber = trackNumber;
		}

		public void actionPerformed(ActionEvent e) {
			JCheckBox cb = (JCheckBox) e.getSource();
			fireTrackMuteEvent(trackNumber, !cb.isSelected());
		}
	}
}
