package org.futurerobotics.bluejay.original.detectors;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FoundationPipeline {

    Mat resizedImage = new Mat();

    List<Foundation> foundations = new ArrayList<Foundation>();

//    static {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//    }

    //debug steps
    Mat red    = new Mat();
    Mat blue   = new Mat();
    Mat yellow = new Mat();
    Mat black  = new Mat();

    Mat histo = new Mat();
    /**
     * Give it the raw image and it will update the Foundations arraylist
     *
     * @return source image with annotations on it
     */
    public Mat process(Mat source0) {
        System.gc();
        System.runFinalization();

        Imgproc.resize(source0, resizedImage, new Size(640, 480), 0.0, 0.0, Imgproc.INTER_LINEAR);

        Mat original = resizedImage.clone();

        double blackCutOff = getHistogramfast(resizedImage);


        //rectangle(resizedImage);

        //For Yellow
        double[] yellowRange = {80,105};

        //For Blue
        double[] blueRange1 = {170,180};
        double[] blueRange2 = {0,10};

        //for Red
        double[] redRange = {110,120};

        double[] satRange = {60, 255};
        double[] valRange = {blackCutOff*0.7, 255};

        Mat redOutput = threshold(resizedImage, redRange, satRange, valRange);

        Mat blueOutput = new Mat();
        Core.bitwise_or(
                threshold(resizedImage, blueRange1, satRange, valRange),
                threshold(resizedImage, blueRange2, satRange, valRange),
                blueOutput);

        Mat blackOutput = threshold(resizedImage,
                new double[]{0, 180},//hue
                new double[]{0, 120},//sat
                //new double[]{0, 80});//val
                new double[]{0, blackCutOff});//val

        //yellow stones face sideways, so there is less glare
        //thus the saturation minumun can be higher
        Mat yellowOutput = threshold(resizedImage, yellowRange, new double[]{170, 255}, valRange);

        red = redOutput.clone();
        blue = blueOutput.clone();
        yellow = yellowOutput.clone();

        // Step Find_Contours0:
        ArrayList<MatOfPoint> hullsRed = findHulls(redOutput);
        ArrayList<MatOfPoint> hullsBlue = findHulls(blueOutput);
        ArrayList<MatOfPoint> hullsYellow = findHulls(yellowOutput);

        redOutput.release();
        blueOutput.release();
        yellowOutput.release();

        //System.out.println("There are "+(hullsBlue.size()+hullsRed.size()+hullsBlack.size())+" matches");
        int numBastards = 0;

        //populate array of detected (color only)
        List<Detected> detected = new ArrayList<Detected>();
        //we will segregate the blacks
        List<Detected> blacks = new ArrayList<Detected>();

        for (MatOfPoint p : hullsRed) {
            Detected toadd = new Detected(p, Detected.Color.RED);
            if (!toadd.isBastard) {
                detected.add(toadd);
            } else numBastards++;
        }
        for (MatOfPoint p : hullsBlue) {
            Detected toadd = new Detected(p, Detected.Color.BLUE);
            if (!toadd.isBastard) {
                detected.add(toadd);
            } else numBastards++;
        }
        for (MatOfPoint p : hullsYellow) {
            Detected toadd = new Detected(p, Detected.Color.YELLOW);
            if (!toadd.isBastard) {
                detected.add(toadd);
            } else numBastards++;
        }

        //cut sides of color contours. Field walls are bad.
        for(Detected d:detected) {
            Point one = new Point(d.bounds.x,d.bounds.y+d.bounds.height*0.7);
            Point two = new Point(d.bounds.x,d.bounds.y+d.bounds.height*0.7+120);
            Imgproc.line(blackOutput,one, two,new Scalar(new double[] {0,0,0}),1);

            one = new Point(d.bounds.x+d.bounds.width,d.bounds.y+d.bounds.height*0.7);
            two = new Point(d.bounds.x+d.bounds.width,d.bounds.y+d.bounds.height*0.7+120);
            Imgproc.line(blackOutput,one, two,new Scalar(new double[] {0,0,0}),1);
        }

        ArrayList<MatOfPoint> hullsBlack = findHulls(blackOutput);
        black = blackOutput.clone();
        blackOutput.release();

        for (MatOfPoint p : hullsBlack) {
            Detected toadd = new Detected(p, Detected.Color.BLACK);
            if (!toadd.isBastard) {
                blacks.add(toadd);
            } else numBastards++;
        }

        for (Detected d : detected) {
            d.draw(original);
        }for (Detected d : blacks) {
            d.draw(original);
        }

        //process sandwiches, populate foundation ArrayList
        foundations.clear();

        for (Detected d : blacks) {
            for (Detected j : detected) {
                if (    Math.abs(d.x - j.x) < 120 &&
                        d.bounds.y > j.bounds.y && d.bounds.y < j.bounds.y+j.bounds.height+30&&
                        Math.abs(d.bounds.width*1.0/j.bounds.width-1)  <  0.6)   {
                    createFoundation(d, j);
                }
            }
        }

        for (Foundation f : foundations) {
            f.draw(original);
        }

        return original;
    }

    /**
     * @param d is a black detected
     * @param j is either blue or red or yellow detected
     */
    void createFoundation(Detected d, Detected j) {
        //combine Point arrays
        Point[] blackPoints = d.shape.toArray();
        Point[] colorPoints = j.shape.toArray();
        Point[] allTogetherNow = new Point[blackPoints.length+colorPoints.length];
        for(int i =0;i<blackPoints.length;i++) {
            allTogetherNow[i]=blackPoints[i];
        }
        for(int i = blackPoints.length;i<allTogetherNow.length;i++) {
            allTogetherNow[i]=colorPoints[i-blackPoints.length];
        }

        //draw Rectangle around them
        Rect foundationbound = Imgproc.boundingRect(new MatOfPoint(allTogetherNow));

        //add to Foundation List
        Foundation.Type type = null;
        if (j.c == Detected.Color.BLUE) type = Foundation.Type.BLUEFOUNDATION;
        if (j.c == Detected.Color.RED) type = Foundation.Type.REDFOUNDATION;
        if (j.c == Detected.Color.YELLOW) type = Foundation.Type.UNKNOWNFOUNDATION;
        foundations.add(new Foundation(
                foundationbound,
                type
        ));

    }

    /**
     * draw rectangle border
     */
    void rectangle(Mat canvas) {
        Point topleftPoint, bottomrightPoint;

        // new point
        topleftPoint = new Point(0, 0);
        bottomrightPoint = new Point(canvas.width(), canvas.height());

        // rectangle
        Scalar cvRectangleColor = new Scalar(0.0, 0.0, 0.0, 0.0);
        int cvRectangleThickness = 2;
        int cvRectangleLinetype = Core.BORDER_DEFAULT;
        int cvRectangleShift = 0;
        Imgproc.rectangle(canvas,
                topleftPoint,
                bottomrightPoint,
                cvRectangleColor,
                cvRectangleThickness,
                cvRectangleLinetype,
                cvRectangleShift);
    }

    /**
     * Takes an RGB image and allplies thresholding based on HSV ranges
     *
     * @param input    An RGB image
     * @param colRange Hue range
     * @param satRange Saturation range
     * @param valRange Value Range
     * @return Thresholded image
     */
    Mat threshold(Mat input, double[] colRange, double[] satRange, double[] valRange) {
        Mat output = new Mat();

        Imgproc.cvtColor(input.clone(), output, Imgproc.COLOR_BGR2HSV);
        Core.inRange(output, new Scalar(colRange[0], satRange[0], valRange[0]),
                new Scalar(colRange[1], satRange[1], valRange[1]), output);

        return output;
    }

    /**
     * Returns a matrix of points representing the convex hulls of the blobs in the input
     *
     * @param inp a binary image
     */
    ArrayList<MatOfPoint> findHulls(Mat inp) {
        rectangle(inp);

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        ArrayList<MatOfPoint> filteredcontours = new ArrayList<MatOfPoint>();
        ArrayList<MatOfPoint> hullsOutput = new ArrayList<MatOfPoint>();

        // Step Find_Contours0:
        Mat findContoursInput = inp;
        boolean findContoursExternalOnly = true;
        findContours(findContoursInput, findContoursExternalOnly, contours);

        // Step Filter_Contours0:
        ArrayList<MatOfPoint> filterContoursContours = contours;
        double filterContoursMinArea = 800.0; //1000
        filterContours(filterContoursContours, filterContoursMinArea,filteredcontours);

        // Step Convex_Hulls0:
        ArrayList<MatOfPoint> convexHullsContours = filteredcontours;
        convexHulls(convexHullsContours, hullsOutput);

        return hullsOutput;
    }

    /**
     * Takes a point matrix and draws the shape it represents on the Mat input
     *
     * @param drawOn thing that gets drawn on
     */
    void drawHulls(ArrayList<MatOfPoint> hulls, Mat drawOn) {
        //draw convex hulls
        Scalar color = new Scalar(0, 255, 0);   // Green

        for (int i = 0; i < hulls.size(); i++) {
            Imgproc.drawContours(drawOn, hulls, i, color);
        }
    }

    /**
     * Finds the shapes of blobs
     *
     * @param externalOnly Whether to ignore shapes inside shapes
     */
    void findContours(Mat input, boolean externalOnly,
                      List<MatOfPoint> contours) {
        Mat hierarchy = new Mat();
        contours.clear();
        int mode;
        if (externalOnly) {
            mode = Imgproc.RETR_EXTERNAL;
        } else {
            mode = Imgproc.RETR_LIST;
        }
        int method = Imgproc.CHAIN_APPROX_SIMPLE;
        Imgproc.findContours(input, contours, hierarchy, mode, method);

    }


    /**
     * Filters out contours that do not meet certain criteria.
     *
     * @param inputContours  is the input list of contours
     * @param output         is the the output list of contours
     * @param minArea        is the minimum area of a contour that will be kept
     */
    private void filterContours(List<MatOfPoint> inputContours, double minArea, List<MatOfPoint> output) {
        final MatOfInt hull = new MatOfInt();
        output.clear();
        //operation
        for (int i = 0; i < inputContours.size(); i++) {
            final MatOfPoint contour = inputContours.get(i);
            final double area = Imgproc.contourArea(contour);
            if (area < minArea) continue;

            output.add(contour);
        }
    }

    /**
     * Compute the convex hulls of contours.
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
                double[] point = new double[]{contour.get(index, 0)[0], contour.get(index, 0)[1]};
                mopHull.put(j, 0, point);
            }
            outputContours.add(mopHull);
        }
    }


    //============Histogram================


    //no visual debug
    public double getHistogramfast(Mat in) {
        int histSize=255;//# bins
        int histogramHeight = 512;//physcical size

        Mat[] histData = new Mat[] {new Mat(),new Mat(),new Mat()};

        Mat output = new Mat(new Size(512,512), in.type());

        //memory leaks and phased out data add static to the image
        Imgproc.rectangle(output,
                new Point(0,0),
                new Point(1000,1000),
                new Scalar(new double[] {0,0,0}),
                -1);

        for (int i = 0; i < 3; i++) {
            getHistdataf(in,i,histData[i],histSize);

            //stretch vertically
            Core.normalize(histData[i], histData[i], histogramHeight, 0, Core.NORM_INF);


        }
        //analysis
        //sum of all white colors
        double whiteSum=0;
        double whiteTot=0;
        for (int j = 0; j < histSize; j++) {
            //avg
            double Val = (histData[0].get(j, 0)[0] +
                    histData[1].get(j, 0)[0] +
                    histData[2].get(j, 0)[0] )/3;
            //min
            double minVal = minf(histData[0].get(j, 0)[0],
                    histData[1].get(j, 0)[0],
                    histData[2].get(j, 0)[0]);

            whiteSum+=minVal * j;
            whiteTot+=Val*j;

        }
        double whiteSumAvg = whiteSum / whiteTot * 500;//width of the thing
        double blackCutOff = whiteSumAvg * 1/6;

        histData[0].release();
        histData[1].release();
        histData[2].release();

        return blackCutOff;
    }

    static void getHistdataf(Mat in, int channel, Mat out, int histsize) {

        Imgproc.calcHist(Arrays.asList(in),
                new MatOfInt(channel),//color channel
                new Mat(),
                out,
                new MatOfInt(histsize), //size
                new MatOfFloat(1,256));//ranges we do not count zeros because they skew it

        Imgproc.blur(out,out,new Size(90,90));
    }

    static double minf(double ... vals) {
        double min=vals[0];

        for(int i = 1 ; i<vals.length;i++) {
            if(vals[i]<min)min=vals[i];
        }

        return min;
    }
}

class Detected {
    enum Color {
        BLUE, RED, YELLOW, BLACK
    }

    double x, y;
    double     length;
    MatOfPoint shape;
    Color      c;
    boolean    isBastard = false;
    Rect       bounds;

    public Detected(MatOfPoint shape, Color c) {
        this.length = circularity(shape);
        this.shape = shape;
        bounds = Imgproc.boundingRect(shape);
        Point centerPoint = center(shape);
        this.x = centerPoint.x;
        this.y = centerPoint.y;
        this.c = c;
        if (length < 3)
            isBastard = true; //We only like the long blacks. The short blacks will be disposed of
        if (c != Color.BLACK)
            isBastard = false;//colors means stacked blocks. Will not always be long shaped
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
        DecimalFormat df = new DecimalFormat("#.00");
        String displayValue = df.format(length);

        Imgproc.drawContours(canvas, Arrays.asList(shape), 0, color, 2);
        Imgproc.putText(canvas, c.toString(), new Point(x, y - 30), Core.FONT_HERSHEY_SIMPLEX, 0.6, black, 7);
        Imgproc.putText(canvas, c.toString(), new Point(x, y - 30), Core.FONT_HERSHEY_SIMPLEX, 0.6, color, 2);
        Imgproc.circle(canvas, new Point(x, y), 4, new Scalar(255, 255, 255), -1);
    }

}

class Foundation {
    enum Type {
        BLUEFOUNDATION, REDFOUNDATION, UNKNOWNFOUNDATION
    }

    int  x = 0;
    int  y = 0;
    Type t;
    Rect bounds;

    public Foundation(Rect bounds, Type t) {
        this.x = bounds.x + bounds.width / 2;
        this.y = bounds.y + bounds.height / 2;
        this.bounds = bounds;
        this.t = t;
    }

    public void draw(Mat canvas) {
        Scalar color = new Scalar(00, 255, 00);
        Scalar black = new Scalar(0, 0, 0);

        Imgproc.rectangle(canvas, bounds.tl(), bounds.br(), new Scalar(255, 0, 0), 4);
        Imgproc.putText(canvas, t.toString(), bounds.tl(), Core.FONT_HERSHEY_SIMPLEX, 0.6, black, 7);
        Imgproc.putText(canvas, t.toString(), bounds.tl(), Core.FONT_HERSHEY_SIMPLEX, 0.6, color, 2);

    }

}
