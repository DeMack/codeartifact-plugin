package com.demack.codeartifact.plugin

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.slot
import io.mockk.spyk
import software.amazon.awssdk.services.codeartifact.model.GetAuthorizationTokenResponse
import java.net.URI

class CodeArtifactTokenServiceTest : BehaviorSpec({

    Given("a CodeArtifactTokenProvider") {
        val provider = spyk<CodeArtifactTokenService>(recordPrivateCalls = true)

        When("getToken called") {
            val url = slot<CodeArtifactUrl>()
            val profile = mutableListOf<String?>()

            beforeContainer {
                every {
                    provider invoke "getAuthToken" withArguments listOf(capture(url), captureNullable(profile))
                } returns GetAuthorizationTokenResponse.builder()
                    .authorizationToken("iambatman")
                    .build()
            }

            afterTest {
                url.clear()
                profile.clear()
            }

            And("profile is blank") {
                Then("expect successful call with no profile") {
                    provider.getToken(
                        URI("https://test-111222333444.d.codeartifact.gotham.amazonaws.com/maven/test-maven/"),
                    )

                    url.captured.apply {
                        domain shouldBe "test"
                        owner shouldBe "111222333444"
                        region shouldBe "gotham"
                    }

                    profile[0].shouldBeNull()
                }
            }

            And("profile is provided") {
                Then("expect successful call with profile") {
                    provider.getToken(
                        URI("https://test-111222333444.d.codeartifact.gotham.amazonaws.com/maven/test-maven/"),
                        "bruce-wayne"
                    )

                    url.captured.apply {
                        domain shouldBe "test"
                        owner shouldBe "111222333444"
                        region shouldBe "gotham"
                    }

                    profile[0] shouldBe "bruce-wayne"
                }
            }
        }
    }
})
