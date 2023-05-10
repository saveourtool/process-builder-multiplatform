package com.saveourtool.processbuilder

import com.saveourtool.processbuilder.utils.CurrentOs
import com.saveourtool.processbuilder.utils.fs
import com.saveourtool.processbuilder.utils.getCurrentOs
import okio.Path
import okio.Path.Companion.toPath
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
         * Output should be discarded
         */
        object Null : Redirect {
            override fun printToFileIfNeeded(stringsToPrint: List<String>): List<String> = emptyList()
        }

        /**
         * @param stringsToPrint strings that should be printed
         * @return [List] of [String] that should be a part of ExecutionResult class
         */
        fun printToFileIfNeeded(stringsToPrint: List<String>): List<String> = stringsToPrint

        /**
         * Output should be redirected to file
         *
         * @property path path to file
         */
        class File(val path: Path) : Redirect {
            /**
             * Flag that determines if [path] is `/dev/null` or `NUL`
             */
            private val isNullAlias = path == getNullAlias()

            /**
             * @param stringsToPrint list of strings that should be printed
             */
            override fun printToFileIfNeeded(stringsToPrint: List<String>): List<String> {
                if (!isNullAlias) {
                    fs.write(path) { stringsToPrint.forEach(::writeUtf8) }
                }
                return emptyList()
            }
            companion object {
                private fun getNullAlias(os: CurrentOs = getCurrentOs()) = when (os) {
                    CurrentOs.WINDOWS -> "NUL".toPath()
                    else -> "/dev/null".toPath()
                }
            }
        }
    }
}
