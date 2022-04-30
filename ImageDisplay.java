
import java.awt.event.ActionEvent;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import  org.wikijava.sound.playWave.*;


public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;
	int width = 480; // default image width and height
	int height = 270;
	ArrayList<BufferedImage> frames = new ArrayList<BufferedImage>();
	static boolean playing;
	JButton playButton = new JButton("Click Here");

	//6291456 - bytes in each frame
	//9000 frames in video

	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	private void readImageRGB(int width, int height, String imgPath, BufferedImage img)
	{
		try
		{
			//three bytes per pixel.
			int frameLength = width*height*3;

			File rgbFile = new File(imgPath);
			StringBuffer buffer = new StringBuffer();
			RandomAccessFile raf = new RandomAccessFile(rgbFile, "rw");

			int i = 0;
			// //counter for the pointer position
			// raf.seek(0);
			long len = frameLength;
			// //creates byte array for pixels to be stored
			byte[] bytes = new byte[(int) len];
			// //reads bytes into the raf buffer
			// raf.read(bytes);

			long sizeOfFile = rgbFile.length();
			System.out.println("size of file length "+sizeOfFile);
			long startTime = System.nanoTime();
			for(long k = 0; k<sizeOfFile; k += frameLength) {
				raf.seek(k);
				raf.read(bytes);
				img = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
				int ind = 0;
				for(int y = 0; y < height; y++)
				{
					for(int x = 0; x < width; x++)
					{
						byte a = 0;
						byte r = bytes[ind];
						byte g = bytes[ind+height*width];
						byte b = bytes[ind+height*width*2]; 
	
						int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
						img.setRGB(x,y,pix);
						ind++;
					}
				}
				frames.add(img);
			}
			long endTime = System.nanoTime();
			long timeElapsed = endTime - startTime;
			System.out.println("Execution time in nanoseconds: " + timeElapsed);
			System.out.println("Execution time in milliseconds: " + timeElapsed / 1000000);
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e /*| InterruptedException e*/)
		{
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e) {
		//if playbutton clicked and playing is true
		if(e.getSource() == playButton && playing == true)
		{
			//Thread.timeout();
			//pause
		}
		else if (playing == false) {
			//clear
		}
	}

	public void showIms(String[] args) throws InterruptedException {
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, args[0], imgOne);
	}

//https://www.macs.hw.ac.uk/cs/java-swing-guidebook/?name=JButton&page=2#:~:text=Event%20Listeners%20are%20the%20things,the%20occurrence%20of%20the%20event.
	public void runFrames() throws InterruptedException {
		System.out.println("video");
		frame = new JFrame();
		JButton playButton = new JButton("> ||");
		playButton.addActionListener(this);

		frame.add(new JLabel(new ImageIcon(imgOne)));
		frame.add(playButton);

		frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setVisible(true);

		for(int n=0; n<frames.size(); n++) {
			frame.getContentPane().removeAll();
			frame.add(new JLabel(new ImageIcon(frames.get(n))));
			frame.invalidate();
			frame.validate();
			frame.repaint();
			Thread.sleep(32,333333);
		}
	}

	public static void main(String[] args) throws InterruptedException {
		ImageDisplay ren = new ImageDisplay();
		ren.showIms(args);
		Callable<Void> callable1 = new Callable<Void>()
		{
			@Override
			public Void call() throws Exception
			{
				if(playing) {
					ren.runFrames();
				}
				return null;
			}
		};
		Callable<Void> callable2 = new Callable<Void>()
		{
			@Override
			public Void call() throws Exception
			{
				if(playing) {
					PlayWaveFile.main(args);
				}

				return null;
			}
		};
		List<Callable<Void>> taskList = new ArrayList<>();
		taskList.add(callable1);
		//taskList.add(callable2);
		ExecutorService executor = Executors.newFixedThreadPool(3);
		executor.invokeAll(taskList);
	}

}
