package moe.nea89.bukkitcoroutines

import org.bukkit.plugin.Plugin
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.resume

/**
 * Launches a coroutine on an async thread. This will always create a new task.
 *
 * This will immediately return.
 */
fun <T> Plugin.launchCoroutineAsync(block: suspend () -> T): CompletableFuture<T> {
    val (future, cont) = createCoroutine(block)
    continueOnAsync(cont, true)
    return future
}

/**
 * Launches a coroutine on the main thread.
 *
 * This will immediately return.
 */
fun <T> Plugin.launchCoroutineOnMain(block: suspend () -> T): CompletableFuture<T> {
    val (future, cont) = createCoroutine(block)
    continueOnMain(cont, true)
    return future
}

/**
 * Starts a coroutine immediately.
 *
 * This will return whene the context is switched or this coroutine returns.
 */
fun <T> Plugin.startCoroutineOnSameThread(block: suspend () -> T): CompletableFuture<T> {
    val (future, cont) = createCoroutine(block)
    cont.resume(Unit)
    return future
}
