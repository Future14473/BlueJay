package org.firstinspires.ftc.teamcode.system

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean

private class WrappedCancellationException(e: CancellationException) : Throwable(e)

/**
 * Switches from suspend to blocking world. Runs the given [callable] in the the given executor,
 * and suspends current coroutine until it is done.
 *
 * When this coroutine's job is cancelled:
 * - If the callable has already started running, this will cancel the callable _with interrupt_ using
 *   [Future.cancel], and _will wait_ for the task to complete before returning after cancelled.
 * - If the callable throws [InterruptedException] _AFTER_ it is cancelled, it will be ignored (interpreted as
 *    a successful cancellation).
 * - Any other exception thrown by the callable during waiting for cancellation will be rethrown.
 * - Otherwise after successful cancellation this will throw the appropriate [CancellationException].
 */
suspend fun <T> runInExecutorAndWait(executor: ExecutorService, callable: Callable<T>): T {
    val deferred = CompletableDeferred<T>() // holds result
    val canRun = AtomicBoolean(true) // Handles corner case where cancelled before run.
    val future = executor.submit {
        try {
            if (canRun.getAndSet(false))
                deferred.complete(callable.call())
        } catch (e: Throwable) {
            val realE = e.let {
                if (it !is CancellationException) it
                else WrappedCancellationException(it) //wrap to distinguish from actual cancellation
            }
            deferred.completeExceptionally(realE)
        }
    }
    try {
        return deferred.await()
    } catch (e: WrappedCancellationException) {
        throw e.cause!!
    } catch (c: CancellationException) { //actual cancellation, not thrown cancellation.
        if (!canRun.getAndSet(false)) { // already running
            future.cancel(true)
            withContext(NonCancellable) {
                try {
                    deferred.await()
                } catch (ignored: InterruptedException) {
                }
            }
        }
        throw c
    }
}

/**
 * [runInExecutorAndWait]
 */
suspend inline fun <T> runInExecutorAndWait(executor: ExecutorService, crossinline callable: () -> T): T =
    runInExecutorAndWait(executor, Callable { callable() })
