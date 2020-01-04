package org.firstinspires.ftc.teamcode.tests

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.util.RobotLog
import org.firstinspires.ftc.teamcode.ftcsystem.BotSystemsOpMode
import org.firstinspires.ftc.teamcode.ftcsystem.OpModeElement
import org.firstinspires.ftc.teamcode.system.AbstractElement

@TeleOp
class TestOpMode : BotSystemsOpMode(Something()) {

    override suspend fun additionalRun() {
        botSystem.get<Something>().hey()
    }
}

private class Something : AbstractElement() {

    private val opMode by botSystem(OpModeElement::class) { opMode }
    private val hardwareMap by lazy { opMode.hardwareMap!! }

    fun hey() {
        hardwareMap.forEach {
            RobotLog.d("%s: %s", hardwareMap.getNamesOf(it).first(), it.deviceName)
        }
    }
}
