package org.firstinspires.ftc.teamcode.imageprocessing;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.opencv.core.Mat;

import detectors.FoundationPipeline.SkyStone;
import detectors.OpenCvDetector;

/*

    If you're using this library, THANKS! I spent a lot of time on it.

    However, stuff isn't as well-documented as I like...still working on that

    So if you have questions, email me at xchenbox@gmail.com and I will get back to you in about a day (usually)

    Enjoy!
*/

@TeleOp(name = "CV Simulator", group = "Auto")
public class SimpleAlign extends OpMode {

	OpenCvDetector fieldElementDetector;

	@Override
	public void init() {

		telemetry.setAutoClear(true);
		fieldElementDetector = new OpenCvDetector(this);
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
		fieldElementDetector.start();

	}

	/*
	 * Code to run REPEATEDLY when the driver hits PLAY
	 */
	@Override
	public void loop() {
		//get X position of first SkyStone
		int xpos;

		//Skystone order in Array is left to right
		SkyStone[] elements = fieldElementDetector.getSkyStones();

		//Array will be empty if nothing is detected
		if(elements.length == 0) {
			telemetry.addLine("Nothing found");
		} else {
			xpos = (int) elements[0].x;
			telemetry.addData("Position",xpos);
			moveRobot(xpos);
		}

		telemetry.update();
	}

	private void moveRobot(int xpos) {
		//The center of the screen has x value of 320
		int difference = xpos-320;

		//75 pixels ought to be close enough
		if(Math.abs(difference) > 75) {
			if(difference > 0) {
				//Strafe right
			} else {
				//Strafe Left
			}
		}
	}

	/*
	 * Code to run ONCE after the driver hits STOP
	 */
	@Override
	public void stop() {
		fieldElementDetector.stop();
	}
}

