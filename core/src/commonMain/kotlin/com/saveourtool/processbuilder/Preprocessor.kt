package com.saveourtool.processbuilder

import com.saveourtool.processbuilder.utils.CurrentOs
import com.saveourtool.processbuilder.utils.getCurrentOs
import com.saveourtool.processbuilder.utils.logger
import okio.Path

/**
 * Class that should process input command as [String] and modify depending on [os].
 *
 * TODO: Make [Preprocessor] `expect class` and implement platform-dependent `actual`s.
 *
 * TODO: [Preprocessor] should return command as [List] of [String]s
 *
 * @property config [ProcessBuilder] configuration
 * @property os OS expected to run a command that should be modified, by default received from [getCurrentOs]
 */
class Preprocessor(
    private val config: ProcessBuilderConfig,
    private val os: CurrentOs = getCurrentOs(),
) {
    /**
     * TODO: make me return a list of strings
     *
     * @param command command that should be modified
     * @return modified command
     */
    fun modifyCmd(
        command: String,
    ): String = command
        .let(::processCommandWithEchoOnWindows)
        .also { cmd -> logger.trace { "Processed command with echo for windows: $cmd" } }
        .let { changeWorkingDirectory(command, config.workingDirectory) }
        .also { cmd -> logger.trace { "Processed command for directory change: $cmd" } }
        .let(::runCommandByNameOfAnotherUser)
        .also { cmd -> logger.trace { "Modified command to run by name of user ${config.childProcessUsername}: $cmd" } }
        .also { cmd -> logger.debug { "Modified command: $cmd" } }

    private fun processCommandWithEchoOnWindows(command: String): String = when (os) {
        CurrentOs.WINDOWS -> processCommandWithEcho(command)
        else -> command
    }

    private fun runCommandByNameOfAnotherUser(command: String): String = when {
        config.childProcessUsername == null -> ""
        os == CurrentOs.WINDOWS -> "runas /user:${config.childProcessUsername} "
        else -> "sudo -u ${config.childProcessUsername} "
    }.plus(command)

    private fun changeWorkingDirectory(command: String, directory: Path?): String = when {
        directory == null -> ""
        directory.segments.isEmpty() -> "".also { logger.warn { "Requested working directory " } }
        os == CurrentOs.WINDOWS -> "cd /d $directory && "
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
            val listOfCommands = if (separator != "") command.split(separator).toMutableList() else mutableListOf(command)
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
