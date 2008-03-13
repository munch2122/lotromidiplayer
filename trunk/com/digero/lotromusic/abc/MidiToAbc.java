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

package com.digero.lotromusic.abc;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import com.digero.lotromusic.keyboard.Note;
import com.digero.lotromusic.midi.LotroMidiReceiver;

public class MidiToAbc {
	public static final int ONE_MINUTE_MICROS = 60000000; // 1 second
	public static final int SHORTEST_NOTE_MICROS = 62500; // 1/16 second
	public static final int LONGEST_NOTE_MICROS = 8000000; // 8 seconds
	public static final int MAX_CHORD_NOTES = 6;

	public static final int META_KEY_SIGNATURE = 0x59;
	public static final int META_TEMPO = 0x51;
	public static final int META_END_OF_TRACK = 0x2F;

	public static void convert(Sequencer seq, String title, PrintStream out) {
		convert(seq, title, title, 0, Note.C2, Note.C5, out);
	}

	public static void convert(Sequencer seq, String title, String originalFileName,
			LotroMidiReceiver rcvr, PrintStream out) {
		convert(seq, title, originalFileName, rcvr.getTranspose(), rcvr.getLowestNote(), rcvr
				.getHighestNote(), out);
	}

	/**
	 * Convert a MIDI song to an ABC file.
	 * 
	 * @param seq
	 *            The sequencer that has the MIDI sequence, track mute info, and tempo multiplier
	 *            info.
	 * @param title
	 *            The title of the ABC song.
	 * @param originalFileName
	 *            The original name of the MIDI file, or <code>null</code> if this isn't
	 *            available.
	 * @param transpose
	 *            The number of semitones to transpose each note up or down.
	 * @param lowestNote
	 *            The lowest playable note.
	 * @param highestNote
	 *            The highest playable note.
	 * @param out
	 *            The ABC file will be written to this stream.
	 */
	public static void convert(Sequencer seq, String title, String originalFileName, int transpose,
			Note lowestNote, Note highestNote, PrintStream out) {
		StringBuilder sb = new StringBuilder();

		Sequence song = seq.getSequence();
		Track[] tracks = song.getTracks();
		int ppqn = song.getResolution();

		List<NoteEvent> events = new ArrayList<NoteEvent>();
		List<NoteEvent> notesOn = new ArrayList<NoteEvent>();

		// The scores for different timing grid options
		long[] gridScores = new long[30];

		int[] trkPos = new int[tracks.length];

		// The last MIDI tick where we saw a tempo message
		long lastTempoTick = 0;
		// The last time we saw a tempo message in microseconds
		long lastTempoMicros = 0;
		// Tempo in microseconds per beat; default is 120 BPM
		int tempo = ONE_MINUTE_MICROS / 120;
		// The current microsecond position in the song
		long micros = 0;
		long songLengthMicros = 0;
		while (true) {
			// Find the track with the next MIDI message
			int trackToAdvance = -1;
			long nextEventTime = Long.MAX_VALUE;
			boolean eventMuted = false;
			for (int i = 0; i < tracks.length; i++) {
				Track t = tracks[i];
				if (trkPos[i] < t.size()) {
					MidiEvent e = t.get(trkPos[i]);
					if (e.getTick() < nextEventTime) {
						trackToAdvance = i;
						eventMuted = seq.getTrackMute(i);
						nextEventTime = e.getTick();
					}
				}
			}
			// We're done if there aren't any more MIDI messages
			if (trackToAdvance == -1) {
				break;
			}

			MidiEvent evt = tracks[trackToAdvance].get(trkPos[trackToAdvance]++);
			MidiMessage msg = evt.getMessage();

			// Compute the event time based on the tempo
			micros = ((evt.getTick() - lastTempoTick) * tempo) / ppqn + lastTempoMicros;

			if (msg instanceof MetaMessage) {
				// Keep track of any tempo changes
				MetaMessage meta = (MetaMessage) msg;
				if (meta.getType() == META_TEMPO) {
					lastTempoMicros = micros;
					lastTempoTick = evt.getTick();
					tempo = (int) (tempoFromMeta(meta) / seq.getTempoFactor());

					// System.out.print("Tempo Message: ");
					// System.out.print(Math.round(ONE_MINUTE_MICROS / (float) tempo) + " BPM @ ");
					// System.out.println(timeToString(Math.round(micros / 1000.0)));
				}
			}
			else if (msg instanceof ShortMessage) {
				ShortMessage m = (ShortMessage) msg;
				int cmd = m.getCommand();
				if (cmd == ShortMessage.NOTE_ON || cmd == ShortMessage.NOTE_OFF) {
					// Try 30 different possible tempos: [62 BPM .. 120 BPM] by 2's
					// We're checking all tracks here, even the muted ones, so that we have
					// identical quantization if the song is split into multiple abc files
					for (int i = 0; i < gridScores.length; i++) {
						// BPM = 120 - i * 2
						int quarterNote = ONE_MINUTE_MICROS / (120 - i * 2);

						// The length of one grid unit = the length of a 1/32nd note
						int grid = quarterNote / 8;
						long distance = micros - grid * (micros / grid);
						if (distance > grid / 2) {
							distance = grid - distance;
						}
						// Add this event's microsecond-distance to the timing grid's score
						gridScores[i] += distance;
					}

					if (!eventMuted) {
						int noteId = m.getData1() + transpose;
						while (noteId > highestNote.id) {
							noteId -= 12;
						}
						while (noteId < lowestNote.id) {
							noteId += 12;
						}

						int speed = m.getData2();
						if (cmd == ShortMessage.NOTE_ON && speed > 0) {
							Note note = Note.getById(noteId);
							if (note == null) {
								System.err.println("Invalid note ID: " + noteId);
								continue;
							}
							NoteEvent ne = new NoteEvent(micros, note);
							events.add(ne);
							notesOn.add(ne);
						}
						else {
							Iterator<NoteEvent> iter = notesOn.iterator();
							while (iter.hasNext()) {
								NoteEvent ne = iter.next();
								if (ne.note.id == noteId) {
									iter.remove();
									ne.endMicros = micros;
									break;
								}
							}
						}
						songLengthMicros = micros;
					}
				}
			}
		}

		if (events.size() == 0) {
			System.err.println("No events!");
			return;
		}

		if (notesOn.size() > 0) {
			System.err.println(notesOn.size() + " notes not turned off at end of song!");
			for (int i = 0; i < notesOn.size(); i++) {
				NoteEvent ne = notesOn.get(i);
				ne.endMicros = micros;
			}
		}

		// Find best tempo (quantization)
		int iBest = 0;
		for (int i = 1; i < gridScores.length; i++) {
			if (gridScores[i] < gridScores[iBest]) {
				iBest = i;
			}
		}
		int bpm = 120 - (2 * iBest);
		int quarterNote = ONE_MINUTE_MICROS / bpm;
		final int shortestNoteDivisor = 8;
		// Shortest note is a 1/32nd note
		int shortestNote = quarterNote / shortestNoteDivisor;
		// Longest note is as close to 8 seconds as we can get
		int longestNote = shortestNote * (LONGEST_NOTE_MICROS / shortestNote);

		// Quantize the events
		for (NoteEvent ne : events) {
			ne.startMicros = ((ne.startMicros + shortestNote / 2) / shortestNote) * shortestNote;
			ne.endMicros = ((ne.endMicros + shortestNote / 2) / shortestNote) * shortestNote;
		}

		// Add initial rest if necessary
		if (events.get(0).startMicros > 0) {
			events.add(0, new NoteEvent(0, Note.Rest, events.get(0).startMicros));
		}

		// Break long notes
		for (int i = 0; i < events.size(); i++) {
			NoteEvent ne = events.get(i);
			long endMicros = ne.endMicros;
			while (ne.getLength() > longestNote) {
				ne.setLength(longestNote);

				ne = new NoteEvent(ne.endMicros, ne.note, endMicros);
				// The insertion point will always be after the current index, so it's not going to
				// screw up the loop over the elements
				int ins = Collections.binarySearch(events, ne, EventTimeComparator.DEFAULT);
				if (ins < 0) {
					ins = -ins - 1;
				}
				events.add(ins, ne);
			}
		}

		// Remove duplicate notes
		notesOn.clear();
		Iterator<NoteEvent> neIter = events.iterator();
		dupLoop: while (neIter.hasNext()) {
			NoteEvent ne = neIter.next();
			Iterator<NoteEvent> onIter = notesOn.iterator();
			while (onIter.hasNext()) {
				NoteEvent on = onIter.next();
				if (on.endMicros < ne.startMicros) {
					// This note has already been turned off
					onIter.remove();
				}
				else if (on.note.id == ne.note.id) {
					if (on.startMicros == ne.startMicros) {
						// If they start at the same time, remove the second event.
						// Lengthen the first one if it's shorter than the second one.
						if (ne.endMicros > on.endMicros) {
							on.endMicros = ne.endMicros;
						}
						// Remove the duplicate note
						neIter.remove();
						continue dupLoop;
					}
					else {
						// Otherwise, if they don't start at the same time, shorten the note that's
						// currently on to end at the same time that the next one starts
						on.endMicros = ne.startMicros;
						onIter.remove();
					}
				}
			}
			notesOn.add(ne);
		}

		// Very rough estimate: ~2 notes per chord average
		List<Chord> chords = new ArrayList<Chord>(events.size() / 2);

		// Combine notes that play at the same time into chords
		Chord currentChord = new Chord(events.get(0));
		chords.add(currentChord);
		for (int i = 1; i < events.size(); i++) {
			NoteEvent ne = events.get(i);
			if (currentChord.getStartMicros() == ne.startMicros) {
				// This note starts at the same time as the rest of the notes in the chord
				currentChord.add(ne);
			}
			else {
				// Create a new chord
				Chord nextChord = new Chord(ne);

				// The next chord starts playing immediately after the *shortest* note (or rest) in
				// the current chord is finished, so we may need to add a rest inside the chord to
				// shorten it, or a rest after the chord to add a pause.

				if (currentChord.getEndMicros() > nextChord.getStartMicros()) {
					// If the chord is too long, add a short rest in the chord to shorten it
					currentChord.add(new NoteEvent(currentChord.getStartMicros(), Note.Rest,
							nextChord.getStartMicros()));
				}
				else if (currentChord.getEndMicros() < nextChord.getStartMicros()) {
					// If the chord is too short, insert rest(s) to fill the gap
					long restStart = currentChord.getEndMicros();
					long restEnd = nextChord.getStartMicros();

					// We may need to add multiple rests since the maximum length for a rest is 8s
					while (restEnd - restStart > longestNote) {
						chords.add(new Chord(new NoteEvent(restStart, Note.Rest, restStart
								+ longestNote)));
						restStart += longestNote;
					}

					// In the event that the rest length was an exact multiple of longestNote, we
					// don't want to add a zero-length rest
					if (restStart < restEnd) {
						chords.add(new Chord(new NoteEvent(restStart, Note.Rest, restEnd)));
					}
				}

				chords.add(nextChord);
				currentChord = nextChord;
			}
		}

		// Keep track of which notes have been sharped or flatted so we can naturalize them the next
		// time they show up.
		boolean[] accented = new boolean[Note.C5.id + 1];

		// Write out ABC notation
		for (Chord c : chords) {
			if (c.size() == 0) {
				System.err.println("Chord has no notes!");
				continue;
			}

			if (c.size() > 1) {
				sb.append('[');
			}

			int notesWritten = 0;
			for (int j = 0; j < c.size() && notesWritten < MAX_CHORD_NOTES; j++) {
				NoteEvent evt = c.get(j);
				if (evt.getLength() == 0) {
					System.err.println("Zero-length note");
					continue;
				}

				if (evt.note.isAccented) {
					accented[evt.note.naturalId] = true;
				}
				else if (accented[evt.note.id]) {
					accented[evt.note.id] = false;
					sb.append('=');
				}

				sb.append(evt.note.abc);
				int numerator = (int) (evt.getLength() / shortestNote);
				// Reduce the fraction
				int gcd = gcd(numerator, shortestNoteDivisor);
				numerator /= gcd;
				int denominator = shortestNoteDivisor / gcd;
				if (numerator != 1) {
					sb.append(numerator);
				}
				if (denominator != 1) {
					sb.append('/').append(denominator);
				}

				notesWritten++;
			}

			if (c.size() > 1) {
				if (notesWritten == 0) {
					// Remove the [
					sb.delete(sb.length() - 1, sb.length());
				}
				else {
					sb.append(']');
				}
			}

			sb.append(' ');
		}

		// Insert line breaks
		final int LINE_LENGTH = 70;
		for (int i = LINE_LENGTH; i < sb.length(); i += LINE_LENGTH) {
			for (int j = 0; j < LINE_LENGTH - 1; j++, i--) {
				if (sb.charAt(i) == ' ') {
					sb.replace(i, i + 1, "\r\n");
					i++;
					break;
				}
			}
		}

		out.println("X: 1");
		out.println("T: " + title + " (" + timeToString(songLengthMicros) + ")");
		out.println("Z: Transcribed by LotRO MIDI Player: http://lotro.acasylum.com/midi");
		if (originalFileName != null) {
			out.println("%  Original file: " + originalFileName);
		}
		out.println("%  Transpose: " + transpose);
		if (seq.getTempoFactor() != 1) {
			out.println("%  Tempo factor: " + Math.round(100 * seq.getTempoFactor()) + "%");
		}
		out.println("L: 1/4");
		out.println("Q: " + bpm);
		out.println("K: C");
		out.println();
		out.print(sb.toString());
	}

