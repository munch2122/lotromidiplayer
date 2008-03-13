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

package com.digero.lotromusic.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;

import com.digero.lotromusic.ui.events.SongPositionEvent;
import com.digero.lotromusic.ui.events.SongPositionListener;

@SuppressWarnings("serial")
public class SongPositionBar extends JPanel {
	private static final int PTR_WIDTH = 12; // 6;
	private static final int PTR_HEIGHT = 12; // 16;
	private static final int BAR_HEIGHT = 8;
	private static final int SIDE_PAD = PTR_WIDTH / 2;
	private static final int ROUND = 8;

	private long songPosition;
	private long ptrPosition;
	private long songLength;
	private boolean mouseHovering = false;
	private boolean mouseDragging = false;

	private Rectangle ptrRect = new Rectangle(0, 0, PTR_WIDTH, PTR_HEIGHT);

	public SongPositionBar() {
		MouseHandler mouseHandler = new MouseHandler();
		addMouseListener(mouseHandler);
		addMouseMotionListener(mouseHandler);

		setSongLength(0);
		setEnabled(false);

		Dimension sz = new Dimension(100, PTR_HEIGHT);
		setMinimumSize(sz);
		setPreferredSize(sz);
		updatePointerRect();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		MainWindow.printThreadInfo();

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int ptrPos;
		if (songLength == 0) {
			ptrPos = 0;
		}
		else {
			ptrPos = (int) (SIDE_PAD + (getWidth() - 2 * SIDE_PAD) * ptrPosition / songLength);
		}

		final int x = SIDE_PAD;
		final int y = (PTR_HEIGHT - BAR_HEIGHT) / 2;
		int right = getWidth() - SIDE_PAD;

		g2.setClip(new RoundRectangle2D.Float(x, y, right - x, BAR_HEIGHT, ROUND, ROUND));
		// g2.setColor(Color.GRAY);
		g2.setPaint(new GradientPaint(0, y, Color.DARK_GRAY, 0, y + BAR_HEIGHT, Color.GRAY));
		g2.fillRect(x, y, ptrPos - x, BAR_HEIGHT);

		// g2.setColor(Color.WHITE);
		g2.setPaint(new GradientPaint(0, y, Color.LIGHT_GRAY, 0, y + BAR_HEIGHT, Color.WHITE));
		g2.fillRect(ptrPos, y, right - ptrPos, BAR_HEIGHT);
		g2.setClip(null);

		g2.setColor(Color.BLACK);
		g2.drawRoundRect(x, y, right - x - 1, BAR_HEIGHT, ROUND, ROUND);
		
		if (mouseHovering && this.isEnabled()) {
			// g2.setColor(Color.LIGHT_GRAY);
			int left = ptrPos - PTR_WIDTH / 2;

			// final Color PTR_COLOR_1 = new Color(0x88FFFFFF & Color.WHITE.getRGB(), true);
			// final Color PTR_COLOR_2 = new Color(0x88FFFFFF & Color.LIGHT_GRAY.getRGB(), true);
			// final Color PTR_COLOR_3 = new Color(0x88FFFFFF & Color.GRAY.getRGB(), true);
			final Color PTR_COLOR_1 = Color.WHITE;
			final Color PTR_COLOR_2 = Color.LIGHT_GRAY;
			final Color PTR_COLOR_3 = Color.GRAY;

			if (mouseHovering) {
				g2.setPaint(new GradientPaint(left, 0, PTR_COLOR_1, left + PTR_WIDTH, 0,
						PTR_COLOR_2));
			}
			else {
				g2.setPaint(new GradientPaint(left, 0, PTR_COLOR_2, left + PTR_WIDTH, 0,
						PTR_COLOR_3));
			}
			// g2.fillRoundRect(left, PAD, PTR_WIDTH, PTR_HEIGHT, ROUND, ROUND);
			g2.fillOval(left, 0, PTR_WIDTH, PTR_HEIGHT);
			g2.setColor(Color.BLACK);
			// g2.drawRoundRect(left, PAD, PTR_WIDTH - 1, PTR_HEIGHT, ROUND, ROUND);
			g2.drawOval(left, 0, PTR_WIDTH - 1, PTR_HEIGHT - 1);
		}
	}

	private void updatePointerRect() {
		if (songLength == 0) {
			ptrRect.x = 0;
		}
		else {
			ptrRect.x = (int) (getWidth() * ptrPosition / songLength - PTR_WIDTH / 2);
		}
	}

