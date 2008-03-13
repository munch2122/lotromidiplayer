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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.digero.lotromusic.windows.WinApi;

public class MusicKeymap {
	private Map<Note, KeyInfo> map;
	private Map<Integer, KeyInfo> intMap;
	private boolean loaded = false;
	private String lastReadError = "";

	public MusicKeymap() {
		load();
	}

	public boolean load() {
		loaded = false;

		if (map == null) {
			map = new HashMap<Note, KeyInfo>();
			intMap = new HashMap<Integer, KeyInfo>();
		}
		else {
			map.clear();
			intMap.clear();
		}

		File file = new File(WinApi.GetMyDocumentsPath()
				+ "\\The Lord of the Rings Online\\lotro.keymap");

		if (file.exists()) {
			Pattern parser = Pattern.compile("MUSIC_([A-G]b?[1-5]) *\\[[ A-Za-z_]*"
					+ "\\[ *0 *(DIK_[A-Za-z0-9_]+) *\\] *(?:0x([0-9A-Fa-f]+))? *\\]");

			String text = "";
			try {
				BufferedReader rdr = new BufferedReader(new FileReader(file));

				String line;
				while ((line = rdr.readLine()) != null) {
					text += line + "\n";
				}

				rdr.close();
			}
			catch (IOException e) {
				e.printStackTrace();
				lastReadError = "Error reading " + file.getAbsolutePath() + "\n" + e.getMessage();
				return false;
			}

			Matcher m = parser.matcher(text);
			while (m.find()) {
				Note note = Enum.valueOf(Note.class, m.group(1));
				DxScanCode scanCode = Enum.valueOf(DxScanCode.class, m.group(2));
				int modifiers = 0;
				if (m.group(3) != null)
					modifiers = Integer.parseInt(m.group(3), 16);

				KeyInfo keyInfo = new KeyInfo(scanCode, modifiers);
				map.put(note, keyInfo);
				intMap.put(note.id, keyInfo);
			}
			lastReadError = "";
			loaded = true;
			return true;
		}
		lastReadError = "File not found:\n" + file.getAbsolutePath();
		return false;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public String getLastReadError() {
		return lastReadError;
	}

	public KeyInfo getKey(Note note) {
		if (map == null)
			load();

		if (map.containsKey(note)) {
			return map.get(note);
		}
		return null;
	}

	public KeyInfo getKey(int note) {
		if (map == null)
			load();

		if (intMap.containsKey(note)) {
			return intMap.get(note);
		}
		return null;
	}

	public void printAllKeys() {
		if (map == null)
			load();

		for (Entry<Note, KeyInfo> entry : map.entrySet()) {
			System.out.println(entry.getKey() + ": \t" + entry.getValue());
		}
	}
}
