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
        val stdoutFile: Path? = redirectStdout(config)
        val stderrFile: Path? = redirectStderr(config)

        val processBuilderInternal = ProcessBuilderInternal(config)
        val redirectStdoutModifier = stdoutFile?.let { stdoutPath ->
            fs.createFile(stdoutPath).also { logger.debug { "Created stdout file $it" } }
            ">$stdoutPath"
        }.orEmpty()
        val redirectStderrModifier = stderrFile?.let { stderrPath ->
            fs.createFile(stderrPath).also { logger.debug { "Created stderr file $it" } }
            "2>$stderrPath"
        }

        val cmd = "($command) $redirectStdoutModifier $redirectStderrModifier"

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
        val stdout: List<String> = run { stdoutFile?.let { fs.readLines(it) }.orEmpty() }
        val stderr: List<String> = run { stderrFile?.let { fs.readLines(it) }.orEmpty() }
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
        stdoutFile: Path?,
        stderrFile: Path?,
        config: ProcessBuilderConfig
    ) {
        try {
            if (config.stdout !is ProcessBuilderConfig.Redirect.File) {
                stdoutFile?.let { fs.delete(it) }
            }
        } finally {
            if (config.stderr !is ProcessBuilderConfig.Redirect.File) {
                stderrFile?.let { fs.delete(it) }
            }
        }
    }

    private fun redirect(
        redirectConfig: ProcessBuilderConfig.Redirect,
        defaultPath: Path,
    ) = when (redirectConfig) {
        is ProcessBuilderConfig.Redirect.File -> redirectConfig.path
        is ProcessBuilderConfig.Redirect.Null -> null
        else -> defaultPath
    }

    private fun redirectStdout(config: ProcessBuilderConfig) = redirect(config.stdout, "stdout.txt".toPath())

    private fun redirectStderr(config: ProcessBuilderConfig) = redirect(config.stderr, "stderr.txt".toPath())
}
