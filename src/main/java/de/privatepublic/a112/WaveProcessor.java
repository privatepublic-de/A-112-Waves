package de.privatepublic.a112;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;




public class WaveProcessor {

	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();
	
	
	public static double[] resample(double[] inBuffer, int targetlen, int factorFrequency) {
		int targetLength = targetlen / factorFrequency;
		
		double ratio = targetLength/(double)inBuffer.length;
		if (targetLength==inBuffer.length) {
			return inBuffer.clone();
		}
		double[] outBuffer = new double[targetLength];
		if (targetLength<inBuffer.length) {
			// decimate
			double inc = ratio;  
			double outPos = 0;
			double maxa = 0;
			for (int i=0;i<inBuffer.length;i++) {
				final int indexBase = (int)outPos;
				final double indexFrac = outPos-indexBase;
				outBuffer[indexBase] += inBuffer[i]*ratio*(1-indexFrac);
				outBuffer[(indexBase+1)%targetLength] += inBuffer[i]*ratio*(indexFrac);
				outPos += inc;
				maxa = Math.max(maxa, Math.abs(outBuffer[indexBase]));
			}
			if (maxa>1) {
				// normalize
				final double factor = 1.0/maxa;
				for (int t=0;t<outBuffer.length;t++) {
					outBuffer[t] *= factor;
				}
			}
		}
		else {
			// stretch
			double inc = inBuffer.length/(double)targetLength;
			double inPos = 0;
			for (int i=0;i<targetLength;i++) {
				final int indexBase = (int)inPos;
				final double indexFrac = inPos-indexBase;
				outBuffer[i] = inBuffer[indexBase]+(inBuffer[(indexBase+1)%inBuffer.length]-inBuffer[indexBase])*indexFrac;
				inPos += inc;
			}
		}
		if (factorFrequency==1) {
			return outBuffer;
		}
		else {
			double[] output = new double[targetlen];
			for (int i=0;i<factorFrequency;++i) {
				for (int is=0;is<outBuffer.length;++is) {
					output[is+i*outBuffer.length] = outBuffer[is]; 
				}
			}
			return output;
		}
	}
	
	
	public static double[] createTableWithOvertones(int tableLength, int numberHarmonics, int overtones, HarmonicAmplitudeFunction haf, int factorFrequency) {
		int len = tableLength / factorFrequency;
		final double[] result = new double[len];
		final double frqinc = PI2/len;
		final double sigindex = PI2/numberHarmonics;
		final double sigma = numberHarmonics>1?Math.sin(sigindex)/sigindex:1;
		double maxa = 0;
		double baseamp = 1;
		double topot = overtones-1;
		for (int ot=0;ot<overtones;ot++) {
			baseamp = topot==0?1:(double)(ot/topot)*(ot/topot);
			for (int t=0;t<len;t++) {
				haf.init();
				for (int n=1;n<numberHarmonics+1;++n) {
					result[t] += baseamp*haf.ampFactor(n)*sigma*Math.sin((ot+n)*(t*frqinc));
				}
				maxa = Math.max(maxa, Math.abs(result[t]));
			}
		}
		if (maxa>1) {
			// normalize
			final double factor = 1.0/maxa;
			for (int t=0;t<result.length;t++) {
				result[t] *= factor;
			}
		}
		if (factorFrequency==1) {
			return result;
		}
		else {
			double[] output = new double[tableLength];
			for (int i=0;i<factorFrequency;++i) {
				for (int is=0;is<result.length;++is) {
					output[is+i*result.length] = result[is]; 
				}
			}
			return output;
		}
	}
	
	
	public static double[] interpolateWaves(double[][] inBuffers, boolean interpolate) {
		double[] result = new double[WaveRenderJob.WAVE_TABLE_LENGTH];
		double inc = inBuffers.length/(double)WaveRenderJob.WAVES_COUNT;
		for (int i=0;i<WaveRenderJob.WAVES_COUNT;i++) {
			int index = (int)(i*inc);
			int indexBaseOut = i* WaveRenderJob.WAVES_COUNT;
			if (interpolate) {
				double indexFrac = (i*inc)-index;
				double[] mix = new double[WaveRenderJob.WAVE_SAMPLE_COUNT];
				for (int m=0;m<WaveRenderJob.WAVE_SAMPLE_COUNT;++m) {
					double val = inBuffers[index][m];
					val += (inBuffers[(index+1)%inBuffers.length][m]-val)*indexFrac;
					mix[m] = val;
				}
				System.arraycopy(mix, 0, result, indexBaseOut, WaveRenderJob.WAVE_SAMPLE_COUNT);
			}
			else {
				System.arraycopy(inBuffers[index], 0, result, indexBaseOut, WaveRenderJob.WAVE_SAMPLE_COUNT);
			}
		}
		return result;
	}
	
	
	public static interface HarmonicAmplitudeFunction {
		public void init();
		public double ampFactor(int harmonicNo);
		public String description();
	}
	
	public static HarmonicAmplitudeFunction HarmonicsSine = new HarmonicAmplitudeFunction() {
		@Override
		public double ampFactor(int harmonicNo) {
			// sine
			if (harmonicNo==1) {
				return 1;
			};
			return 0;
		}

		@Override
		public void init() {}

		@Override
		public String description() {
			return "Sine";
		}
	};
	public static HarmonicAmplitudeFunction HarmonicsTriangle =  new HarmonicAmplitudeFunction() {
		private double sign = -1.0;
		@Override
		public double ampFactor(int harmonicNo) {
			if (harmonicNo%2==0) { 
				return 0;
			}
			// only odd numbers, alternating sign
			sign = -sign;
			return sign/(harmonicNo*harmonicNo);
		}
		@Override
		public void init() {
			sign = -1.0;
		}
		@Override
		public String description() {
			return "Triangle";
		}
	};
	public static HarmonicAmplitudeFunction HarmonicsSaw = new HarmonicAmplitudeFunction() {
		@Override
		public double ampFactor(int harmonicNo) {
			return 1.0/harmonicNo;
		}

		@Override
		public void init() {}

		@Override
		public String description() {
			return "Sawtooth";
		}
	};
	public static HarmonicAmplitudeFunction HarmonicsSquare = new HarmonicAmplitudeFunction() {
		@Override
		public double ampFactor(int harmonicNo) {
			if (harmonicNo%2==0) { 
				return 0;
			}
			// only odd numbers
			return 1.0/harmonicNo;
		}

		@Override
		public void init() {}

		@Override
		public String description() {
			return "Square";
		}
	};
	

	private static final double PI2 = Math.PI*2;
	public static final int TABLE_LENGTH = 256;
}
