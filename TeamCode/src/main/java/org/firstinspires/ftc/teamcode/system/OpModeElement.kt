package org.firstinspires.ftc.teamcode.system

import com.qualcomm.robotcore.eventloop.opmode.OpMode

/**
 * An element which simply contains an [opMode].
 *
 * This does not have a default.
 */
class OpModeElement(val opMode: OpMode) : AbstractElement() {

    override fun init(botSystem: BotSystem) {
    }
}
