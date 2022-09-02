package com.demack.codeartifact.plugin

import java.net.URL

private const val URL_TAIL_LENGTH = 3

/**
 * Utility class that takes in a CodeArtifact URL and pulls out the information that's needed later on in the
 * authentication process.
 *
 * Example:
 * ```
 *              |owner
 * https://test-111222333444.d.codeartifact.gotham.amazonaws.com/maven/test-maven/
 *         ^domain                          ^region
 * ```
 */
class CodeArtifactUrl(url: URL) {

    val domain: String
    val owner: String
    val region: String

    init {
        val urlParts = url.host.split(Regex("""\."""))
        this.domain = urlParts[0].substringBeforeLast('-')
        this.owner = urlParts[0].substringAfterLast('-')
        this.region = urlParts[urlParts.size - URL_TAIL_LENGTH]
    }
}
