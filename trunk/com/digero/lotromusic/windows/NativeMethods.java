package com.digero.lotromusic.windows;

import com.digero.lotromusic.keyboard.KeyInfo;

public interface NativeMethods {
	public String GetMyDocumentsPath();
	
	public void SendFocusMessage(int hWnd, boolean focus) throws NativeApiUnavailableException;
	public void KeyDown(int hWnd, KeyInfo info, boolean setFocus) throws NativeApiUnavailableException;
	public void KeyUp(int hWnd, KeyInfo info, boolean setFocus) throws NativeApiUnavailableException;
	public int FindWindow(String className, String windowName) throws NativeApiUnavailableException;
	public void SendKey(int hWnd, KeyInfo info, boolean sendUnfocus) throws NativeApiUnavailableException;
	public void SendKeyString(int hWnd, String message, boolean sendUnfocus) throws NativeApiUnavailableException;
}
