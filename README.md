# CodeArtifact Gradle Plugin

This is a Gradle plugin that allows for easy connection to [AWS CodeArtifact](https://aws.amazon.com/codeartifact/)
using local
credentials. It was inspired by Clarity AI's
[codeartifact-gradle-plugin](https://github.com/clarityai-eng/codeartifact-gradle-plugin), however, while, that plugin
has the limitation that it can only be used to obtain project-level dependencies, this is designed to allow for
retrieving plugin dependencies as well.

## Usage

In order to connect to CodeArtifact, you need to have previously authenticated in one of a few ways. From the AWS SDK
documentation:

```
AWS credentials provider chain that looks for credentials in this order:
1. Java System Properties - aws.accessKeyId and aws.secretAccessKey
2. Environment Variables - AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY
3. Web Identity Token credentials from system properties or environment variables
4. Credential profiles file at the default location (~/.aws/credentials) shared by all AWS SDKs and the AWS CLI
5. Credentials delivered through the Amazon EC2 container service if AWS_CONTAINER_CREDENTIALS_RELATIVE_URI" environment
   variable is set and security manager has permission to access the variable,
6. Instance profile credentials delivered through the Amazon EC2 metadata service
```

see:
[DefaultCredentialsProvider](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/credentials/DefaultCredentialsProvider.html)

Once you have decided your general authentication method, this plugin can be applied to a Gradle
[Initialization Script](https://docs.gradle.org/current/userguide/init_scripts.html) as in the below:

```kotlin
initscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.demack:codeartifact-plugin:{plugin-version}")
    }
}

apply(plugin = "com.demack.codeartifact")
```

**A note about profiles**

By default, a `ProfileCredentialsProvider` is included in the credentials validation chain. If that is the desired
strategy, and you wish to use a profile other than your `AWS_PROFILE`, you can set the environment variable
`AWS_GRADLE_PROFILE` to the profile you wish to use. The plugin will automatically search in the default locations for
the configuration file (`~/.aws/config`) and the credentials file(`~/.aws/credentials`) for that profile's information.
