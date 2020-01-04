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

    private val delegates = mutableListOf<ElementHolder<*>>()
    private val _dependsOn = moreDependsOn.toHashSet()
    final override val dependsOn: Set<Class<out Element>> = Collections.unmodifiableSet(_dependsOn)


    final override fun init(botSystem: BotSystem) {
        delegates.forEach {
            it.init(botSystem)
        }
        moreInit(botSystem)
    }


    protected open fun moreInit(botSystem: BotSystem) {}

    protected val botSystem = BotSystemDelegateProvider()

    protected inner class BotSystemDelegateProvider internal constructor() {
        operator fun provideDelegate(
            thisRef: Any,
            prop: KProperty<*>
        ): ElementDelegate {
            @Suppress("UNCHECKED_CAST")
            val clazz = (prop.returnType.classifier as KClass<out Element>).java
            return ElementDelegateImpl(clazz)
        }
    }

    fun <T : Element, R> gettingFrom(clazz: KClass<T>, get: T.() -> R): ReadOnlyProperty<Any, R> {
        return gettingFrom(clazz.java, get)
    }

    fun <T : Element, R> gettingFrom(clazz: Class<T>, get: T.() -> R): ReadOnlyProperty<Any, R> {
        return ElementGettingDelegate(clazz, get)
    }

    @Suppress("LeakingThis")
    private abstract inner class ElementHolder<T : Element>(clazz: Class<out T>) {

        init {
            delegates += this
            _dependsOn += clazz
        }

        private var clazz: Class<out T>? = clazz

        fun init(botSystem: BotSystem) {
            onInit(botSystem, clazz!!)
            clazz = null
        }

        protected abstract fun onInit(botSystem: BotSystem, clazz: Class<out T>)
    }

    protected interface ElementDelegate {
        operator fun <T : Element> getValue(thisRef: Any, property: KProperty<*>): T
    }

    private inner class ElementDelegateImpl(clazz: Class<out Element>) : ElementHolder<Element>(clazz),
                                                                         ElementDelegate {

        private lateinit var element: Element
        override fun onInit(botSystem: BotSystem, clazz: Class<out Element>) {
            element = botSystem.get(clazz)
        }

        @Suppress("UNCHECKED_CAST")
        override operator fun <T : Element> getValue(thisRef: Any, property: KProperty<*>): T = element as T
    }

    private inner class ElementGettingDelegate<T : Element, R>(
        clazz: Class<out T>,
        private var getter: (T.() -> R)?
    ) : ElementHolder<T>(clazz), ReadOnlyProperty<Any, R> {

        private var value: R? = null
        override fun onInit(botSystem: BotSystem, clazz: Class<out T>) {
            val getter = getter!!
            value = botSystem.get(clazz).getter()
            this.getter = null
        }

        override fun getValue(thisRef: Any, property: KProperty<*>): R = value as R
    }
}
