
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opencv.calib3d.Calib3d;
import org.opencv.*;
import org.opencv.core.CvType;
import org.opencv.features2d.Feature2D;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;
import threads.PausableScheduledThreadPoolExecutor;
import threads.PausableExecutor;
import  org.wikijava.sound.playWave.*;


public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;
	int width = 480; // default image width and height
	int height = 270;
	ArrayList<BufferedImage> frames = new ArrayList<BufferedImage>();
	static boolean playing =true;
	JButton playButton = new JButton("> ||");
	Callable<Void> c1;
	Callable<Void> c2;
	PausableScheduledThreadPoolExecutor p1;
	List<Future<Void>> f;
	PlaySound playSound;

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



	public void showIms(String[] args) throws InterruptedException {
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, args[0], imgOne);
	}

	//https://www.macs.hw.ac.uk/cs/java-swing-guidebook/?name=JButton&page=2#:~:text=Event%20Listeners%20are%20the%20things,the%20occurrence%20of%20the%20event.
	public void runFrames() throws InterruptedException {
		System.out.println("video");
		frame = new JFrame();
		frame.setSize(500,500);
//		frame.setLayout(new BoxLayout(frame, BoxLayout.Y_AXIS));
		//JButton playButton = new JButton("> ||");

		// Listener for play/pause button
		playButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(playing) {
					playing = false;
					playSound.pause();
//


				}
				else {
					playing = true;
					playSound.resume();
//

				}
			}
		});
		playButton.setLocation(275, 500);
		playButton.setSize(40, 30);
		JLabel label = new JLabel(new ImageIcon(imgOne));
		frame.add(label);
		frame.getContentPane().add(playButton);

		frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setVisible(true);
		System.out.println(frames.size());
		for(int n=0; n<frames.size();) {
			if(playing) {
				frame.getContentPane().remove(label);
				label = new JLabel(new ImageIcon(frames.get(n)));
				frame.add(label);
				frame.invalidate();
				frame.validate();
				frame.repaint();
				Thread.sleep(32, 333333);
				n++;
				//System.out.println("playing");
			}
			else{
				System.out.println("pause");
				continue;
			}
		}
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		 /*Process p = Runtime.getRuntime().exec("python Part_2.py");
		BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		System.out.println(in.readLine());
		File file = new File(".");
		File [] files = file.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".avi");
			}
		});*/
		
		ImageDisplay ren = new ImageDisplay();
		ren.showIms(args);
		ren.c1 = new Callable<Void>()
		{
			@Override
			public Void call() throws Exception
			{

					ren.runFrames();

				return null;
			}
		};
		ren.c2 = new Callable<Void>()
		{
			@Override
			public Void call() throws Exception
			{

				String filename = args[1];

				// opens the inputStream
				FileInputStream inputStream = null;
				try {
					inputStream = new FileInputStream(filename);
					//inputStream = this.getClass().getResourceAsStream(filename);
				} catch (FileNotFoundException e) {
					e.printStackTrace();

				}

				// initializes the playSound Object
				ren.playSound = new PlaySound(inputStream);

				// plays the sound
				try {
					ren.playSound.play();
				} catch (PlayWaveException e) {
					e.printStackTrace();

				}


				return null;
			}
		};
		List<Callable<Void>> taskList = new ArrayList<>();
		taskList.add(ren.c1);
		taskList.add(ren.c2);
		ren.p1 = new PausableScheduledThreadPoolExecutor(3);


		ren.f = ren.p1.invokeAll(taskList);
		ren.p1.shutdown();

	}

}

