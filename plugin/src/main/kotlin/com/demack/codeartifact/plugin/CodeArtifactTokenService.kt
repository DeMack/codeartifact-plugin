package com.demack.codeartifact.plugin

import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.codeartifact.CodeartifactClient
import software.amazon.awssdk.services.codeartifact.model.GetAuthorizationTokenResponse
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

/**
 * Utility class for obtaining authentication tokens from AWS.
 */
class CodeArtifactTokenService : BuildService<BuildServiceParameters.None> {
    /** Since this can be used for multiple repos, cache any previously found tokens */
    private val tokenCache = ConcurrentHashMap<String, String>()

    /** Retrieves the token and stores it in the cache */
    internal fun getToken(uri: URI, profile: String? = null): String =
        tokenCache.getOrPut("${profile ?: "undefined"}@$uri") {
            getAuthToken(CodeArtifactUrl(uri.toURL()), profile).authorizationToken()
        }

    private fun getAuthToken(codeArtifactUrl: CodeArtifactUrl, profile: String?): GetAuthorizationTokenResponse {
        return CodeartifactClient.builder()
            .region(Region.of(codeArtifactUrl.region))
            .credentialsProvider(
                DefaultCredentialsProvider.builder()
                    .profileName(profile)
                    .build()
            ).build().use {
                it.getAuthorizationToken { req ->
                    req.domain(codeArtifactUrl.domain)
                    req.domainOwner(codeArtifactUrl.owner)
                }
            }
    }

    @Suppress("WRONG_NULLABILITY_FOR_JAVA_OVERRIDE")
    override fun getParameters() = null
}
