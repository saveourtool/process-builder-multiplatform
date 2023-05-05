/**
 * Main class of library used to run a process and get its result.
 */

package com.saveourtool.processbuilder

import com.saveourtool.processbuilder.exceptions.ProcessExecutionException
import com.saveourtool.processbuilder.utils.logErrorAndThrowProcessBuilderException
import com.saveourtool.processbuilder.utils.logger

import okio.FileSystem

/**
 * Main class that is used for running commands
 *
 * @property engine underlying process builder [Engine]
 * @property fs describes the current file system
 */
class ProcessBuilder(private val engine: Engine, private val fs: FileSystem) {
    /**
     * Execute [command] and wait for its completion.
     *
     * @param command executable command with arguments
     * @param configBuilder
     * @return [ExecutionResult] built from process output
     * @throws ProcessExecutionException in case of impossibility of command execution
     */
    suspend fun execute(
        command: String,
        configBuilder: ProcessBuilderConfig.() -> Unit = { },
    ): ExecutionResult {
        val config = ProcessBuilderConfig().apply(configBuilder)
        val preprocessor = Preprocessor(config)
        if (command.isBlank()) {
            logErrorAndThrowProcessBuilderException("Execution command in ProcessBuilder couldn't be empty!")
        }
        val cmd = preprocessor.modifyCmd(command)

        logger.debug { "Executing: $cmd with timeout ${config.executionTimeout} ms" }

        return engine.execute(cmd, config)
    }
}
