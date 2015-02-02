package de.privatepublic.a112;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.privatepublic.a112.MidiController.MidiDeviceWrapper;
import de.privatepublic.a112.WaveRenderJob.ParseException;
import de.privatepublic.a112.wav.WavFileException;

public class A112Waves {

	private static final Logger logger = LogManager.getLogger();
	
	private static final Preferences PREFS = Preferences.userNodeForPackage(A112Waves.class);
	private static final String PREF_WAV_DIRECTORY = "wav-directory";
	private static final String PREF_WAV_FILE = "wav-filename";
	private static final String PREF_OUT_FILENAME = "out-filename";
	private static final String PREF_MIDI_INTERFACE_NAME = "midi-interface-name";
	private static final String[] FREQUENCY_FACTOR_DISPLAY_STRINGS = new String[] {"1x","2x","4x"};
	
	private static final Cursor CURSOR_BUSY = new Cursor(Cursor.WAIT_CURSOR);
	private static final Cursor CURSOR_DEFAULT = new Cursor(Cursor.DEFAULT_CURSOR);
	
	private JFrame frame;
	private JTextArea textLog;
	private WaveRenderPanel waveRenderPanel;
	private File selectedDir;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		// take the menu bar off the jframe
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		// set the name of the application menu item
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "A-112 Waves");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					A112Waves window = new A112Waves();
					window.frame.setVisible(true);
				} catch (Exception e) {
					logger.error(e);
				}
			}
		});
		MidiController.instance();
	}

	/**
	 * Create the application.
	 */
	public A112Waves() {
		initialize();
	}

	/**
	 * Initialize user interface.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initialize() {
		String abouttext;
		try {
			abouttext = org.apache.commons.io.IOUtils.toString(A112Waves.class.getResource("/about.txt"), "UTF-8");
		} catch (IOException e1) {
			logger.error(e1);
			abouttext = "Error reading about text, sorry :(";
		}
		
		ImageIcon icon = new ImageIcon(A112Waves.class.getResource("/icon.jpg"));
		ImageIcon icon_small = new ImageIcon(A112Waves.class.getResource("/icon-small.jpg"));
		Image iconimage = icon.getImage();
		frame = new JFrame();
		frame.getContentPane().setBackground(Color.WHITE);
		frame.setIconImage(iconimage);
		frame.setBounds(100, 100, 667, 520);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.setTitle(I18N.s(I18N.APP_TITLE));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		JPanel panelGenerate = new JPanel();
		tabbedPane.addTab(I18N.s(I18N.TAB_TITLE_GENERATE), null, panelGenerate, null);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		
		final JButton btnCreateWavetable = new JButton(I18N.s(I18N.BTN_CREATE_WAVETABLE));
		btnCreateWavetable.setEnabled(false);
		
		final JLabel lblCompileStatus = new JLabel(I18N.s(I18N.LBL_SCRIPT_HINT));
		
		final JCheckBox chckbxInterpolated_1 = new JCheckBox(I18N.s(I18N.CHB_INTERPOLATED));
		
		JLabel lblWaveformRenderScript = new JLabel(I18N.s(I18N.LBL_SCRIPT));
		
		final JComboBox comboBoxFactorGene = new JComboBox(FREQUENCY_FACTOR_DISPLAY_STRINGS);
		comboBoxFactorGene.setToolTipText(I18N.s(I18N.TT_FACTOR));
		GroupLayout gl_panelGenerate = new GroupLayout(panelGenerate);
		gl_panelGenerate.setHorizontalGroup(
			gl_panelGenerate.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panelGenerate.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelGenerate.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 634, Short.MAX_VALUE)
						.addGroup(gl_panelGenerate.createSequentialGroup()
							.addComponent(btnCreateWavetable)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(comboBoxFactorGene, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(chckbxInterpolated_1))
						.addGroup(gl_panelGenerate.createSequentialGroup()
							.addGap(6)
							.addComponent(lblCompileStatus, GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE))
						.addComponent(lblWaveformRenderScript))
					.addContainerGap())
		);
		gl_panelGenerate.setVerticalGroup(
			gl_panelGenerate.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panelGenerate.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblWaveformRenderScript)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
					.addGap(4)
					.addComponent(lblCompileStatus)
					.addGap(18)
					.addGroup(gl_panelGenerate.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnCreateWavetable)
						.addComponent(chckbxInterpolated_1)
						.addComponent(comboBoxFactorGene, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(16))
		);
		
		final JTextArea txtrRenderCode = new JTextArea();
		txtrRenderCode.setText("");
		scrollPane_1.setViewportView(txtrRenderCode);
		panelGenerate.setLayout(gl_panelGenerate);
		txtrRenderCode.getDocument().addDocumentListener(new DocumentListener() {

	        @Override
	        public void removeUpdate(DocumentEvent e) {
	        	parse();
	        }

	        @Override
	        public void insertUpdate(DocumentEvent e) {
	        	parse();
	        }

	        @Override
	        public void changedUpdate(DocumentEvent arg0) {
	        	parse();
	        }
	        
	        private void parse() {
	        	try {
					List<WaveRenderJob> list = WaveRenderJob.parseScript(txtrRenderCode.getText());
					int count = 0;
					for (WaveRenderJob j:list) {
						count += j.getHarmonics().size();
					}
					if (count>0) {
						if (count<=WaveRenderJob.WAVES_COUNT) {
							lblCompileStatus.setText(I18N.s(I18N.MSG_SCRIPT_OK, count));
						}
						else {
							lblCompileStatus.setText(I18N.s(I18N.MSG_SCRIPT_OVERFLOW, WaveRenderJob.WAVES_COUNT, count));
						}
						btnCreateWavetable.setEnabled(true);
					}
					else {
						lblCompileStatus.setText(I18N.s(I18N.LBL_SCRIPT_HINT));
						btnCreateWavetable.setEnabled(false);
					}
				} catch (ParseException e) {
					lblCompileStatus.setText(e.getMessage());
					btnCreateWavetable.setEnabled(false);
				}
	        }
	        
	    });
		
		
		
		btnCreateWavetable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					int factor = (int)Math.pow(2, comboBoxFactorGene.getSelectedIndex());
					CreateScriptedWavetableTask task = new CreateScriptedWavetableTask(WaveRenderJob.parseScript(txtrRenderCode.getText()), chckbxInterpolated_1.isSelected(), factor);
					task.addPropertyChangeListener(new PropertyChangeListener() {
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							if (WORKER_STATE_UPDATE.equals(evt.getPropertyName())) {
								lblCompileStatus.setText((String) evt.getNewValue());
				             }
						}
					});
					task.execute();
				} catch (ParseException e) {
					logger.error(e);
				}
			}
		});
		
		JPanel panelWAVfiles = new JPanel();
		tabbedPane.addTab(I18N.s(I18N.TAB_TITLE_WAVFILES), null, panelWAVfiles, null);
		
		JLabel lblCreateWavetableFrom_1 = new JLabel(I18N.s(I18N.LBL_WAVEFILES));
		lblCreateWavetableFrom_1.setAlignmentY(Component.TOP_ALIGNMENT);
		lblCreateWavetableFrom_1.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		final JLabel lblnoFolderSelected_1 = new JLabel(I18N.s(I18N.LBL_SELECT_FILE_OR_FOLDER));
		lblnoFolderSelected_1.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		JButton btnSelect = new JButton(I18N.s(I18N.BTN_SELECT_INPUT_FOLDER));
		btnSelect.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		final JButton btnCreateWavetable_1 = new JButton(I18N.s(I18N.BTN_CREATE_WAVETABLE));
		btnCreateWavetable_1.setEnabled(false);
		btnCreateWavetable_1.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		scrollPane.setViewportBorder(null);
		
		final JCheckBox chckbxInterpolated = new JCheckBox(I18N.s(I18N.CHB_INTERPOLATED));
		
		final JComboBox comboBoxFactorWav = new JComboBox(FREQUENCY_FACTOR_DISPLAY_STRINGS);
		comboBoxFactorWav.setToolTipText(I18N.s(I18N.TT_FACTOR));
		
		JButton btnSingleWavFile = new JButton(I18N.s(I18N.BTN_SINGLE_WAVFILE));
		btnSingleWavFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String recentPath = PREFS.get(PREF_WAV_FILE, null);
		        JFileChooser chooser = new JFileChooser();
		        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		        chooser.setMultiSelectionEnabled(false);
		        chooser.setAcceptAllFileFilterUsed(false);
		        chooser.setFileFilter(new FileFilter() {
					@Override
					public String getDescription() { return I18N.s(I18N.FILETYPE_WAV); }
					@Override
					public boolean accept(File f) {	return (f.getName().toLowerCase().endsWith(".wav"));}
				});
		        chooser.setDialogTitle(I18N.s(I18N.DLG_TITLE_SELECT_WAV));
		        if (recentPath!=null) {
		        	chooser.setSelectedFile(new File(recentPath));
		        }
		        int retvalue = chooser.showDialog(null, I18N.s(I18N.DLG_OPEN_BUTTON));
		        if (retvalue==JFileChooser.APPROVE_OPTION) {
		        	File selectedFile = chooser.getSelectedFile();
		        	putPrefInstantly(PREF_WAV_FILE, selectedFile.getPath());
		        	try {
						double[] filedata = FileIO.readWavFile(selectedFile);
						WaveRenderJob.WAVE_DATA = WaveProcessor.resample(filedata, WaveRenderJob.WAVE_TABLE_LENGTH, 1);
						lblnoFolderSelected_1.setText(selectedFile.getPath());
						textLog.setText(I18N.s(I18N.LOG_RESAMPLED, selectedFile.getName(), filedata.length, WaveRenderJob.WAVE_TABLE_LENGTH));
						waveRenderPanel.repaint();
					} catch (IOException | WavFileException e) {
						lblnoFolderSelected_1.setText(I18N.s(I18N.LOG_ERROR_READING, selectedFile.getPath()));
						logger.error(e);
					}
		            
		        }
			}
		});
		GroupLayout gl_panelWAVfiles = new GroupLayout(panelWAVfiles);
		gl_panelWAVfiles.setHorizontalGroup(
			gl_panelWAVfiles.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelWAVfiles.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelWAVfiles.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelWAVfiles.createSequentialGroup()
							.addGroup(gl_panelWAVfiles.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panelWAVfiles.createSequentialGroup()
									.addComponent(btnSelect)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnCreateWavetable_1)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(comboBoxFactorWav, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(chckbxInterpolated))
								.addComponent(lblCreateWavetableFrom_1))
							.addGap(160))
						.addComponent(btnSingleWavFile)
						.addGroup(gl_panelWAVfiles.createParallelGroup(Alignment.TRAILING, false)
							.addComponent(scrollPane, Alignment.LEADING, 0, 0, Short.MAX_VALUE)
							.addComponent(lblnoFolderSelected_1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 632, Short.MAX_VALUE)))
					.addContainerGap())
		);
		gl_panelWAVfiles.setVerticalGroup(
			gl_panelWAVfiles.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelWAVfiles.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblCreateWavetableFrom_1)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnSingleWavFile)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panelWAVfiles.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnSelect)
						.addComponent(btnCreateWavetable_1)
						.addComponent(comboBoxFactorWav, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(chckbxInterpolated))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblnoFolderSelected_1)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
					.addContainerGap())
		);
		
		textLog = new JTextArea();
		textLog.setFont(textLog.getFont().deriveFont(textLog.getFont().getStyle() | Font.ITALIC));
		textLog.setForeground(SystemColor.inactiveCaptionText);
		textLog.setBorder(null);
		textLog.setBackground(UIManager.getColor("Panel.background"));
		scrollPane.setViewportView(textLog);
		textLog.setEditable(false);
		textLog.setWrapStyleWord(true);
		textLog.setLineWrap(true);
		textLog.setText(I18N.s(I18N.LOG_INITIAL_TEXT));
		panelWAVfiles.setLayout(gl_panelWAVfiles);
		
		
		
		btnSelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String recentPath = PREFS.get(PREF_WAV_DIRECTORY, null);
		        JFileChooser chooser = new JFileChooser();
		        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		        chooser.setMultiSelectionEnabled(false);
		        chooser.setDialogTitle(I18N.s(I18N.DLG_TITLE_SELECT_FOLDER));
		        if (recentPath!=null) {
		        	chooser.setSelectedFile(new File(recentPath));
		        }
		        int retvalue = chooser.showDialog(null, I18N.s(I18N.DLG_CHOOSE_BUTTON));
		        if (retvalue==JFileChooser.APPROVE_OPTION) {
		        	selectedDir = chooser.getSelectedFile();
		        	putPrefInstantly(PREF_WAV_DIRECTORY, selectedDir.getPath());
		        	if (!selectedDir.isDirectory()) {
		        		selectedDir = new File(selectedDir.getParent());
		        	}
		        	List<File> filelist = FileIO.matchingWavFiles(selectedDir);
		        	lblnoFolderSelected_1.setText(selectedDir.getPath());
		        	int filecount = filelist.size();
		        	btnCreateWavetable_1.setEnabled(filelist.size()>0);
		        	textLog.setText(I18N.s(I18N.LOG_FOLDER_STATUS, filecount));
		        	if (filecount>WaveRenderJob.WAVES_COUNT) {
		        		textLog.append(I18N.s(I18N.LOG_FOLDER_STATUS_CUT, WaveRenderJob.WAVES_COUNT));
		        	}
		        }
			}
		});
		
		btnCreateWavetable_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				int factor = (int)Math.pow(2, comboBoxFactorWav.getSelectedIndex());
				CreateWavetableTask task = new CreateWavetableTask(selectedDir, chckbxInterpolated.isSelected(), factor);
	        	task.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if (WORKER_STATE_UPDATE.equals(evt.getPropertyName())) {
			                 textLog.append(evt.getNewValue()+"\n");
			             }
					}
				});
	        	task.execute();
			}
		});
		
		JPanel panelTransmit = new JPanel();
		panelTransmit.setBorder(new EmptyBorder(10, 10, 10, 10));
		tabbedPane.addTab(I18N.s(I18N.TAB_TITLE_TRANSMIT), null, panelTransmit, null);
		GridBagLayout gbl_panelTransmit = new GridBagLayout();
		gbl_panelTransmit.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panelTransmit.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panelTransmit.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panelTransmit.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panelTransmit.setLayout(gbl_panelTransmit);
		
		JLabel lblTransmitGeneratedWavetable = new JLabel(I18N.s(I18N.LBL_TRANSMIT));
		GridBagConstraints gbc_lblTransmitGeneratedWavetable = new GridBagConstraints();
		gbc_lblTransmitGeneratedWavetable.anchor = GridBagConstraints.WEST;
		gbc_lblTransmitGeneratedWavetable.gridwidth = 2;
		gbc_lblTransmitGeneratedWavetable.insets = new Insets(0, 0, 5, 5);
		gbc_lblTransmitGeneratedWavetable.gridx = 0;
		gbc_lblTransmitGeneratedWavetable.gridy = 1;
		panelTransmit.add(lblTransmitGeneratedWavetable, gbc_lblTransmitGeneratedWavetable);
		
		JLabel lblMidiInterface = new JLabel(I18N.s(I18N.LBL_MIDI_INTERFACE));
		GridBagConstraints gbc_lblMidiInterface = new GridBagConstraints();
		gbc_lblMidiInterface.insets = new Insets(0, 0, 5, 5);
		gbc_lblMidiInterface.anchor = GridBagConstraints.WEST;
		gbc_lblMidiInterface.gridx = 0;
		gbc_lblMidiInterface.gridy = 2;
		panelTransmit.add(lblMidiInterface, gbc_lblMidiInterface);
		
		final JComboBox comboBoxMidiInterface = new JComboBox();
		comboBoxMidiInterface.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				MidiDeviceWrapper midi = (MidiDeviceWrapper)comboBoxMidiInterface.getSelectedItem();
				if (midi!=null) {
					putPrefInstantly(PREF_MIDI_INTERFACE_NAME, midi.toString());
				}
			}
		});
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 2;
		panelTransmit.add(comboBoxMidiInterface, gbc_comboBox);
		
		
		final JButton btnMidiTransmit = new JButton(I18N.s(I18N.BTN_MIDI_TRANSMIT));
		btnMidiTransmit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final String label = btnMidiTransmit.getText();
				SwingWorker<Void, Void> task = new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {
						MidiDeviceWrapper midi = (MidiDeviceWrapper)comboBoxMidiInterface.getSelectedItem();
						int length = midi.sendSampleDump(WaveRenderJob.WAVE_DATA);
						if (length>0) {
							int seconds = (int)Math.round(length/3906.25);
							for (int i=seconds;i>0;i--) {
								btnMidiTransmit.setText(I18N.s(I18N.MSG_TRANSMIT_STATUS,i));
								Thread.sleep(1000);
							}
						}
						return null;
					}
					
					@Override
					protected void done() {
						btnMidiTransmit.setEnabled(true);
						btnMidiTransmit.setText(label);
						frame.setCursor(CURSOR_DEFAULT);
					}
				};
				btnMidiTransmit.setEnabled(false);
				frame.setCursor(CURSOR_BUSY);
				task.execute();
			}
		});
		GridBagConstraints gbc_btnMidiTransmit = new GridBagConstraints();
		gbc_btnMidiTransmit.anchor = GridBagConstraints.WEST;
		gbc_btnMidiTransmit.insets = new Insets(0, 0, 5, 5);
		gbc_btnMidiTransmit.gridx = 1;
		gbc_btnMidiTransmit.gridy = 3;
		panelTransmit.add(btnMidiTransmit, gbc_btnMidiTransmit);
		
		
		JSeparator separator = new JSeparator();
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.insets = new Insets(0, 0, 5, 5);
		gbc_separator.gridx = 1;
		gbc_separator.gridy = 4;
		panelTransmit.add(separator, gbc_separator);
		
		JLabel lblSaveGeneratedWavetable = new JLabel(I18N.s(I18N.LBL_SAVE_WAV_SYX));
		GridBagConstraints gbc_lblSaveGeneratedWavetable = new GridBagConstraints();
		gbc_lblSaveGeneratedWavetable.anchor = GridBagConstraints.WEST;
		gbc_lblSaveGeneratedWavetable.gridwidth = 2;
		gbc_lblSaveGeneratedWavetable.insets = new Insets(0, 0, 5, 5);
		gbc_lblSaveGeneratedWavetable.gridx = 0;
		gbc_lblSaveGeneratedWavetable.gridy = 5;
		panelTransmit.add(lblSaveGeneratedWavetable, gbc_lblSaveGeneratedWavetable);
		
		final JButton btnSaveAs = new JButton(I18N.s(I18N.BTN_SAVE_WAV_SYX));
		btnSaveAs.addActionListener(new ActionListener() {
			@SuppressWarnings("serial")
			public void actionPerformed(ActionEvent arg0) {
				String recentFile = PREFS.get(PREF_OUT_FILENAME, null);
		        JFileChooser chooser = new JFileChooser() {
					@Override
		            public void approveSelection(){
		                File f = getSelectedFile();
		                if(f.exists()){
		                    int result = JOptionPane.showConfirmDialog(this,I18N.s(I18N.MSG_FILE_EXISTS),I18N.s(I18N.MSG_FILE_EXISTS_TITLE), JOptionPane.YES_NO_CANCEL_OPTION);
		                    switch(result){
		                        case JOptionPane.YES_OPTION:
		                            super.approveSelection();
		                            return;
		                        case JOptionPane.NO_OPTION:
		                            return;
		                        case JOptionPane.CLOSED_OPTION:
		                            return;
		                        case JOptionPane.CANCEL_OPTION:
		                            cancelSelection();
		                            return;
		                    }
		                }
		                super.approveSelection();
		            }        
		        };
		        chooser.setAcceptAllFileFilterUsed(false);
		        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		        chooser.addChoosableFileFilter(new FileFilter() {
					@Override
					public String getDescription() { return I18N.s(I18N.FILETYPE_WAV); }
					@Override
					public boolean accept(File f) {	return (f.getName().toLowerCase().endsWith(".wav"));}
				});
		        chooser.addChoosableFileFilter(new FileFilter() {
					@Override
					public String getDescription() { return I18N.s(I18N.FILETYPE_SYX); }
					@Override
					public boolean accept(File f) {	return (f.getName().toLowerCase().endsWith(".syx"));}
				});
		        if (recentFile!=null) {
		        	chooser.setSelectedFile(new File(recentFile));
		        	if (recentFile.endsWith(".wav")) {
		        		chooser.setFileFilter(chooser.getChoosableFileFilters()[0]);
		        	}
		        	else {
		        		chooser.setFileFilter(chooser.getChoosableFileFilters()[1]);
		        	}
		        }
		        else {
		        	chooser.setFileFilter(chooser.getChoosableFileFilters()[0]);
		        }
		        chooser.setMultiSelectionEnabled(false);
		        chooser.setDialogTitle(I18N.s(I18N.DLG_TITLE_SAVE_WAVETABLE));
		        int retvalue = chooser.showDialog(null, I18N.s(I18N.DLG_SAVE_BUTTON));
		        if (retvalue==JFileChooser.APPROVE_OPTION) {
		        	File selectedFile = chooser.getSelectedFile();
		        	FileFilter selectedFilter = chooser.getFileFilter();
		        	putPrefInstantly(PREF_OUT_FILENAME, selectedFile.getPath());
		        	try {
						if (selectedFilter.getDescription().indexOf(".wav")>-1) {
							if (!"wav".equals(FilenameUtils.getExtension(selectedFile.getName()))) {
								selectedFile = new File(selectedFile.getPath()+".wav");
							}
							FileIO.writeBufferAsWavFile(selectedFile, WaveRenderJob.WAVE_DATA);
						}
						else {
							if (!"syx".equals(FilenameUtils.getExtension(selectedFile.getName()))) {
								selectedFile = new File(selectedFile.getPath()+".syx");
							}
							SysExDump.writeSysExDumpFile(selectedFile, WaveRenderJob.WAVE_DATA);
						}
					} catch (IOException | WavFileException e) {
						JOptionPane.showMessageDialog(null, I18N.s(I18N.MSG_FILE_WRITE_ERROR)+"\n"+e.getMessage());
						logger.error(e);
					}
		        	putPrefInstantly(PREF_OUT_FILENAME, selectedFile.getPath());
		        }
			}
		});
		GridBagConstraints gbc_btnSaveAs = new GridBagConstraints();
		gbc_btnSaveAs.insets = new Insets(0, 0, 0, 5);
		gbc_btnSaveAs.anchor = GridBagConstraints.WEST;
		gbc_btnSaveAs.gridx = 1;
		gbc_btnSaveAs.gridy = 6;
		panelTransmit.add(btnSaveAs, gbc_btnSaveAs);
		
		JPanel panelCredits = new JPanel();
		panelCredits.setBorder(null);
		tabbedPane.addTab(I18N.s(I18N.TAB_TITLE_ABOUT), null, panelCredits, null);
		panelCredits.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPaneAbout = new JScrollPane();
		scrollPaneAbout.setBorder(null);
		scrollPaneAbout.setViewportBorder(null);
		panelCredits.add(scrollPaneAbout, BorderLayout.CENTER);

		
		JTextPane txtpnAbout = new JTextPane();
		txtpnAbout.setBorder(new EmptyBorder(0, 10, 0, 10));
		scrollPaneAbout.setViewportView(txtpnAbout);
		txtpnAbout.setBackground(UIManager.getColor("Panel.background"));
		txtpnAbout.setEditable(false);
		txtpnAbout.setContentType("text/html");
		txtpnAbout.setText(abouttext);
		txtpnAbout.setCaretPosition(0);
		txtpnAbout.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent hle) {
				if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
					try {
						try {
							Desktop.getDesktop().browse(new URI(hle.getURL().toString()));
						} catch (URISyntaxException ex) {
							logger.error(ex);
						}
					} catch (IOException ex) {
						logger.error(ex);
					}

				}
			}
		});
		
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				JTabbedPane sourceTabbedPane = (JTabbedPane) evt.getSource();
		        int index = sourceTabbedPane.getSelectedIndex();
		        if (index==2) { // save/transmitpanel
		        	boolean enabled = WaveRenderJob.WAVE_DATA!=null;
		        	btnMidiTransmit.setEnabled(enabled);
		        	String recentMidiDevice = PREFS.get(PREF_MIDI_INTERFACE_NAME, null);
		        	btnSaveAs.setEnabled(enabled);
		        	DefaultComboBoxModel<MidiDeviceWrapper> model = (DefaultComboBoxModel) comboBoxMidiInterface.getModel();
		        	model.removeAllElements();
		        	for (MidiDeviceWrapper info:MidiController.instance().getDevices()) {
		        		model.addElement(info);
		        		if (info.toString().equals(recentMidiDevice)) {
		        			comboBoxMidiInterface.setSelectedItem(info);
		        		}
		        	}
		        }
			}
		});
		
		JLabel lblAWaves = new JLabel(I18N.s(I18N.APP_TITLE));
		lblAWaves.setIcon(icon_small);
		lblAWaves.setHorizontalAlignment(SwingConstants.LEFT);
		lblAWaves.setFont(new Font("Lucida Grande", Font.BOLD, 22));
		frame.getContentPane().add(lblAWaves, BorderLayout.NORTH);
		
		waveRenderPanel = new WaveRenderPanel();
		waveRenderPanel.setToolTipText(I18N.s(I18N.TT_OSCILLOSCOPE));
		waveRenderPanel.setBorder(null);
		waveRenderPanel.setBackground(Color.BLACK);
		JScrollPane scrollPaneWaveform = new JScrollPane(waveRenderPanel);
		scrollPaneWaveform.setViewportBorder(null);
		scrollPaneWaveform.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		frame.getContentPane().add(scrollPaneWaveform, BorderLayout.SOUTH);
		
		
		
	}
	
	/** PropertyChange update key for SwingWorkers */
	private static String WORKER_STATE_UPDATE = "progressupdate";
	
	/**
	 * Worker to combine wav files in directory to wavetable
	 *
	 */
	class CreateWavetableTask extends SwingWorker<Void, String> {
		private File inputDirectory;
		private boolean interpolated;
		private int factorFrequency = 1;
		
		public CreateWavetableTask(File inputDirectory, boolean interpolated, int factor) {
			this.inputDirectory = inputDirectory;
			this.interpolated = interpolated;
			this.factorFrequency = factor;
		}
		
		@Override
		protected Void doInBackground() throws Exception {
			List<File> fileList = FileIO.matchingWavFiles(inputDirectory);
			if (fileList.size()>0) {
				int fileCount = Math.min(WaveRenderJob.WAVES_COUNT, fileList.size());
				double[][] resampled = new double[fileCount][WaveRenderJob.WAVE_SAMPLE_COUNT];
				for (int i=0;i<fileCount;++i) {
					File f = fileList.get(i);
					double[] inbuff = FileIO.readWavFile(f);
					resampled[i] = WaveProcessor.resample(inbuff, WaveRenderJob.WAVE_SAMPLE_COUNT, factorFrequency);
					firePropertyChange(WORKER_STATE_UPDATE, null, I18N.s(I18N.LOG_RESAMPLED, f.getName(), inbuff.length, WaveRenderJob.WAVE_SAMPLE_COUNT));
				}
				WaveRenderJob.WAVE_DATA = WaveProcessor.interpolateWaves(resampled, interpolated);
				waveRenderPanel.repaint();
				firePropertyChange(WORKER_STATE_UPDATE, null, I18N.s(I18N.LOG_FINISHED_GENERATING));
			}
			else {
				firePropertyChange(WORKER_STATE_UPDATE, null, I18N.s(I18N.LOG_NOTHING_TO_DO));
			}
			return null;
		}
		
	}
	
	/**
	 * Worker to create scripted wavetables
	 *
	 */
	class CreateScriptedWavetableTask extends SwingWorker<Void, String> {
		private boolean interpolated;
		private List<WaveRenderJob> joblist;
		private int factorFrequency = 1;
		
		public CreateScriptedWavetableTask(List<WaveRenderJob> joblist, boolean interpolated, int factor) {
			this.joblist = joblist;
			this.interpolated = interpolated;
			this.factorFrequency = factor;
		}
		
		@Override
		protected Void doInBackground() throws Exception {
			int numberwaves = 0;
			for (WaveRenderJob j:joblist) {
				numberwaves += j.getHarmonics().size();
			}
			numberwaves = Math.min(WaveRenderJob.WAVES_COUNT, numberwaves);
			double[][] aWave = new double[numberwaves][WaveRenderJob.WAVE_SAMPLE_COUNT];
			int producedwaves = 0;
			for (WaveRenderJob j:joblist) {
				for (int i:j.getHarmonics()) {
					if (producedwaves<numberwaves) {
						aWave[producedwaves]  = WaveProcessor.createTableWithOvertones(WaveProcessor.TABLE_LENGTH, WaveRenderJob.WAVE_SAMPLE_COUNT/2, i, j.getFunction(), factorFrequency);
						producedwaves++;
						firePropertyChange(WORKER_STATE_UPDATE, null, I18N.s(I18N.MSG_GENERATE_FINISHED, producedwaves, numberwaves));
					}
				}
			}
			WaveRenderJob.WAVE_DATA = WaveProcessor.interpolateWaves(aWave, interpolated);
			waveRenderPanel.repaint();
			firePropertyChange(WORKER_STATE_UPDATE, null, I18N.s(I18N.LOG_FINISHED_GENERATING));
			return null;
		}
	}
	
	/**
	 * Store application preference instantly with silently catching possible BackingStoreException.
	 * @param key
	 * @param val
	 */
	private void putPrefInstantly(String key, String val) {
		PREFS.put(key, val);
    	try {
			PREFS.flush();
		} catch (BackingStoreException e) {
			logger.error("Couldn't write pref {}={}", key, val);
			logger.error(e);
		}
	}
	
}
