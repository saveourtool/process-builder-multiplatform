package com.saveourtool.processbuilder

import com.saveourtool.processbuilder.utils.createFile
import com.saveourtool.processbuilder.utils.fs
import com.saveourtool.processbuilder.utils.logErrorAndThrowProcessBuilderException
import com.saveourtool.processbuilder.utils.logger
import com.saveourtool.processbuilder.utils.readLines

import okio.Path
import okio.Path.Companion.toPath

import kotlinx.coroutines.TimeoutCancellationException

object SaveEngine : Engine {
    override suspend fun execute(command: String, config: ProcessBuilderConfig): ExecutionResult {
        val stdoutFile: Path = redirectStdout(config)
        val stderrFile: Path = redirectStderr(config)

        val processBuilderInternal = ProcessBuilderInternal(config)
        fs.createFile(stdoutFile).also { logger.debug { "Created stdout file $it" } }
        fs.createFile(stderrFile).also { logger.debug { "Created stderr file $it" } }

        val cmd = "($command) >$stdoutFile 2>$stderrFile"

        logger.debug { "Executing: $cmd with timeout ${config.executionTimeout}" }
        @Suppress("TooGenericExceptionCaught")
        val status = try {
            processBuilderInternal.exec(cmd)
        } catch (ex: TimeoutCancellationException) {
            cleanup(stdoutFile, stderrFile, config)
            throw ex
        } catch (ex: Exception) {
            cleanup(stdoutFile, stderrFile, config)
            logErrorAndThrowProcessBuilderException(ex.message ?: "Couldn't execute $cmd")
        }
        val stdout: List<String> = run { fs.readLines(stdoutFile) }
        val stderr: List<String> = run { fs.readLines(stderrFile) }
        cleanup(stdoutFile, stderrFile, config)
        val executionResult = ExecutionResult(status, stdout, stderr)
        if (executionResult.stderr.isNotEmpty()) {
            logger.debug { "stderr of `$cmd`:\t${executionResult.stderr.joinToString("\t")}" }
        }
        return executionResult
    }

    /**
     * For now, we use temp files for storing stdout and stderr, which should be cleaned up
     */
    private fun cleanup(
        stdoutFile: Path,
        stderrFile: Path,
        config: ProcessBuilderConfig
    ) {
        try {
            if (config.stdout !is ProcessBuilderConfig.Redirect.File) {
                fs.delete(stdoutFile)
            }
        } finally {
            if (config.stderr !is ProcessBuilderConfig.Redirect.File) {
                fs.delete(stderrFile)
            }
        }
    }

    private fun redirect(
        redirectConfig: ProcessBuilderConfig.Redirect,
        defaultPath: Path,
    ) = if (redirectConfig is ProcessBuilderConfig.Redirect.File) {
        redirectConfig.path
    } else {
        defaultPath
    }

    private fun redirectStdout(config: ProcessBuilderConfig) = redirect(config.stdout, "stdout.txt".toPath())

    private fun redirectStderr(config: ProcessBuilderConfig) = redirect(config.stderr, "stderr.txt".toPath())
}
