package org.futurerobotics.bluejay.original.detectors;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.RobotLog;
import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.List;

public class StoneDetector implements StartStoppable {
	List<Recognition> objects;
	volatile boolean activated = false;
	private TFObjectDetector tfod;
	
	private HardwareMap hardwareMap;
	
	public StoneDetector(OpMode opMode, boolean useDisplay) {
		this.hardwareMap = opMode.hardwareMap;
		
		//by creating an image detector, we ensure that the vulocalizer singleton has been created
		tfod = SetupTensorflow(new ImageDetector(opMode).getVuforia(), useDisplay);
	}
	
	private TFObjectDetector SetupTensorflow(VuforiaLocalizer vuforia, boolean useDisplay) {
		if (!ClassFactory.getInstance().canCreateTFObjectDetector()) {
			RobotLog.e("Tensorflow bootup failed. Brace for errors");
			return null;
		}
		
		//apply viewID to parameter object
		int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
				"tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
		TFObjectDetector.Parameters tfodParameters;
		if (useDisplay) tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
		else tfodParameters = new TFObjectDetector.Parameters();
		
		tfodParameters.minimumConfidence = 0.7;
		
		//create a detector with the viewID
		TFObjectDetector tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
		
		//tell the detector what to detect
		tfod.loadModelFromAsset("Skystone.tflite", "Stone", "Skystone");
		
		return tfod;
	}
	
	public void start() {
		tfod.activate();
		activated = true;
	}
	
	@Override
	public void loop() {
		updateObjects();
	}
	
	public void stop() {
		activated = false;
		tfod.shutdown();
	}
	
	private void updateObjects() {
		List<Recognition> sto = tfod.getUpdatedRecognitions();
		
		if (sto == null) return;
		
		objects = sto;
	}
	
	/**
	 * may be null if see nothing
	 */
	public List<Recognition> getObjects() {
		if (!activated) return null;
		
		return objects;
	}
}
