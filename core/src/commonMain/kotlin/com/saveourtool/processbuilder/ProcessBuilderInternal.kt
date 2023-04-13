/**
 * Utilities to run a process and get its result.
 */

package com.saveourtool.processbuilder

import kotlin.time.Duration

/**
 * A class that is capable of executing processes, specific to different OS and returning their output.
 */
expect class ProcessBuilderInternal(
    redirects: ProcessBuilderConfig.Redirects,
    childProcessUserName: String?,
) {
    /**
     * Modify execution command according behavior of different OS,
     * also stdout and stderr will be redirected to tmp files
     *
     * @param command raw command
     * @return command prepared for execution
     */
    fun appendShellCommand(command: String): String

    /**
     * Modify execution command according to requested executing user
     *
     * @param command raw command
     * @return command with `sudo -u` prepended
     */
    fun runCommandByNameOfAnotherUser(command: String): String

    /**
     * Execute [cmd] and wait for its completion.
     *
     * @param cmd executable command with arguments
     * @param timeoutDuration max command execution time
     * @return [ExecutionResult]
     */
    fun execute(
        cmd: String,
        timeoutDuration: Duration,
    ): ExecutionResult
}
