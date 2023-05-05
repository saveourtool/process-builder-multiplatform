package com.saveourtool.processbuilder

import okio.Path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Configuration file that is used to set up run configuration for [ProcessBuilder]'s run
 *
 * @property childProcessUsername user which should run the child process
 * @property executionTimeout execution timeout as [Duration], 1 min by default
 * @property workingDirectory [Path] to working directory (`cd $defaultWorkingDirectory` is appended to command)
 *           or [null] if no working dir change is required, can be overwritten in [ProcessBuilder.exec], [null] by default
 * @property stdout WIP
 * @property stderr WIP
 */
data class ProcessBuilderConfig(
    var childProcessUsername: String? = null,
    var executionTimeout: Duration = 1.minutes,
    var workingDirectory: Path? = null,
    var stdout: Redirect = Redirect.Pipe,
    var stderr: Redirect = Redirect.Pipe,
) {
    /**
     * Configuration for redirects
     */
    sealed interface Redirect {
        /**
         * Redirect should be inherited from parent process
         */
        object Inherit : Redirect

        /**
         * Pipes should be used in order for redirect
         */
        object Pipe : Redirect

        /**
         * Output should be redirected to file
         *
         * @property path path to file
         */
        class File(val path: Path) : Redirect
    }
}
