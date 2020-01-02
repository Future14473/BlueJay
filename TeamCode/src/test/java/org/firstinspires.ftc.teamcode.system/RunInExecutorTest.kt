package org.firstinspires.ftc.teamcode.system

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

class RunInExecutorTest {


    @Test
    fun willItWait() {
        val executor = Executors.newSingleThreadExecutor()

        runBlocking {
            val backgroundLaunch = launch {
                runInExecutorAndWait(executor) {
                    try {
                        println("B: Running and sleeping")
                        Thread.sleep(100)
                    } catch (e: InterruptedException) {
                        println("B: Interrupted, sleeping again")
                        Thread.sleep(100)
                        println("B: Done sleeping again")
                    } finally {
                        println("B: Done executing")
                    }
                }
            }
            delay(20)
            println("A: Cancelling and joining background")
            backgroundLaunch.cancel()
            val time = measureTimeMillis {
                backgroundLaunch.join()
            }
            println("A: Join done took $time millis ")
        }
    }
}
