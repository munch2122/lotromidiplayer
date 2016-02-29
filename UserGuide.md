# User Guide #
After you've run LotRO MIDI Player once from this website, you can use the shortcut on your desktop to run it again without coming back here.  If you don't have a desktop shortcut [read these instructions](InstallShortcuts.md) to restore it.

### Configure LotRO ###
If you want to use the live MIDI playing feature (not exporting to ABC), you must have a keyboard key mapped to each of the notes in the game. It tends to work best if none of the keys are mapped with Shift, Ctrl, or Alt modifiers. LotRO MIDI player will automatically determine your key mappings; however, if you change your keyboard mapping while the program is running, you must select "Reload Key Mappings" from the Edit menu.

You MUST be in music mode when you press play or bad things will happen! Do not try to type using the in-game chat box while playing music.

### Opening Songs ###
The easiest way to open a MIDI file is to DRAG and DROP the file onto the LotRO MIDI Player window. You can also open a MIDI file from the file menu.

### Tips ###
  * Try to find MIDI files that were arranged for the piano or guitar. These sound the best when played on the lute.
  * If the game gives you a "Too many requests" error, try slowing the tempo or unchecking some of the tracks.
  * Stringed instruments (Lute, Harp, Theorbo) sound the best. Wind instruments (Flute, Clarinet, Horn, Bagpipe) sound decent. Drums and Cowbell are NOT supported.

<img src='http://lotromidiplayer.googlecode.com/svn/trunk/Website/screenshot-white.png' align='right' />
<img src='http://lotromidiplayer.googlecode.com/svn/trunk/Website/spacer.png' align='right' width='10' height='441' />
### Track List ###
Uncheck tracks that you don't want to be played. The note count lists how many notes that particular instrument plays for the entire piece.

### MIDI preview mode ###
When this is enabled, the song will be played using the computer's MIDI synthesizer rather than in-game. Use this to help figure out which tracks should be turned on and off for complicated songs. Note: If you can't hear any music when Local Preview Mode is enabled, try turning up the "MIDI Synth" volume in the system volume control panel. Windows Media Player seems to set the MIDI Synth volume to 0 if you use it to play a MIDI file.

### Transpose ###
Set the number of half-steps to transpose the notes in the song. This will help move the song to fit within the LotRO instruments' three-octave of range. Any notes that still fall outside of the instrument's range will be automatically transposed up or down by a full octave so that they can still be played.

### Instrument ###
Select which instrument you'll be using in-game, or for which instrument the ABC export is intended. This will make sure that the "breath" sound isn't used for the horn and clarinet.

### Type text in the game ###
Since LotRO MIDI Player works by simulating key presses in the game, you cannot type in the chat bar in the game. To work around this, you can type using this text box. It will safely type the message into the game for you when you press Enter or click Send.

### Exporting to ABC ###
To export to an ABC file, choose the option from the File menu. If you want to make a multi-part ABC arrangement, simply export the song multiple times with different tracks turned on. You may want to change the title between exports to add information about the part (e.g. " - Lute").

### Connect MIDI Piano ###
Select "Connect MIDI Piano..." from the main window's File menu.
If your device isn't listed, make sure it's connected and turned on. If clicking refresh doesn't make the device show up, try restarting LotRO MIDI Player.

<img src='http://lotromidiplayer.googlecode.com/svn/trunk/Website/mididevice-white.png' />


---

Created by Digero of Landroval

[![](https://www.paypal.com/en_US/i/btn/x-click-but04.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=10589444)