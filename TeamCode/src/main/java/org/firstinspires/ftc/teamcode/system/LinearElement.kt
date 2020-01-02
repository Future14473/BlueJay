package org.firstinspires.ftc.teamcode.system

import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * An element made to resemble LinearOpMode as closely as possible. Like
 * most of the code is copied over.
 *
 * This still uses coroutines for cancellation/exception handling behind the scenes.
 *
 * @see CoroutineElement for a coroutine variant of this
 */
abstract class LinearElement(vararg dependsOn: Class<out Element>) :
    AbstractElement(*dependsOn, CoroutineScopeElement::class.java), StartableElement {

    private var executorService: ExecutorService? = null
    private val startedLatch = CountDownLatch(1)
    private lateinit var scope: CoroutineScopeElement

    /**
     * Performs possible additional initialization.
     */
    protected abstract fun moreInit(botSystem: BotSystem)

    /**
     * Override this method and place your code here.
     *
     * Please do not swallow the InterruptedException, as it is used in cases
     * where the op mode needs to be terminated early.
     * @throws InterruptedException
     */
    @Throws(InterruptedException::class)
    protected abstract fun runElement()

    /**
     * Pauses the Linear Op Mode until start has been pressed or until the current thread
     * is interrupted.
     */
    protected fun waitForStart(): Unit = try {
        startedLatch.await()
    } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
    }

    /**
     * Puts the current thread to sleep for a bit as it has nothing better to do. This allows other
     * threads in the system to run.
     *
     *
     * One can use this method when you have nothing better to do in your code as you await state
     * managed by other threads to change. Calling idle() is entirely optional: it just helps make
     * the system a little more responsive and a little more efficient.
     *
     *
     * [idle] is conceptually related to waitOneFullHardwareCycle(), but makes no
     * guarantees as to completing any particular number of hardware cycles, if any.
     *
     * @see opModeIsActive
     */
    protected fun idle(): Unit = Thread.yield()


    /**
     * Sleeps for the given amount of milliseconds, or until the thread is interrupted. This is
     * simple shorthand for the operating-system-provided [sleep()][Thread.sleep] method.
     *
     * @param milliseconds amount of time to sleep, in milliseconds
     * @see Thread.sleep
     */
    protected fun sleep(milliseconds: Long): Unit = delay(milliseconds)

    /** [sleep], to make migration a bit easier */
    protected fun delay(milliseconds: Long): Unit = try {
        Thread.sleep(milliseconds)
    } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
    }


    /**
     * Answer as to whether this opMode is active and the robot should continue onwards. If the
     * opMode is not active, the OpMode should terminate at its earliest convenience.
     *
     *
     * Note that internally this method calls [.idle]
     *
     * @return whether the OpMode is currently active. If this returns false, you should
     * break out of the loop in your [.runOpMode] method and return to its caller.
     * @see runElement
     * @see isStarted
     * @see isStopRequested
     */
    protected fun opModeIsActive(): Boolean {
        val isActive = !this.isStopRequested() && this.isStarted()
        if (isActive) idle()
        return isActive
    }


    /**
     * Has the opMode been started?
     *
     * @return whether this opMode has been started or not
     * @see opModeIsActive
     * @see isStopRequested
     */
    protected fun isStarted(): Boolean = startedLatch.count == 0L || isStopRequested()

    /**
     * Has the the stopping of the opMode been requested?
     *
     * @return whether stopping opMode has been requested or not
     * @see opModeIsActive
     * @see isStarted
     */
    protected fun isStopRequested(): Boolean = Thread.currentThread().isInterrupted

    /**
     * Requests to stop the entire op mode.
     *
     * Callable from a not-OpMode.
     */
    protected fun requestOpModeStop() {
        scope.cancel("Request stop from LinearElement")
    }

    final override fun init(botSystem: BotSystem) {
        executorService = Executors.newSingleThreadExecutor()
        scope = botSystem.get(CoroutineScopeElement::class.java)
        moreInit(botSystem)
        scope.launch {
            runInExecutorAndWait(executorService!!) {
                runElement()
            }
        }
        scope.job.invokeOnCompletion {
            executorService?.shutdownNow()
        }
    }

    final override fun onStart(botSystem: BotSystem) {
        startedLatch.countDown()
    }
}
