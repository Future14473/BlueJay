package org.firstinspires.ftc.teamcode.system

import java.util.*
import kotlin.properties.ReadOnlyProperty
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

    private val delegates = mutableListOf<BotSystemGettingDelegate<*>>()
    private val _dependsOn = moreDependsOn.toHashSet()
    final override val dependsOn: Set<Class<out Element>> = Collections.unmodifiableSet(_dependsOn)


    final override fun init(botSystem: BotSystem) {
        delegates.forEach {
            it.init(botSystem)
        }
        moreInit(botSystem)
    }


    protected open fun moreInit(botSystem: BotSystem) {}

    fun <T : Element, R> botSystem(clazz: KClass<T>, getValue: T.() -> R): ReadOnlyProperty<Any, R> {
        val java = clazz.java
        this._dependsOn += java
        return BotSystemGettingDelegate { get(java).getValue() }
    }

    fun <T : Element> botSystem(clazz: KClass<T>): ReadOnlyProperty<Any, T> {
        val java = clazz.java
        this._dependsOn += java
        return BotSystemGettingDelegate { get(java) }
    }

    inline fun <reified T : Element> botSystem() = botSystem(T::class)

    fun <R> onInit(getter: BotSystem.() -> R): ReadOnlyProperty<Any, R> {
        return BotSystemGettingDelegate(getter)
    }

    private object NoValue


    private inner class BotSystemGettingDelegate<R>(
        private var getter: (BotSystem.() -> R)?
    ) : ReadOnlyProperty<Any, R> {

        init {
            @Suppress("LeakingThis")
            delegates += this
        }

        private var value: Any? = NoValue

        fun init(botSystem: BotSystem) {
            check(value === NoValue) { "Double initialization" }
            val getter = getter!!
            value = botSystem.getter()
            this.getter = null
        }

        @Suppress("UNCHECKED_CAST")
        override fun getValue(thisRef: Any, property: KProperty<*>): R {
            check(value !== NoValue) { "Element not initialized!" }
            return value as R
        }
    }
}
