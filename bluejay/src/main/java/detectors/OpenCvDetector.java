package detectors;

import android.graphics.Bitmap;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvInternalCamera;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.ArrayList;
import java.util.List;

import detectors.FoundationPipeline.Foundation;
import detectors.FoundationPipeline.Pipeline;
import detectors.FoundationPipeline.SkyStone;
import detectors.FoundationPipeline.Stone;

public class OpenCvDetector extends StartStoppable {

	//Originally in RobotControllerActivity, but caused the camera shutter to make weird noises, so now it lives here
	static {
		//OpenCVLoader.initDebug();
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	//This is a reference to the camera
	private OpenCvCamera phoneCam;

	//OpMode
	OpMode OpMode;

	private List<Foundation> foundations = new ArrayList<>(); //detected foundations
	private List<Stone>      stones      = new ArrayList<>();
	private List<SkyStone>   skyStones   = new ArrayList<>();

	public  OpenCvDetector (OpMode opmode) {
		OpMode = opmode;

		//init EOCV
		int cameraMonitorViewId = OpMode.hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", OpMode.hardwareMap.appContext.getPackageName());
		phoneCam = OpenCvCameraFactory.getInstance().createInternalCamera(OpenCvInternalCamera.CameraDirection.BACK, cameraMonitorViewId);

		Pipeline.doFoundations = false;
		Pipeline.doStones = false;
		Pipeline.doSkyStones = true;

		phoneCam.setPipeline(new OpenCvPipeline() {
			@Override
			public Mat processFrame(Mat input) {
				return Pipeline.process(input);
			}
		});

		phoneCam.openCameraDevice();
	}

	@Override
	public void loop() {
		//will be called repeatedly when detector is active
		foundations = Pipeline.foundations;
		stones = Pipeline.stones;
		skyStones = Pipeline.skyStones;
	}

	@Override
	public void begin() {
		phoneCam.startStreaming(640, 480, OpenCvCameraRotation.UPRIGHT);
	}

	@Override
	public void end() {
		phoneCam.stopStreaming();
		phoneCam.closeCameraDevice();
	}

	/*
	 * hold the phone sideways w/ camera on right
	 * x: 0 at the top, increases as you go down
	 * y: 0 at the right, increases as you go left
	 */

	public Foundation[] getFoundations() {
		if (!activated) throw new IllegalStateException("Not activated");

		return foundations.toArray(new Foundation[0]);
	}

	public Stone[] getStones() {
		if (!activated) throw new IllegalStateException("Not activated");

		return stones.toArray(new Stone[0]);
	}

	public SkyStone[] getSkyStones() {
		if (!activated) throw new IllegalStateException("Not activated");

		return skyStones.toArray(new SkyStone[0]);
	}
}
