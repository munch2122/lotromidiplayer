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

public class KeyInfo {
	public static final int DX_NONE = 0x0;
	public static final int DX_SHIFT = 0x1;
	public static final int DX_CTRL = 0x2;
	public static final int DX_ALT = 0x4;

	private DxScanCode scanCode;
	private int modifiers;

	public KeyInfo(DxScanCode scanCode, int modifiers) {
		this.scanCode = scanCode;
		this.modifiers = modifiers;
	}

	public DxScanCode getScanCode() {
		return scanCode;
	}

	public int getModifiers() {
		return modifiers;
	}

	@Override
	public String toString() {
		String s = "";
		if ((modifiers & DX_CTRL) != 0)
			s += "Ctrl+";
		if ((modifiers & DX_ALT) != 0)
			s += "Alt+";
		if ((modifiers & DX_SHIFT) != 0)
			s += "Shift+";

		return s + scanCode.toString().replace("DIK_", "");
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}

		KeyInfo that = (KeyInfo) obj;
		return (this.scanCode == that.scanCode) && (this.modifiers == that.modifiers);
	}

	@Override
	public int hashCode() {
		return scanCode.value ^ (modifiers << 16);
	}
}