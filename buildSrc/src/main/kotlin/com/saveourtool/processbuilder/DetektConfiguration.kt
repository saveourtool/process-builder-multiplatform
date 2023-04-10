/**
 * Configuration for detekt static analysis
 */

package com.saveourtool.processbuilder

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.*

/**
 * Configure Detekt for a single project
 */
fun Project.configureDetekt() {
    apply<DetektPlugin>()
    configure<DetektExtension> {
        config = rootProject.files("detekt.yml")
        basePath = rootDir.canonicalPath
        buildUponDefaultConfig = true
    }
}

/**
 * Register a unified detekt task
 */
fun Project.createDetektTask() {
    val detektAllTask = tasks.register("detektAll") {
        allprojects {
            this@register.dependsOn(tasks.withType<Detekt>())
        }
    }

    tasks.register("mergeDetektReports", ReportMergeTask::class) {
        mustRunAfter(detektAllTask)
        output.set(buildDir.resolve("detekt-sarif-reports/detekt-merged.sarif"))
    }

    @Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
    val reportMerge: TaskProvider<ReportMergeTask> = rootProject.tasks.named<ReportMergeTask>("mergeDetektReports") {
        input.from(
            tasks.withType<Detekt>().map { it.sarifReportFile }
        )
        shouldRunAfter(tasks.withType<Detekt>())
    }
    tasks.withType<Detekt>().configureEach {
        reports.sarif.required.set(true)
        finalizedBy(reportMerge)
    }
}
