package org.firstinspires.ftc.teamcode.bot

import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.system.DelegatesElement
import org.firstinspires.ftc.teamcode.system.Element
import org.firstinspires.ftc.teamcode.system.OpModeElement
import org.futurerobotics.jargon.ftcbridge.FtcMotor
import org.futurerobotics.jargon.util.asUnmodifiableList
import org.futurerobotics.jargon.util.uncheckedCast
import kotlin.properties.ReadOnlyProperty

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
    DelegatesElement(*moreDependsOn, OpModeElement::class.java) {

    private inline fun <R> hardwareMap(crossinline getter: HardwareMap.() -> R): ReadOnlyProperty<Any, R> =
        botSystem(OpModeElement::class) { opMode.hardwareMap.getter() }

    private fun <T : Any> Array<out HardwareMapConfig<T>>.getAllOrNull(): ReadOnlyProperty<Any, List<T>?> =
        hardwareMap {
            val map = this@getAllOrNull.map { tryGetAll(it) }
            noNullsOrNull(map)
        }

    val hardwareMap: HardwareMap by botSystem(OpModeElement::class) { opMode.hardwareMap }

    val wheelMotors by wheelConfigs.getAllOrNull()

    val liftsMotors by liftConfigs.getAllOrNull()

    val intakeMotors by intakeConfigs.getAllOrNull()

    val imu by hardwareMap { tryGet(BNO055IMU::class.java, imuName) }
}

private fun <T : Any> noNullsOrNull(map: List<T?>): List<T>? =
    map.takeIf { it.none { it == null } }
        ?.asUnmodifiableList()
        .uncheckedCast()

interface HardwareMapConfig<T> {
    fun tryGetFrom(map: HardwareMap): T?
}

data class SimpleMotorConfig(
    val name: String,
    val direction: DcMotorSimple.Direction
) : HardwareMapConfig<DcMotorEx> {

    override fun tryGetFrom(map: HardwareMap): DcMotorEx? =
        map.tryGet(DcMotorEx::class.java, name)
            ?.also { it.direction = direction }
}

data class MotorConfig(
    val name: String,
    val direction: DcMotorSimple.Direction,
    val ticksPerRev: Double
) : HardwareMapConfig<FtcMotor> {

    override fun tryGetFrom(map: HardwareMap): FtcMotor? {
        val motor = map.tryGet(DcMotorEx::class.java, name) ?: return null
        motor.direction = direction
        return FtcMotor(motor, ticksPerRev)
    }
}

fun <T> HardwareMap.tryGetAll(hardwareMapConfig: HardwareMapConfig<T>) = hardwareMapConfig.tryGetFrom(this)
