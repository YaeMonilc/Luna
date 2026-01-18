import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    id("com.gradleup.shadow") version "9.3.0"
}

group = "io.github.yaemonilc.reze.plugin.luna"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("io.github.yaemonilc.reze:Core")

    // Source: https://mvnrepository.com/artifact/ai.koog/koog-agents
    implementation("ai.koog:koog-agents:0.6.0")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.register<Copy>("runPlugin") {
    dependsOn("shadowJar")

    tasks.named<ShadowJar>("shadowJar").apply {
        from(get().archiveFile.get())
        into("/Reze/plugins")
    }

    finalizedBy(gradle.includedBuild("Reze").task(":Launcher:run"))
}

tasks.test {
    useJUnitPlatform()
}