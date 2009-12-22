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

package com.digero.lotromusic.midi;

import java.util.LinkedList;
import java.util.Queue;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import com.digero.lotromusic.keyboard.KeyInfo;
import com.digero.lotromusic.keyboard.MusicKeymap;
import com.digero.lotromusic.keyboard.Note;
import com.digero.lotromusic.windows.WinApi;

public class LotroMidiReceiver implements Receiver {
	public static final int MAX_POLYPHONY = 3;
	private static final int ALL_NOTES_OFF = 0x7B;

	private int hWnd = 0;
	private int transpose;
	private boolean unfocusAfterNote = false;
	private MusicKeymap map = null;
	private Queue<KeyInfo> keysDown = new LinkedList<KeyInfo>();
	private Receiver midiReceiver;
	private boolean localPreviewMode;
	private long lastTimeStamp = 0;
	private Note lowestNote = Note.MIN_PLAYABLE;
	private Note highestNote = Note.MAX_PLAYABLE;
	private boolean transposeNotesOutOfRange = true;

	public LotroMidiReceiver() {
		this(0);
	}

	public LotroMidiReceiver(int transpose) {
		this.transpose = transpose;
		try {
			midiReceiver = MidiSystem.getReceiver();
		}
		catch (MidiUnavailableException e) {
			midiReceiver = null;
		}
		localPreviewMode = false;
	}

	public void close() {
		releaseAllKeys();
		if (midiReceiver != null) {
			midiReceiver.close();
		}
	}

	public int getTranspose() {
		return transpose;
	}

	public void setTranspose(int transpose) {
		releaseAllKeys();
		this.transpose = transpose;
	}

	public void setPlaySoundInactive(boolean playSoundInactive) {
		this.unfocusAfterNote = !playSoundInactive;
	}

	public boolean isPlaySoundInactive() {
		return !unfocusAfterNote;
	}

	public int getHWnd() {
		return hWnd;
	}

	public boolean resetHWnd() {
		hWnd = WinApi.FindWindow("Turbine Device Class", null);
		return hWnd != 0;
	}

	public boolean isLocalPreviewMode() {
		return localPreviewMode;
	}

	public void setLocalPreviewMode(boolean localPreviewMode) {
		this.localPreviewMode = localPreviewMode;
		releaseAllKeys();
	}

	public MusicKeymap getKeyMap() {
		if (map == null) {
			map = new MusicKeymap();
		}
		return map;
	}

	public boolean isValidHWnd() {
		return hWnd != 0;
	}

	public Note getLowestNote() {
		return lowestNote;
	}

	public void setLowestNote(Note lowestNote) {
		this.lowestNote = lowestNote;
	}

	public Note getHighestNote() {
		return highestNote;
	}

	public void setHighestNote(Note highestNote) {
		this.highestNote = highestNote;
	}

	public void setTransposeNotesOutOfRange(boolean transposeNotesOutOfRange) {
		this.transposeNotesOutOfRange = transposeNotesOutOfRange;
	}

	public boolean getTransposeNotesOutOfRange() {
		return transposeNotesOutOfRange;
	}

	public void releaseAllKeys() {
		if (keysDown.size() > 0) {
			WinApi.SendFocusMessage(hWnd, true);
			while (keysDown.size() > 0) {
				WinApi.KeyUp(hWnd, keysDown.poll(), true);
			}
			WinApi.SendFocusMessage(hWnd, false);
		}

		if (midiReceiver != null) {
			try {
				ShortMessage allNotesOff = new ShortMessage();
				for (int i = 0; i < 16; i++) {
					allNotesOff.setMessage(ShortMessage.CONTROL_CHANGE, i, ALL_NOTES_OFF, 0);
					midiReceiver.send(allNotesOff, lastTimeStamp);
				}
			}
			catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}
		}
	}

	public void send(MidiMessage message, long timeStamp) {
		lastTimeStamp = timeStamp;
		if (localPreviewMode && midiReceiver != null) {
			if (message instanceof ShortMessage) {
				ShortMessage m = (ShortMessage) message;
				int cmd = m.getCommand();
				if (cmd == ShortMessage.NOTE_ON || cmd == ShortMessage.NOTE_OFF) {
					int note = m.getData1() + transpose;
					if (transposeNotesOutOfRange) {
						while (note > highestNote.id) {
							note -= 12;
						}
						while (note < lowestNote.id) {
							note += 12;
						}
					}
					try {
						m.setMessage(cmd, m.getChannel(), note, m.getData2());
					}
					catch (InvalidMidiDataException e) {
						e.printStackTrace();
					}
				}
			}

			midiReceiver.send(message, timeStamp);
		}
		else if (message instanceof ShortMessage) {
			ShortMessage m = (ShortMessage) message;

			int cmd = m.getCommand();
			if (cmd == ShortMessage.NOTE_ON || cmd == ShortMessage.NOTE_OFF) {
				int speed = m.getData2();
				int note = m.getData1() + transpose;
				if (transposeNotesOutOfRange) {
					while (note > highestNote.id) {
						note -= 12;
					}
					while (note < lowestNote.id) {
						note += 12;
					}
				}

				KeyInfo kbdKey = getKeyMap().getKey(note);

				if (kbdKey != null) {
					if (cmd == ShortMessage.NOTE_ON && speed > 0) {
						Thread t = Thread.currentThread();
						if (t.getPriority() != 3) {
							t.setPriority(3);
						}

						WinApi.SendFocusMessage(hWnd, true);

						if (keysDown.size() >= MAX_POLYPHONY) {
							WinApi.KeyUp(hWnd, keysDown.poll(), false);
						}

						WinApi.KeyDown(hWnd, kbdKey, false);
						if (unfocusAfterNote) {
							WinApi.SendFocusMessage(hWnd, false);
						}
						keysDown.add(kbdKey);
					}
					else {
						if (keysDown.contains(kbdKey)) {
							WinApi.SendFocusMessage(hWnd, true);
							WinApi.KeyUp(hWnd, kbdKey, false);
							if (unfocusAfterNote) {
								WinApi.SendFocusMessage(hWnd, false);
							}
							keysDown.remove(kbdKey);
						}
					}
				}
			}
			else if (cmd == ShortMessage.CONTROL_CHANGE) {
				// Handle "all notes off"
				if (m.getData1() == ALL_NOTES_OFF) {
					releaseAllKeys();
				}
				if (midiReceiver != null) {
					midiReceiver.send(message, timeStamp);
				}
			}
			else if (cmd == ShortMessage.PROGRAM_CHANGE && midiReceiver != null) {
				midiReceiver.send(message, timeStamp);
			}
		}
	}
}
