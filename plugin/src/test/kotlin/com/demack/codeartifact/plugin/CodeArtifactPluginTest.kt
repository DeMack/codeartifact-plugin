package com.demack.codeartifact.plugin

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText

const val GENERAL_AUTH_MESSAGE = "Authenticating build with CodeArtifact"
const val SETTINGS_AUTH_MESSAGE = "Authenticating plugin repos"
const val DEPENDENCIES_AUTH_MESSAGE = "Authenticating project dependency repos"

val testProjectDir = createTempDirectory()
val initFile: Path = testProjectDir.resolve("init.gradle")
val settingsFile: Path = testProjectDir.resolve("settings.gradle")
val buildFile: Path = testProjectDir.resolve("build.gradle")

class CodeArtifactPluginTest : BehaviorSpec({
    afterSpec {
        testProjectDir.toFile().deleteRecursively().shouldBeTrue()
    }

    afterContainer {
        settingsFile.deleteIfExists()
        buildFile.deleteIfExists()
    }

    Given("an init script with CodeArtifact plugin applied") {
        beforeContainer { writeInitFile() }
        afterContainer { initFile.deleteIfExists() }

        When("a settings file has repos in plugin management") {
            writeSettingsFile()

            Then("there should only be logs for plugin repos") {
                val output = runBuild().output
                output shouldContain GENERAL_AUTH_MESSAGE
                output shouldContain SETTINGS_AUTH_MESSAGE
                output shouldNotContain DEPENDENCIES_AUTH_MESSAGE
            }
        }

        When("a build file has dependency repos") {
            writeBuildFile()

            Then("There should only be logs for dependency repos") {
                val output = runBuild().output
                output shouldContain GENERAL_AUTH_MESSAGE
                output shouldNotContain SETTINGS_AUTH_MESSAGE
                output shouldContain DEPENDENCIES_AUTH_MESSAGE
            }
        }

        When("a settings and build file are both present") {
            writeSettingsFile()
            writeBuildFile()

            Then("There should be logs for plugins and dependencies") {
                val output = runBuild().output
                output shouldContain GENERAL_AUTH_MESSAGE
                output shouldContain SETTINGS_AUTH_MESSAGE
                output shouldContain DEPENDENCIES_AUTH_MESSAGE
            }
        }
    }
})

fun writeInitFile() {
    initFile.writeText(
        """
            |initscript {
            |    repositories {
            |        mavenLocal()
            |        mavenCentral()
            |    }
            |    dependencies {
            |        classpath 'com.demack.codeartifact:com.demack.codeartifact.gradle.plugin:0.0.1'
            |    }
            |}
            |
            |apply plugin: com.demack.codeartifact.plugin.CodeArtifactPlugin
        """.trimMargin()
    )
}

fun writeSettingsFile() {
    settingsFile.writeText(
        """
            |pluginManagement {
            |    repositories {
            |        maven {
            |            url 'https://localhost.not.real/maven/test-maven/'
            |            name 'codeArtifact'
            |        }
            |    }
            |}
        """.trimMargin()
    )
}

fun writeBuildFile() {
    buildFile.writeText(
        """
            |repositories {
            |    maven {
            |        url 'https://localhost.not.real/maven/test-maven/'
            |        name 'codeArtifact'
            |    }
            |}
        """.trimMargin()
    )
}

fun runBuild(): BuildResult {
    val runner = GradleRunner.create()
        .forwardOutput()
        .withArguments("--init-script=${initFile.absolutePathString()}", "--stacktrace", "--debug")
        .withPluginClasspath()
        .withProjectDir(testProjectDir.toFile())
        .withDebug(false)

    return runner.build()
}
