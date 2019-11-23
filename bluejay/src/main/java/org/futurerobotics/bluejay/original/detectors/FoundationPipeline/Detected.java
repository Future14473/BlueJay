package org.futurerobotics.bluejay.original.detectors.FoundationPipeline;

import java.util.Arrays;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import f.FoundationPipeline.Detected.Color;

class Detected {
    enum Color {
        BLUE, RED, YELLOW, BLACK, STONE
    }

    double x, y;
    double     length;
    MatOfPoint shape;
    Color      c;
    boolean    isBastard = false;
    Rect       bounds;

    public Detected(MatOfPoint shape, Color c) {
        this.length = circularity(shape);
        
        if (length < 3)
            isBastard = true; //We only like the long blacks. The short blacks will be disposed of
        if (c != Color.BLACK)
            isBastard = false;//colors means stacked blocks. Will not always be long shaped 
        if(isBastard)return;
        
        this.shape = shape;
        bounds = Imgproc.boundingRect(shape);
        Point centerPoint = center(shape);
        this.x = centerPoint.x;
        this.y = centerPoint.y;
        this.c = c;
        
    }

    //will use rectangular bounds instead of moments because Moments
    //are expensive to calculate and Rectangles are perfectly
    //fine for our horozontally aligned foundations
    Point center(MatOfPoint inp) {
        return new Point(bounds.x+bounds.width/2, bounds.y+bounds.height/2);
    }

    //We will cheat and do width/height because the correct calculation is expensive
    double circularity(MatOfPoint inp) {
        final Rect bb = Imgproc.boundingRect(inp);
        final double ratio = bb.width / (double) bb.height;
        return ratio;
    }

    public void draw(Mat canvas) {
        Scalar color = new Scalar(00, 255, 00);
        Scalar black = new Scalar(0, 0, 0);

        Imgproc.drawContours(canvas, Arrays.asList(shape), 0, color, 2);
        Imgproc.putText(canvas, c.toString(), new Point(x, y - 30), Core.FONT_HERSHEY_SIMPLEX, 0.6, black, 7);
        Imgproc.putText(canvas, c.toString(), new Point(x, y - 30), Core.FONT_HERSHEY_SIMPLEX, 0.6, color, 2);
        Imgproc.circle(canvas, new Point(x, y), 4, new Scalar(255, 255, 255), -1);
    }

}
