package org.firstinspires.ftc.teamcode.ftcsystem

import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.system.DelegatesElement
import org.firstinspires.ftc.teamcode.system.Element
import org.firstinspires.ftc.teamcode.system.OpModeElement
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class HardwareMapElement(vararg moreDependsOn: Class<out Element>) :
    DelegatesElement(*moreDependsOn, OpModeElement::class.java) {

    protected val hardwareMap: HardwareMap by gettingFrom(OpModeElement::class) { opMode.hardwareMap }

    protected fun <R : Any> hardwareMap(getter: HardwareMap.() -> R): ReadOnlyProperty<Any, R> =
        object : ReadOnlyProperty<Any, R> {
            private var getter: (HardwareMap.() -> R)? = getter
            private lateinit var value: R
            override fun getValue(thisRef: Any, property: KProperty<*>): R {
                if (!::value.isInitialized) {
                    value = this.getter!!.let { hardwareMap.it() }
                    this.getter = null
                }
                return value
            }
        }
}
