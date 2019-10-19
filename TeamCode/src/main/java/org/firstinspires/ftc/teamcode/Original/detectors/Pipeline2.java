package org.firstinspires.ftc.teamcode.Original.detectors;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Pipeline2 {

    //Outputs
    private Mat                   resizeImageOutput    = new Mat();
    private Point                 newPoint0Output      = new Point();
    private Point                 newPoint1Output      = new Point();
    private Mat                   cvRectangleOutput    = new Mat();
    private Mat                   hsvThresholdOutput   = new Mat();
    private ArrayList<MatOfPoint> findContoursOutput   = new ArrayList<MatOfPoint>();
    private ArrayList<MatOfPoint> filterContoursOutput = new ArrayList<MatOfPoint>();
    private ArrayList<MatOfPoint> convexHullsOutput    = new ArrayList<MatOfPoint>();

//    static {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//    }

    /**
     * This is the primary method that runs the entire pipeline and updates the outputs.
     */
    public Mat process(Mat source0) {
        System.gc();
        System.runFinalization();

        Mat original = source0.clone();

        // Step Resize_Image0:
        Mat resizeImageInput = source0;
        double resizeImageWidth = 640.0;
        double resizeImageHeight = 480.0;
        int resizeImageInterpolation = Imgproc.INTER_CUBIC;
        resizeImage(resizeImageInput, resizeImageWidth, resizeImageHeight, resizeImageInterpolation, resizeImageOutput);

        // Step New_Point0:
        double newPoint0X = 0.0;
        double newPoint0Y = 450.0;
        newPoint(newPoint0X, newPoint0Y, newPoint0Output);

        // Step New_Point1:
        double newPoint1X = 640.0;
        double newPoint1Y = 480.0;
        newPoint(newPoint1X, newPoint1Y, newPoint1Output);

        // Step CV_rectangle0:
        Mat cvRectangleSrc = resizeImageOutput;
        Point cvRectanglePt1 = newPoint0Output;
        Point cvRectanglePt2 = newPoint1Output;
        Scalar cvRectangleColor = new Scalar(0.0, 0.0, 0.0, 0.0);
        double cvRectangleThickness = 100.0;
        int cvRectangleLinetype = Core.FILLED;
        double cvRectangleShift = 0.0;
        cvRectangle(cvRectangleSrc, cvRectanglePt1, cvRectanglePt2, cvRectangleColor, cvRectangleThickness, cvRectangleLinetype, cvRectangleShift, cvRectangleOutput);

        // Step HSV_Threshold0:
        Mat hsvThresholdInput = cvRectangleOutput;
        //For Blue
        //double[] hsvThresholdHue = {0,20};

        //for Red
        double[] hsvThresholdHue = {110,130};

        double[] hsvThresholdSaturation = {190.76258992805754, 255.0};
        double[] hsvThresholdValue = {30.57014388489208, 255.0};
        hsvThreshold(hsvThresholdInput, hsvThresholdHue, hsvThresholdSaturation, hsvThresholdValue, hsvThresholdOutput);

        // Step Find_Contours0:
        Mat findContoursInput = hsvThresholdOutput;
        boolean findContoursExternalOnly = true;
        findContours(findContoursInput, findContoursExternalOnly, findContoursOutput);

        // Step Filter_Contours0:
        ArrayList<MatOfPoint> filterContoursContours = findContoursOutput;
        double filterContoursMinArea = 1000.0;
        double filterContoursMinPerimeter = 0.0;
        double filterContoursMinWidth = 0.0;
        double filterContoursMaxWidth = 1000.0;
        double filterContoursMinHeight = 0.0;
        double filterContoursMaxHeight = 1000.0;
        double[] filterContoursSolidity = {0.0, 100.0};
        double filterContoursMaxVertices = 1000000.0;
        double filterContoursMinVertices = 0.0;
        double filterContoursMinRatio = 0.0;
        double filterContoursMaxRatio = 1000.0;
        filterContours(filterContoursContours, filterContoursMinArea, filterContoursMinPerimeter, filterContoursMinWidth, filterContoursMaxWidth, filterContoursMinHeight, filterContoursMaxHeight, filterContoursSolidity, filterContoursMaxVertices, filterContoursMinVertices, filterContoursMinRatio, filterContoursMaxRatio, filterContoursOutput);

        // Step Convex_Hulls0:
        ArrayList<MatOfPoint> convexHullsContours = filterContoursOutput;
        convexHulls(convexHullsContours, convexHullsOutput);

        //draw convex hulls
        Scalar color = new Scalar(0, 255, 0);   // Green
        for(int i=0; i < convexHullsOutput.size(); i++){
            Imgproc.drawContours(original, convexHullsOutput, i, color);
        }

        return  original;
    }

    public Mat resizeImageOutput() {
        return resizeImageOutput;
    }

    public Point newPoint0Output() {
        return newPoint0Output;
    }

    public Point newPoint1Output() {
        return newPoint1Output;
    }

    public Mat cvRectangleOutput() {
        return cvRectangleOutput;
    }

    public Mat hsvThresholdOutput() {
        return hsvThresholdOutput;
    }

    public ArrayList<MatOfPoint> findContoursOutput() {
        return findContoursOutput;
    }

    public ArrayList<MatOfPoint> filterContoursOutput() {
        return filterContoursOutput;
    }

    public ArrayList<MatOfPoint> convexHullsOutput() {
        return convexHullsOutput;
    }


    /**
     * Scales and image to an exact size.
     * @param input The image on which to perform the Resize.
     * @param width The width of the output in pixels.
     * @param height The height of the output in pixels.
     * @param interpolation The type of interpolation.
     * @param output The image in which to store the output.
     */
    private void resizeImage(Mat input, double width, double height,
                             int interpolation, Mat output) {
        Imgproc.resize(input, output, new Size(width, height), 0.0, 0.0, interpolation);
    }

    /**
     * Fills a point with given x and y values.
     * @param x the x value to put in the point
     * @param y the y value to put in the point
     * @param point the point to fill
     */
    private void newPoint(double x, double y, Point point) {
        point.x = x;
        point.y = y;
    }

    /**
     * Draws a rectangle on an image.
     * @param src Image to draw rectangle on.
     * @param pt1 one corner of the rectangle.
     * @param pt2 opposite corner of the rectangle.
     * @param color Scalar indicating color to make the rectangle.
     * @param thickness Thickness of the lines of the rectangle.
     * @param lineType Type of line for the rectangle.
     * @param shift Number of decimal places in the points.
     * @param dst output image.
     */
    private void cvRectangle(Mat src, Point pt1, Point pt2, Scalar color,
                             double thickness, int lineType, double shift, Mat dst) {
        src.copyTo(dst);
        if (color == null) {
            color = Scalar.all(1.0);
        }
        Imgproc.rectangle(dst, pt1, pt2, color, (int)thickness, lineType, (int)shift);
    }

    /**
     * Segment an image based on hue, saturation, and value ranges.
     *
     * @param input The image on which to perform the HSL threshold.
     * @param hue The min and max hue
     * @param sat The min and max saturation
     * @param val The min and max value
     * @param //output The image in which to store the output.
     */
    private void hsvThreshold(Mat input, double[] hue, double[] sat, double[] val,
                              Mat out) {
        Imgproc.cvtColor(input, out, Imgproc.COLOR_BGR2HSV);
        Core.inRange(out, new Scalar(hue[0], sat[0], val[0]),
                new Scalar(hue[1], sat[1], val[1]), out);
    }

    /**
     * Sets the values of pixels in a binary image to their distance to the nearest black pixel.
     * @param input The image on which to perform the Distance Transform.
     * @param //type The Transform.
     * @param //maskSize the size of the mask.
     * @param //output The image in which to store the output.
     */
    private void findContours(Mat input, boolean externalOnly,
                              List<MatOfPoint> contours) {
        Mat hierarchy = new Mat();
        contours.clear();
        int mode;
        if (externalOnly) {
            mode = Imgproc.RETR_EXTERNAL;
        }
        else {
            mode = Imgproc.RETR_LIST;
        }
        int method = Imgproc.CHAIN_APPROX_SIMPLE;
        Imgproc.findContours(input, contours, hierarchy, mode, method);
    }


    /**
     * Filters out contours that do not meet certain criteria.
     * @param inputContours is the input list of contours
     * @param output is the the output list of contours
     * @param minArea is the minimum area of a contour that will be kept
     * @param minPerimeter is the minimum perimeter of a contour that will be kept
     * @param minWidth minimum width of a contour
     * @param maxWidth maximum width
     * @param minHeight minimum height
     * @param maxHeight maximimum height
     * //@param Solidity the minimum and maximum solidity of a contour
     * @param minVertexCount minimum vertex Count of the contours
     * @param maxVertexCount maximum vertex Count
     * @param minRatio minimum ratio of width to height
     * @param maxRatio maximum ratio of width to height
     */
    private void filterContours(List<MatOfPoint> inputContours, double minArea,
                                double minPerimeter, double minWidth, double maxWidth, double minHeight, double
                                        maxHeight, double[] solidity, double maxVertexCount, double minVertexCount, double
                                        minRatio, double maxRatio, List<MatOfPoint> output) {
        final MatOfInt hull = new MatOfInt();
        output.clear();
        //operation
        for (int i = 0; i < inputContours.size(); i++) {
            final MatOfPoint contour = inputContours.get(i);
            final Rect bb = Imgproc.boundingRect(contour);
            if (bb.width < minWidth || bb.width > maxWidth) continue;
            if (bb.height < minHeight || bb.height > maxHeight) continue;
            final double area = Imgproc.contourArea(contour);
            if (area < minArea) continue;
            if (Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true) < minPerimeter) continue;
            Imgproc.convexHull(contour, hull);
            MatOfPoint mopHull = new MatOfPoint();
            mopHull.create((int) hull.size().height, 1, CvType.CV_32SC2);
            for (int j = 0; j < hull.size().height; j++) {
                int index = (int)hull.get(j, 0)[0];
                double[] point = new double[] { contour.get(index, 0)[0], contour.get(index, 0)[1]};
                mopHull.put(j, 0, point);
            }
            final double solid = 100 * area / Imgproc.contourArea(mopHull);
            if (solid < solidity[0] || solid > solidity[1]) continue;
            if (contour.rows() < minVertexCount || contour.rows() > maxVertexCount)	continue;
            final double ratio = bb.width / (double)bb.height;
            if (ratio < minRatio || ratio > maxRatio) continue;
            output.add(contour);
        }
    }

    /**
     * Compute the convex hulls of contours.
     * @param inputContours The contours on which to perform the operation.
     * @param outputContours The contours where the output will be stored.
     */
    private void convexHulls(List<MatOfPoint> inputContours,
                             ArrayList<MatOfPoint> outputContours) {
        final MatOfInt hull = new MatOfInt();
        outputContours.clear();
        for (int i = 0; i < inputContours.size(); i++) {
            final MatOfPoint contour = inputContours.get(i);
            final MatOfPoint mopHull = new MatOfPoint();
            Imgproc.convexHull(contour, hull);
            mopHull.create((int) hull.size().height, 1, CvType.CV_32SC2);
            for (int j = 0; j < hull.size().height; j++) {
                int index = (int) hull.get(j, 0)[0];
                double[] point = new double[] {contour.get(index, 0)[0], contour.get(index, 0)[1]};
                mopHull.put(j, 0, point);
            }
            outputContours.add(mopHull);
        }
    }
}
