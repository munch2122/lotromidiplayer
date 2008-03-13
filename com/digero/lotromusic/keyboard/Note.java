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

package com.digero.lotromusic.keyboard;

public enum Note {
	Rest(0),
	//
	C2(36), Cs2, Db2(Cs2), D2, Ds2, Eb2(Ds2), E2, F2, Fs2, Gb2(Fs2), G2, Gs2, Ab2(Gs2), A2, As2, Bb2(
			As2), B2,
	//
	C3, Cs3, Db3(Cs3), D3, Ds3, Eb3(Ds3), E3, F3, Fs3, Gb3(Fs3), G3, Gs3, Ab3(Gs3), A3, As3, Bb3(
			As3), B3,
	//
	C4, Cs4, Db4(Cs4), D4, Ds4, Eb4(Ds4), E4, F4, Fs4, Gb4(Fs4), G4, Gs4, Ab4(Gs4), A4, As4, Bb4(
			As4), B4,
	//
	C5;

	/** The MIDI ID for this note. */
	public final int id;
	/** The ABC notation for this file. */
	public final String abc;
	/** True if this note is a sharp or flat. */
	public final boolean isAccented;
	/** The ID of the natural of this note, if it's accented */
	public final int naturalId;

	private static class Meta {
		private static int nextId = 0;
		private static Note[] lookupMap = null;
	}

	public static Note getById(int id) {
		if (Meta.lookupMap == null) {
			Meta.lookupMap = new Note[C5.id + 1];
			for (Note n : values()) {
				if (Meta.lookupMap[n.id] == null)
					Meta.lookupMap[n.id] = n;
			}
		}

		if (id < 0 || id >= Meta.lookupMap.length) {
			return null;
		}
		return Meta.lookupMap[id];
	}

	private Note() {
		this(Meta.nextId);
	}

	private Note(Note copyFrom) {
		this(copyFrom.id);
	}

	private Note(int id) {
		this.id = id;
		Meta.nextId = id + 1;

		String abcTmp;
		if (id == 0) {
			abcTmp = "z";
			isAccented = false;
			naturalId = id;
		}
		else {
			String s = toString();
			abcTmp = s.substring(0, 1);
			switch (s.charAt(s.length() - 1)) {
				case '2':
					abcTmp = abcTmp.toUpperCase() + ",";
					break;
				case '3':
					abcTmp = abcTmp.toUpperCase();
					break;
				case '4':
					abcTmp = abcTmp.toLowerCase();
					break;
				case '5':
					abcTmp = abcTmp.toLowerCase() + "'";
					break;
				case '1':
					abcTmp = abcTmp.toUpperCase() + ",,";
					break;
				case '6':
					abcTmp = abcTmp.toLowerCase() + "''";
					break;
			}
			if (s.indexOf('s') == 1) {
				abcTmp = "^" + abcTmp;
				isAccented = true;
				naturalId = id - 1;
			}
			else if (s.indexOf('b') == 1) {
				abcTmp = "_" + abcTmp;
				isAccented = true;
				naturalId = id + 1;
			}
			else {
				isAccented = false;
				naturalId = id;
				// abcTmp = "=" + abcTmp;
			}
		}

		abc = abcTmp;
	}
}