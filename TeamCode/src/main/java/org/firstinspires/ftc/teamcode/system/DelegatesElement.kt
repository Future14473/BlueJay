package org.firstinspires.ftc.teamcode.system

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * An [Element] that allows getting parts and dependencies and via kotlin delegates.
 *
 * By saying `val someElement: SomeElement by botSystem`, a dependency on SomeElement will automatically
 * be added and the value will be filled upon init.
 *
 * Java people please silently ignore.
 */
abstract class DelegatesElement(vararg moreDependsOn: Class<out Element>) : Element {

    private val delegates = mutableListOf<BotSystemDelegateImpl>()
    private val _dependsOn = moreDependsOn.toHashSet()
    override val dependsOn: Set<Class<out Element>> = Collections.unmodifiableSet(_dependsOn)

    protected val botSystem = BotSystemDelegateProvider()

    protected inner class BotSystemDelegateProvider internal constructor() {
        operator fun provideDelegate(
            thisRef: Any,
            prop: KProperty<*>
        ): BotSystemDelegate {
            @Suppress("UNCHECKED_CAST")
            val clazz = (prop.returnType.classifier as KClass<out Element>).java
            return BotSystemDelegateImpl(clazz).also {
                _dependsOn += clazz
                delegates += it
            }
        }
    }

    protected interface BotSystemDelegate {
        operator fun <T : Element> getValue(thisRef: Any, property: KProperty<*>): T
    }

    private class BotSystemDelegateImpl(clazz: Class<out Element>) : BotSystemDelegate {

        private var clazz: Class<out Element>? = clazz
        private lateinit var value: Element
        @Suppress("UNCHECKED_CAST")
        override operator fun <T : Element> getValue(thisRef: Any, property: KProperty<*>): T = value as T

        fun fillElement(botSystem: BotSystem) {
            value = botSystem.get(clazz!!)
            clazz = null
        }
    }

    final override fun init(botSystem: BotSystem) {
        delegates.forEach {
            it.fillElement(botSystem)
        }
        moreInit(botSystem)
    }


    protected open fun moreInit(botSystem: BotSystem) {}
}
