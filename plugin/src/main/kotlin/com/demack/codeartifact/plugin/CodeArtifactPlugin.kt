package com.demack.codeartifact.plugin

import org.gradle.api.Plugin
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.invocation.Gradle
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.slf4j.LoggerFactory
import java.net.URI

/**
 * Entry point for the plugin. Grabs a token and then authenticates with CodeArtifact for all repos
 */
@Suppress("unused") // This is a plugin, so won't be used locally. It is invoked in tests, though.
class CodeArtifactPlugin : Plugin<Gradle> {
    private val logger = LoggerFactory.getLogger(CodeArtifactPlugin::class.java)
    private val awsProfile: String?

    init {
        awsProfile = try {
            System.getenv(AWS_GRADLE_PROFILE)
        } catch (ex: SecurityException) {
            logger.warn(
                "Security exception thrown when trying to access the aws profile environment variable. " +
                        "Another method will need to be used.",
                ex
            )
            null
        }
    }

    override fun apply(gradle: Gradle) {
        logger.info("Authenticating build with CodeArtifact using profile: $awsProfile")

        val tokenProvider =
            gradle.sharedServices
                .registerIfAbsent("codeartifact-token", CodeArtifactTokenService::class.java) {}

        authenticatePluginRepos(gradle, tokenProvider)
        authenticateProjectRepos(gradle, tokenProvider)
    }

    private fun authenticatePluginRepos(gradle: Gradle, tokenProvider: Provider<CodeArtifactTokenService>) {
        gradle.settingsEvaluated {
            if (it.pluginManagement.repositories.isNotEmpty()) {
                logger.info("Authenticating plugin repos")
                authenticateRepos(it.pluginManagement.repositories, tokenProvider)
            }
        }
    }

    private fun authenticateProjectRepos(gradle: Gradle, tokenProvider: Provider<CodeArtifactTokenService>) {
        gradle.allprojects { proj ->
            proj.afterEvaluate {
                if (it.repositories.isNotEmpty()) {
                    logger.info("Authenticating project dependency repos")
                    authenticateRepos(it.repositories, tokenProvider)
                }

                if (it.plugins.hasPlugin("maven-publish")) {
                    logger.info("Authenticating publishing repos")
                    it.extensions.configure<PublishingExtension>("publishing") { ext ->
                        authenticateRepos(ext.repositories, tokenProvider)
                    }
                }
            }
        }
    }

    private fun authenticateRepos(repositories: RepositoryHandler, tokenProvider: Provider<CodeArtifactTokenService>) {
        repositories.forEach {
            if (it is MavenArtifactRepository) {
                val url = it.url
                if (url.isCARepo() && it.credentialsAreEmpty()) {
                    logger.debug("Authenticating repo found at $url")
                    val token = tokenProvider.get().getToken(url, awsProfile)
                    it.credentials { credentials ->
                        credentials.username = "aws"
                        credentials.password = token
                    }
                }
            }
        }
    }

    private fun URI.isCARepo() = toString().matches(codeArtifactUrlRegex)

    private fun MavenArtifactRepository.credentialsAreEmpty() =
        credentials.username.isNullOrBlank() && credentials.password.isNullOrBlank()

    companion object {
        private const val AWS_GRADLE_PROFILE = "AWS_GRADLE_PROFILE"

        private val codeArtifactUrlRegex = Regex("""^.+\.codeartifact\..+\.amazonaws\..+""", RegexOption.IGNORE_CASE)
    }
}
