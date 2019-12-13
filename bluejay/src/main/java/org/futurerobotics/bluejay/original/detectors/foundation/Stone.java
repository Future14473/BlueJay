package org.futurerobotics.bluejay.original.detectors.foundation;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class Stone {
	
	//made immutable class; no copy necessary.
	public final double x, y;
	final double length;
	final double properLength;
	final MatOfPoint shape;
	final Rect bounds;
	protected boolean isBastard;
	
	public Stone(MatOfPoint shape) {
		bounds = Imgproc.boundingRect(shape);
		this.length = (1.0 * bounds.width) / bounds.height;
		this.properLength = circularity(shape);
		
		//We only like the long ones. The short ones will be disposed of
		this.isBastard = length < 1 || Imgproc.contourArea(shape) < 70;
		this.shape = shape;
		
		Point centerPoint = center(bounds);
		this.x = centerPoint.x;
		this.y = centerPoint.y;
		
		//expand bounds
		bounds.height += 30;
		bounds.width += 30;
		bounds.x -= 15;
		bounds.y -= 15;
	}
	
	//will use rectangular bounds instead of moments because Moments
	//are expensive to calculate and Rectangles are perfectly
	//fine for our horozontally aligned foundations
	Point center(Rect bounds) {
		return new Point(bounds.x + bounds.width / 2f, bounds.y + bounds.height / 2f);
	}
	
	//We will cheat and do width/height because the correct calculation is expensive
	double circularity(MatOfPoint inp) {
		//final Rect bb = Imgproc.boundingRect(inp);
		//final double ratio = bb.width / (double) bb.height;
		//return ratio;
		double perimeter = Imgproc.arcLength(new MatOfPoint2f(inp.toArray()), true);
		return (Math.PI * perimeter * perimeter) / (4 * Imgproc.contourArea(inp));
	}
	
	public void draw(Mat canvas) {
		Scalar color = new Scalar(0, 255, 0);
		Scalar black = new Scalar(0, 0, 0);
		
		//Imgproc.drawContours(canvas, Arrays.asList(shape), 0, new Scalar(0, 0, 255), 4);
		Imgproc.rectangle(canvas, bounds.tl(), bounds.br(), new Scalar(0, 0, 255), 4);
		Imgproc.putText(canvas, "STONE", new Point(x, y - 30), Core.FONT_HERSHEY_SIMPLEX, 0.6, black, 7);
		Imgproc.putText(canvas, "STONE", new Point(x, y - 30), Core.FONT_HERSHEY_SIMPLEX, 0.6, color, 2);
		Imgproc.circle(canvas, new Point(x, y), 4, new Scalar(255, 255, 255), -1);
	}
	
	public boolean isBastard() {
		return isBastard;
	}
}

