package org.firstinspires.ftc.teamcode.Original.detectors;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.List;

public class StoneDetector implements Detector {
    private TFObjectDetector tfod;
    private OpMode opMode;
    private HardwareMap hardwareMap;
    private Telemetry telemetry;

    List<Recognition> objects;
    volatile boolean activated=false;

    Thread run = new Thread(){
        @Override
        public void run() {
            while (activated){
                updateObjects();
            }
        }
    };

    public StoneDetector(OpMode opMode, boolean useDisplay){
        this.opMode=opMode;
        this.hardwareMap=opMode.hardwareMap;
        this.telemetry=opMode.telemetry;

        //by creating an image detector, we ensure that the vulocalizer singleton has been created
        tfod = SetupTensorflow(new ImageDetector(opMode).getVuforia(), useDisplay);
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
        activated=true;
        run.start();
    }

    public void stop(){
        activated=false;
        tfod.shutdown();
    }

    private void updateObjects(){
        List<Recognition> sto = tfod.getUpdatedRecognitions();

        if(sto==null)return;

        objects=sto;
    }

    /**
     * may be null if see nothing
     * @return
     */
    public List<Recognition> getObjects(){
        if(!activated)return null;

        return objects;
    }

    public void print(List<Recognition> obj){
        if(obj==null || obj.size()==0){
            telemetry.addData("Tensorflow","offline");
            return;
        }

        for(Recognition r : obj){
            telemetry.addData("Tensorflow-object",r.getLabel());
        }
    }
}
