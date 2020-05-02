package Main;

import java.awt.Image;

import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;

public class Display {
	static final int screenWidth = 1920;
	static final int screenHeight = 1080;
	static final int displayDivide = 1;
	static int x,y = 0;
	
	public static void show(Mat image, String name) {
		Mat data = image;
		
		//positioning
		if(x+data.width()/displayDivide>screenWidth-40) {
			x=0;
			y+=data.height()/displayDivide+20;
		}
		 //hey 
		//resizeWindow() is destructive! wtf?
		String hashname = name+"  "+String.valueOf(Math.random());
		
		HighGui.imshow(hashname, data.clone());
		
		double resizeFactor = resizeFactor(image);
		HighGui.resizeWindow(name, (int)(image.width() * resizeFactor),
							(int)(image.height() * resizeFactor));

		HighGui.moveWindow(hashname, x, y);
		
		x+=data.width()/displayDivide+20;
	}
	
	public static double resizeFactor(Mat image) {
		return image.width()>image.height()?500.0/image.width():500.0/image.height();
	}
}
