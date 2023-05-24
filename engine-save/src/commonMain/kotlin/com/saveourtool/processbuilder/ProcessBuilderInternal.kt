/**
 * Utilities to run a process and get its result.
 */

package com.saveourtool.processbuilder

/**
 * A class that is capable of executing processes, specific to different OS and returning their output.
 *
 * @param config [ProcessBuilderConfig] that should be used for execution configuration
 */
expect class ProcessBuilderInternal internal constructor(config: ProcessBuilderConfig) {
    /**
     * Execute [cmd] and wait for its completion.
     *
     * @param cmd executable command with arguments
     * @return exit status
     */
    fun exec(cmd: String): Int
}
