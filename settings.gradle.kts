pluginManagement {
    val kotlinVersion: String by settings
    val detektVersion: String by settings
    val gradlePluginPublishVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        id("io.gitlab.arturbosch.detekt") version detektVersion
        id("com.gradle.plugin-publish") version gradlePluginPublishVersion
    }
}

rootProject.name = "codeartifact-plugin"
include("plugin")
