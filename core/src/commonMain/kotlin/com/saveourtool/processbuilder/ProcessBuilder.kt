/**
 * Utilities to run a process and get its result.
 */

package com.saveourtool.processbuilder

import com.saveourtool.processbuilder.exceptions.ProcessExecutionException
import com.saveourtool.processbuilder.exceptions.ProcessTimeoutException
import com.saveourtool.processbuilder.utils.createFile
import com.saveourtool.processbuilder.utils.isCurrentOsWindows

import io.github.oshai.KotlinLogging
import okio.FileSystem
import okio.Path

import kotlin.time.Duration

internal val logger = KotlinLogging.logger { }

/**
 * Class contains common logic for all platforms
 *
 * @property fs describes the current file system
 */
class ProcessBuilder(
    private val fs: FileSystem,
    configBuilder: ProcessBuilderConfig.() -> Unit = { },
) {
    private val config = ProcessBuilderConfig().apply(configBuilder)

    /**
     * Execute [command] and wait for its completion.
     *
     * @param command executable command with arguments
     * @param directory where to execute provided command, i.e. `cd [directory]` will be performed before [command] execution
     * @param timeOutDuration max command execution time
     * @param redirectTo a file where process output and errors should be redirected. If null, output will be returned as [ExecutionResult.stdout] and [ExecutionResult.stderr].
     * @return [ExecutionResult] built from process output
     * @throws ProcessExecutionException in case of impossibility of command execution
     * @throws ProcessTimeoutException if timeout is exceeded
     */
    @Suppress(
        "TOO_LONG_FUNCTION",
        "TooGenericExceptionCaught",
        "ReturnCount",
        "SwallowedException",
    )
    fun exec(
        command: String,
        directory: Path? = config.defaultWorkingDirectory,
        timeOutDuration: Duration = config.defaultExecutionTimeout,
        redirectTo: ProcessBuilderConfig.Redirects = config.defaultRedirects,
    ): ExecutionResult {
        if (command.isBlank()) {
            logErrorAndThrowProcessBuilderException("Execution command in ProcessBuilder couldn't be empty!")
        }
        if (config.defaultRedirects !is ProcessBuilderConfig.Redirects.None && command.contains(">")) {
            logger.error {
                "Found user provided redirections in `$command`. " +
                        "SAVE will create own redirections for internal purpose, " +
                        "please refuse redirects or use corresponding argument [redirectTo]"
            }
        }

        // Temporary directory for stderr and stdout (posix `system()` can't separate streams, so we do it ourselves)
        // Path to stdout file
        val stdoutFile = redirectTo.redirectStdoutTo
        logger.trace { "Creating stdout file of ProcessBuilder: $stdoutFile" }
        // Path to stderr file
        val stderrFile = redirectTo.redirectStderrTo
        logger.trace { "Creating stderr file of ProcessBuilder: $stderrFile" }
        // Instance, containing platform-dependent realization of command execution
        val processBuilderInternal = ProcessBuilderInternal(redirectTo, config.childProcessUsername)

        stdoutFile?.let {  path ->
            fs.createFile(path)
            logger.debug { "Created file $path" }
        }
        stderrFile?.let { path ->
            fs.createFile(path)
            logger.debug { "Created file $path" }
        }

        val cmd = modifyCmd(command, directory, processBuilderInternal)

        logger.debug { "Executing: $cmd with timeout $timeOutDuration ms" }
        val executionResult = try {
            processBuilderInternal.execute(cmd, timeOutDuration)
        } catch (ex: ProcessTimeoutException) {
            stdoutFile?.let { fs.delete(it) }
            stderrFile?.let { fs.delete(it) }
            throw ex
        } catch (ex: Exception) {
            stdoutFile?.let { fs.delete(it) }
            stderrFile?.let { fs.delete(it) }
            logErrorAndThrowProcessBuilderException(ex.message ?: "Couldn't execute $cmd")
        }

        if (executionResult.stderr.isNotEmpty()) {
            logger.debug { "stderr of `$command`:\t${executionResult.stderr.joinToString("\t")}" }
        }

        return executionResult
    }

    private fun modifyCmd(
        command: String,
        directory: Path?,
        processBuilderInternal: ProcessBuilderInternal,
    ): String = command
        .let(::processCommandWithEchoOnWindows)
        .also { cmd -> logger.trace { "Processed command with echo for windows: $cmd" } }
        .let { changeWorkingDirectory(command, directory) }
        .also { cmd -> logger.trace { "Processed command for directory change: $cmd" } }
        .let(processBuilderInternal::appendShellCommand)
        .also { cmd -> logger.trace { "Redirected stdout and stderr: $cmd" } }
        .let(processBuilderInternal::runCommandByNameOfAnotherUser)
        .also { cmd -> logger.trace { "Modified command to run by name of user ${config.childProcessUsername}: $cmd" } }

    private fun processCommandWithEchoOnWindows(command: String): String = when {
        isCurrentOsWindows() -> processCommandWithEcho(command)
        else -> command
    }

    private fun changeWorkingDirectory(command: String, directory: Path?): String = when {
        directory == null -> ""
        directory.segments.isEmpty() -> "".also { logger.warn { "Requested working directory " } }
        isCurrentOsWindows() -> "cd /d $directory && "
        else -> "cd $directory && "
    }.plus(command)

    companion object {
        /**
         * Check whether there are exists `echo` commands, and process them, since in Windows
         * `echo` adds extra whitespaces and newlines. This method will remove them
         *
         * @param command command to process
         * @return unmodified command, if there is no `echo` subcommands, otherwise add parameter `set /p=` to `echo`
         */
        @Suppress("ReturnCount")
        fun processCommandWithEcho(command: String): String {
            if (!command.contains("echo")) {
                return command
            }
            // Command already contains correct signature.
            // We also believe not to met complex cases: `echo a; echo | set /p="a && echo b"`
            // TODO: https://github.com/saveourtool/process-builder-multiplatform/issues/5
            val cmdWithoutWhitespaces = command.replace(" ", "")
            if (cmdWithoutWhitespaces.contains("echo|set")) {
                return command
            }
            if (cmdWithoutWhitespaces.contains("echo\"")) {
                logger.warn { "You can use echo | set /p\"your command\" to avoid extra whitespaces on Windows" }
                return command
            }
            // If command is complex (have `&&` or `;`), we need to modify only `echo` subcommands
            val separator = if (command.contains("&&")) {
                "&&"
            } else if (command.contains(";")) {
                ";"
            } else {
                ""
            }
            val listOfCommands = if (separator != "") command.split(separator) as MutableList<String> else mutableListOf(command)
            listOfCommands.forEachIndexed { index, cmd ->
                if (cmd.contains("echo")) {
                    // TODO: https://github.com/saveourtool/process-builder-multiplatform/issues/5
                    var newEchoCommand = cmd.trim(' ').replace("echo ", " echo | set /p dummyName=\"")
                    // Now we need to add closing `"` in proper place
                    // Despite the fact, that we don't expect user redirections, for out internal tests we use them,
                    // so we need to process such cases
                    // There are three different cases, where we need to insert closing `"`.
                    // 1) Before stdout redirection
                    // 2) Before stderr redirection
                    // 3) At the end of string, if there is no redirections
                    val indexOfStdoutRedirection = if (newEchoCommand.indexOf(">") != -1) newEchoCommand.indexOf(">") else newEchoCommand.length
                    val indexOfStderrRedirection = if (newEchoCommand.indexOf("2>") != -1) newEchoCommand.indexOf("2>") else newEchoCommand.length
                    val insertIndex = minOf(indexOfStdoutRedirection, indexOfStderrRedirection)
                    newEchoCommand = newEchoCommand.substring(0, insertIndex).trimEnd(' ') + "\" " + newEchoCommand.substring(insertIndex, newEchoCommand.length) + " "
                    listOfCommands[index] = newEchoCommand
                }
            }
            val modifiedCommand = listOfCommands.joinToString(separator).trim(' ')
            logger.trace { "Additionally modify command:`$command` to `$modifiedCommand` because of `echo` on Windows add extra newlines" }
            return modifiedCommand
        }
    }
}

/**
 * Log error message and throw exception
 *
 * @param errMsg error message
 * @throws ProcessExecutionException
 */
private fun logErrorAndThrowProcessBuilderException(errMsg: String): Nothing {
    logger.error { errMsg }
    throw ProcessExecutionException(errMsg)
}
