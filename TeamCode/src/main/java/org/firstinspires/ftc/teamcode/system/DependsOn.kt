package org.firstinspires.ftc.teamcode.system

/**
 * An [Element] which only contains [dependsOn] values.
 */
class DependsOn(vararg dependsOn: Class<out Element>) : AbstractElement(*dependsOn) {

    override val identifierClass: Class<out Element>? get() = null

    override fun init(botSystem: BotSystem) {
    }
}
