package com.saveourtool.processbuilder

import okio.Path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Configuration file that
 *
 * @property childProcessUsername User which should run the child process
 * @property defaultExecutionTimeout default timeout [Duration], can be overwritten in [ProcessBuilder.exec], 1 min by default
 * @property defaultWorkingDirectory [Path] to working directory (`cd $defaultWorkingDirectory` is appended to command)
 *           or [null] if no working dir change is required, can be overwritten in [ProcessBuilder.exec], [null] by default
 * @property defaultRedirects
 */
data class ProcessBuilderConfig internal constructor(
    var childProcessUsername: String? = null,
    var defaultExecutionTimeout: Duration = 1.minutes,
    var defaultWorkingDirectory: Path? = null,
    var defaultRedirects: Redirects = Redirects.None,
) {
    /**
     * Configuration for redirects
     *
     * @property redirectStdoutTo [Path] to redirect stdout of a child process
     * @property redirectStderrTo [Path] to redirect stderr of a child process
     */
    sealed class Redirects(
        val redirectStdoutTo: Path?,
        val redirectStderrTo: Path?,
    ) {
        /**
         * [Redirects] which defines no redirects:
         *  * stdout will be read by [ProcessBuilder] through pipe
         *  * stderr will be read by [ProcessBuilder] through pipe
         *
         * @see [Stdout]
         * @see [Stderr]
         * @see [All]
         */
        object None : Redirects(null, null)

        /**
         * [Redirects] which defines stdout redirect only:
         *  * stdout will be redirected to [redirectStdoutTo]
         *  * stderr will be read by [ProcessBuilder] through pipe
         *
         * @see [None]
         * @see [Stderr]
         * @see [All]
         */
        class Stdout(redirectStdoutTo: Path) : Redirects(redirectStdoutTo, null)

        /**
         * [Redirects] which defines stderr redirect only:
         *  * stdout will be read by [ProcessBuilder] through pipe
         *  * stderr will be redirected to [redirectStderrTo]
         *
         * @see [None]
         * @see [Stdout]
         * @see [All]
         */
        class Stderr(redirectStderrTo: Path) : Redirects(null, redirectStderrTo)

        /**
         * [Redirects] which defines stdout and stderr redirects:
         *  * stdout will be redirected to [redirectStdoutTo]
         *  * stderr will be redirected to [redirectStderrTo]
         *
         * @see [None]
         * @see [Stdout]
         * @see [All]
         */
        class All(redirectStdoutTo: Path, redirectStderrTo: Path) : Redirects(redirectStdoutTo, redirectStderrTo)
    }
}
