import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import kr.entree.spigradle.kotlin.spigot


plugins {
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.dokka") version "1.5.31"
    id("kr.entree.spigradle") version "2.2.4"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    `maven-publish`
}

group = "moe.nea89"
version = "1.0.0"

repositories {
    maven("https://libraries.minecraft.net/")
    maven("https://jitpack.io/")
    mavenCentral()
}

val build by tasks.getting
val jar by tasks.getting

val sourcesJar by tasks.creating(Jar::class){
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")

}

val dokkaHtmlJar by tasks.creating(Jar::class) {
    dependsOn(tasks.dokkaHtml)
    archiveClassifier.set("html-doc")
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
}

val dokkaJavadocJar by tasks.creating(Jar::class) {
    dependsOn(tasks.dokkaJavadoc)
    archiveClassifier.set("javadoc")
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
}


val generateSpigotDescription by tasks.getting(kr.entree.spigradle.module.common.YamlGenerate::class) {
    enabled = false
}

val testShadowJar by tasks.creating(ShadowJar::class) {
    from(sourceSets.test.get().output, sourceSets.main.get().output)
    archiveClassifier.set("test")
    this.configurations.add(project.configurations.testRuntimeClasspath.get())
    mergeServiceFiles()
}

build.dependsOn(sourcesJar, dokkaHtmlJar, dokkaJavadocJar, jar)

val prepareSpigotPlugins by tasks.getting(Copy::class) {
    from(testShadowJar)
}

publishing {
    publications {
        create<MavenPublication>("library") {
            artifact(jar)
            artifact(dokkaHtmlJar)
            artifact(dokkaJavadocJar)
            artifact(sourcesJar)
        }
    }
}


dependencies {
    implementation(spigot("1.16.5"))
    implementation(kotlin("stdlib"))
}
