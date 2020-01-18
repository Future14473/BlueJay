package org.firstinspires.ftc.teamcode.imageprocessing;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.opencv.core.Mat;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvInternalCamera;
import org.openftc.easyopencv.OpenCvPipeline;

import detectors.FoundationPipeline.Pipeline;

/*

    If you're using this library, THANKS! I spent a lot of time on it.

    However, stuff isn't as well-documented as I like...still working on that

    So if you have questions, email me at xchenbox@gmail.com and I will get back to you in about a day (usually)

    Enjoy!

    Below is the code to display to the RC; thanks DogeCV! I tried easyOpenCV, but it was lagging and stuttering. (??)
	If it crashes after about a minute, it's probably because OpenCV is using too much native memory. My solution
	is to call System.gc() whenever it reaches 70% (works on my g4 play) , but if someone knows more please contact me.
 */

@TeleOp(name = "CV Simulator", group = "Auto")
public class CVDisplay extends OpMode {

	OpenCvCamera phoneCam;


	@Override
	public void init() {
		telemetry.setAutoClear(true);

		//intit EOCV
		int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
		phoneCam = OpenCvCameraFactory.getInstance().createInternalCamera(OpenCvInternalCamera.CameraDirection.BACK, cameraMonitorViewId);

		Pipeline.doFoundations = false;
		Pipeline.doStones = false;
		Pipeline.doSkyStones = true;

		phoneCam.openCameraDevice();

		phoneCam.setPipeline(new OpenCvPipeline() {
			@Override
			public Mat processFrame(Mat input) {
				//return input;
				return Pipeline.process(input);
			}
		});

		phoneCam.startStreaming(640*1, 480*1, OpenCvCameraRotation.UPRIGHT);
	}
	/*
	 * Code to run REPEATEDLY when the driver hits INIT
	 */
	@Override
	public void init_loop() {
	}

	/*
	 * Code to run ONCE when the driver hits PLAY
	 */
	@Override
	public void start() {

	}

	/*
	 * Code to run REPEATEDLY when the driver hits PLAY
	 */
	@Override
	public void loop() {
		telemetry.update();
	}

	/*
	 * Code to run ONCE after the driver hits STOP
	 */
	@Override
	public void stop() {

	}
}
