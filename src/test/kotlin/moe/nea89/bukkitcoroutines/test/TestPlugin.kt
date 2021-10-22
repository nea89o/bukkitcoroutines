package moe.nea89.bukkitcoroutines.test

import moe.nea89.bukkitcoroutines.startCoroutineOnSameThread
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

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
            logger.info("Before start keepThread | is primary thread: ${Bukkit.isPrimaryThread()}")
            keepThread {
                logger.info("After start keepThread | is primary thread: ${Bukkit.isPrimaryThread()}")
                switchToAsync()
                logger.info("After switchToAsync in keepthread | is primary thread: ${Bukkit.isPrimaryThread()}")
            }
            logger.info("After keepThread  | is primary thread: ${Bukkit.isPrimaryThread()}")
            try {
                keepThread<Unit> {
                    switchToAsync()
                    logger.info("Before throwing after switchToAsync inside keepThread | is primary thread: ${Bukkit.isPrimaryThread()}")
                    throw RuntimeException("Some Exception")
                }
            } catch (e: Exception) {
                logger.info("After catching outside keepThread | is primary thread: ${Bukkit.isPrimaryThread()}")
            }
        }
    }
}
