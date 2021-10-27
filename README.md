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

## Documentation

Javadoc is available
on [Github Pages](https://romangraef.github.io/bukkitcoroutines/bukkitcoroutines/moe.nea89.bukkitcoroutines/index.html)

## Installation

This code should be shaded into your plugin: ![[https://search.maven.org/artifact/moe.nea89/bukkitcoroutines](https://search.maven.org/artifact/moe.nea89/bukkitcoroutines)](https://img.shields.io/maven-central/v/moe.nea89/bukkitcoroutines)

```kotlin
implementation("moe.nea89:bukkitcoroutines:1.0.0")

// If you want to be compatible with other plugins, make sure to relocate us:

shadowJar.relocate("moe.nea89.bukkitcoroutines", "yourpluginid.relocated.bukkitcoroutines")
```

