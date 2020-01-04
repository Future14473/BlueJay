package org.firstinspires.ftc.teamcode.system

import android.support.annotation.CallSuper
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

/**
 * An element that runs a suspend function on init, and has utilities for wait for start, etc, just like
 * CoroutineOpMode.
 *
 * This is also a [DelegatesElement] since coroutines are kotlin only.
 *
 * @see LinearElement for a variant like LinearOpMode instead (uses blocking, and has a dedicated thread).
 */
abstract class CoroutineElement(vararg dependsOn: Class<out Element>) :
    AbstractElement(*dependsOn), StartableElement {

    private val scope: CoroutineScopeElement by botSystem()
    private val startedJob = Job()

    /**
     * Override this method and place your awesome coroutine code here.
     *
     * The coroutine may be cancelled if stop is pressed. In this case,`CancellationException` will be
     * thrown whenever suspend functions that wait are called. ***This is different from LinearOpMode***, it is
     * better practice with coroutines, so be mindful whenever you have a suspending function.
     *
     * One may typically start this function using `= coroutineScope { ... }` to launch a series of coroutines that
     * will live and die together (see [coroutineScope]).
     */
    protected abstract suspend fun runElement()

    /**
     * Suspends the current coroutine op mode until start has been pressed.
     *
     * Can be called from _any_ coroutine.
     *
     * @throws CancellationException if coroutine is cancelled.
     */
    protected suspend fun waitForStart() {
        startedJob.join()
    }

    /**
     * Allows other coroutines to run a bit, when you have nothing to do (calls [yield]).
     *
     * Spin-waiting is generally discouraged for coroutines, but sometimes you have no better option.
     *
     * @throws CancellationException if coroutine is cancelled.
     */
    @Throws(CancellationException::class)
    protected suspend fun idle() {
        yield()
    }

    /**
     * Sleeps for the given amount of milliseconds, or until the coroutine is cancelled.
     *
     * This simply calls [delay].
     *
     * @throws CancellationException if coroutine is cancelled.
     */
    @Throws(CancellationException::class)
    protected suspend fun sleep(milliseconds: Long) {
        delay(milliseconds)
    }

    /**
     * If is started and still running.
     *
     * This will [idle] (call [yield]) if is active, as this is intended for use in loops.
     *
     * *This wil __NOT__ throw cancellation exception if cancelled.*
     */
    protected suspend fun isActive(): Boolean {
        val isActive = isStarted && coroutineContext.isActive
        if (isActive)
            try {
                idle()
            } catch (_: CancellationException) {
            }
        return isActive
    }

    /** Requests to stop the entire system, __not just this element__. To stop just this element, exit [runElement]. */
    protected fun requestOpModeStop() {
        scope.cancel("Request stop from CoroutineElement")
    }

    /**
     * Have we started yet?
     * @see waitForStart
     */
    protected val isStarted: Boolean get() = startedJob.isCompleted

    @CallSuper
    final override fun init(botSystem: BotSystem) {
        super.init(botSystem)
        scope.launch {
            runElement()
        }
    }

    final override fun onStart(botSystem: BotSystem) {
        startedJob.complete()
    }
}
