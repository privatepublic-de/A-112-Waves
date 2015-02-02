package de.privatepublic.a112;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class WaveRenderJob {

	private static final Logger logger = LogManager.getLogger();

	public static final int WAVES_COUNT = 256;
	public static final int WAVE_SAMPLE_COUNT = 256;
	public static final int WAVE_TABLE_LENGTH = WAVES_COUNT*WAVE_SAMPLE_COUNT;
	
	public static double[] WAVE_DATA;

	public static final String SYNTAX_WAVE_SINE = "sin";
	public static final String SYNTAX_WAVE_TRIANGLE = "tri";
	public static final String SYNTAX_WAVE_SAW = "saw";
	public static final String SYNTAX_WAVE_SQUARE = "squ";
	public static final String SYNTAX_DELIMITER = ",";
	public static final String SYNTAX_RANGE = "-";
	public static final String SYNTAX_COMMENT = "#";

	public static List<WaveRenderJob> parseScript(String script) throws ParseException {
		List<WaveRenderJob> result = new ArrayList<WaveRenderJob>();
		try {
			List<String> lines = org.apache.commons.io.IOUtils.readLines(new StringReader(script));
			for (int l=0;l<lines.size();++l) {
				if ("".equals(lines.get(l).trim()) || lines.get(l).trim().startsWith(SYNTAX_COMMENT)) { // TODO optimize
					continue;
				}
				try {
					WaveRenderJob job = new WaveRenderJob(lines.get(l));
					result.add(job);
				} catch (Exception e) {
					throw new ParseException(I18N.s(I18N.MSG_SCRIPT_ERROR_LINE, e.getMessage(), l+1));
				}
			}
		} catch (IOException e) {
			logger.error(e);
		}

		return result;
	}


	private WaveProcessor.HarmonicAmplitudeFunction function;
	private ArrayList<Integer> harmonics = new ArrayList<Integer>();

	private WaveRenderJob(String line) throws ParseException {
		line = line.trim().toLowerCase();
		String parts[] = line.split("\\s", 2);
		if (parts.length<2) {
			throw new ParseException(I18N.s(I18N.MSG_MISSING_ARGUMENTS));
		}
		String wave = parts[0];
		if (wave.startsWith(SYNTAX_WAVE_SINE)) {
			function = WaveProcessor.HarmonicsSine;
		}
		else if (wave.startsWith(SYNTAX_WAVE_TRIANGLE)) {
			function = WaveProcessor.HarmonicsTriangle;
		}
		else if (wave.startsWith(SYNTAX_WAVE_SAW)) {
			function = WaveProcessor.HarmonicsSaw;
		}
		else if (wave.startsWith(SYNTAX_WAVE_SQUARE)) {
			function = WaveProcessor.HarmonicsSquare;
		}
		else {
			throw new ParseException(I18N.s(I18N.MSG_UNKNOWN_WAVE_FUNCTION, wave));
		}
		parseHarmonics(parts[1]);
	}

	private void parseHarmonics(String s) throws ParseException {
		String[] parts = s.split(SYNTAX_DELIMITER);
		for (int i=0;i<parts.length;++i) {
			String arg = parts[i].replaceAll("\\s", "");
			if (arg.indexOf(SYNTAX_RANGE)>-1) {
				String[] range = arg.split(SYNTAX_RANGE,2);
				if (range.length!=2) {
					throw new ParseException(I18N.s(I18N.MSG_RANGE_FORMAT_ERROR));
				}
				try {
					Integer start = Integer.parseInt(range[0]);
					Integer end = Integer.parseInt(range[1]);
					if (start<1 || end<1) {
						throw new ParseException(I18N.s(I18N.MSG_RANGE_NO_ZEROS));
					}
					int inc = start<end?1:(start>end?-1:0);
					int val = start;
					do {
						harmonics.add(val);
						if (val==end) {
							break;
						}
						val += inc;
					} while(true);
				} catch (NumberFormatException e) {
					throw new ParseException(I18N.s(I18N.MSG_RANGE_NUMBER_FORMAT_ERROR));
				}
			}
			else {
				try {
					Integer val = Integer.parseInt(arg);
					if (val<1) {
						throw new ParseException(I18N.s(I18N.MSG_HARMONIC_NO_ZERO));
					}
					harmonics.add(val);
				} catch (NumberFormatException e) {
					throw new ParseException(I18N.s(I18N.MSG_NUMBER_FORMAT_ERROR));
				}
			}
		}
	}

	public WaveProcessor.HarmonicAmplitudeFunction getFunction() {
		return function;
	}

	public ArrayList<Integer> getHarmonics() {
		return harmonics;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(function.description());
		for (int h:harmonics) {
			sb.append(' ');
			sb.append(h);
		}
		return sb.toString();
	}


	@SuppressWarnings("serial")
	public static class ParseException extends Exception {

		public ParseException() {
			super();
		}

		public ParseException(String message) {
			super(message);
		}

		public ParseException(Throwable th) {
			super(th);
		}

	}
	
}
