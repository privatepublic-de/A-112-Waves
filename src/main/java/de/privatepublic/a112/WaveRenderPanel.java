package de.privatepublic.a112;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public class WaveRenderPanel extends JComponent {

	private Dimension preferredSize;
	
	public WaveRenderPanel() {
		setPreferredSize(new Dimension(WaveRenderJob.WAVE_TABLE_LENGTH, 148));
	}
	

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(preferredSize);
        this.preferredSize = preferredSize;
    }


    @Override
    public Dimension getPreferredSize() {
        return this.preferredSize;
    }

	@Override
	public void paint(Graphics go) {
		Graphics2D g = (Graphics2D)go;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, WaveRenderJob.WAVE_TABLE_LENGTH, 148);
		g.setColor(LIGHT_GRAY);
		g.drawLine(0, 64, WaveRenderJob.WAVE_TABLE_LENGTH, 64);
		for (int i=0;i<WaveRenderJob.WAVES_COUNT;i++) {
			g.drawString("#"+(i+1), i*WaveRenderJob.WAVE_SAMPLE_COUNT+2, 16);
			g.drawLine(i*WaveRenderJob.WAVE_SAMPLE_COUNT, 0, i*WaveRenderJob.WAVE_SAMPLE_COUNT, 128);	
		}
		if (WaveRenderJob.WAVE_DATA!=null) {
			g.setColor(Color.GREEN);
			for (int i=1;i<WaveRenderJob.WAVE_DATA.length;i++) {
				g.drawLine(i-1, (int)(WaveRenderJob.WAVE_DATA[i-1]*64d+64), i, (int)(WaveRenderJob.WAVE_DATA[i]*64d+64));
			}
		}
	}
	
	private static Color LIGHT_GRAY = new Color(0xd0, 0xd0, 0xd0);
	
}
