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

package com.digero.lotromusic;

import java.io.File;

import javax.jnlp.ServiceManager;
import javax.jnlp.SingleInstanceListener;
import javax.jnlp.SingleInstanceService;
import javax.jnlp.UnavailableServiceException;
import javax.swing.JFrame;
import javax.swing.UIManager;

import com.digero.lotromusic.ui.MainWindow;

public class LotroMusicMain {
	private static SingleInstanceService sis = null;
	private static SingleInstanceListener sil = new SISListener();
	private static MainWindow mainWindow = null;

	public static void main(String[] args) {
		try {
			sis = (SingleInstanceService) ServiceManager.lookup("javax.jnlp.SingleInstanceService");
			sis.addSingleInstanceListener(sil);
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					if (sis != null) {
						sis.removeSingleInstanceListener(sil);
					}
				}
			});
		}
		catch (UnavailableServiceException e) {
			sis = null;
		}

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {
		}

		File fileToOpen = null;
		if (args.length > 0) {
			fileToOpen = new File(args[0]);
		}

		mainWindow = new MainWindow(fileToOpen);
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mainWindow.setVisible(true);
	}

	private static class SISListener implements SingleInstanceListener {
		public void newActivation(String[] args) {
			if (args.length > 0) {
				File fileToOpen = new File(args[0]);
				mainWindow.openSong(fileToOpen);
			}
			mainWindow.toFront();
		}
	}
}
