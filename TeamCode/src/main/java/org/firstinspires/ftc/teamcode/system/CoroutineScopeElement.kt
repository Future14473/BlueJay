package org.firstinspires.ftc.teamcode.system

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A [Element] which contains a [CoroutineContext], which will have a [Job]. This will supervise all coroutines in
 * a [BotSystem].
 *
 * The default uses an [EmptyCoroutineContext].
 *
 * Since coroutines are just so well done in terms of task cancellation and coordination and bridging with threads too,
 * I've decided to make everything run using coroutines.
 */
class CoroutineScopeElement
@JvmOverloads constructor(
    context: CoroutineContext = EmptyCoroutineContext
) : AbstractElement(), CoroutineScope {

    constructor(scope: CoroutineScope) : this(scope.coroutineContext)

    override val coroutineContext = if (context[Job] != null) context else context + Job()

    val job get() = coroutineContext[Job]!!
}
