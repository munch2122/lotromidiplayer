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

#include "WinApi.h"
#include <Windows.h>
#include <shlobj.h>

JNIEXPORT jint JNICALL Java_com_digero_lotromusic_windows_WinApi_FindWindow
  (JNIEnv *env, jclass cls, jstring className, jstring windowName)
{
	jint ret;
	const jbyte *_className = (className == NULL) ? NULL : (*env)->GetStringUTFChars(env, className, NULL);
	const jbyte *_windowName = (windowName == NULL) ? NULL : (*env)->GetStringUTFChars(env, windowName, NULL);

	ret = (jint)FindWindowA(_className, _windowName);

	(*env)->ReleaseStringUTFChars(env, className, _className);
	(*env)->ReleaseStringUTFChars(env, windowName, _windowName);

	return ret;
}

JNIEXPORT jint JNICALL Java_com_digero_lotromusic_windows_WinApi_GetForegroundWindow
  (JNIEnv *env, jclass cls)
{
	return (jint)GetForegroundWindow();
}

JNIEXPORT jint JNICALL Java_com_digero_lotromusic_windows_WinApi_SendMessage
  (JNIEnv *env, jclass cls, jint hWnd, jint msg, jint wParam, jint lParam)
{
	return (jint)SendMessageA((HWND)hWnd, msg, wParam, lParam);
}

JNIEXPORT jboolean JNICALL Java_com_digero_lotromusic_windows_WinApi_PostMessage
  (JNIEnv *env, jclass cls, jint hWnd, jint msg, jint wParam, jint lParam)
{
	return (jint)PostMessageA((HWND)hWnd, msg, wParam, lParam);
}

JNIEXPORT jint JNICALL Java_com_digero_lotromusic_windows_WinApi_MapVirtualKey
  (JNIEnv *env, jclass cls, jint code, jint mapType)
{
	return (jint)MapVirtualKeyA(code, mapType);
}

JNIEXPORT jstring JNICALL Java_com_digero_lotromusic_windows_WinApi_GetMyDocumentsPath
  (JNIEnv *env, jclass cls)
{
	CHAR pszPath[MAX_PATH];
	SHGetFolderPathA(NULL, CSIDL_PERSONAL, NULL, 0, pszPath);

	return (*env)->NewStringUTF(env, pszPath);
}