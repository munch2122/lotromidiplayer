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

public class Instrument {
	public static final Instrument LUTE = new Instrument("Lute", Note.C2, Note.C5);
	public static final Instrument HARP = new Instrument("Harp", Note.C2, Note.C5);
	public static final Instrument THEORBO = new Instrument("Theorbo", Note.C2, Note.C5);

	public static final Instrument FLUTE = new Instrument("Flute", Note.C2, Note.C5);
	public static final Instrument CLARINET = new Instrument("Clarinet", Note.D2, Note.C5);
	public static final Instrument HORN = new Instrument("Horn", Note.Cs2, Note.C5);
	public static final Instrument BAGPIPE = new Instrument("Bagpipe", Note.C2, Note.C5);

	public static final Instrument[] INSTRUMENTS = { LUTE, HARP, THEORBO, BAGPIPE, CLARINET, FLUTE,
			HORN };

	public final String name;
	public final Note lowestPlayable;
	public final Note highestPlayable;

	public static Instrument fromString(String string, Instrument defaultInstrument) {
		for (Instrument i : INSTRUMENTS) {
			if (i.name.equalsIgnoreCase(string)) {
				return i;
			}
		}
		return defaultInstrument;
	}

	private Instrument(String name, Note low, Note high) {
		this.name = name;
		this.lowestPlayable = low;
		this.highestPlayable = high;
	}

	@Override
	public String toString() {
		return name;
	}
}
