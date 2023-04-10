/**
 * Configuration for diktat static analysis
 */

package com.saveourtool.processbuilder

import org.cqfn.diktat.plugin.gradle.DiktatExtension
import org.cqfn.diktat.plugin.gradle.DiktatGradlePlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

/**
 * Applies diktat gradle plugin and configures diktat for [this] project
 */
fun Project.configureDiktat() {
    apply<DiktatGradlePlugin>()
    configure<DiktatExtension> {
        diktatConfigFile = rootProject.file("diktat-analysis.yml")
        githubActions = findProperty("diktat.githubActions")?.toString()?.toBoolean() ?: false
        inputs {
            include("src/**/*.kt", "*.kts", "src/**/*.kts")
            exclude("build/**")
        }
    }
}

