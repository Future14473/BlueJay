package org.firstinspires.ftc.teamcode.Original;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.List;

public class StoneDetector {
    private TFObjectDetector tfod;
    private OpMode opMode;
    private HardwareMap hardwareMap;
    private Telemetry telemetry;

    public StoneDetector(ImageDetector imager, OpMode opMode, boolean useDisplay){
        this.opMode=opMode;
        this.hardwareMap=opMode.hardwareMap;
        this.telemetry=opMode.telemetry;

        tfod = SetupTensorflow(imager.vuforia, useDisplay);
    }

    private TFObjectDetector SetupTensorflow(VuforiaLocalizer vuforia, boolean useDisplay) {
        if (!ClassFactory.getInstance().canCreateTFObjectDetector()) {
            telemetry.addData("Init Error:", "Tensorflow bootup failed. Brace for errors");
            telemetry.update();
            return null;
        }

        //apply viewID to parameter object
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters;
        if(useDisplay) tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        else           tfodParameters = new TFObjectDetector.Parameters();

        tfodParameters.minimumConfidence = 0.7;

        //create a detector with the viewID
        TFObjectDetector tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);

        //tell the detector what to detect
        tfod.loadModelFromAsset("Skystone.tflite", "Stone", "Skystone");

        return tfod;
    }

    public void start(){
        tfod.activate();
    }

    public void stop(){
        tfod.shutdown();
    }

    public List<Recognition> getObjects(){
        return tfod.getUpdatedRecognitions();
    }
}
