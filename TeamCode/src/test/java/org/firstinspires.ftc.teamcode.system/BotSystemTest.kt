package org.firstinspires.ftc.teamcode.system

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.debug.DebugProbes
import org.junit.jupiter.api.Test

class BotSystemTest {
    @UseExperimental(ExperimentalCoroutinesApi::class)
    @Test
    fun runIt() {
        DebugProbes.install()
        val system = BotSystem(
            DependsOn(Dependency1::class),
            LoopAFewTimes(),
            Receive()
        )
        val launchJob = GlobalScope.launch {
            system.initSuspend()
            system.start()
            val job = system.get<CoroutineScopeElement>().job
            (job as CompletableJob).complete()
            job.join()
        }
        runBlocking {
            try {
                withTimeout(3000) {
                    launchJob.join()
                }
            } catch (e: TimeoutCancellationException) {
                println("Timed out:")
                DebugProbes.dumpCoroutines()
                launchJob.cancel()
            }
        }
    }
}

private class Dependency1 : AbstractElement() {
    private val aThing by botSystem(Dependency2::class) { thing }

    override fun init(botSystem: BotSystem) {
        super.init(botSystem)
        println(aThing)
    }
}

private class Dependency2 : AbstractElement(/*Dependency1::class*/) {
    val thing = 0
    override fun init(botSystem: BotSystem) {
    }
}


private class LoopAFewTimes : LinearElement() {

    override fun runElement() {
        repeat(5) {
            delay(50)
            println(it)
        }
    }
}

private class Send : CoroutineElement() {
    val channel = Channel<String>()

    @UseExperimental(ExperimentalCoroutinesApi::class)
    override suspend fun runElement() = coroutineScope<Unit> {
        repeat(10) {
            channel.send("Hey $it")
            delay(25)
        }
        channel.close()
    }
}

private class Receive : CoroutineElement() {
    private val send: Send by botSystem()

    override suspend fun runElement() {
        for (s in send.channel) {
            println(s)
        }
    }
}
