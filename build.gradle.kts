import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import kr.entree.spigradle.kotlin.spigot


plugins {
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.dokka") version "1.5.31"
    id("kr.entree.spigradle") version "2.2.4"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    `maven-publish`
    signing
}

group = "moe.nea89"
version = "2.0.0"

repositories {
    maven("https://libraries.minecraft.net/")
    maven("https://jitpack.io/")
    mavenCentral()
}

val build by tasks.getting
val jar by tasks.getting

val sourcesJar by tasks.creating(Jar::class) {
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
            artifactId = "bukkitcoroutines"
            artifact(jar)
            artifact(dokkaHtmlJar)
            artifact(dokkaJavadocJar)
            artifact(sourcesJar)
            pom {
                name.set("Bukkit Coroutines")
                description.set("Extensions to the bukkit scheduler to work with kotlin coroutines")
                url.set("https://github.com/romangraef/bukkitcoroutines")
                licenses {
                    license {
                        name.set("ISC License")
                        url.set("https://opensource.org/licenses/isc-license.txt")
                    }
                }
                developers {
                    developer {
                        id.set("nea89")
                        name.set("Nea Gräf")
                        email.set("hello@nea89.moe")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/romangraef/bukkitcoroutines.git")
                    developerConnection.set("scm:git:ssh://github.com/romangraef/bukkitcoroutines.git")
                    url.set("https://github.com/romangraef/bukkitcoroutines")
                }
            }
            repositories {
                maven {
                    name = "OSSRH"
                    setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                    credentials {
                        username = project.findProperty("OSSRH_USERNAME") as? String ?: return@credentials
                        password = project.findProperty("OSSRH_PASSWORD") as? String ?: return@credentials
                    }
                }
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["library"])
}

tasks.withType(Sign::class) {
    onlyIf {
        gradle.taskGraph.allTasks.any { "publish" in it.name }
    }
}


dependencies {
    implementation(spigot("1.16.5"))
    implementation(kotlin("stdlib"))
}
