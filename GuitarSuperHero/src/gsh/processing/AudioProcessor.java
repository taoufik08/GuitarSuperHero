package gsh.processing;


public interface AudioProcessor {
	
	/**
	 * Process the audio event. Do the actual signal processing on an
	 * (optionally) overlapping buffer.
	 * 
	 * @param audioEvent
	 *            The audio event that contains audio data.
	 * @return False if the chain needs to stop here, true otherwise. This can
	 *         be used to implement e.g. a silence detector.
	 */
    boolean process(AudioEvent audioEvent);

    /**
     * Notify the AudioProcessor that no more data is available and processing
     * has finished. Can be used to deallocate resources or cleanup.
     */
    void processingFinished();
    
}
