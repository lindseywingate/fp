package org.wikijava.sound.playWave;



import java.io.*;
import java.util.*;

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
	public boolean playing = true;

    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb

    /**
     * CONSTRUCTOR
     */
    public PlaySound(InputStream waveStream) {
	this.waveStream = waveStream;
    }

    public void play() throws PlayWaveException, IOException {
		HashMap<String,Integer> adFrame = new LinkedHashMap<>();
		BufferedReader br = new BufferedReader(new FileReader("ad_times.txt"));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				String[] adTime = line.split(" ");
				adFrame.put(adTime[0], Integer.parseInt(adTime[1])/30);
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			String everything = sb.toString();
			System.out.println(everything);
		} finally {
			br.close();
		}



	AudioInputStream audioInputStream = null;
		AudioInputStream audioInputStream2 = null;
		ArrayList<AudioInputStream> audioList = new ArrayList<>();
	try {
		File f = new File("dataset/Ads/");

		for (Map.Entry<String,Integer> entry:
				adFrame.entrySet()) {
			File[] files = f.listFiles((dir1, name) -> name.startsWith(entry.getKey()) && name.endsWith(".wav"));
			InputStream I = new BufferedInputStream(new FileInputStream(files[0]));
			audioList.add(AudioSystem.getAudioInputStream(I));
		}
	    //audioInputStream = AudioSystem.getAudioInputStream(this.waveStream);
		
		//add buffer for mark/reset support, modified by Jian
		InputStream bufferedIn = new BufferedInputStream(this.waveStream);
	    audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
		InputStream bufferedIn2 = new BufferedInputStream(new FileInputStream("dataset/Ads/Starbucks_Ad_15s.wav"));
		audioInputStream2 = AudioSystem.getAudioInputStream(bufferedIn2);
	} catch (UnsupportedAudioFileException e1) {
	    throw new PlayWaveException(e1);
	} catch (IOException e1) {
	    throw new PlayWaveException(e1);
	}

	// Obtain the information about the AudioInputStream
	AudioFormat audioFormat = audioInputStream.getFormat();
	Info info = new Info(SourceDataLine.class, audioFormat);
	float sample = audioFormat.getSampleRate();
	int bitDepth = audioFormat.getSampleSizeInBits();
	int channel = audioFormat.getChannels();
	int bitRate = (int) sample*bitDepth*channel;
	int byteRate = bitRate/8;
	int framelength = byteRate;

	// opens the audio channel
	SourceDataLine dataLine = null;
	try {
	    dataLine = (SourceDataLine) AudioSystem.getLine(info);
	   // dataLine.open(audioFormat, this.EXTERNAL_BUFFER_SIZE);
		dataLine.open(audioFormat, byteRate);
	} catch (LineUnavailableException e1) {
	    throw new PlayWaveException(e1);
	}

	// Starts the music :P
	dataLine.start();

	int readBytes = 0;
	int readBytes2 =0;
	//byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];
	byte[] audioBuffer = new byte[framelength];
	byte[] audioBuffer2 = new byte[framelength];
		int count = 0;
		Iterator<Map.Entry<String,Integer>> adIterator = adFrame.entrySet().iterator();
		Iterator<AudioInputStream> audioIterator = audioList.iterator();
		AudioInputStream adAudio = audioIterator.next();
		Map.Entry<String,Integer> adTime = adIterator.next();
	try {
	    while (readBytes != -1) {
		if(playing) {
			readBytes = audioInputStream.read(audioBuffer, 0,
					audioBuffer.length);


			double amplitude = 0;

			for (int i = 0; i < audioBuffer.length / 2; i++) {
				double y = (audioBuffer[i * 2] | audioBuffer[i * 2 + 1] << 8) / 32768.0;
				// depending on your endianness:
				//double y = (audioBuffer[i*2]<<8 | audioBuffer[i*2+1]) / 32768.0;
				amplitude += Math.abs(y);
			}
			amplitude = amplitude / audioBuffer.length / 2;
			//System.out.println(amplitude);
			if (readBytes >= 0) {
				byte[] clone = audioBuffer.clone();
				//Arrays.sort(clone);
				//System.out.println(audioBuffer[clone.length-1]);
				byte temp = clone[0];
				byte end = clone[clone.length - 1];
				clone[clone.length - 1] = temp;
				clone[0] = end;

				if ((count >= adTime.getValue() && count < adTime.getValue() + 15) ) {

					readBytes2 = adAudio.read(audioBuffer2, 0,
							audioBuffer2.length);
					System.out.println(readBytes2);
					dataLine.write(audioBuffer2, 0, readBytes2);
					if(count == adTime.getValue() + 14){
						if(adIterator.hasNext()){
							adTime = adIterator.next();
							adAudio = audioIterator.next();
						}
					}

				} else {

					dataLine.write(audioBuffer, 0, readBytes);
				}
				System.out.println(count + " s");
				count++;

			}
		}
		else{
			System.out.println("sound paused");
			continue;
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
	public void pause(){
		playing = false;
	}
	public void resume(){
		playing = true;
	}
}
