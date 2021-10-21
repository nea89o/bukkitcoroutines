# This repository

This is for now just a POC repo for using Kotlin continuations, but i plan on using this to make a general library for
kotlin integrations into bukkit.

## Bukkit Kotlin Coroutines

Interact with the bukkit scheduler in a very kotlin-y way:

```kotlin
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
```

would produce the following output:

```log
[23:21:38 INFO]: [bukkitcoroutines] Loading bukkitcoroutines v1.0.0
[23:21:42 INFO]: [bukkitcoroutines] Enabling bukkitcoroutines v1.0.0*
[23:21:42 INFO]: [bukkitcoroutines] OnEnable | is primary thread: true
[23:21:42 INFO]: [bukkitcoroutines] Started Coroutine | is primary thread: true
[23:21:42 INFO]: [bukkitcoroutines] After switchToAsync | is primary thread: false
[23:21:45 INFO]: [bukkitcoroutines] After wait | is primary thread: false
[23:21:45 INFO]: [bukkitcoroutines] After switchToMain | is primary thread: true
```

Some functions I implemented in this:

Creating a coroutine: all of these methods need to be called with plugin.XXX(block) where block is
a `suspend BukkitCoroutine<T>.()->T`:

```
launchCoroutineAsync        - launches a coroutine in an async task, will return immediately
launchCoroutineOnMain       - launches a coroutine in the main thread (will be started later on), will return immediately
startCoroutineOnSameThread  - starts a coroutine in the current thread, will return once the context of the coroutine 
                              is switched, or the coroutine is finished
```

Controlling the flow of the coroutine: all of these functions have to be called on a `BukkitCoroutine<T>`

```
switchToAsync()                         - continues the coroutine on the async thread, returns immediately if already in an async context
switchToMain()                          - continues the coroutine on the main thread, returns immediately if already in the main thread
callOnMain(block)                       - continues on the same thread, but runs the block on the main thread before continuing
callOnAsync(block)                      - continues on the same thread, but runs the block on the async thread before continuing
waitAndContinueInSameThread(duration)   - continues on the same thread, after waiting that specified duration
```

