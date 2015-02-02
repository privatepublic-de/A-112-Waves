package de.privatepublic.a112;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.SysexMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *	The MidiController provides a list of all available MIDI devices with output ports.
 *  A singleton instance of the MidiController can be optained with MidiController.getInstance()
 */
public class MidiController {

	private static final Logger logger = LogManager.getLogger();
	private static final MidiController instance = new MidiController(); 
	
	/**
	 * Get singleton instance.
	 * @return MidiController
	 */
	public static MidiController instance() {
		return instance;
	}
	
	private ArrayList<MidiDeviceWrapper> deviceList = new ArrayList<MidiDeviceWrapper>();
	
	private MidiController() {
		Info[] midiDeviceInfos = MidiSystem.getMidiDeviceInfo();
		for (Info info : midiDeviceInfos) {
			try {
				MidiDevice midiDevice = MidiSystem.getMidiDevice(info);
				if (midiDevice.getMaxReceivers() == -1) {
					MidiDeviceWrapper description = new MidiDeviceWrapper(info, midiDevice);
					deviceList.add(description);
				}
			}
			catch (MidiUnavailableException e) {
				logger.error(e);
			}
		}
		Collections.sort(deviceList, new Comparator<MidiDeviceWrapper>() {
			@Override
			public int compare(MidiDeviceWrapper o1, MidiDeviceWrapper o2) {
				return o1.info.getVendor().compareTo(o2.info.getVendor());
			}
		});
	}
	
	public List<MidiDeviceWrapper> getDevices() {
		return deviceList;
	}
	
	
	/**
	 * Wrapper for a single MIDI output device. The toString method returns a 
	 * human readable device name.
	 */
	public static class MidiDeviceWrapper {
		private Info info;
		private MidiDevice device;
		
		public MidiDeviceWrapper(Info info, MidiDevice device) {
			this.info = info;
			this.device = device;
		}
		
		@Override
		/**
		 * Human readable device description.
		 */
		public String toString() {
			return info.getName()+" ("+info.getVendor()+", "+info.getDescription()+")";
		}
		
		/**
		 * Sends the given wave data as doepfer compatible sysex message.
		 * @param waveData
		 * @return number of bytes sent
		 */
		public int sendSampleDump(double[] waveData) {
			try {
				device.open();
				Receiver receiver = device.getReceiver();
				SysexMessage msg = new SysexMessage();
				byte[] data = SysExDump.sysExSampleDump(waveData);
				msg.setMessage(data, data.length);
				receiver.send(msg, 0);
				device.close();
				return data.length;
			// TODO: throw exceptions and handle on ui layer				
			} catch (MidiUnavailableException e) {
				logger.error(e); 
			} catch (InvalidMidiDataException e) {
				logger.error(e);
			}
			return 0;
		}
		
		
	}
	
}
