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

package org.firstinspires.ftc.teamcode.Original;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.tfod.Recognition;

import java.util.List;

@TeleOp(name = "Concept: TensorFlow Object Detection", group = "Concept")
public class TensorFlowDetectionDEMO extends LinearOpMode {

    @Override
    public void runOpMode() {
        ImageDetector imager = new ImageDetector(this, false);
        StoneDetector stone = new StoneDetector(imager, this, true);

        //start streaming UI and activate detection loop
        stone.start();

        waitForStart();

        while (opModeIsActive()) {


            //get recognitions
            List<Recognition> updatedRecognitions = stone.getObjects();

            if (updatedRecognitions != null) continue;

            // step through the list of recognitions and display boundary info.
            for (Recognition recognition : updatedRecognitions) {
                telemetry.addData("Recognition Label", recognition.getLabel());
                telemetry.addData("Left Top", (recognition.getLeft()) + (recognition.getTop()));
                telemetry.addData("Right Bottom", (recognition.getRight()) + (recognition.getBottom()));

                /*
                 * additional features:
                 * Confidence, height, Width, Angle To Object (AngleUnit)
                 * Ex: double degrees = recognition.estimateAngleToObject(AngleUnit.DEGREES);
                 */
            }

            telemetry.update();
        }

        //the ftcapp cannot stop the TFod thread! YOU must do it.
        stone.stop();

    }

}
