package gsh.processing;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;

import gsh.processing.GshAudioFormat;
import gsh.processing.GshAudioInputStream;


/**
 * Encapsulates an AudioInputStream to make it work with the library.

 */
public class EncapsAudioInputStream implements GshAudioInputStream {
	
	private final AudioInputStream underlyingStream;
	private final GshAudioFormat gshAudioFormat;
	public EncapsAudioInputStream(AudioInputStream stream){
		this.underlyingStream = stream;
		this.gshAudioFormat = EncapsAudioInputStream.toGshDSPFormat(stream.getFormat());
	}

	@Override
	public long skip(long bytesToSkip) throws IOException {
		return underlyingStream.skip(bytesToSkip);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return underlyingStream.read(b, off, len);
	}

	@Override
	public void close() throws IOException {
		underlyingStream.close();
	}

	@Override
	public long getFrameLength() {

		return underlyingStream.getFrameLength();
	}

	@Override
	public GshAudioFormat getFormat() {
		return gshAudioFormat;
	}
	
	/**
	 * Converts a AudioFormat to a {@link TarsosDSPAudioFormat}.
	 * 
	 * @param format
	 *            The AudioFormat
	 * @return A GshAudioFormat
	 */
	public static GshAudioFormat toGshDSPFormat(AudioFormat format) {
		boolean isSigned = format.getEncoding() == Encoding.PCM_SIGNED;
		GshAudioFormat gshDSPFormat = new GshAudioFormat(format.getSampleRate(), format.getSampleSizeInBits(), format.getChannels(), isSigned, format.isBigEndian());
		return gshDSPFormat;
	}

	/**
	 * Converts a GshAudioFormat to a {@link AudioFormat}.
	 * 
	 * @param format
	 *            The GshAudioFormat
	 * @return A GshAudioFormat
	 */
	public static AudioFormat toAudioFormat(GshAudioFormat format) {
		boolean isSigned = format.getEncoding() == GshAudioFormat.Encoding.PCM_SIGNED;
		AudioFormat audioFormat = new AudioFormat(format.getSampleRate(), format.getSampleSizeInBits(), format.getChannels(), isSigned, format.isBigEndian());
		return audioFormat;
	}
	
}
