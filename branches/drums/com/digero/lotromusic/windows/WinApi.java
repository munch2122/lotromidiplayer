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

package com.digero.lotromusic.windows;

import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;

import com.digero.lotromusic.keyboard.DxScanCode;
import com.digero.lotromusic.keyboard.KeyInfo;

public class WinApi {
	static {
		System.out.println("Loading WinApi");
		
		try {
			System.loadLibrary("JavaWinApi");
		}
		catch (Throwable ex) {
			JOptionPane.showMessageDialog(null,
					"Failed to load required DLL file: JavaWinApi.dll\n\n" + ex.getMessage(),
					"Failed to load required DLL", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		// System.load(WinApi.class.getResource("JavaWinApi.dll").getPath());
		// System.load(new File("JavaWinApi.dll").getAbsolutePath());
	}

	public static final int WM_KEYDOWN = 0x0100;
	public static final int WM_KEYUP = 0x0101;
	public static final int WM_CHAR = 0x0102;

	public static final int WM_ACTIVATE = 0x0006;
	public static final int WM_ACTIVATEAPP = 0x001C;
	public static final int WA_INACTIVE = 0;
	public static final int WA_ACTIVE = 1;
	public static final int WA_CLICKACTIVE = 2;

	public static final int WM_SETFOCUS = 0x0007;
	public static final int WM_KILLFOCUS = 0x0008;

	public static final int MAPVK_VK_TO_VSC = 0;
	public static final int MAPVK_VSC_TO_VK = 1;
	public static final int MAPVK_VK_TO_CHAR = 2;

	public static native int FindWindow(String className, String windowName);

	public static native int GetForegroundWindow();

	public static native int SendMessage(int hWnd, int msg, int wParam, int lParam);

	public static native boolean PostMessage(int hWnd, int msg, int wParam, int lParam);

	public static native int MapVirtualKey(int code, int mapType);

	public static native String GetMyDocumentsPath();

	private static final Object keyPressLock = new Object();

	public static void SendFocusMessage(int hWnd, boolean focus) {
		int hFgWnd = GetForegroundWindow();
		if (hFgWnd != hWnd) {
			if (focus) {
				PostMessage(hWnd, WM_ACTIVATE, WA_ACTIVE, 0);
				PostMessage(hWnd, WM_SETFOCUS, 0, 0);
			}
			else {
				PostMessage(hWnd, WM_KILLFOCUS, 0, 0);
				PostMessage(hWnd, WM_ACTIVATE, WA_INACTIVE, 0);
			}
		}
	}

	public static void KeyDown(int hWnd, KeyInfo info, boolean setFocus) {
		synchronized (keyPressLock) {
			if (setFocus) {
				SendFocusMessage(hWnd, true);
			}

			if ((info.getModifiers() & KeyInfo.DX_CTRL) != 0) {
				int lParam = (DxScanCode.DIK_LCONTROL.value << 16) | 1;
				PostMessage(hWnd, WM_KEYDOWN, KeyEvent.VK_CONTROL, lParam);
			}
			if ((info.getModifiers() & KeyInfo.DX_ALT) != 0) {
				int lParam = (DxScanCode.DIK_LALT.value << 16) | 1;
				PostMessage(hWnd, WM_KEYDOWN, KeyEvent.VK_ALT, lParam);
			}
			if ((info.getModifiers() & KeyInfo.DX_SHIFT) != 0) {
				int lParam = (DxScanCode.DIK_LSHIFT.value << 16) | 1;
				PostMessage(hWnd, WM_KEYDOWN, KeyEvent.VK_SHIFT, lParam);
			}

			{
				int vk = MapVirtualKey(info.getScanCode().value, MAPVK_VSC_TO_VK);
				int lParam = (info.getScanCode().value << 16) | 1;
				PostMessage(hWnd, WM_KEYDOWN, vk, lParam);
			}

			// Release modifier keys right away
			if ((info.getModifiers() & KeyInfo.DX_CTRL) != 0) {
				int lParam = (DxScanCode.DIK_LCONTROL.value << 16) | 0xC0000001;
				PostMessage(hWnd, WM_KEYUP, KeyEvent.VK_CONTROL, lParam);
			}
			if ((info.getModifiers() & KeyInfo.DX_ALT) != 0) {
				int lParam = (DxScanCode.DIK_LALT.value << 16) | 0xC0000001;
				PostMessage(hWnd, WM_KEYUP, KeyEvent.VK_ALT, lParam);
			}
			if ((info.getModifiers() & KeyInfo.DX_SHIFT) != 0) {
				int lParam = (DxScanCode.DIK_LSHIFT.value << 16) | 0xC0000001;
				PostMessage(hWnd, WM_KEYUP, KeyEvent.VK_SHIFT, lParam);
			}

			if (setFocus) {
				SendFocusMessage(hWnd, false);
			}
		}
	}

	public static void KeyUp(int hWnd, KeyInfo info, boolean setFocus) {
		synchronized (keyPressLock) {
			if (setFocus) {
				SendFocusMessage(hWnd, true);
			}

			{
				int vk = MapVirtualKey(info.getScanCode().value, MAPVK_VSC_TO_VK);
				int lParam = (info.getScanCode().value << 16) | 0xC0000001;
				PostMessage(hWnd, WM_KEYUP, vk, lParam);
			}

//			if ((info.getModifiers() & KeyInfo.DX_CTRL) != 0) {
//				int lParam = (DxScanCode.DIK_LCONTROL.value << 16) | 0xC0000001;
//				PostMessage(hWnd, WM_KEYUP, KeyEvent.VK_CONTROL, lParam);
//			}
//			if ((info.getModifiers() & KeyInfo.DX_ALT) != 0) {
//				int lParam = (DxScanCode.DIK_LALT.value << 16) | 0xC0000001;
//				PostMessage(hWnd, WM_KEYUP, KeyEvent.VK_ALT, lParam);
//			}
//			if ((info.getModifiers() & KeyInfo.DX_SHIFT) != 0) {
//				int lParam = (DxScanCode.DIK_LSHIFT.value << 16) | 0xC0000001;
//				PostMessage(hWnd, WM_KEYUP, KeyEvent.VK_SHIFT, lParam);
//			}

			if (setFocus) {
				SendFocusMessage(hWnd, false);
			}
		}
	}

	public static void SendKey(int hWnd, KeyInfo info, boolean sendUnfocus) {
		synchronized (keyPressLock) {
			SendFocusMessage(hWnd, true);
			KeyDown(hWnd, info, false);
			KeyUp(hWnd, info, false);
			if (sendUnfocus) {
				SendFocusMessage(hWnd, false);
			}
		}
	}

	public static void SendKeyString(int hWnd, String message, boolean sendUnfocus) {
		synchronized (keyPressLock) {
			SendFocusMessage(hWnd, true);

			for (int i = 0; i < message.length(); i++) {
				char c = message.charAt(i);

				int vk;
				switch (c) {
					case '~':
						vk = '\r';
						break;
					case '/':
						vk = 191;
						break;
					default:
						vk = (int) Character.toUpperCase(c);
						break;
				}

				int sc = MapVirtualKey(vk, MAPVK_VK_TO_VSC);
				int lParam = (sc << 16) | 1;
				if (c >= ' ' && c < '~') {
					PostMessage(hWnd, WM_CHAR, (int) c, lParam);
				}
				else {
					PostMessage(hWnd, WM_KEYDOWN, vk, lParam);
					PostMessage(hWnd, WM_KEYUP, vk, 0xC0000000 | lParam);
				}
			}

			if (sendUnfocus) {
				SendFocusMessage(hWnd, false);
			}
		}
	}
}
