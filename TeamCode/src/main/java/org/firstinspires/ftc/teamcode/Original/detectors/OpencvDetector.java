package org.firstinspires.ftc.teamcode.Original.detectors;

import android.graphics.Bitmap;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

public class OpencvDetector implements Detector {

    //Originally in RobotControllerActivity, but caused the camera shutter to make weird noises, so now it lives here
    static{
        OpenCVLoader.initDebug();
        //OR System.LoadLibrary("opencv_java3");
    }

    private OpMode opMode;
    HardwareMap   hardwareMap;
    Telemetry     telemetry;
    ImageDetector vuforia;

    private Bitmap image; //raw image for camera
    private Mat Matimage; //image converted to OpenCV Mat
    Point FoundationLocation; //point of detected foundation
    int   matches =0;//TODO testing purposes

    volatile boolean activated=false;

    Thread run = new Thread() {
        @Override
        public void run() {
            while (activated){
                updateObjects();
            }
        }
    };

    public OpencvDetector (OpMode opMode){
        this.opMode = opMode;
        this.hardwareMap = opMode.hardwareMap;
        this.telemetry = opMode.telemetry;

        this.vuforia=new ImageDetector(opMode);
    }

    //for future interface
    public void start(){
        activated=true;
        run.start();
    }
    /**
     * hold the phone as you would use it to scroll reddit
     * x: 0 at the top, increases as you go down
     * y: 0 at the right, increases as you go left
    */
    private void updateObjects(){
        //get raw image
        image = vuforia.getImage();

        //raw to Mat
        Matimage = new Mat (image.getWidth(), image.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(image, Matimage);

        //Opencv pipeline
        ArrayList<MatOfPoint> contours = new Pipeline().process(Matimage);

        if(contours.size()==0){
            FoundationLocation=null;
            return;
        }

        matches = contours.size();

        Point sum=new Point();
        double length = contours.get(0).toList().size();

        for(Point p : contours.get(0).toList()){
            sum.x+=p.x;
            sum.y+=p.y;
        }

        Point fin = new Point(sum.x/length,sum.y/length);

        FoundationLocation = fin.clone();
    }

    /**
     * will be null if not available
     * @return
     */
    public Point getObjects(){
        if(!activated)return null;

        return FoundationLocation;
    }

    public void stop() {
        activated=false;
    }

    public void print(Point im){

        if(im==null){
            telemetry.addData("OpenCV","not available");
            return;
        }

        telemetry.addData("OpenCv-Position (x,y)",im.x+" "+im.y+" matches "+ matches);
    }

    public double flatness (MatOfPoint in){
        Point sum=new Point();
        List<Point> points = in.toList();

        for(Point p : points){
            sum.x+=p.x;
            sum.y+=p.y;
        }

        //sum is now avg
        sum = new Point(sum.x/(double)points.size(),sum.y/(double)points.size());

        List<Point> deviations = new ArrayList<Point>();

        for(Point p: points){
            deviations.add(new Point(Math.abs(p.x-sum.x),Math.abs(p.y-sum.y)));
        }

        Point sum2=new Point();

        for(Point p : deviations){
            sum2.x+=p.x;
            sum2.y+=p.y;
        }
        Point mean = new Point(sum2.x/(double)deviations.size(),sum2.y/(double)deviations.size());

        Point totaldeviation = new Point();
        for(Point p: deviations){
            deviations.add(new Point(Math.abs(p.x-mean.x),Math.abs(p.y-mean.y)));
        }

        return totaldeviation.x+totaldeviation.y;
    }
}
