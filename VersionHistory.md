### Version 1.1.0.5 ###
  * Exporting drum tracks to ABC works once again.

### Version 1.1.0.4 ###
  * Improved support for loading "Type 0" MIDI files, which have all instruments in the same track. These types of files will now load with each instrument in a separate track.
  * Created a standalone download version, which will hopefully work people who've been having trouble running the web launch version. This version doesn't support live playing in LOTRO (only exporting to ABC).

### Version 1.1.0.3 ###
  * If you have "Always Play Game Sound in Background" enabled, the game sound will come back much faster after you alt-tab now. Previously it could take up to 1 second for the game sound to come back.  Now, it'll happen within 1/10th of a second.
  * The "MIDI Preview Mode" option saves its state when you restart LotRO MIDI Player.  This option also now defaults to ON the first time you run the program.
  * Updated links in the Upgrade and About dialogs to point to the Google Code website.

### Version 1.1.0.2 ###
  * Fixed a bug that caused chords with 6 notes in them to be the incorrect length, which could mess up the timing of the exported ABC file.
  * Fixed a bug where very short notes could mess up the timing of the exported ABC file.

### Version 1.1.0.1 ###
  * Export to ABC respects the currently selected instrument's lowest-playable note.
  * Export to ABC now shouldn't allow chords with more than 6 notes.
  * Fixed the occasional "unexpected token" error in some exported ABC files.
  * The name of the exported ABC file saves on a per-song basis.
  * Added an "Always play game sound in background" option. With this enabled, LotRO will always think it's the active window, even if another window has focus. Note: this option will cause your computer to feel sluggish when LotRO is running in the background.
  * The program now attempts to use the key mapping without Ctrl/Alt modifiers if there is an option.
  * Fixed a bug when playing simultaneous notes with Ctrl/Alt modifiers that would cause the wrong note to be played.
  * Added accelerator keys to Open a MIDI file (Ctrl+O) and Export to ABC (Ctrl+S).

### Version 1.1.0.0 ###
  * Added the ability to export to ABC files.
  * Added the ability to connect an external MIDI piano.
  * Added an instrument drop-down.

### Version 1.0.0.0 ###
  * Initial release.


---

Created by Digero of Landroval

[![](https://www.paypal.com/en_US/i/btn/x-click-but04.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=10589444)