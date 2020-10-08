package gsh.processing;

import gsh.processing.AudioEvent;
import gsh.processing.AudioProcessor;


public class PitchProcessor  implements AudioProcessor {
	
	/*
	  A list of pitch estimation algorithms.
	 */
	public enum PitchEstimationAlgorithm {
		/*
		 http://recherche.ircam.fr/equipes/pcm/cheveign/ps/2002_JASA_YIN_proof.pdf
		 */
		YIN;
		
		/*
		  Returns a new instance of a pitch detector object based on the provided values.
		  sampleRate The sample rate of the audio buffer.
		  bufferSize The size (in samples) of the audio buffer.
		  A new pitch detector object.
		*/
		public PitchDetector getDetector(float sampleRate,int bufferSize){
			PitchDetector detector;
			
				detector = new Yin(sampleRate, bufferSize);
			
			return detector;
		}
		
	};
	
	/*
		The underlying pitch detector;
	 */
	private final PitchDetector detector;
	
	private final PitchDetectionHandler handler;
	
	/**
	 * Initialize a new pitch processor.
	  algorithm: An enum defining the algorithm.
	  sampleRate : The sample rate of the buffer (Hz).
	 * @param bufferSize
	 *            The size of the buffer in samples.
	 * @param handler
	 *            The handler handles detected pitch.
	 */
	public PitchProcessor(PitchEstimationAlgorithm algorithm, float sampleRate,
			int bufferSize,
			PitchDetectionHandler handler) {
		detector = algorithm.getDetector(sampleRate, bufferSize);
		this.handler = handler;	
	}
	
	@Override
	public boolean process(AudioEvent audioEvent) {
		float[] audioFloatBuffer = audioEvent.getFloatBuffer();
		
		PitchDetectionResult result = detector.getPitch(audioFloatBuffer);
		
		
		handler.handlePitch(result,audioEvent);
		return true;
	}

	@Override
	public void processingFinished() {
	}
	
	

}
