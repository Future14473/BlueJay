package org.firstinspires.ftc.teamcode.bot

import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import org.futurerobotics.jargon.ftcbridge.FtcMotor

/**
 * Something which can be extracted from a [HardwareMap].
 */
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

fun <T> HardwareMap.tryGet(config: HardwareMapConfig<T>) = config.tryGetFrom(this)
fun <T> HardwareMap.tryGetAll(list: List<HardwareMapConfig<T>>): List<T>? {
    val result = ArrayList<T>(list.size)
    list.forEach {
        val t = tryGet(it) ?: return null
        result += t
    }
    return result
}
