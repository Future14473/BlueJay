package Main;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Processing {
	enum channel{
		HUE,
		SATURATION,
		VALUE
	}

	Mat loadSource(String uri) {
		Mat input = Imgcodecs.imread(uri);
		double resizeFactor = Display.resizeFactor(input);
		Imgproc.resize(input, input, new Size(input.cols() * resizeFactor, input.rows() * resizeFactor));
		return input;
	}
	
	void display(Mat image, String winname) {		
		Display.show(image, winname);
	}
	
	void info(Mat m) {
		System.out.format("Mat width: %d height: %d channels: %d depth: %d type:%d\n",m.width(),m.height(),m.channels(),m.depth(),m.type());
	}
	
	List<MatOfPoint> ColorBlobSearch(Mat input, color color) {
		api.bumpZeroHue(input);
					
		//rid of darks
		Mat mask = new Mat();
		api.allow(input,color.allowedRangeLower, color.allowedRangeUpper);

		//actual Main.color match operation
		Mat keyRegionS = api.MatOf(input, false, false, 1);
		Mat keyRegionV = api.MatOf(input, false, false, 1);
		Mat keyRegionH = api.MatOf(input, false, false, 1);
		Core.bitwise_not(keyRegionH, keyRegionH);
		Core.bitwise_not(keyRegionS, keyRegionS);
		Core.bitwise_not(keyRegionV, keyRegionV);

		if(color.val != -1) {
			keyRegionV = regionsNear(input, channel.VALUE.ordinal(), 255, (int) color.hardRange.val[2], color);
			api.dilate(keyRegionV, 2);
		}
		if(color.sat != -1) {
			keyRegionH = regionsNear(input, channel.HUE.ordinal(), color.hue, (int) color.hardRange.val[0], color);
			api.dilate(keyRegionH, 2);
		}
		if(color.hue != -1) {
			keyRegionS = regionsNear(input, channel.SATURATION.ordinal(), color.sat, (int) color.hardRange.val[1], color);
			api.dilate(keyRegionS, 2);
		}

		//combine
		Mat keyRegion = api.MatOf(input, false, false, 1);
		Core.bitwise_and(keyRegionH, keyRegionS, keyRegion);
		Core.bitwise_and(keyRegion, keyRegionV, keyRegion);

				//display(keyRegion, "combined fin");

		//find big enough areas
		var contours = api.contours(keyRegion, true);
		api.sortContours(contours);

		return contours;
	}
	
	Mat regionsNear(Mat input, int channel, int ideal, color color) {
		return regionsNear(input, channel, ideal, 30, color);
	}
	Mat regionsNear(Mat input, int channel, int ideal, int hardRange, color color) {
		//distance to Color
		Mat difference = api.distanceBetween(api.getChannelHSV(input, channel), ideal);
			
			display(difference, "distance from");
		
		//don't give a shit if >30 diff. it's different Main.color
		Mat inRange = new Mat();
		if(hardRange==255)Core.inRange(difference, new Scalar(0), new Scalar(hardRange), inRange);
		
			//display(inRange, "in  30");
		
		//find places that are close enough
		api.binarize(difference, color.standoutness);
		Core.bitwise_not(difference, difference);	
			
			//display(difference, "binarize");
		
		if(hardRange==255)Core.bitwise_and(difference, inRange, difference);
		
		return difference;
	}
	
	List<MatOfPoint> subDivide(Mat input, List<MatOfPoint> contours) {
		
		//we only want the shape
		Mat keyRegion = api.leaveOnly(input, contours);
				
			//display(keyRegion, "what we want to subdivide");
		
		//love this function. Preserves edges and flattens unicolor surfaces
		Imgproc.medianBlur(keyRegion, keyRegion, 3);
		
		//put some gaps between edges
		Mat pieces = borderCut(keyRegion);
		
			//display(pieces, "pieces");
		
		//strict and absolute separation
		var islands = isolate(pieces);
		api.sortContours(islands);
		
		return islands;
	}
	
	Mat borderCut(Mat input) {
		var mask = api.MatOf(input);
		List<Mat> channels = new ArrayList<>();
		Core.split(input.clone(), channels);
		
		for(Mat chan: channels) {
			api.edge(chan);
		}
		
		Core.merge(channels, mask);
		
		api.toGray(mask);
		
		api.binarize(mask, 100);
		
			display(mask, "edges");
		
		var difference = api.subtract(input, api.toColor(mask));
		api.erode(difference, 7);
		
		return difference;
	}
	
	List<MatOfPoint> isolate(Mat input) {
		
		api.toGray(input);
		Imgproc.threshold(input, input, 0, 255, CvType.CV_8UC1);
		input = api.oneChannel(input);

		var distance = new Mat();
		Imgproc.distanceTransform(input, distance, Imgproc.DIST_C, 3);
		Core.convertScaleAbs(distance, distance);
		Core.multiply(distance, new Scalar(9), distance);
				
		Core.inRange(distance, new Scalar(20), new Scalar(255), distance);
		
			display(distance, "distance transform islands");

		return api.contours(distance, true);
		
	}
	
	void end() {
		HighGui.waitKey(0);
		System.exit(0);
	}
}
