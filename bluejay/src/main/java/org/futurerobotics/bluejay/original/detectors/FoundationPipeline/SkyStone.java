package org.futurerobotics.bluejay.original.detectors.FoundationPipeline;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

public class SkyStone extends Stone {

	public SkyStone(MatOfPoint shape) {
		super(shape);

		isBastard = false;
		if(length<0.2 || Properlength>25)isBastard = true;
	}

	public void draw(Mat canvas) {
		Scalar color = new Scalar(0, 255, 0);
		Scalar black = new Scalar(0, 0, 0);

		Imgproc.drawContours(canvas, Arrays.asList(shape), 0, new Scalar(255, 0, 255), 4);
		Imgproc.putText(canvas, "SKYSTONE", new Point(x, y - 30), Core.FONT_HERSHEY_SIMPLEX, 0.6, black, 7);
		Imgproc.putText(canvas, "SKYSTONE", new Point(x, y - 30), Core.FONT_HERSHEY_SIMPLEX, 0.6, color, 2);
		Imgproc.circle(canvas, new Point(x, y), 4, new Scalar(255, 255, 255), -1);
	}

}
