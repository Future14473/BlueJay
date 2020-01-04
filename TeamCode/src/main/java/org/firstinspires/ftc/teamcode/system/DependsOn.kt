package org.firstinspires.ftc.teamcode.system

import kotlin.reflect.KClass

/**
 * An [Element] which only contains [dependsOn] values.
 */
class DependsOn : AbstractElement {

    constructor(vararg dependsOn: Class<out Element>) : super(*dependsOn)
    constructor(vararg dependsOn: KClass<out Element>) : super(*dependsOn.map { it.java }.toTypedArray())

    override val identifierClass: Class<out Element>? get() = null
}
