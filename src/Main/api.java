package Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


public class api {
	static final Scalar white = new Scalar(255,255,255);
	
	public static Mat edge(Mat input) {		
		var X = api.MatOf(input);
		var Y = api.MatOf(input);
		
		var edge = new Mat();
		Imgproc.Scharr(input, edge, CvType.CV_16S, 1, 0);
		Core.convertScaleAbs(edge, X);
		
		Imgproc.Scharr(input, edge, CvType.CV_16S, 0, 1);
		Core.convertScaleAbs(edge, Y);
		
		Core.addWeighted(X, 0.5, Y, 0.8, 0, input);
		
		return input;
	}
	
	public static List<MatOfPoint> contours(Mat input, boolean externalOnly) {
		if(input.channels()!=1) {
			System.out.println("ur image sucks. too many channels");
			return null;
		}
		
		List<MatOfPoint> contours = new ArrayList<>();
		
		var mode = externalOnly?Imgproc.RETR_EXTERNAL:Imgproc.RETR_LIST;
		var inputMethod = Imgproc.CHAIN_APPROX_SIMPLE;
		Imgproc.findContours(input, contours, new Mat(), mode, inputMethod);
		
		return contours;
	}

	public static void drawContours(Mat input, List<MatOfPoint> contours) {
		drawContours(input, contours, new Scalar(2100, 150, 255));
	}
	
	public static void drawContours(Mat input, List<MatOfPoint> contours, Scalar color) {		
		for (int i = 0; i < contours.size(); i++) {
			Imgproc.drawContours(input, contours, i, color, -1);
		}
	}
	
	public static void drawOutlines(Mat input, List<MatOfPoint> contours) {		
		for (int i = 0; i < contours.size(); i++) {
			Imgproc.drawContours(input, contours, i, white, 1);
		}
	}
	
	public static Mat leaveOnly(Mat input, List<MatOfPoint> contours) {
		//where the borders will be seen
		var mask = api.MatOf(input);
		
		//draw a filled contour and flip
		api.drawContours(mask, contours, api.white);
		api.invert(mask);
		
		//the final image
		var ret = input.clone();
		//remove outer image from original photo
		Core.subtract(input, mask, ret);
		
		return ret;
	}
	
