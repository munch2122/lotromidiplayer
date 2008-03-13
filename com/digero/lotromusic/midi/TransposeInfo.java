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

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import com.digero.lotromusic.keyboard.Instrument;
import com.digero.lotromusic.keyboard.Note;

public class TransposeInfo {
	private int[] noteCounts;
	private int bestTranspose;
	private int totalNoteCount;
	private Note highestPlayable, lowestPlayable;

	public TransposeInfo(Sequencer seq) {
		this(seq, Note.C2, Note.C5);
	}

	public TransposeInfo(Sequencer seq, Instrument instrument) {
		this(seq, instrument.lowestPlayable, instrument.highestPlayable);
	}

	public TransposeInfo(Sequencer seq, Note lowestPlayable, Note highestPlayable) {
		Sequence song = seq.getSequence();
		Track[] tracks = song.getTracks();
		this.lowestPlayable = lowestPlayable;
		this.highestPlayable = highestPlayable;

		noteCounts = new int[128];
		totalNoteCount = 0;
		int highestNote = 0, lowestNote = noteCounts.length;

		for (int i = 0; i < tracks.length; i++) {
			if (seq.getTrackMute(i))
				continue;

			Track t = tracks[i];
			for (int j = 0, sz = t.size(); j < sz; j++) {
				MidiMessage msg = t.get(j).getMessage();

				if (msg instanceof ShortMessage) {
					ShortMessage m = (ShortMessage) msg;
					if (m.getCommand() == ShortMessage.NOTE_ON) {
						int note = m.getData1();
						int volume = m.getData2();
						if (note >= 0 && note < noteCounts.length && volume != 0) {
							totalNoteCount++;
							noteCounts[note]++;
							if (note < lowestNote) {
								lowestNote = note;
							}
							else if (note > highestNote) {
								highestNote = note;
							}
						}
					}
				}
			}
		}

		// Integrate noteCounts
		for (int i = 1; i < noteCounts.length; i++) {
			noteCounts[i] += noteCounts[i - 1];
		}

		int high = highestPlayable.id, low = lowestPlayable.id;

		bestTranspose = 0;
		int bestMissed = totalNoteCount;

		for (int tp = Math.max(0, low - lowestNote); tp >= Math.min(0, high - highestNote); tp--) {
			int missed = noteCounts[low - tp] + 2 * (totalNoteCount - noteCounts[high - tp]);

			if (missed + Math.abs(tp) < bestMissed + Math.abs(bestTranspose)) {
				bestTranspose = tp;
				bestMissed = missed;
			}
		}
	}

	public int getBestTranspose() {
		return bestTranspose;
	}

	public int getBestTransposeMissed() {
		return getNotesMissed(bestTranspose);
	}

	public int getNotesMissed(int transpose) {
		return noteCounts[lowestPlayable.id - transpose] + totalNoteCount
				- noteCounts[highestPlayable.id - transpose];
	}

	public int getNotesTooHigh(int transpose) {
		return totalNoteCount - noteCounts[highestPlayable.id - transpose];
	}

	public int getNotesTooLow(int transpose) {
		return noteCounts[lowestPlayable.id - transpose];
	}
}
