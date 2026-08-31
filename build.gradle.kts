import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.GradleException
import org.gradle.plugins.signing.SigningExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.maven.publish) apply false
}

val capsuleVersion = if (providers.environmentVariable("GITHUB_REF_TYPE").orNull == "tag") {
    providers.environmentVariable("GITHUB_REF_NAME").orNull?.removePrefix("v")
} else {
    providers.gradleProperty("VERSION_NAME").orNull
} ?: "0.1.0-SNAPSHOT"

allprojects {
    group = "io.github.0dimidrol0"
    version = capsuleVersion
}

data class PublicationMetadata(
    val name: String,
    val description: String,
)

val publications = mapOf(
    ":capsule-core" to PublicationMetadata(
        name = "Capsule Core",
        description = "Pure Kotlin feature runtime for Capsule Architecture.",
    ),
    ":capsule-base-viewmodel" to PublicationMetadata(
        name = "Capsule Base ViewModel",
        description = "Android ViewModel shell and state time-travel contracts for Capsule Architecture.",
    ),
    ":capsule-base-fragment-xml" to PublicationMetadata(
        name = "Capsule Base Fragment XML",
        description = "Reusable Fragment and XML screen foundations for Capsule Architecture.",
    ),
    ":capsule-debug" to PublicationMetadata(
        name = "Capsule Debug",
        description = "Debug inspector, state history, and time-travel tools for Capsule Architecture.",
    ),
    ":capsule-middleware" to PublicationMetadata(
        name = "Capsule Middleware",
        description = "Logging, timing, state history, and debug timeline middleware for Capsule Architecture.",
    ),
    ":capsule-network" to PublicationMetadata(
        name = "Capsule Network",
        description = "Network-aware operation policies and Android connectivity monitoring for Capsule Architecture.",
    ),
    ":capsule-navigation-compose" to PublicationMetadata(
        name = "Capsule Navigation Compose",
        description = "Jetpack Compose navigation effect helpers for Capsule Architecture.",
    ),
    ":capsule-navigation-xml" to PublicationMetadata(
        name = "Capsule Navigation XML",
        description = "Fragment and Navigation Component effect helpers for Capsule Architecture.",
    ),
)

subprojects {
    val publication = publications[path] ?: return@subprojects

    pluginManager.withPlugin("com.vanniktech.maven.publish") {
        extensions.configure<MavenPublishBaseExtension> {
            publishToMavenCentral()
            signAllPublications()
            coordinates(
                groupId = "io.github.0dimidrol0",
                artifactId = project.name,
                version = capsuleVersion,
            )

            pom {
                name.set(publication.name)
                description.set(publication.description)
                inceptionYear.set("2026")
                url.set("https://github.com/0Dimidrol0/Capsule-Architecture")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("0Dimidrol0")
                        name.set("Dimidrol")
                        url.set("https://github.com/0Dimidrol0")
                    }
                }

                scm {
                    url.set("https://github.com/0Dimidrol0/Capsule-Architecture")
                    connection.set("scm:git:git://github.com/0Dimidrol0/Capsule-Architecture.git")
                    developerConnection.set("scm:git:ssh://git@github.com:0Dimidrol0/Capsule-Architecture.git")
                }
            }
        }

        // Keep CI's in-memory signing; local releases use the GnuPG keyring.
        if (providers.gradleProperty("signingInMemoryKey").orNull.isNullOrBlank()) {
            extensions.configure<SigningExtension> {
                useGpgCmd()
            }
        }
    }
}

tasks.register("verifyKotlinFileLayout") {
    group = "verification"
    description = "Checks that each Kotlin source file contains at most one top-level type with a matching name."

    val kotlinSources = fileTree(rootDir) {
        include("**/src/**/*.kt")
        exclude("**/build/**")
    }
    inputs.files(kotlinSources)

    doLast {
        val declaration = Regex(
            """^(?:(?:public|internal|private|sealed|data|enum|annotation|value|fun|open|abstract)\s+)*(class|interface|object|typealias)\s+([A-Za-z_][A-Za-z0-9_]*)""",
        )
        val violations = kotlinSources.files.mapNotNull { source ->
            val declarations = source.useLines { lines ->
                lines.mapNotNull { declaration.find(it)?.groupValues?.get(2) }.toList()
            }
            when {
                declarations.size > 1 -> "${source.relativeTo(rootDir)} contains: ${declarations.joinToString()}"
                declarations.size == 1 && declarations.single() != source.nameWithoutExtension -> {
                    "${source.relativeTo(rootDir)} must be named ${declarations.single()}.kt"
                }
                else -> null
            }
        }

        if (violations.isNotEmpty()) {
            throw GradleException(
                "Kotlin file layout violations:\n${violations.joinToString(separator = "\n")}",
            )
        }
    }
}
