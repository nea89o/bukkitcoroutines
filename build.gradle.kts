import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import kr.entree.spigradle.kotlin.spigot


plugins {
    kotlin("jvm") version "1.5.31"
    id("kr.entree.spigradle") version "2.2.4"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "moe.nea89"
version = "1.0.0"

repositories {
    maven("https://libraries.minecraft.net/")
    maven("https://jitpack.io/")
    mavenCentral()
}
val build by tasks.getting

val shadowJar by tasks.getting(ShadowJar::class) {
    mergeServiceFiles()
}
val prepareSpigotPlugins by tasks.getting(Copy::class) {
    dependsOn(shadowJar)
}


dependencies {
    shadow(spigot("1.16.5"))
    implementation(kotlin("stdlib"))
}