	public static void sortContours(List<MatOfPoint> contours) {
		if (contours == null) return;

		for(int i=0;i<contours.size();i++) {
			double area = Imgproc.contourArea(contours.get(i));
			double perimeter = Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true);
			if(!(area > 100 && (perimeter*perimeter)/area < 10000)) {//big enough, short enough
				contours.remove(i);
				i--;
			}
		}
	}
	
	public static void translate(Mat input, int x, int y) {
		Mat dst = new Mat();
		
		Point[] inputTri = new Point[3];
	        inputTri[0] = new Point( 0, 0 );
	        inputTri[1] = new Point( input.cols() - x, 0 );
	        inputTri[2] = new Point( input.cols() - x, input.rows() + y);
        Point[] dstTri = new Point[3];
	        dstTri[0] = new Point( x, -y );
	        dstTri[1] = new Point( input.cols() - 1, -y );
	        dstTri[2] = new Point( input.cols() - 1, input.rows() - 1);
	        
	    
	    Mat warpMat = Imgproc.getAffineTransform(new MatOfPoint2f(inputTri), new MatOfPoint2f(dstTri));
	    Imgproc.warpAffine(input, input, warpMat, input.size() );
	        
	}
	
	public static void resize(Mat input, double ratio) {
		Imgproc.resize(input, input, new Size(input.width()*ratio,input.height()*ratio));
	}
	
	public static Mat subtract(Mat one, Mat two) {
		Mat ret = new Mat();
		Core.subtract(one, two, ret);
		return ret;
	}
	
	public static Mat add(Mat one, Mat two) {
		Mat ret = new Mat();
		Core.add(one, two, ret);
		return ret;
	}
	
	public static void invert(Mat input) {
		Core.bitwise_not(input, input);
	}
	
	public static void binarize(Mat input, int selectiveness, int size) {//140	
		Imgproc.adaptiveThreshold(input, input, 255, 
				Imgproc.ADAPTIVE_THRESH_MEAN_C, 
				Imgproc.THRESH_BINARY, 
				size, //ksize
				-selectiveness); //raise threshold
	}
	
	public static void binarizeGaussian(Mat input, int selectiveness, int size) {//140	
		Imgproc.adaptiveThreshold(input, input, 255, 
				Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, 
				Imgproc.THRESH_BINARY, 
				size, //ksize
				-selectiveness); //raise threshold
	}
	
	public static void binarize(Mat input, int selectiveness) {//140	
		binarize(input, selectiveness, 601);
	}
	public static void erode(Mat input, int power) {//3
		var kernel = Mat.ones(power, 1, CvType.CV_32F);
		
		Imgproc.morphologyEx(input, input, Imgproc.MORPH_OPEN, kernel);
	}
	public static void dilate(Mat input, int power) {
		var kernel = Mat.ones(power, power, CvType.CV_32F);
		
		Imgproc.morphologyEx(input, input, Imgproc.MORPH_DILATE, kernel);
	}
	public static void dilateHoro(Mat input, int power) {
		var kernel = Mat.ones(1, power, CvType.CV_32F);
		
		Imgproc.morphologyEx(input, input, Imgproc.MORPH_DILATE, kernel);
	}
	public static void dilateVert(Mat input, int power) {
		var kernel = Mat.ones(power, 2, CvType.CV_32F);
		
		//Oh God the anchor coords are reversed
		Imgproc.morphologyEx(input, input, Imgproc.MORPH_DILATE, kernel,new Point(0,power*0.5));
	}
	
	public static void equalize(Mat input) {
		Imgproc.equalizeHist(input, input);
	}
	
	public static void blur(Mat input, int size) {
		//Imgproc.GaussianBlur(input, input, new Size(dx,dy), 0, 0, Core.BORDER_DEFAULT );

		Imgproc.medianBlur(input, input, size );
	}
	
	public static Mat toGray(Mat input) {
		Imgproc.cvtColor(input, input, Imgproc.COLOR_RGB2GRAY);
		return input;
	}
	public static Mat toColor(Mat input) {
		Imgproc.cvtColor(input, input, Imgproc.COLOR_GRAY2BGR);
		return input;
	}
	
	public static Mat toHSV(Mat input) {
		Imgproc.cvtColor(input, input, Imgproc.COLOR_BGR2HSV);
		return input;
	}
	
	public static Mat toBGR(Mat input) {
		Imgproc.cvtColor(input, input, Imgproc.COLOR_HSV2BGR);
		return input;
	}
	
	public static Point tl() {
		return new Point(0,0);
	}
	
	public static Point br(Mat input) {
		return new Point(input.cols(), input.rows());
	}
	
	public static Mat MatOf(Mat input) {
		return new Mat(input.size(),input.type());
	}
	
	public static Mat MatOf(Mat input, boolean signed, boolean big,int channel) {
		return new Mat(input.rows(),input.cols(),
				big?
					signed?
						CvType.CV_16SC(channel):
						CvType.CV_16UC(channel):
					signed?
						CvType.CV_8SC(channel):
						CvType.CV_8UC(channel));
	}
	
	public static Mat oneChannel(Mat in) {
		List<Mat> paths = new ArrayList<>();
		Core.split(in, paths);
		
		return paths.get(0);
	}
	
	public static Mat getChannelHSV(Mat input, int channel) {
		List<Mat> channels = new ArrayList<>();
		Core.split(api.toHSV(input.clone()), channels);
		
		return channels.get(channel);
	}
	
	public static Mat distanceBetween(Mat input, int idealVal) {		
		
		//distance from Hue channel to Color
		Mat difference = api.MatOf(input,false, true, 1);
		Core.absdiff(input, new Scalar(idealVal), difference);
		
		//Main.Display.show(difference, "diff");
		
		return difference;
	}
	
	//Soooo we are kinda hijacking this for the HSV flip
	//instead of flipping from 0 to 255 we need 0 to 180
	//so.... this function is gonna be Mutay-TeD!!!!
	public static void invertZeroes(Mat input) {
		Mat blacks = new Mat();
		Core.inRange(input, new Scalar(0), new Scalar(1), blacks);
		Core.multiply(blacks, new Scalar(178.0/255), blacks);
		Core.add(input, blacks, input);
	}
	
	public static void allow(Mat input, Scalar low, Scalar high) {
		Mat bastard = new Mat();
		Core.inRange(toHSV(input.clone()), low, high, bastard);

		Core.bitwise_not(bastard, bastard);
		api.toColor(bastard);
		Core.subtract(input, bastard, input);
	}

	public static void bumpZeroHue(Mat input) {
		//change all 0s of H channel to 255
		Mat Hinvert = api.getChannelHSV(input, 0);
		api.invertZeroes(Hinvert);
		Core.merge(new ArrayList<>(
						Arrays.asList(Hinvert,
								api.getChannelHSV(input, 1),
								api.getChannelHSV(input, 2))),
				input);
		api.toBGR(input);
	}
}
