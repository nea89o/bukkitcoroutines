package moe.nea89.bukkitcoroutines

import org.bukkit.plugin.Plugin
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.*

/**
 * Launches a coroutine on an async thread. This will always create a new task.
 *
 * This will immediately return.
 */
fun Plugin.launchCoroutineAsync(block: suspend BukkitCoroutine<Unit>.() -> Unit): BukkitCoroutine<Unit> {
    val (bukkit, cont) = createCoroutine(block)
    continueOnAsync(cont, true)
    return bukkit
}

/**
 * Launches a coroutine on the main thread.
 *
 * This will immediately return.
 */
fun Plugin.launchCoroutineOnMain(block: suspend BukkitCoroutine<Unit>.() -> Unit): BukkitCoroutine<Unit> {
    val (bukkit, cont) = createCoroutine(block)
    continueOnMain(cont, true)
    return bukkit
}

/**
 * Starts a coroutine immediately.
 *
 * This will return whene the context is switched or this coroutine returns.
 */
fun Plugin.startCoroutineOnSameThread(block: suspend BukkitCoroutine<Unit>.() -> Unit): BukkitCoroutine<Unit> {
    val (bukkit, cont) = createCoroutine(block)
    cont.resume(Unit)
    return bukkit
}

internal fun <T> Plugin.createCoroutine(block: suspend BukkitCoroutine<T>.() -> T): Pair<BukkitCoroutine<T>, Continuation<Unit>> {
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



