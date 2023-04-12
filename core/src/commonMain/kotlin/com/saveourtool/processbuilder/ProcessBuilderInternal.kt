/**
 * Utilities to run a process and get its result.
 */

package com.saveourtool.processbuilder

import okio.Path

/**
 * A class that is capable of executing processes, specific to different OS and returning their output.
 */
expect class ProcessBuilderInternal(
    stdoutFile: Path,
    stderrFile: Path,
    useInternalRedirections: Boolean,
) {
    /**
     * Modify execution command according behavior of different OS,
     * also stdout and stderr will be redirected to tmp files
     *
     * @param command raw command
     * @return command with redirection of stderr to tmp file
     */
    fun prepareCmd(command: String): String

    /**
     * Execute [cmd] and wait for its completion.
     *
     * @param cmd executable command with arguments
     * @param timeOutMillis max command execution time
     * @return exit status
     */
    fun exec(
        cmd: String,
        timeOutMillis: Long,
    ): Int
}
