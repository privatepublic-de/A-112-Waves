package de.privatepublic.a112;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.IntBuffer;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SysExDump {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * Create byte array with A-112 sysex dump of given sample input
	 * @param in
	 * @return byte[]
	 */
	public static byte[] sysExSampleDump(double[] in) {
		IntBuffer out = IntBuffer.allocate((int)(in.length*1.5));
		out.put(HEADER_SEQUENCE);
		out.put(0); // bank #
		out.put(sampleFrequency(44100));
		// hsbs
		for (int i=0;i<in.length;++i) {
			int val = (convert(in[i]) & 0xff) >> 1;
			out.put(val);
		}
		// lsbs
		int bitcount = 0;
		int byteresult = 0;
		for (int i=0;i<in.length;++i) {
			int val = (convert(in[i]) & 0xff) & 1;
			val = val << bitcount;
			byteresult = byteresult | val;
			if (++bitcount==7) {
				out.put(byteresult);
				bitcount = 0;
				byteresult = 0;
			}
		}
		out.put(byteresult); // last lsb byte
		out.put(0xf7); // sysex end
		
		byte[] result = new byte[out.position()];
		for (int i=0;i<out.position();i++) {
			result[i] = (byte)(out.get(i) & 0xff);
		}
		
		return result;
	}
	
	/**
	 * Write sysex dump of wavedata to file
	 * @param file
	 * @param wavedata
	 * @throws IOException
	 */
	public static void writeSysExDumpFile(File file, double[] wavedata) throws IOException {
		OutputStream out = new FileOutputStream(file);
		byte[] data = sysExSampleDump(wavedata);
		IOUtils.write(data, out);
	}
	
	
	/**
	 * Converts a double sample value to a byte
	 * @param v sample double
	 * @return sample value as byte 0x00-0xff
	 */
	private static int convert(double v) {
		return (int)(255*((v+1)*.5));
	}
	
	/**
	 * Converts a sample frequency in hertz to bytes for a-112 sysex
	 * @param freq
	 * @return
	 */
	private static int[] sampleFrequency(int freq) {
		int value = (5000000 / freq); // 5 MHz internal clock
		return new int[] {
				(value >> 9 & 127),
				(value >> 1 & 127),
				(value >> 7 & 2) & (value & 1)
		};
	}
	
	
	private static final int[] HEADER_SEQUENCE = new int []{ 0xf0, 0x00, 0x20, 0x20, 0x7e};
	// f0 = start sysex dump, 00 20 20 = doepfer equipment id; 7e = indicates sample dump
}
