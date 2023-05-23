package com.saveourtool.processbuilder

import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.configurationcache.extensions.serviceOf
import org.gradle.internal.logging.text.StyledTextOutput
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.SigningExtension

fun Project.configurePublishing() {
    configureGitHubPublishing()
    configurePublications()
    configureSigning()
}

private fun Project.configureGitHubPublishing() {
    apply(plugin = "maven-publish")
    configure<PublishingExtension> {
        println("${rootProject.name}/${project.name}")
        repositories {
            maven {
                name = "GitHub"
                url = uri("https://maven.pkg.github.com/saveourtool/${rootProject.name}/${project.name}")
                credentials {
                    username = findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                    password = findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}

private fun Project.configurePublications() {
    val dokkaJar = tasks.create<Jar>("dokkaJar") {
        group = "documentation"
        archiveClassifier.set("javadoc")
        from(tasks.findByName("dokkaHtml"))
    }

    configure<PublishingExtension> {
        publications.withType<MavenPublication>().configureEach {
            this.artifact(dokkaJar)
            this.pom {
                val project = this@configurePublications
                name.set(project.name)
                description.set(project.description ?: project.name)
                url.set("https://github.com/saveourtool/${project.name}")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/license/MIT")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("sanyavertolet")
                        name.set("Alexander Frolov")
                        email.set("lxnfrolov@gmail.com")
                    }
                }
                scm {
                    url.set("https://github.com/saveourtool/${project.name}")
                    connection.set("scm:git:https://github.com/saveourtool/${project.name}.git")
                    developerConnection.set("scm:git:git@github.com:saveourtool/${project.name}.git")
                }
            }
        }
    }
}

/**
 * Enables signing of the artifacts if the `signingKey` project property is set.
 *
 * Should be explicitly called after each custom `publishing {}` section.
 */
private fun Project.configureSigning() {
    System.getenv("GPG_SEC")?.let {
        extra.set("signingKey", it)
    }
    System.getenv("GPG_PASSWORD")?.let {
        extra.set("signingPassword", it)
    }

    if (hasProperty("signingKey")) {
        /*
         * GitHub Actions.
         */
        configureSigningCommon {
            useInMemoryPgpKeys(property("signingKey") as String?, findProperty("signingPassword") as String?)
        }
    } else if (
        hasProperties(
            "signing.keyId",
            "signing.password",
            "signing.secretKeyRingFile",
        )
    ) {
        /*-
         * Pure-Java signing mechanism via `org.bouncycastle.bcpg`.
         *
         * Requires an 8-digit (short form) PGP key id and a present `~/.gnupg/secring.gpg`
         * (for gpg 2.1, run
         * `gpg --keyring secring.gpg --export-secret-keys >~/.gnupg/secring.gpg`
         * to generate one).
         */
        configureSigningCommon()
    } else if (hasProperty("signing.gnupg.keyName")) {
        /*-
         * Use an external `gpg` executable.
         *
         * On Windows, you may need to additionally specify the path to `gpg` via
         * `signing.gnupg.executable`.
         */
        configureSigningCommon {
            useGpgCmd()
        }
    }
}

/**
 * @param useKeys the block which configures the PGP keys. Use either
 *   [SigningExtension.useInMemoryPgpKeys], [SigningExtension.useGpgCmd], or an
 *   empty lambda.
 * @see SigningExtension.useInMemoryPgpKeys
 * @see SigningExtension.useGpgCmd
 */
@Suppress(
    "MaxLineLength",
    "SpreadOperator",
)
private fun Project.configureSigningCommon(useKeys: SigningExtension.() -> Unit = {}) {
    configure<SigningExtension> {
        useKeys()
        val publications = extensions.getByType<PublishingExtension>().publications
        val publicationCount = publications.size
        val message = "The following $publicationCount publication(s) are getting signed: ${publications.map(Named::getName)}"
        val style = when (publicationCount) {
            0 -> StyledTextOutput.Style.Failure
            else -> StyledTextOutput.Style.Success
        }
        styledOut(logCategory = "signing").style(style).println(message)
        sign(*publications.toTypedArray())
    }
}

private fun Project.styledOut(logCategory: String): StyledTextOutput =
    serviceOf<StyledTextOutputFactory>().create(logCategory)

/**
 * Determines if this project has all the given properties.
 *
 * @param propertyNames the names of the properties to locate.
 * @return `true` if this project has all the given properties, `false` otherwise.
 * @see Project.hasProperty
 */
@Suppress("SameParameterValue")
private fun Project.hasProperties(vararg propertyNames: String): Boolean =
    propertyNames.asSequence().all(this::hasProperty)
