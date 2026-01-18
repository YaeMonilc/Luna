plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "Luna"

includeBuild("Reze") {
    dependencySubstitution {
        substitute(module("io.github.yaemonilc.reze:Core"))
            .using(project(":Core"))
    }
}