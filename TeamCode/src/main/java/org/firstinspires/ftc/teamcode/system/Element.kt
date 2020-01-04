package org.firstinspires.ftc.teamcode.system

import android.support.annotation.CallSuper
import kotlinx.coroutines.runBlocking
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


/**
 * An element in a [BotSystem]. It is basically a thing that can be added to a [BotSystem], and later retrieved
 * using [BotSystem.get]. It is identified by a [identifierClass] which is _usually_ the class of the element
 * itself. There can only be one element for one given [identifierClass].
 *
 * An element may also retrieve other elements through the [BotSystem] to interact with them during [init].
 * Related, each may also [dependsOn] other elements; if something that something depends on does not exist, a default
 * will try to be made or else an exception will be thrown. In this way by simply depending on another element that
 * other element will be added, and the element is guaranteed to exist during [init].
 *
 * Defaults can either be made through:
 * - _a class's_ static method with signature exactly `static T createDefault()` where `T` is the same or subclass of
 *   the current class, and any access modifier.
 * - A public no parameters constructor.
 *
 * I would use companion objects instead of classes but we want to make this more java-friendly.
 */
interface Element {

    /**
     * The class used to identify this [Element].
     *
     * This is what is used to identify dependencies in [dependsOn].
     */
    @JvmDefault
    val identifierClass: Class<out Element>?
        get() = this::class.java

    /**
     * A list of elements that this depends on, identified through their [identifierClass]. These will be guaranteed
     * to exist when [init] is called.
     *
     * There can be no circular dependencies.
     *
     * If a dependency does not exist, a default will try to be made. see [Element] doc. If A depends on B, B will
     * have [init] called before A.
     */
    val dependsOn: Set<Class<out Element>>

    /**
     * Initializes, but should not run long-term things yet.
     *
     * Other elements can be retrieved by the given [BotSystem], and will contain everything
     * this [dependsOn].
     *
     * Other [Element]s are only guaranteed to already have been [init]ed if this [dependsOn] it.
     */
    fun init(botSystem: BotSystem)

    companion object {
        private const val DEFAULT_CREATOR_NAME = "createDefault"

        internal fun <T : Element> tryCreateDefault(elementClass: Class<T>): T? {
            val method = kotlin.runCatching {
                elementClass.getDeclaredMethod(DEFAULT_CREATOR_NAME).takeIf { Modifier.isStatic(it.modifiers) }
            }.getOrElse { null }
            if (method != null) {
                if (!elementClass.isAssignableFrom(method.returnType))
                    error(
                        "$elementClass defined a $DEFAULT_CREATOR_NAME method but does not have a return type " +
                                "compatible with itself."
                    )
                method.isAccessible = true
                //rethrow everything
                @Suppress("UNCHECKED_CAST")
                return method.invoke(null) as T
            }
            return if (Modifier.isAbstract(elementClass.modifiers)) null
            else try {
                elementClass.getConstructor().newInstance()
            } catch (e: NoSuchMethodException) {
                null
            } catch (e: InvocationTargetException) {
                throw e.cause!!
            }
        }
    }
}

interface SuspendElement : Element {
    override fun init(botSystem: BotSystem) = runBlocking {
        initSuspend(botSystem)
    }

    suspend fun initSuspend(botSystem: BotSystem)
}

suspend fun Element.initSuspend(botSystem: BotSystem) =
    (this as? SuspendElement)?.initSuspend(botSystem) ?: init(botSystem)

/**
 * Skeletal implementation of [Element].
 *
 * One can declare additional using the [dependsOn] function, during construction, or by passing to super varargs
 *
 * One can also use the functions [botSystem] and [onInit] to get a [Property]
 * that will retrieve values when [init] is called.
 */
abstract class AbstractElement(vararg dependsOn: Class<out Element>) : Element {

    private val delegates = mutableListOf<BotSystemGettingDelegate<*>>()
    private val _dependsOn = dependsOn.toHashSet()
    final override val dependsOn: Set<Class<out Element>> = Collections.unmodifiableSet(_dependsOn)

    @CallSuper
    override fun init(botSystem: BotSystem) {
        delegates.forEach {
            it.init(botSystem)
        }
    }

    /** Declares additional dependencies on the given [classes]. */
    protected fun dependsOn(vararg classes: Class<out Element>) {
        _dependsOn += classes
    }

    /** Declares additional dependencies on the given class */
    @JvmSynthetic
    protected inline fun <reified T : Element> dependsOn() {
        dependsOn(T::class.java)
    }

    /** Declares additional dependencies on the given [classes]. */
    @JvmSynthetic
    protected fun dependsOn(vararg classes: KClass<out Element>) {
        _dependsOn += classes.map { it.java }
    }

    /**
     * Gets a [Property] which will have the value the element by the given [clazz] on init.
     *
     * This also ensures the given [clazz] is a dependency.
     */
    protected fun <T : Element> botSystem(clazz: Class<T>): Property<T> {
        this._dependsOn += clazz
        return BotSystemGettingDelegate { get(clazz) }
    }


    /**
     * Gets a [Property] which will have the value the element by the given [clazz], then called on [getValue], on init.
     *
     * This also ensures the given [clazz] is a dependency.
     */
    protected fun <T : Element, R> botSystem(clazz: Class<T>, getValue: T.() -> R): Property<R> {
        this._dependsOn += clazz
        return BotSystemGettingDelegate { get(clazz).getValue() }
    }

    /** [botSystem] for Kotlin */
    @JvmSynthetic
    protected fun <T : Element> botSystem(clazz: KClass<T>): Property<T> = botSystem(clazz.java)

    /** [botSystem] for Kotlin */
    @JvmSynthetic
    protected fun <T : Element, R> botSystem(clazz: KClass<T>, getValue: T.() -> R): Property<R> =
        botSystem(clazz.java, getValue)

    @JvmSynthetic
    protected inline fun <reified T : Element> botSystem() = botSystem(T::class.java)

    /**
     * Gets a [Property] which will have the value given by the [getValue] on init, with the [botSystem] passed.
     *
     * These will run with properties gotten by [botSystem] in the same order they were declared.
     */
    protected fun <R> onInit(getValue: BotSystem.() -> R): Property<R> = BotSystemGettingDelegate(getValue)

    private object NoValue

    private inner class BotSystemGettingDelegate<R>(
        private var getValue: (BotSystem.() -> R)?
    ) : Property<R> {

        init {
            @Suppress("LeakingThis")
            delegates += this
        }

        private var _value: Any? = NoValue

        fun init(botSystem: BotSystem) {
            check(value === NoValue) { "Double initialization" }
            _value = getValue!!.invoke(botSystem)
            getValue = null
        }

        @Suppress("UNCHECKED_CAST")
        override val value: R
            get() = if (_value === NoValue) error("Not initialized") else _value as R
    }
}

/**
 * Something in which you can retrieve a [value].
 *
 * Can also be used as a kotlin property delegate.
 */
interface Property<T> {

    val value: T

    @JvmSynthetic
    @JvmDefault
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}
