import kotlinx.benchmark.gradle.JvmBenchmarkTarget
import org.jetbrains.kotlin.allopen.gradle.AllOpenExtension

plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.allopen") version "2.1.10"

    id("org.jetbrains.kotlinx.benchmark") version "0.4.13"
    id("org.jlleitschuh.gradle.ktlint") version "12.2.0"
}

group = "br.com.rodrigogurgel.snowflakeid"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
    resources.srcDirs("src/main/resources")
}

sourceSets.test {
    java.srcDirs("src/test/kotlin")
    resources.srcDirs("src/test/resources")
}

configure<AllOpenExtension> {
    annotation("org.openjdk.jmh.annotations.State")
}

dependencies {
    // Kotlin
    val kotlinVersion = "2.1.10"
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    // Coroutine
    val coroutinesVersion = "1.10.1"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")

    // Benchmark
    val benchmarkVersion = "0.4.13"
    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:$benchmarkVersion")

    // Jmh
    val jmhVersion = "1.37"
    implementation("org.openjdk.jmh:jmh-core:$jmhVersion")
    implementation("org.openjdk.jmh:jmh-generator-annprocess:$jmhVersion")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:$jmhVersion")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

benchmark {
    targets {
        register("main") {
            this as JvmBenchmarkTarget
            jmhVersion = "1.37"
        }
    }
}
