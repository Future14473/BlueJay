package org.futurerobotics.bluejay.original.detectors.foundation;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public final class FoundationPipeline {
	
	private static final int regionSideClipExtensionLength = 120; //120
	
	public Mat resizedImage = new Mat();
	public List<Foundation> foundations = new ArrayList<>();
	public List<Stone> stones = new ArrayList<>();
	public List<SkyStone> skyStones = new ArrayList<>();
	
	//debug steps
	public Mat red = new Mat();
	public Mat blue = new Mat();
	public Mat yellow = new Mat();
	public Mat black = new Mat();
	public int blackcut = 0;
	
	public boolean doFoundations = true;
	public boolean doStones = true;
	public boolean doSkyStones = true;
	
	public Mat process(Mat source) {
		return process(source, 640, 480);
		//return process(source, 3264/4, 2448/4);
	}
	
	/**
	 * Give it the raw image and it will update the Foundations arrayList
	 *
	 * @return source image with annotations on it
	 */
	public Mat process(Mat source0, double width, double height) {
//        System.gc();
//        System.runFinalization();
		
		//default image size: 3264 x 2448
		Imgproc.resize(source0, resizedImage, new Size(width * 1, height * 1), 0.0, 0.0, Imgproc.INTER_LINEAR);
		
		Mat equalizedImage = Compute.equalize(resizedImage);
		
		//compute.whiteBalance(resizedImage, 1.15,0.9);
		
		Mat original = resizedImage.clone();
		
		//set ranges
		double blackCutOff = Compute.getHistogramfast(resizedImage);
		blackcut = (int) blackCutOff;
		blackcut = 85;
		
		Constants.updateColors(resizedImage, equalizedImage, blackCutOff);
		
		Mat redOutput = Constants.redOutput;
		Mat blueOutput = Constants.blueOutput;
		Mat blackOutput = Constants.blackOutput;
		Mat yellowOutput = Constants.yellowOutput;
		Mat yellowTags = Constants.yellowTags;
		
		//For debug display
		red = redOutput.clone();
		blue = blueOutput.clone();
		yellow = yellowOutput.clone();
		black = blackOutput.clone();
		
		skyStones.clear();
		stones.clear();
		foundations.clear();
		
		if (doSkyStones) skyStones = computeSkyStones(yellowTags);
		if (doStones) stones = computeStones(yellowOutput);
		if (doFoundations) foundations = computeFoundations(redOutput, blueOutput, yellowOutput, blackOutput,
				original);
		
		for (Stone s : stones) {
			s.draw(original);
		}
		for (Foundation f : foundations) {
			f.draw(original);
		}
		for (SkyStone s : skyStones) {
			s.draw(original);
		}
		
		redOutput.release();
		blueOutput.release();
		yellowOutput.release();
		blackOutput.release();
		
		return original;
	}
	
	static List<SkyStone> computeSkyStones(Mat yellowTags) {
		//morph
		//yellowTags = compute.fillHoro(yellowTags);
		
		List<SkyStone> skyStones = new ArrayList<>();
		
		List<MatOfPoint> hulls = Compute.findHulls(yellowTags);
		Compute.drawHulls(hulls, yellowTags, new Scalar(255, 255, 255), 2);
		
		Mat drawInternalHulls = new Mat(yellowTags.rows(), yellowTags.cols(), CvType.CV_8UC3);
		
		Imgproc.rectangle(drawInternalHulls,
				new Point(0, 0),
				new Point(drawInternalHulls.width(), drawInternalHulls.height()),
				new Scalar(0, 0, 0),
				-1);
		
		yellowTags = Compute.flip(yellowTags);
		//NEEDS TO BE FUCKING CONNECTED TO BORDER
		//Imgproc.floodFill(yellowTags, new Mat(482,642,yellowTags.type()), new Point(1,479), new Scalar(0,0,0));
		Compute.floodFill(yellowTags, new Point(639, 380));
		
		Compute.floodFill(yellowTags, new Point(639, 479));
		Compute.floodFill(yellowTags, new Point(440, 479));
		Compute.floodFill(yellowTags, new Point(200, 479));
		Compute.floodFill(yellowTags, new Point(1, 479));
		
		Compute.floodFill(yellowTags, new Point(1, 380));
		//compute.rectangle(yellowTags);
		
		List<MatOfPoint> internalHulls = Compute.findHulls(yellowTags);
		internalHulls = Compute.filterContours(internalHulls, 1200);
		
		Compute.drawHulls(internalHulls, drawInternalHulls);
		
		for (MatOfPoint h : internalHulls) {
			SkyStone ss = new SkyStone(h);
			if (!ss.isBastard) skyStones.add(ss);
		}
		
		return skyStones;
	}
	
	/*
	 * Takes in yellow masks, and image to annotate on
	 * spits out list of Stones
	 */
	static List<Stone> computeStones(Mat yellowOutput) {
		Mat dTrans = Compute.distanceTransform(yellowOutput);
		//Start.display(dTrans,1,"trans");
		
		List<MatOfPoint> stonesContour = Compute.findHulls(dTrans);
		
		List<Stone> stones = new ArrayList<>();
		
		for (MatOfPoint con : stonesContour) {
			Stone d = new Stone(con);
			if (!d.isBastard) {
				stones.add(d);
			}
		}
		
		return stones;
	}
	
	/*
	 * Takes in red, blue, yellow, black masks, and image to annotate on
	 * spits out list of Foundations
	 */
	List<Foundation> computeFoundations(Mat redOutput, Mat blueOutput, Mat yellowOutput, Mat blackOutput,
	                                    Mat canvas) {
		//Find Contours
		List<MatOfPoint> hullsRed = Compute.findHulls(redOutput);
		List<MatOfPoint> hullsBlue = Compute.findHulls(blueOutput);
//		List<MatOfPoint> hullsYellow = Compute.findHulls(yellowOutput);
		
		//populate array of detected (color only)
		List<Detected> detected = new ArrayList<>();
		List<MatOfPoint> detectedHulls = new ArrayList<>();
		
		//we will segregate the blacks
		List<Detected> blacks = new ArrayList<>();
		
		for (MatOfPoint p : hullsRed) {
			Detected toAdd = new Detected(p, Detected.Color.RED);
			if (!toAdd.isBastard) {
				detected.add(toAdd);
				detectedHulls.add(p);
			}
		}
		for (MatOfPoint p : hullsBlue) {
			Detected toadd = new Detected(p, Detected.Color.BLUE);
			if (!toadd.isBastard) {
				detected.add(toadd);
				detectedHulls.add(p);
			}
		}
        /*
        for (MatOfPoint p : hullsYellow) {
            Detected toadd = new Detected(p, Detected.Color.YELLOW);
            if (!toadd.isBastard) {
                detected.add(toadd);
            }
        }*/
		Mat detectedAll = new Mat(redOutput.rows(), redOutput.cols(), redOutput.type());
		Imgproc.rectangle(detectedAll,
				new Point(0, 0),
				new Point(detectedAll.width(), detectedAll.height()),
				new Scalar(0, 0, 0),
				-1);
		Compute.drawHulls(detectedHulls, detectedAll, new Scalar(255, 255, 255), -1);
		
		//limit black to regions underneath
		Imgproc.dilate(detectedAll, detectedAll,
				Imgproc.getStructuringElement(
						Imgproc.MORPH_RECT,
						new Size(1, 80),
						new Point(0, 0)
				));
		detectedAll = Compute.flip(detectedAll);
		blackOutput = Compute.subtract(blackOutput, detectedAll);
		
		//cut sides of color contours. Field walls are bad.
		for (Detected d : detected) {
			Point one = new Point(d.bounds.x, d.bounds.y + d.bounds.height * 0.1);
			Point two = new Point(d.bounds.x, d.bounds.y + d.bounds.height * 0.1 + regionSideClipExtensionLength);
			Imgproc.line(blackOutput, one, two, new Scalar(new double[]{0, 0, 0}), 1);
			
			one = new Point(d.bounds.x + d.bounds.width, d.bounds.y + d.bounds.height * 0.1);
			two = new Point(d.bounds.x + d.bounds.width,
					d.bounds.y + d.bounds.height * 0.1 + regionSideClipExtensionLength);
			Imgproc.line(blackOutput, one, two, new Scalar(new double[]{0, 0, 0}), 1);
		}
		
		ArrayList<MatOfPoint> hullsBlack = Compute.findHulls(blackOutput);
		
		for (MatOfPoint p : hullsBlack) {
			Detected toAdd = new Detected(p, Detected.Color.BLACK);
			if (!toAdd.isBastard) {
				blacks.add(toAdd);
			}
		}
		
		for (Detected d : detected) {
			d.draw(canvas);
		}
		for (Detected d : blacks) {
			d.draw(canvas);
		}
		
		//process sandwiches, populate foundation ArrayList
		List<Foundation> foundations = new ArrayList<>();
		
		Imgproc.putText(canvas, String.valueOf(blackcut), new Point(20, 20), Core.FONT_HERSHEY_SIMPLEX, 0.6,
				new Scalar(0, 0, 0), 7);
		Imgproc.putText(canvas, String.valueOf(blackcut), new Point(20, 20), Core.FONT_HERSHEY_SIMPLEX, 0.6,
				new Scalar(255, 255, 0), 2);
		
		for (Detected d : blacks) {
			for (Detected j : detected) {
				if (Math.abs(d.x - j.x) < 120 &&
						    d.bounds.y > j.bounds.y - 40 &&  //below the other
						    d.bounds.y < j.bounds.y + j.bounds.height + 30 &&//touching, whitin 30 pixels
						    Math.abs(d.bounds.width * 1.0 / j.bounds.width - 1) < 0.6) {
					foundations.add(Foundation.createFoundation(d, j));
				}
			}
		}
		
		return foundations;
	}
}

