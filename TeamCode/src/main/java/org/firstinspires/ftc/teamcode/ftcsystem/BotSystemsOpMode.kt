package org.firstinspires.ftc.teamcode.ftcsystem

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.firstinspires.ftc.teamcode.system.BotSystem
import org.firstinspires.ftc.teamcode.system.CoroutineScopeElement
import org.firstinspires.ftc.teamcode.system.Element
import org.firstinspires.ftc.teamcode.system.OpModeElement
import org.futurerobotics.ftcutils.CoroutineOpMode
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * An op mode which runs a [BotSystem], given some [elementsMap].
 *
 * - [OpModeElement] will be _replaced_
 * - [CoroutineScopeElement] will be overriden (or rather it is reused).
 */
abstract class BotSystemsOpMode(
    initialElements: Collection<Element>
) : CoroutineOpMode() {

    constructor(vararg initialElements: Element) : this(initialElements.asList())

    private val elementsMap = initialElements
        .groupBy { it.identifierClass }
        .entries.associateTo(HashMap()) { (cls, list) ->
        if (cls != null)
            require(list.size == 1) { "Cannot have two elements with the same identifier" }
        val element = list.first()
        (cls ?: element) to element
    }

    protected lateinit var botSystem: BotSystem
        private set


    final override fun getCoroutineContext(): CoroutineContext =
        (elementsMap[CoroutineScopeElement::class.java] as CoroutineScopeElement?)?.coroutineContext
            ?: EmptyCoroutineContext

    private fun replaceElement(element: Element) {
        elementsMap[element.identifierClass!!] = element
    }

    final override suspend fun runOpMode() = coroutineScope {
        replaceElement(OpModeElement(this@BotSystemsOpMode))
        replaceElement(CoroutineScopeElement(this))
        botSystem = BotSystem(elementsMap.values)
        botSystem.initSuspend()
        launch {
            additionalRun()
        }
        waitForStart()
        botSystem.start()
    }

    protected open suspend fun additionalRun() {
    }
}
