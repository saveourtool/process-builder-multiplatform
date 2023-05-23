/**
 * Configuration for project versioning
 */

package com.saveourtool.processbuilder

import org.ajoberstar.reckon.core.Scope
import org.ajoberstar.reckon.gradle.ReckonExtension
import org.ajoberstar.reckon.gradle.ReckonPlugin
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

/**
 * Configures reckon plugin for [this] project, should be applied for root project only
 */
fun Project.configureVersioning() {
    apply<ReckonPlugin>()

    // should be provided in the gradle.properties
    val isDevelopmentVersion = hasProperty("com.saveourtool.profile") && property("com.saveourtool.profile") == "dev"
    configure<ReckonExtension> {
        setDefaultInferredScope(Scope.MINOR.name)
        setScopeCalc(calcScopeFromProp())
        if (isDevelopmentVersion) {
            // this should be used during local development most of the time, so that constantly changing version
            // on a dirty git tree doesn't cause other task updates
            snapshots()
            setStageCalc(calcStageFromProp())
        } else {
            stages("alpha", "rc", "final")
            setStageCalc(calcStageFromProp())
        }
    }

    val status = FileRepositoryBuilder()
        .findGitDir(project.rootDir)
        .setup()
        .let(::FileRepository)
        .let(::Git)
        .status()
        .call()

    if (!status.isClean) {
        logger.warn("git tree is not clean; " +
                "Untracked files: ${status.untracked}, uncommitted changes: ${status.uncommittedChanges}"
        )
    }
}
