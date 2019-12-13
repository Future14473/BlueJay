package org.futurerobotics.bluejay.original.detectors.foundation;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class Foundation {
	public int x;
	public int y;
	public Type t;
	Rect bounds;
	
	public Foundation(Rect bounds, Type t) {
		this.x = bounds.x + bounds.width / 2;
		this.y = bounds.y + bounds.height / 2;
		this.bounds = bounds;
		this.t = t;
	}
	
	/**
	 * @param d is a black detected
	 * @param j is either blue or red or yellow detected
	 */
	static Foundation createFoundation(Detected d, Detected j) {
		//combine Point arrays
		Point[] blackPoints = d.shape.toArray();
		Point[] colorPoints = j.shape.toArray();
		Point[] allTogetherNow = new Point[blackPoints.length + colorPoints.length];
		System.arraycopy(blackPoints, 0, allTogetherNow, 0, blackPoints.length);
		if (allTogetherNow.length - blackPoints.length >= 0)
			System.arraycopy(colorPoints, 0, allTogetherNow, blackPoints.length,
					allTogetherNow.length - blackPoints.length);
		
		//draw Rectangle around them
		Rect foundationBound = Imgproc.boundingRect(new MatOfPoint(allTogetherNow));
		
		//add to Foundation List
		Type type = null;
		if (j.c == Detected.Color.BLUE) type = Type.BLUE;
		if (j.c == Detected.Color.RED) type = Type.RED;
		if (j.c == Detected.Color.YELLOW) type = Type.UNKNOWN;
		return new Foundation(foundationBound, type);
	}
	
	public void draw(Mat canvas) {
		Scalar color = new Scalar(0, 255, 0);
		Scalar black = new Scalar(0, 0, 0);
		
		Imgproc.rectangle(canvas, bounds.tl(), bounds.br(), new Scalar(255, 0, 0), 4);
		Imgproc.putText(canvas, t.toString(), new Point(bounds.tl().x, bounds.tl().y + 20), Core.FONT_HERSHEY_SIMPLEX,
				0.6, black, 7);
		Imgproc.putText(canvas, t.toString(), new Point(bounds.tl().x, bounds.tl().y + 20), Core.FONT_HERSHEY_SIMPLEX,
				0.6, color, 2);
	}
	
	enum Type {
		BLUE, RED, UNKNOWN
	}
}
