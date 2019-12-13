package org.futurerobotics.bluejay.original.detectors.foundation;

import org.opencv.core.Mat;

public class Constants {
	public static final double[] blueColor1 = {170, 180};
	public static final double[] blueColor2 = {0, 10};
	
	public static final double[] redColor = {110, 120};
	
	public static final double[] yellowColor = {7 + 67, 38 + 67};
	public static final double[] stressedYellowColor = {5 + 67, 40 + 67};
	public static Mat redOutput = new Mat();
	public static Mat blueOutput = new Mat();
	public static Mat blackOutput = new Mat();
	public static Mat yellowOutput = new Mat();
	public static Mat yellowTags = new Mat();
	
	public static void updateColors(Mat resizedImage, Mat equalizedImage, double blackCutOff) {
        /*
        //For yellow
        double[] yellowRange = {73,86};

        //For Blue
        double[] blueRange1 = {160,180};
        double[] blueRange2 = {0,20};

        //for Red
        double[] redRange = {40,63};
        */
		
		double[] satRange = {80, 255};
		double[] valRange = {blackCutOff * 0.7, 255};
		
		redOutput = Compute.threshold(resizedImage, redColor, satRange, valRange);
		
		blueOutput = Compute.combine(
				Compute.threshold(resizedImage, blueColor1, satRange, valRange),
				Compute.threshold(resizedImage, blueColor2, satRange, valRange));
		
		blackOutput = Compute.threshold(
				resizedImage,
				new double[]{0, 255},//hue  0, 180
				new double[]{0, 180},//sat  0, 180
				new double[]{0, blackCutOff * 0.8});//val
		
		//For yellow HUE HUE HUE
		
		//STONE STONE STONE
		yellowOutput = Compute.threshold( //equalize to spread. yellowRange to be less
				equalizedImage,
				yellowColor,
				new double[]{100, 255},//sat
				new double[]{blackCutOff * 1.0, 255}); //val
		
		//SKYSTONE SKYSTONE
		yellowTags = Compute.threshold(  //just want all of it
				resizedImage,
				stressedYellowColor,
				new double[]{190, 255},//sat
				new double[]{blackCutOff * 1.9, 255}); //val
		//        Mat yellowTags = compute.threshold(
//                resizedImage,
//                new double[]{80,105},
//                new double[]{100, 255},//sat
//                new double[]{blackCutOff*1.5, 255}); //val
	
	}
}
