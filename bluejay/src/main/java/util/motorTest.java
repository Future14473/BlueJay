package util;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name = "motor test",group = "teleop")

public class motorTest extends LinearOpMode {
	@Override
	public void runOpMode() throws InterruptedException {
		telemetry.addLine("Hello Dere! Use Controller 1 left/right stick up/down to control motors");
		telemetry.update();
		waitForStart();

		DcMotor motor = hardwareMap.get(DcMotor.class, "left");
		DcMotor motor2 = hardwareMap.get(DcMotor.class, "right");

		motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		motor2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
		motor2.setDirection(DcMotorSimple.Direction.REVERSE);

		telemetry.addData("motor 1 status",motor);
		telemetry.addData("motor 2 status", motor2);
		telemetry.update();

		while (opModeIsActive()) {
			if(Math.abs(gamepad1.left_stick_y) > 0.1) {
				motor.setPower(gamepad1.left_stick_y);
			}else{
				motor.setPower(0);
			}

			if(Math.abs(gamepad1.right_stick_y) > 0.1) {
				motor2.setPower(gamepad1.right_stick_y);
			}else{
				motor2.setPower(0);
			}
		}
	}
}
