package gsh.processing;

import gsh.processing.AudioEvent;


public interface PitchDetectionHandler {
	/**
	 * Handle a detected pitch. 
	 */
	void handlePitch(PitchDetectionResult pitchDetectionResult,AudioEvent audioEvent);

}
