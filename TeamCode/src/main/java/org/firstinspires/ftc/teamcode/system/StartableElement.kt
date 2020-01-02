package org.firstinspires.ftc.teamcode.system

/**
 * An [Element] that can be [onStart]ed.
 *
 * This should NOT be a long operation (only kick starts something else).
 *
 * We suggest finding a [CoroutineScopeElement] to launch things for coroutine stuff.
 */
interface StartableElement : Element {

    /**
     * Called on start. Should not be a long operation (can kick off something else).
     */
    fun onStart(botSystem: BotSystem)
}
