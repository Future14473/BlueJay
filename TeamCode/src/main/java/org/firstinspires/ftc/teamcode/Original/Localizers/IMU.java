package org.firstinspires.ftc.teamcode.Original.Localizers;

import android.graphics.Point;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.Position;

public class IMU implements deltaLocalizer{
    BNO055IMU   imu;
    OpMode opmode;

    Orientation lastAngle = new Orientation();
    Position lastPos = new Position();

    double      deltaAngle;
    Position    deltaPos;

    volatile  boolean activated=false;

    public IMU (OpMode opMode) {

        this.opmode = opMode;

        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();

        parameters.mode = BNO055IMU.SensorMode.IMU;
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.loggingEnabled = false;

        //TODO assumption that that thing is named "imu"
        imu = opmode.hardwareMap.get(BNO055IMU.class, "imu");

        imu.initialize(parameters);

        // make sure the imu gyro is calibrated before continuing.
        while (!imu.isGyroCalibrated()) {
            //wait
        }

    }

    /**
     * Resets the cumulative angle tracking to zero.
     */
    private void resetAngle() {
        lastAngle = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        lastPos = imu.getPosition();

        deltaAngle=0;
        deltaPos=new Position();
    }

    /**
     * Based on the last time we called get angle, take the smallest possible rotation difference
     * trig angles
     */
    private void updatePos() {
        //z axis is the one we want

        Orientation angle = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        Position pos = imu.getPosition();

        //=========ANGLE=============
                double deltaAngle = angle.firstAngle - lastAngle.firstAngle;

                if (deltaAngle < -180)
                    deltaAngle += 360;

                else if (deltaAngle > 180)
                    deltaAngle -= 360;

                this.deltaAngle=deltaAngle;

                lastAngle = angle;
        //========POSITION==========

                deltaPos.x=pos.x-lastPos.x;
                deltaPos.y=pos.y-lastPos.y;

                lastPos = pos;

    }

    public void start(){
        resetAngle();
    }

    public void stop(){
    }

    /**
     * call this as fast as you can!
     * return difference in position to  when you last called it
     * @return defaults to mm for position x and y
     */
    public orientation getDeltaPosition(){
        updatePos();
        return new orientation(deltaPos.x,deltaPos.y,deltaAngle);
    }

    public void print(double angle){
        if(activated){
            opmode.telemetry.addData("IMU (angle)",angle);
        }else{
            opmode.telemetry.addData("IMU","Offline");
        }
    }

}
