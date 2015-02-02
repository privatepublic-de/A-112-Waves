package de.privatepublic.a112;

import java.util.ResourceBundle;

public class I18N {
	
	
	private static final ResourceBundle RES = ResourceBundle.getBundle("Strings");
	
	/**
	 * Localized string for given key. Please use the key constants in I18N, to avoid key not found errors.
	 * @param key
	 * @return localized string
	 */
	public static String s(String key) {
		return RES.getString(key);
	}
	
	/**
	 * Localized string for given key with variable substitution.
	 * Variables are referenced in the string with @0 ... @n. For substitution
	 * the toString() method of the variable objects is used.
	 * <p>
	 * Example:
	 * Localized string with key "filesize" is "File @0 is @1 bytes long",
	 * s("filesize", afile, afile.length()) substitutes @0 with file name and @1 with the file lenght.
	 * </p>
	 * @param key
	 * @param objects
	 * @return localized string with substituted variables
	 */
	public static String s(String key, Object... objects) {
		String s = RES.getString(key);
		for (int i=0;i<objects.length;++i) {
			s = s.replace("@"+i, objects[i].toString());
		}
		return s;
	}

	public static final String TT_OSCILLOSCOPE = "tt-oscilloscope";
	public static final String TAB_TITLE_ABOUT = "tab-title-about";
	public static final String TAB_TITLE_GENERATE = "tab-title-generate";
	public static final String TAB_TITLE_TRANSMIT = "tab-title-transmit";
	public static final String TAB_TITLE_WAVFILES = "tab-title-wavfiles";
	public static final String TT_FACTOR = "tt-factor";
	public static final String MSG_ERROR_FOLDER = "msg-error-folder";
	public static final String MSG_FILE_EXISTS = "msg-file-exists";
	public static final String MSG_FILE_EXISTS_TITLE = "msg-file-exists-title";
	public static final String MSG_FILE_WRITE_ERROR = "msg-file-write-error";
	public static final String MSG_SCRIPT_OK = "msg-script-ok";
	public static final String MSG_SCRIPT_OVERFLOW = "msg-script-overflow";
	public static final String MSG_TRANSMIT_STATUS = "msg-transmit-status";
	public static final String FILETYPE_SYX = "filetype-syx";
	public static final String FILETYPE_WAV = "filetype-wav";
	public static final String LBL_MIDI_INTERFACE = "lbl-midi-interface";
	public static final String LBL_SAVE_WAV_SYX = "lbl-save-wav-syx";
	public static final String LBL_SCRIPT = "lbl-script";
	public static final String LBL_SCRIPT_HINT = "lbl-script-hint";
	public static final String LBL_SELECT_FILE_OR_FOLDER = "lbl-select-file-or-folder";
	public static final String LBL_TRANSMIT = "lbl-transmit";
	public static final String LBL_WAVEFILES = "lbl-wavefiles";
	public static final String LOG_ERROR_READING = "log-error-reading";
	public static final String LOG_FOLDER_STATUS = "log-folder-status";
	public static final String LOG_FOLDER_STATUS_CUT = "log-folder-status-cut";
	public static final String LOG_INITIAL_TEXT = "log-initial-text";
	public static final String LOG_RESAMPLED = "log-resampled";
	public static final String DLG_CHOOSE_BUTTON = "dlg-choose-button";
	public static final String DLG_OPEN_BUTTON = "dlg-open-button";
	public static final String DLG_SAVE_BUTTON = "dlg-save-button";
	public static final String DLG_TITLE_SAVE_WAVETABLE = "dlg-title-save-wavetable";
	public static final String DLG_TITLE_SELECT_FOLDER = "dlg-title-select-folder";
	public static final String DLG_TITLE_SELECT_WAV = "dlg-title-select-wav";
	public static final String APP_TITLE = "app-title";
	public static final String BTN_CREATE_WAVETABLE = "btn-create-wavetable";
	public static final String BTN_MIDI_TRANSMIT = "btn-midi-transmit";
	public static final String BTN_SAVE_WAV_SYX = "btn-save-wav-syx";
	public static final String BTN_SELECT_INPUT_FOLDER = "btn-select-input-folder";
	public static final String BTN_SINGLE_WAVFILE = "btn-single-wavfile";
	public static final String CHB_INTERPOLATED = "chb-interpolated";
	public static final String LOG_FINISHED_GENERATING = "log-finished-generating";
	public static final String LOG_NOTHING_TO_DO = "log-nothing-to-do";
	public static final String MSG_GENERATE_FINISHED = "msg-generate-finished";
	public static final String MSG_HARMONIC_NO_ZERO = "msg-harmonic-no-zero";
	public static final String MSG_MISSING_ARGUMENTS = "msg-missing-arguments";
	public static final String MSG_NUMBER_FORMAT_ERROR = "msg-number-format-error";
	public static final String MSG_RANGE_FORMAT_ERROR = "msg-range-format-error";
	public static final String MSG_RANGE_NO_ZEROS = "msg-range-no-zeros";
	public static final String MSG_RANGE_NUMBER_FORMAT_ERROR = "msg-range-number-format-error";
	public static final String MSG_SCRIPT_ERROR_LINE = "msg-script-error-line";
	public static final String MSG_UNKNOWN_WAVE_FUNCTION = "msg-unknown-wave-function";
	

}
