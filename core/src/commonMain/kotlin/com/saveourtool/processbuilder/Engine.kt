package com.saveourtool.processbuilder

/**
 * Interface that should be implemented in order to provide some backend for [ProcessBuilder].
 */
interface Engine {
    /**
     * Method that executes [command] (configured with [config]) and returns the [ExecutionResult]
     *
     * @param command command that should be run as [String]
     * @param config [ProcessBuilderConfig] that should be used for execution
     * @return result of an execution as [ExecutionResult]
     */
    suspend fun execute(command: String, config: ProcessBuilderConfig): ExecutionResult
}