	public void setSongParameters(long songPosition, long songLength) {
		setSongPosition(songPosition);
		setSongLength(songLength);
	}

	public void setSongPosition(long songPosition) {
		if (songPosition < 0) {
			songPosition = 0;
		}
		else if (songPosition > songLength) {
			songPosition = songLength;
		}

		if (this.songPosition != songPosition) {
			this.songPosition = songPosition;
			if (!mouseDragging) {
				setPtrPosition(songPosition);
			}
			repaint();
		}
	}

	public long getSongPosition() {
		return this.songPosition;
	}

	public void setSongLength(long songLength) {
		if (this.songLength != songLength) {
			this.songLength = songLength;
			setEnabled(songLength != 0);
			repaint();
		}
	}

	public long getSongLength() {
		return this.songLength;
	}

	public long getPointerPosition() {
		return ptrPosition;
	}

	public boolean isMouseDragging() {
		return mouseDragging;
	}

	private void setPtrPosition(long ptrPosition) {
		if (ptrPosition < 0) {
			ptrPosition = 0;
		}
		else if (ptrPosition > songLength) {
			ptrPosition = songLength;
		}
		if (this.ptrPosition != ptrPosition) {
			this.ptrPosition = ptrPosition;
			fireSongPositionDrag((int) ptrPosition);
			updatePointerRect();
			repaint();
		}
	}

	public void addSongPositionListener(SongPositionListener l) {
		listenerList.add(SongPositionListener.class, l);
	}

	public void removeSongPositionListener(SongPositionListener l) {
		listenerList.remove(SongPositionListener.class, l);
	}

	protected void fireSongPositionChanged(int newPosition) {
		Object[] listeners = listenerList.getListenerList();

		SongPositionEvent evt = null;
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == SongPositionListener.class) {
				if (evt == null)
					evt = new SongPositionEvent(this, newPosition);
				((SongPositionListener) listeners[i + 1]).songPositionChanged(evt);
			}
		}
	}

	protected void fireSongPositionDrag(int newPosition) {
		Object[] listeners = listenerList.getListenerList();

		SongPositionEvent evt = null;
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == SongPositionListener.class) {
				if (evt == null)
					evt = new SongPositionEvent(this, newPosition);
				((SongPositionListener) listeners[i + 1]).songPositionDrag(evt);
			}
		}
	}

	private class MouseHandler implements MouseListener, MouseMotionListener {
		private static final int MAX_MOUSE_DIST = 100;

		private int getPosition(int x) {
			int pos = (int) ((x + 1 - SIDE_PAD) * songLength / (getWidth() - 2 * SIDE_PAD));
			if (pos < 0) {
				pos = 0;
			}
			if (pos > songLength - 1) {
				pos = (int) songLength - 1;
			}
			return pos;
		}

		private void setMouseHovering(MouseEvent e) {
			Point pt = e.getPoint();

			boolean inside = pt.x >= 0 && pt.x < getWidth() && pt.y >= 0 && pt.y < getHeight();
			boolean newMouseHovering = SongPositionBar.this.isEnabled()
					&& (mouseDragging || inside);

			if (newMouseHovering != mouseHovering) {
				mouseHovering = newMouseHovering;
				repaint();
			}
		}

		public void mouseClicked(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
			if (!SongPositionBar.this.isEnabled())
				return;
			setPtrPosition(getPosition(e.getX()));
			setMouseHovering(e);
			requestFocus();
		}

		public void mouseReleased(MouseEvent e) {
			mouseDragging = false;
			if (e.getY() > -MAX_MOUSE_DIST && e.getY() < getHeight() + MAX_MOUSE_DIST) {
				int newPosition = getPosition(e.getX());
				setSongPosition(newPosition);
				fireSongPositionChanged(newPosition);
			}
			setMouseHovering(e);
		}

		public void mouseDragged(MouseEvent e) {
			if (!SongPositionBar.this.isEnabled())
				return;
			if (e.getY() > -MAX_MOUSE_DIST && e.getY() < getHeight() + MAX_MOUSE_DIST)  {
				mouseDragging = true;
				setPtrPosition(getPosition(e.getX()));
			}
			else {
				mouseDragging = false;
				setPtrPosition(songPosition);
			}
			setMouseHovering(e);
		}

		public void mouseMoved(MouseEvent e) {
			if (!SongPositionBar.this.isEnabled())
				return;
			setMouseHovering(e);
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
			if (mouseHovering) {
				mouseHovering = false;
				repaint();
			}
		}
	}
}
