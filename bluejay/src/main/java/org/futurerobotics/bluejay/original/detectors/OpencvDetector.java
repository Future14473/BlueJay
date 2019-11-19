package org.futurerobotics.bluejay.original.detectors;

import android.graphics.Bitmap;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class OpencvDetector implements Detector {

    //Originally in RobotControllerActivity, but caused the camera shutter to make weird noises, so now it lives here
    static {
        OpenCVLoader.initDebug();
        //OR System.LoadLibrary("opencv_java3");
    }

    private OpMode opMode;
    HardwareMap   hardwareMap;
    Telemetry     telemetry;
    ImageDetector vuforia;

    private Bitmap image; //raw image for camera
    private Mat    Matimage; //image converted to OpenCV Mat
    public List<Foundation> foundations = new ArrayList<Foundation>(); //detected foundations

    volatile boolean activated = false;

    Thread run = new Thread() {
        @Override
        public void run() {
            while (activated) {
                updateObjects();
            }
        }
    };

    public OpencvDetector(OpMode opMode) {
        this.opMode = opMode;
        this.hardwareMap = opMode.hardwareMap;
        this.telemetry = opMode.telemetry;

        this.vuforia = new ImageDetector(opMode);
    }

    //for future interface
    public void start() {
        activated = true;
        run.start();
    }

    /**
     * hold the phone as you would use it to browse reddit
     * x: 0 at the top, increases as you go down
     * y: 0 at the right, increases as you go left
     */
    private void updateObjects() {
        //get raw image
        image = vuforia.getImage();

        //raw to Mat
        Matimage = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(image, Matimage);

        //Opencv pipeline
        FoundationPipeline fp = new FoundationPipeline();
        fp.process(Matimage);

        foundations.clear();

        foundations = new ArrayList<>(fp.foundations);
    }

    /**
     * will be null if not available
     *
     * @return
     */
    public List<Foundation> getObjects() {
        if (!activated) return null;

        return foundations;
    }

    public void stop() {
        activated = false;
    }

    public void print(List<Foundation> inp) {

        if (inp == null) {
            telemetry.addData("OpenCV", "not available");
            return;
        }

        for(Foundation f : inp){
            // tabs not supported on tele
            telemetry.addData("    Foundation", f.x+" "+f.y+" "+f.t.toString());
        }

    }
}
