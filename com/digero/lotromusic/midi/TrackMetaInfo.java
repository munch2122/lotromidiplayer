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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class TrackMetaInfo {
	public static final int META_TEXT = 0x01;
	public static final int META_COPYRIGHT = 0x02;
	public static final int META_TRACKNAME = 0x03;
	public static final int META_INSTRUMENT = 0x04;
	public static final int META_PROGRAMNAME = 0x08;

	public static TrackMetaInfo[] analyzeTracks(Sequencer seq) {
		Sequence song = seq.getSequence();
		Track[] tracks = song.getTracks();
		TrackMetaInfo[] trackInfo = new TrackMetaInfo[tracks.length];

		for (int i = 0; i < tracks.length; i++) {
			Track t = tracks[i];
			trackInfo[i] = new TrackMetaInfo();

			for (int j = 0, sz = t.size(); j < sz; j++) {
				MidiMessage msg = t.get(j).getMessage();

				if (msg instanceof ShortMessage) {
					ShortMessage m = (ShortMessage) msg;
					int cmd = m.getCommand();
					int speed = m.getData2();
					if (cmd == ShortMessage.NOTE_ON && speed != 0) {
						if (m.getChannel() == 9 && !trackInfo[i].hasDrums) {
							// Percussion channel
							trackInfo[i].hasDrums = true;
							if (trackInfo[i].noteCount == 0) {
								trackInfo[i].instruments.clear();
							}
						}
						trackInfo[i].noteCount++;
					}
					else if (cmd == ShortMessage.PROGRAM_CHANGE) {
						int instrument = m.getData1();
						trackInfo[i].addInstrument(instrument);
					}
				}
				else if (msg instanceof MetaMessage) {
					MetaMessage m = (MetaMessage) msg;

					byte[] data = m.getData();
					try {
						switch (m.getType()) {
							case META_TEXT: {
								String value = new String(data, 0, data.length, "US-ASCII");
								if (trackInfo[i].text == null)
									trackInfo[i].text = value.trim();
								break;
							}
							case META_TRACKNAME: {
								String value = new String(data, 0, data.length, "US-ASCII");
								trackInfo[i].name = value.trim();
								break;
							}
							case META_INSTRUMENT: {
								String value = new String(data, 0, data.length, "US-ASCII");
								trackInfo[i].instrumentName = value.trim();
								break;
							}
							case META_PROGRAMNAME: {
								String value = new String(data, 0, data.length, "US-ASCII");
								trackInfo[i].programName = value.trim();
								break;
							}
						}
					}
					catch (UnsupportedEncodingException ex) {
						throw new RuntimeException(ex);
					}
				}
			}
		}

		return trackInfo;
	}

	public static String getInstrumentName(int id) {
		if (id < 0 || id >= midiInstrumentNames.length) {
			return "Unknown";
		}
		return midiInstrumentNames[id];
	}

	public String name;
	public String instrumentName;
	public String text;
	public String programName;
	public int noteCount;
	public boolean hasDrums = false;
	public List<Integer> instruments = new ArrayList<Integer>();

	public void addInstrument(int instrument) {
		if (!instruments.contains(instrument)) {
			instruments.add(instrument);
		}
	}

	public String getDescription() {
		return getDescription(" / ");
	}

	public String getDescription(String separator) {
		StringBuilder sb = new StringBuilder();

		if (name != null) {
			sb.append(name);
		}
		if (instrumentName != null) {
			if (sb.length() != 0)
				sb.append(separator);
			sb.append(instrumentName);
		}

		if (sb.length() == 0) {
			if (text != null) {
				sb.append(text);
			}
			else if (programName != null) {
				sb.append(programName);
			}
		}

		return sb.toString();
	}

	public String getInstrumentNames() {
		if (instruments.size() == 0) {
			if (hasDrums)
				return "Drums";
			else
				return midiInstrumentNames[0];
		}

		StringBuilder sb = new StringBuilder();
		if (hasDrums) {
			sb.append("Drums, ");
		}

		sb.append(getInstrumentName(instruments.get(0)));

		for (int i = 1; i < instruments.size(); i++) {
			sb.append(", ").append(getInstrumentName(instruments.get(i)));
		}

		return sb.toString();
	}

	public String getNoteCountString() {
		if (noteCount == 1) {
			return "1 note";
		}
		return noteCount + " notes";
	}

	@Override
	public String toString() {
		return getDescription();
	}

	private static final String[] midiInstrumentNames = { "Piano", "Bright Piano", "Elec Piano",
			"Honky-tonk Piano", "Rhodes Piano", "Chorus Piano", "Harpschord", "Clavinet",
			"Celesta", "Glockenspiel", "Music Box", "Vibraphone", "Marimba", "Xylophone",
			"Tubular Bells", "Dulcimer", "Hammond Organ", "Perc Organ", "Rock Organ",
			"Church Organ", "Reed Organ", "Accordion", "Harmonica", "Tango Acordn", "Nylon Guitar",
			"Steel String Guitar", "Jazz Guitar", "Clean Electric Guitar", "Mute Electric Guitar",
			"Ovrdrive Guitar", "Distorted Guitar", "Harmonics", "Acoustic Bass",
			"Fingered Electric Bass", "Picked Electric Bass", "Fretles Bass", "Slap Bass 1",
			"Slap Bass 2", "Synth Bass 1", "Synth Bass 2", "Violin", "Viola", "Cello",
			"Contrabass", "Tremolo Strings", "Pizzicato Strings", "Orchestra Harp", "Timpani",
			"String Ensemble 1", "String Ensemble 2", "Synth String 1", "Synth String 2",
			"Choir Aahs", "Voice Oohs", "Synth Voice", "Orchestra Hit", "Trumpet", "Trombone",
			"Tuba", "Mute Trumpet", "French Horn", "Brass Section", "Synth Brass 1",
			"Synth Brass 2", "Soprano Sax", "Alto Sax", "Tenor Sax", "Bari Sax", "Oboe",
			"Englsh Horn", "Bassoon", "Clarinet", "Piccolo", "Flute", "Recorder", "Pan Flute",
			"Bottle Blow", "Shakuhachi", "Whistle", "Ocarina", "Square Wave", "Saw Tooth",
			"Caliope", "Chiff Lead", "Charang", "Solo Synth Vox", "Brite Saw", "Brass & Lead",
			"Fantasa Pad", "Warm Pad", "Poly Synth Pad", "Space Vox Pad", "Bow Glass Pad",
			"Metal Pad", "Halo Pad", "Sweep Pad", "Ice Rain", "Sound Track", "Crystal",
			"Atmosphere", "Brightness", "Goblin", "Echo Drops", "Star Theme", "Sitar", "Banjo",
			"Shamisen", "Koto", "Kalimba", "Bag Pipe", "Fiddle", "Shanai", "Tinkle Bell", "Agogo",
			"Steel Drums", "Woodblock", "Taiko Drum", "Melodic Tom", "Synth Drum", "Revrse Cymbal",
			"Guitar Fret Noise", "Breath Noise", "Sea Shore", "Bird Tweet", "Telephone Ring",
			"Helicopter", "Applause", "Gun Shot" };
}
