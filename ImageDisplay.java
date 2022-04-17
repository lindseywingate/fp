
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.zip.ZipEntry;

import javax.swing.*;


public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;
	int width = 512; // default image width and height
	int height = 512;
	String scale;
	int quantization;
	int mode;
	String filename;
	int quantArray[];
	boolean quantize = true;

	private void populateQuantUniform(int value) {
		System.out.println("QUANTIZING UNIFORMLY");
		double power = Math.pow(2, value);
		double step = 256/power;
		int intStep = (int) step;

		quantArray = new int[300];
		int count = 0;
		int x = 0;
		while(x<255) {
		 	quantArray[count] = x; 
		 	count++;
		 	x+=intStep;
		}
	}
	
	private void populateQuantLog(int pivot) {
		System.out.println("QUANTIZING LOGARITHMICALLY");
		double first[];
		double second[];

		double value = pivot;
		int exponent = 0;
		int count = 0;
		first = new double[20];

		while(value + Math.pow(2, exponent) < 255) {
			value = value + Math.pow(2, exponent);
			first[count] = value;
			exponent++;
			count++;
		}

		value = pivot;
		second = new double[20];
		exponent = 0;
		count = 0;
		while(value - Math.pow(2, exponent) > 0) {
			value = value - Math.pow(2, exponent);
			second[count] = value;
			exponent++;
			count++;
		}

		for(int c = 0; c<first.length; c++) {
			//System.out.println("FIRST INDEX " + c + "IS " + first[c]);
		}
		for(int c = 0; c<second.length; c++) {
			System.out.println("FIRST INDEX " + c + "IS " + second[c]);
		}

		int index = 1;
		quantArray = new int[30];
		quantArray[0] = 0;
		for(int x=second.length-1; x>-1; x--) {
			if(second[x] != 0.0) {
				quantArray[index] = (int) second[x];
				index++;
			}
		}

		quantArray[index] = pivot;
		index++;

		for(int y=0; y<first.length-1; y++) {
			if(first[y] != 0.0) {
				quantArray[index] = (int) first[y];
				index++;
			}
		}
		quantArray[index] = 255;

		// System.out.println("QUANT ARRAY \n");
		// for(int c = 0; c<quantArray.length; c++) {
		// 	System.out.println("FIRST INDEX " + c + "IS " + quantArray[c]);
		// }
	}

	private byte quantize(byte color) {
		int newColor = (color & 0xff);
		int repColor;   
		byte returnColor;                  

		for(int d=0; d<quantArray.length-2; d++) {
			if(newColor > quantArray[d] && newColor < quantArray[d+1]) {
				repColor = quantArray[d]+quantArray[d+1]/2;
				returnColor = (byte) repColor;
				return returnColor;
			}
		}
		return 0;
	}

	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	private void readAndScaleImageRGB(int width, int height, String imgPath, BufferedImage img, String scale, int quantization, int mode) {
		try {
			if(mode == -1 && quantization == 8) {
				quantize = false;
			}
			else if(mode == -1) {
				populateQuantUniform(quantization);
			} else {
				populateQuantLog(mode);
			}
			float testScale = Float.parseFloat(scale);
			float frameLength = width*height*3;

			File file = new File(imgPath);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(0);

			long len = (long) frameLength;
			byte[] bytes = new byte[(int) len];

			raf.read(bytes);

			int ind = 0;
			int h = 0;
			int pix;
			for(int y = 0; y < height; y++) {
				int w = 0;
				for(int x = 0; x < width; x++) {
					if(quantize) {
						byte a = 0;
						byte r = bytes[ind];
						byte qR = quantize(r);
						byte g = bytes[ind+height*width];
						byte qG = quantize(g);
						byte b = bytes[ind+height*width*2]; 
						byte qB = quantize(b);

						pix = 0xff000000 | ((qR & 0xff) << 16) | ((qG & 0xff) << 8) | (qB & 0xff);
					}
					else {
						byte a = 0;
						byte r = bytes[ind];
						byte g = bytes[ind+height*width];
						byte b = bytes[ind+height*width*2]; 
	
						pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					}

					//int pix = ((a << 24) + (qR << 16) + (qG << 8) + qB);
					float scaledWidth = x * testScale;
					int intScaledWidth = (int)scaledWidth;
					float scaledHeight = h * testScale;
					int intScaledHeight = (int)scaledHeight;



					img.setRGB(intScaledWidth,intScaledHeight,pix);
					ind++;
					w++;
				}
				h++;
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void showIms(String[] args){
		// Read a parameter from command line
		try {
			filename = args[0];
			scale = args[1];
			quantization = Integer.parseInt(args[2]);
			mode = Integer.parseInt(args[3]);
		} catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("Your command was missing an argument for scale, quantize, or mode. Please try again.\n");
			return;
		}

		float newScale = Float.parseFloat(scale);
		float scaledWidth = newScale * width;
		float scaledHeight = newScale * height;
		int intScaledWidth = (int) scaledWidth;
		int intScaledHeight = (int) scaledHeight;

		imgOne = new BufferedImage(intScaledWidth+1, intScaledHeight+1, BufferedImage.TYPE_INT_RGB);
		readAndScaleImageRGB(width, height, args[0], imgOne, scale, quantization, mode);

		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		lbIm1 = new JLabel(new ImageIcon(imgOne));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		ImageDisplay ren = new ImageDisplay();
		ren.showIms(args);
	}

}
