package org.firstinspires.ftc.teamcode.bot

import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.ftcsystem.OpModeElement
import org.firstinspires.ftc.teamcode.system.AbstractElement
import org.firstinspires.ftc.teamcode.system.Element
import org.firstinspires.ftc.teamcode.system.Property

private const val imuName = "imu"

private val wheelConfigs = run {
    val tpr = 383.6
    arrayOf(
        MotorConfig("FrontLeft", DcMotorSimple.Direction.REVERSE, tpr),
        MotorConfig("FrontRight", DcMotorSimple.Direction.FORWARD, tpr),
        MotorConfig("BackLeft", DcMotorSimple.Direction.REVERSE, tpr),
        MotorConfig("BackRight", DcMotorSimple.Direction.FORWARD, tpr)
    )
}

private val liftConfigs = run {
    val tpr = 537.6
    arrayOf(
        MotorConfig("LiftLeft", DcMotorSimple.Direction.REVERSE, -tpr),
        MotorConfig("LiftRight", DcMotorSimple.Direction.FORWARD, tpr)
    )
}
private val intakeConfigs = arrayOf(
    SimpleMotorConfig("IntakeLeft", DcMotorSimple.Direction.FORWARD),
    SimpleMotorConfig("IntakeRight", DcMotorSimple.Direction.REVERSE)
)


class HardwareMapElement(vararg moreDependsOn: Class<out Element>) :
    AbstractElement(*moreDependsOn) {

    private inline fun <R> hardwareMap(crossinline getter: HardwareMap.() -> R): Property<R> =
        botSystem(OpModeElement::class) { opMode.hardwareMap.getter() }

    private fun <T : Any> Array<out HardwareMapConfig<T>>.getAllOrNull() =
        hardwareMap { tryGetAll(asList()) }

    val hardwareMap: HardwareMap by botSystem(OpModeElement::class) { opMode.hardwareMap }

    val wheelMotors by wheelConfigs.getAllOrNull()

    val liftsMotors by liftConfigs.getAllOrNull()

    val intakeMotors by intakeConfigs.getAllOrNull()

    val imu by hardwareMap { tryGet(BNO055IMU::class.java, imuName) }
}
