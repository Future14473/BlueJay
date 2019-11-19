package org.futurerobotics.bluejay.original.CVBridger;

import org.futurerobotics.bluejay.original.math.MathFTC;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


/**
 * Created by Victo on 9/10/2018.
 */

public abstract class DogeCVDetector extends OpenCVPipeline{

    public abstract Mat process(Mat input);

    private Size               initSize;
    private Size               adjustedSize;
    private Mat                workingMat    = new Mat();

    public Point cropTLCorner = null; //The top left corner of the image used for processing
    public Point cropBRCorner = null; //The bottom right corner of the image used for processing

    public    double                downscale           = 0.5;
    public    Size                  downscaleResolution = new Size(640, 480);
    public    boolean               useFixedDownscale   = true;
    protected String                detectorName        = "DogeCV Detector";

    public DogeCVDetector(){

    }

    @Override
    public Mat processFrame(Mat rgba) {
        initSize = rgba.size();

        if(useFixedDownscale){
            adjustedSize = downscaleResolution;
        }else{
            adjustedSize = new Size(initSize.width * downscale, initSize.height * downscale);
        }

        rgba.copyTo(workingMat);

        if(workingMat.empty()){
            return rgba;
        }
        //Downscale
        Imgproc.resize(workingMat, workingMat,adjustedSize);

        //crop with Math package
        workingMat = MathFTC.crop(workingMat, cropTLCorner, cropBRCorner);

        // Process and scale back to original size for viewing
        Imgproc.resize(process(workingMat),workingMat,getInitSize());

        //Print Watermark
        Imgproc.putText(workingMat,"DogeCV 2019.1 " + detectorName + ": " + getAdjustedSize().toString() ,new Point(5,30),0,0.5,new Scalar(0,255,255),2);

        return workingMat;
    }

    public Size getInitSize() {
        return initSize;
    }

    public Size getAdjustedSize() {
        return adjustedSize;
    }

}
