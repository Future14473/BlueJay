package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

public class Test extends LinearOpMode {
	@Override
	public void runOpMode() throws InterruptedException {
		telemetry.addLine("HELLO");
		telemetry.update();
		Thread.sleep(10000000);
	}
}
