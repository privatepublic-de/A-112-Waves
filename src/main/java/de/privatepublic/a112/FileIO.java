package de.privatepublic.a112;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.privatepublic.a112.wav.WavFile;
import de.privatepublic.a112.wav.WavFileException;

public class FileIO {

	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * Write double buffer as 8bit, mono, 22.05kHz wav file.
	 * @param file
	 * @param buf
	 * @throws IOException
	 * @throws WavFileException
	 */
	public static void writeBufferAsWavFile(File file, double[] buf) throws IOException, WavFileException {
		int sampleRate = 22050;
		int numFrames = buf.length;
		WavFile wavFile = WavFile.newWavFile(file, 1, numFrames, 8, sampleRate);
		wavFile.writeFrames(buf, numFrames);
		wavFile.close();
	}

	/**
	 * Read wav file into double buffer. Stereo files are mixed down to mono.
	 * @param file
	 * @return double buffer with floating point wav data
	 * @throws IOException
	 * @throws WavFileException
	 */
	public static double[] readWavFile(File file) throws IOException, WavFileException {
		WavFile wavFile = WavFile.openWavFile(file);
		int numChannels = wavFile.getNumChannels();
		if (numChannels>2) {
			throw new WavFileException("Only mono or stereo WAV files can be processed!");
		}
		final boolean isMono = numChannels==1;
		final int outlength = isMono?(int)wavFile.getNumFrames():(int)wavFile.getNumFrames()/2;
		double[] outBuffer = new double[outlength];
		double[] readBuffer = new double[100 * numChannels];
	
		int framesRead;
		int frameCount = -1;
		do {
			framesRead = wavFile.readFrames(readBuffer, 100);
			if (isMono) {
				for (int i=0; i<framesRead; i++) {
					outBuffer[++frameCount] = readBuffer[i];
				}
			}
			else {
				for (int i=0; i<framesRead/2; i++) {
					outBuffer[++frameCount] = (readBuffer[i*2] + readBuffer[i*2+1])*.5;
				}
			}
		}
		while (framesRead != 0);
		wavFile.close();
		return outBuffer;
	}

	/**
	 * Find all matching wav files in given directory. Ignores wav files that are unreadably. 
	 * The returned list of files is alphabetically sorted.
	 * @param directory
	 * @return List of processable wav files (list is empty if no files are found)
	 * @throws IOException
	 * @throws WavFileException
	 */
	public static List<File> matchingWavFiles(File directory) {
		logger.debug("Collecting files in {} ...", directory);
		Collection<File> list = FileUtils.listFiles(directory, new String[] {"wav"}, false);
		ArrayList<File> result = new ArrayList<File>();
		for (File file:list) {
			try {
				WavFile wav = WavFile.openWavFile(file);
				if (wav.getNumChannels()<3) {
					if (wav.getNumFrames()<Integer.MAX_VALUE) { // TODO define a max size
						result.add(file);
					}
					else {
						logger.debug("File {} ignored. It's too large.", file);	
					}
				}
				else {
					logger.debug("File {} ignored. Only mono or stereo files are accepted.", file);
				}
				wav.close();
			} catch (IOException | WavFileException e) {
				logger.error("Error with file {}: {}", file, e.getMessage());
			}
		}
		Collections.sort(result, new Comparator<File>(){
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		logger.debug("Found {} usable wav files.", result.size());
		return result;
	}

}
