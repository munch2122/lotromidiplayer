package com.digero.lotromusic.windows;

public class NativeApi {
	private static NativeMethods instance = null;

	public static final String ERROR_MESSAGE = "This feature is not supported in the standalone \n"
			+ "version of LOTRO MIDI Player";
	public static final String ERROR_TITLE = "Feature not supported";

	private static final boolean DEBUG_DISABLE_WINAPI = false;

	public static NativeMethods getInstance() {
		if (instance == null) {
			try {
				if (DEBUG_DISABLE_WINAPI) {
					throw new Exception();
				}
				else {
					System.loadLibrary("JavaWinApi");
					instance = new JavaWinApi.Proxy();
				}
			}
			catch (Throwable ex) {
				instance = new NonWindowsStubs();
			}
		}

		return instance;
	}

	public static boolean isWindowsApi() {
		return getInstance() instanceof JavaWinApi.Proxy;
	}

	private NativeApi() {
	}
}
