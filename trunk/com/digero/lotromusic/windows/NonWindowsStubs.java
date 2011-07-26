package com.digero.lotromusic.windows;

import java.io.File;

import com.digero.lotromusic.keyboard.KeyInfo;

class NonWindowsStubs implements NativeMethods {
	public static File getUserDocumentsPath() {
		String userHome = System.getProperty("user.home", "");
		File docs = new File(userHome + "/Documents");
		if (docs.isDirectory())
			return docs;
		docs = new File(userHome + "/My Documents");
		if (docs.isDirectory())
			return docs;
		return new File(userHome);
	}

	@Override
	public String GetMyDocumentsPath() {
		return getUserDocumentsPath().getAbsolutePath();
	}

	@Override
	public int FindWindow(String className, String windowName) throws NativeApiUnavailableException {
		throw new NativeApiUnavailableException();
	}

	@Override
	public void KeyDown(int hWnd, KeyInfo info, boolean setFocus) throws NativeApiUnavailableException {
		throw new NativeApiUnavailableException();
	}

	@Override
	public void KeyUp(int hWnd, KeyInfo info, boolean setFocus) throws NativeApiUnavailableException {
		throw new NativeApiUnavailableException();
	}

	@Override
	public void SendFocusMessage(int hWnd, boolean focus) throws NativeApiUnavailableException {
		throw new NativeApiUnavailableException();
	}

	@Override
	public void SendKey(int hWnd, KeyInfo info, boolean sendUnfocus) throws NativeApiUnavailableException {
		throw new NativeApiUnavailableException();
	}

	@Override
	public void SendKeyString(int hWnd, String message, boolean sendUnfocus) throws NativeApiUnavailableException {
		throw new NativeApiUnavailableException();
	}
}
