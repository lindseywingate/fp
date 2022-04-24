package org.wikijava.sound.playWave;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;
/**
 *
 * <Replace this with a short description of the class.>
 *
 * @author Giulio
 */
public class PlaySound {

    private InputStream waveStream;

    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb

    /**
     * CONSTRUCTOR
     */
    public PlaySound(InputStream waveStream) {
        this.waveStream = waveStream;
    }

    public void play() throws PlayWaveException {

        AudioInputStream audioInputStream = null;
        try {
            //audioInputStream = AudioSystem.getAudioInputStream(this.waveStream);

            //add buffer for mark/reset support, modified by Jian
            InputStream bufferedIn = new BufferedInputStream(this.waveStream);
            audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
            long frameLength = audioInputStream.getFrameLength();
            System.out.println("frame length " + frameLength);

        } catch (UnsupportedAudioFileException e1) {
            throw new PlayWaveException(e1);
        } catch (IOException e1) {
            throw new PlayWaveException(e1);
        }

        // Obtain the information about the AudioInputStream
        AudioFormat audioFormat = audioInputStream.getFormat();
        System.out.println("audio format"+ audioFormat);
        Info info = new Info(SourceDataLine.class, audioFormat);
        System.out.println("sound data: "+audioFormat.getSampleRate());
        //48kHz, 1 channel, 16 sample size, 2 frame size, 48kHz frame rate
        ///48000 samples per second
        //take length in seconds * samples per second to get total samples
        //https://softwareengineering.stackexchange.com/questions/136215/how-to-determine-frequency-in-hertz-real-time-with-java-sound
        //FFT/FT
        //https://www.developer.com/java/fun-with-java-understanding-the-fast-fourier-transform-fft-algorithm/

        // opens the audio channel
        SourceDataLine dataLine = null;
        try {
            dataLine = (SourceDataLine) AudioSystem.getLine(info);
            dataLine.open(audioFormat, this.EXTERNAL_BUFFER_SIZE);
        } catch (LineUnavailableException e1) {
            throw new PlayWaveException(e1);
        }

        // Starts the music :P
        dataLine.start();

        int readBytes = 0;
        byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];

        try {
            while (readBytes != -1) {
                double amplitude = 0;
                readBytes = audioInputStream.read(audioBuffer, 0,
                        audioBuffer.length);
                for (int i = 0; i < audioBuffer.length/2; i++) {
                    double y = (audioBuffer[i*2] | audioBuffer[i*2+1] << 8) / 32768.0;
                    amplitude += Math.abs(y);
                }
                amplitude = amplitude / audioBuffer.length / 2;
                System.out.println(amplitude);
                //System.out.println("microseconds "+dataLine.getMicrosecondPosition());
                //System.out.println("frame position "+dataLine.getFramePosition());
                //System.out.println("get level "+ dataLine.getLevel());
                if (readBytes >= 0){
                    //clone byte array
                    byte[] clone = audioBuffer.clone();
                    //System.out.println(audioBuffer[clone.length-1]);
                    //temp holder for first spot in bytes
                    byte temp = clone[0];
                    //end holder for last spot in bytes
                    byte end = clone[clone.length-1];
                    //removing section from cloned array
                    clone[clone.length-1] = temp;
                    clone[0] = end;
                    dataLine.write(audioBuffer, 0, readBytes);
                }
            }
        } catch (IOException e1) {
            throw new PlayWaveException(e1);
        } finally {
            // plays what's left and and closes the audioChannel
            dataLine.drain();
            dataLine.close();
        }

    }
}