	private static int gcd(int a, int b) {
		while (b != 0) {
			int t = b;
			b = a % b;
			a = t;
		}
		return a;
	}

	private static int tempoFromMeta(MetaMessage msg) {
		byte[] tempoBytes = msg.getData();
		int tempo = (((int) tempoBytes[0]) & 0xFF) << 16;
		tempo |= (((int) tempoBytes[1]) & 0xFF) << 8;
		tempo |= (((int) tempoBytes[2]) & 0xFF);
		return tempo;
	}

	private static String timeToString(long timeMicros) {
		long hr = timeMicros / (1000000 * 60 * 60);
		long min = timeMicros / (1000000 * 60) - hr * 60;
		long sec = Math.round(timeMicros / 1000000.0) - (hr * 60 + min) * 60;

		StringBuilder sb = new StringBuilder();

		if (hr > 0) {
			sb.append(hr).append(":");
			if (min < 10) {
				sb.append("0");
			}
		}
		sb.append(min).append(":");
		if (sec < 10) {
			sb.append("0");
		}
		sb.append(sec);

		return sb.toString();
	}

	private static class EventTimeComparator implements Comparator<NoteEvent> {
		public static final EventTimeComparator DEFAULT = new EventTimeComparator();

		public int compare(NoteEvent n1, NoteEvent n2) {
			return (int) Math.signum(n1.startMicros - n2.startMicros);
		}
	}
}
