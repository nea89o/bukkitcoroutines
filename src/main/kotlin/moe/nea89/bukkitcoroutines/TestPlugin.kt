package moe.nea89.bukkitcoroutines

import kr.entree.spigradle.annotations.SpigotPlugin
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@SpigotPlugin
class TestPlugin : JavaPlugin() {
    @OptIn(ExperimentalTime::class)
    override fun onEnable() {
        logger.info("OnEnable | is primary thread: ${Bukkit.isPrimaryThread()}")
        startCoroutineOnSameThread {
            logger.info("Started Coroutine | is primary thread: ${Bukkit.isPrimaryThread()}")
            switchToAsync()
            logger.info("After switchToAsync | is primary thread: ${Bukkit.isPrimaryThread()}")
            waitAndContinueInSameThread(Duration.seconds(3))
            logger.info("After wait | is primary thread: ${Bukkit.isPrimaryThread()}")
            switchToMain()
            logger.info("After switchToMain | is primary thread: ${Bukkit.isPrimaryThread()}")
        }
    }
}

data class BukkitCoroutine<T>(val plugin: Plugin, val future: CompletableFuture<T>) {
    suspend fun switchToAsync(): Unit = suspendCoroutine<Unit> {
        plugin.continueOnAsync(it)
    }

    /**
     * Waits and continues in the same asyncness (continues on main if called on main, otherwise continues on any async thread)
     */
    @OptIn(ExperimentalTime::class)
    suspend fun waitAndContinueInSameThread(duration: Duration): Unit = suspendCoroutine {
        val ticks = duration.toLong(DurationUnit.MILLISECONDS) * 20 / 1000
        plugin.waitAndContinueInSameThread(it, ticks)
    }

    suspend fun <T> callOnMain(block: () -> T): T = suspendCoroutine {
        plugin.callOnMain(it, block)
    }

    suspend fun <T> callOnAsync(block: () -> T): T = suspendCoroutine {
        plugin.callOnAsync(it, block)
    }

    suspend fun switchToMain(): Unit = suspendCoroutine {
        plugin.continueOnMain(it)
    }
}

@OptIn(ExperimentalTime::class)
private fun Plugin.waitAndContinueInSameThread(cont: Continuation<Unit>, ticks: Long) {
    if (Bukkit.isPrimaryThread()) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, Runnable {
            cont.resume(Unit)
        }, ticks)
    } else {
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, Runnable {
            cont.resume(Unit)
        }, ticks)
    }
}

private fun <T> Plugin.callOnAsync(cont: Continuation<T>, block: () -> T) {
    if (Bukkit.isPrimaryThread()) {
        Bukkit.getScheduler().runTaskAsynchronously(this, Runnable {
            try {
                val res = block()
                Bukkit.getScheduler().callSyncMethod(this, Callable {
                    cont.resume(res)
                })
            } catch (e: Throwable) {
                Bukkit.getScheduler().callSyncMethod(this, Callable {
                    cont.resumeWithException(e)
                })
            }
        })
    } else {
        try {
            cont.resume(block())
        } catch (e: Throwable) {
            cont.resumeWithException(e)
        }
    }
}

private fun <T> Plugin.callOnMain(cont: Continuation<T>, block: () -> T) {
    if (Bukkit.isPrimaryThread())
        try {
            cont.resume(block())
        } catch (e: Throwable) {
            cont.resumeWithException(e)
        }
    else
        try {
            Bukkit.getScheduler().callSyncMethod(this) {
                block()
            }.get()
        } catch (e: Throwable) {
            cont.resumeWithException(e)
        }
}

private fun Plugin.continueOnAsync(cont: Continuation<Unit>, force: Boolean = true) {
    if (Bukkit.isPrimaryThread() || force)
        Bukkit.getScheduler().runTaskAsynchronously(this, Runnable {
            cont.resume(Unit)
        })
    else
        cont.resume(Unit)
}

private fun Plugin.continueOnMain(cont: Continuation<Unit>, force: Boolean = true) {
    if (Bukkit.isPrimaryThread() && !force) {
        cont.resume(Unit)
    } else {
        Bukkit.getScheduler().callSyncMethod(this) {
            cont.resume(Unit)
        }
    }
}

fun Plugin.launchCoroutineAsync(block: suspend BukkitCoroutine<Unit>.() -> Unit): BukkitCoroutine<Unit> {
    val (bukkit, cont) = createCoroutine(block)
    continueOnAsync(cont, true)
    return bukkit
}

fun Plugin.launchCoroutineOnMain(block: suspend BukkitCoroutine<Unit>.() -> Unit): BukkitCoroutine<Unit> {
    val (bukkit, cont) = createCoroutine(block)
    continueOnMain(cont, true)
    return bukkit
}

fun Plugin.startCoroutineOnSameThread(block: suspend BukkitCoroutine<Unit>.() -> Unit): BukkitCoroutine<Unit> {
    val (bukkit, cont) = createCoroutine(block)
    cont.resume(Unit)
    return bukkit
}

private fun <T> Plugin.createCoroutine(block: suspend BukkitCoroutine<T>.() -> T): Pair<BukkitCoroutine<T>, Continuation<Unit>> {
    val bk = BukkitCoroutine(this, CompletableFuture<T>())
    val coroutine = block.createCoroutine(bk, object : Continuation<T> {
        override val context: CoroutineContext
            get() = EmptyCoroutineContext

        override fun resumeWith(result: Result<T>) {
            if (result.isFailure)
                bk.future.completeExceptionally(result.exceptionOrNull())
            else
                bk.future.complete(result.getOrNull())
        }
    })
    return bk to coroutine
}



