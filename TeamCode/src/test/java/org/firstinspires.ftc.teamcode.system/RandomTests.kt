package org.firstinspires.ftc.teamcode.system

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test

internal class RandomTests {
    @Test
    fun whatHappensInRunBlockingThrow() {
        val job = Job()
        job.cancel()
        runBlocking {
            job.join()
        }
    }

    @Test
    fun exceptional() {
        val handler = CoroutineExceptionHandler { _, e ->
            e.printStackTrace(System.out)
        }
        val scope = CoroutineScope(handler)
        scope.launch {
            delay(Long.MAX_VALUE)
        }

        scope.launch {
            throw Exception()
        }
        runBlocking {
            scope.coroutineContext[Job]!!.join()
        }
    }

    @Test//init
    fun runFromAnotherScope() = runBlocking<Unit> {
        withTimeout(5000) {
            launch {
                //runOpMode
                coroutineScope {
                    val scopeElement = this

                    //start
                    runBlocking(scopeElement.coroutineContext) {
                        //start that runs task
                        launch {
                            println("Launching background task...")
                            scopeElement.launch {
                                delay(Long.MAX_VALUE)
                            }
                        }.invokeOnCompletion {
                            println("Background task done")
                        }
                    }
                    println("runBlocking done")
                    yield()
                    println("Cancelling...")
                    this.cancel()

                    println("Did cancel...")
                }
                println("Scope done")
            }
        }

    }
// yes, it does.
//    @Test
//    fun doesRunBlockingWait() = runBlocking<Unit> {
//        launch {
//            delay(10000)
//        }
//    }
}
