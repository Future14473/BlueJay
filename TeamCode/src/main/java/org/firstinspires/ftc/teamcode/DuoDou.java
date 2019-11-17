/* Copyright (c) 2019 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Original.detectors.ImageDetector;
import org.firstinspires.ftc.teamcode.Original.detectors.OpencvDetector;
import org.firstinspires.ftc.teamcode.Original.detectors.StoneDetector;

import java.util.Iterator;

@TeleOp(name = "The Three <<Holy Systems>>", group = "Primordial Artifact")
public class DuoDou extends LinearOpMode {

    public void runOpMode() throws InterruptedException {
//        try {
//            ExpansionHubEx hub = new ExpansionHubEx((LynxModule) (hardwareMap.get("Expansion Hub Portal 1")));
//            hub.setPhoneChargeEnabled(true);
//            telemetry.addData("Charging set to", hub.isPhoneChargeEnabled());
//            telemetry.update();
//        }catch(IllegalArgumentException e){
//            telemetry.addData("excpetion!", e.toString());
//            telemetry.update();
//        }
        telemetry.setAutoClear(true);

        telemetry.addData("Booting Up"," . . .");
        telemetry.update();

        OpencvDetector foundation = new OpencvDetector(this);
        ImageDetector detector = new ImageDetector(this, false);
        StoneDetector stone = new StoneDetector(this, false);

        stone.start();
        detector.start();
        foundation.start();
        //imu.start();

        while (!isStopRequested()) {
            detector.printposition(detector.getPosition());

            foundation.print(foundation.getObjects());

            stone.print(stone.getObjects());

            //imu.printposition(imu.getDeltaPosition());

            telemetry.addData("==========","Loop delimiter");

            telemetry.update();
        }

        // Disable Tracking when we are done
        detector.stop();
        stone.stop();
        foundation.stop();
        //imu.stop();
    }

    public void listhardware(){
        telemetry.setAutoClear(false);

        Iterator<com.qualcomm.robotcore.hardware.HardwareDevice> t = hardwareMap.iterator();
        while(t.hasNext()){

            telemetry.addData("device found",(t.next().getDeviceName()));
            telemetry.update();
        }

        telemetry.setAutoClear(true);

    }
}
