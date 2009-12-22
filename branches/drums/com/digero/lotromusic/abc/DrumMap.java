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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.digero.lotromusic.keyboard.Instrument;
import com.digero.lotromusic.keyboard.Note;

public class DrumMap {
	public static void main(String[] args) throws Exception {
		DrumMap map = new DrumMap("drums.chg");
		map.get(55);
	}

	private Map<Integer, Note> map = null;
	private Note defaultNote = null;
	private final File drumMapFile;

	private static final String PATTERN_ABC = "[=_^]?(?:[A-G],*|[a-g]'*)"; // e.g. ^D,
	private static final String PATTERN_NAME = "[A-G][sb]?\\d"; //            e.g. Ds2
	private static final String PATTERN_ID = "\\d+"; //                       e.g. 39
	private static final String PATTERN_NOTE = "(" + PATTERN_ABC + ")|(" + PATTERN_NAME + ")|(" + PATTERN_ID + ")";
	private static final Pattern PATTERN_LINE = Pattern.compile("(?:\\s*(" + PATTERN_NOTE + "|(\\*))\\s*=>\\s*("
			+ PATTERN_NOTE + "))?\\s*(?:;.*)?");
	private static final int GROUP_FROM = 1, GROUP_TO = 6;
	private static final int SUBGROUP_ABC = 1, SUBGROUP_NAME = 2, SUBGROUP_ID = 3, SUBGROUP_DEFAULT = 4;

	public DrumMap(String drumMapFileName) {
		this(new File(drumMapFileName));
	}

	public DrumMap(File drumMapFile) {
		this.drumMapFile = drumMapFile;
	}

	private void load() throws ParseException, IOException {
		map = new HashMap<Integer, Note>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(drumMapFile));

			int lineNumber = 1;
			String lineTest;
			while ((lineTest = reader.readLine()) != null) {
				Matcher m = PATTERN_LINE.matcher(lineTest);

				if (!m.matches()) {
					throw new ParseException("Invalid entry format.\n"
							+ "Expected format: \"[Note] => [Note]\" or \"* => [Note]\"", lineNumber);
				}

				if (m.group(GROUP_FROM) != null && m.group(GROUP_TO) != null) {
					Note from, to;

					to = getNoteFromMatch(m, GROUP_TO, lineNumber);
					if (!Instrument.DRUMS.isPlayable(to)) {
						Note min = Instrument.DRUMS.lowestPlayable;
						Note max = Instrument.DRUMS.highestPlayable;
						throw new ParseException("Note out of range. Valid values are in the range " + min.id + " ("
								+ min.abc + ") to " + max.id + " (" + max.abc + ").", lineNumber, m.start(GROUP_TO));
					}

					if (m.group(GROUP_FROM + SUBGROUP_DEFAULT) != null) {
						defaultNote = to;
						continue;
					}

					from = getNoteFromMatch(m, GROUP_FROM, lineNumber);

					if (map.containsKey(from.id) && map.get(from.id).id != to.id) {
						throw new ParseException("Note " + m.group(GROUP_FROM) + " appears multiple times.", lineNumber,
								m.start(GROUP_FROM));
					}

					map.put(from.id, to);
				}

				lineNumber++;
			}
		}
		finally {
			if (reader != null)
				reader.close();
		}
	}

	private Note getNoteFromMatch(MatchResult m, int groupIndex, int parseLine) throws ParseException {
		String tmp;
		Note note = null;

		if ((tmp = m.group(groupIndex + SUBGROUP_ABC)) != null)
			note = Note.fromAbc(tmp);
		else if ((tmp = m.group(groupIndex + SUBGROUP_NAME)) != null)
			note = Note.fromName(tmp);
		else if ((tmp = m.group(groupIndex + SUBGROUP_ID)) != null)
			note = Note.fromId(Integer.parseInt(tmp));

		if (note == null)
			throw new ParseException("Invalid note. Notes can be either MIDI note IDs or ABC notes.", parseLine, m
					.start(groupIndex));

		return note;
	}

	public Note get(int midiIndex) throws ParseException, IOException {
		if (map == null)
			load();

		Note note = map.get(midiIndex);
		if (note != null)
			return note;
		if (defaultNote != null)
			return defaultNote;

		throw new ParseException();
	}
	
	

	@SuppressWarnings("serial")
	public class ParseException extends Exception {
		private String error;
		private int line, column;

		public ParseException(String error) {
			super("Error reading " + getFilePath(drumMapFile) + ": " + error);
			this.error = error;
			this.line = 0;
			this.column = 0;
		}

		public ParseException(String error, int line) {
			super("Error reading " + getFilePath(drumMapFile) + " on line " + line + ": " + error);
			this.error = error;
			this.line = line;
			this.column = 0;
		}

		public ParseException(String error, int line, int column) {
			super("Error reading " + getFilePath(drumMapFile) + " on line " + line + ", column " + column + ": "
					+ error);
			this.error = error;
			this.line = line;
			this.column = column;
		}

		public File getFile() {
			return drumMapFile;
		}

		public int getLine() {
			return line;
		}

		public int getColumn() {
			return column;
		}

		public String getError() {
			return error;
		}
	}

	private static String getFilePath(File file) {
		try {
			return file.getCanonicalPath();
		}
		catch (IOException ioe) {
			return file.getAbsolutePath();
		}
	}
}
