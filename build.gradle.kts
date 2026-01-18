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

    // Source: https://mvnrepository.com/artifact/com.openai/openai-java
    implementation("com.openai:openai-java:4.15.0")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.register<Copy>("compilePlugin") {
    dependsOn("shadowJar")

    val shadowJar = tasks.named<ShadowJar>("shadowJar")
    from(shadowJar.get().archiveFile.get())
    into("/Reze/plugins")
}

tasks.register("runReze") {
    dependsOn(gradle.includedBuild("Reze").task(":Launcher:run"))
}

tasks.register("runPlugin") {
    dependsOn("compilePlugin")

    finalizedBy("runReze")
}

tasks.test {
    useJUnitPlatform()
}