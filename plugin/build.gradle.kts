plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish")
    id("io.gitlab.arturbosch.detekt")
}

group = "com.demack"
version = "0.0.1"

val kotestVersion: String by project
val mockkVersion: String by project
val assertjVersion: String by project

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation(platform("software.amazon.awssdk:bom:2.17.207"))
    implementation("software.amazon.awssdk:codeartifact")
    implementation("software.amazon.awssdk:sso")

    implementation(gradleApi())

    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

gradlePlugin {
    // Define the plugin
    plugins {
        create("codeartifact-plugin") {
            id = "com.demack.codeartifact"
            implementationClass = "com.demack.codeartifact.plugin.CodeArtifactPlugin"
            description = "Facilitates connecting to AWS CodeArtifact"
            displayName = "CodeArtifact Gradle Plugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/DeMack/codeartifact-plugin"
    vcsUrl = "https://github.com/DeMack/codeartifact-plugin.git"
    description = "Facilitates connecting to AWS CodeArtifact"
    tags = listOf("aws", "codeartifact", "code", "artifact")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt> {
    jvmTarget = JavaVersion.VERSION_17.toString()
    ignoreFailures = true
    buildUponDefaultConfig = true
    autoCorrect = true
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
