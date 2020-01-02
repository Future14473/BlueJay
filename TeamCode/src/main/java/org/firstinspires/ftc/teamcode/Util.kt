package org.firstinspires.ftc.teamcode

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.coroutineContext

@Suppress("NOTHING_TO_INLINE")
inline fun illArg(message: String): Nothing = throw IllegalArgumentException(message)


suspend fun recoverScope() = CoroutineScope(coroutineContext)

suspend inline fun <T> withCoroutineScope(block: CoroutineScope.() -> T): T = recoverScope().run(block)
