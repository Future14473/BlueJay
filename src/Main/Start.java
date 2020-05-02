package Main;

import org.opencv.core.Core;
import org.opencv.core.Scalar;

public class Start {
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		Processing processing = new Processing();
		
		//var input = processing.loadSource("D:\\media\\Pictures\\shet\\9.jpg");
		var input = processing.loadSource("D:\\media\\Robotics\\minerals\\dither.jpg");
			
			processing.display(input, "input");
			
		//find a continuous solid Main.color shape
		var blobs = processing.ColorBlobSearch(input.clone(), color.YELLOW);

		//divide this shape into sub chunks
		//blobs = processing.subDivide(input.clone(), blobs);
		
		api.drawContours(input, blobs, new Scalar(255,50,100));
		
		processing.display(input, "output");
		processing.end();
	}
}
