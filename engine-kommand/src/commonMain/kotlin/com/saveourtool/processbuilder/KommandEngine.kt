package com.saveourtool.processbuilder

import com.saveourtool.processbuilder.utils.CurrentOs
import com.saveourtool.processbuilder.utils.cwd
import com.saveourtool.processbuilder.utils.getCurrentOs
import com.saveourtool.processbuilder.utils.logger
import com.saveourtool.processbuilder.utils.stderr
import com.saveourtool.processbuilder.utils.stdout

import com.kgit2.process.Command

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

private typealias CommandWithArgs = Pair<String, Array<String>>

object KommandEngine : Engine {
    private val scope = CoroutineScope(Dispatchers.Default)

    override suspend fun execute(
        command: String,
        config: ProcessBuilderConfig,
    ): ExecutionResult {
        val (cmd, args) = getCommandWithArgs(escapeDoubleTicks(command)).also {
            logger.info { "${it.first} ${it.second.joinToString(" ")}" }
        }
        val stdout = config.stdout
        val stderr = config.stderr
        val child = Command(cmd)
            .args(*args)
            .cwd(config.workingDirectory)
            .stdout(stdout)
            .stderr(stderr)
            .spawn()
        return scope.async {
            val stdoutLines = child.getChildStdout()?.lines()?.toList().orEmpty()
                .let(stdout::printToFileIfNeeded)
            val stderrLines = child.getChildStderr()?.lines()?.toList().orEmpty()
                .let(stderr::printToFileIfNeeded)
            val childExitStatus = child.wait()
            val executionResult = ExecutionResult(
                childExitStatus.code,
                stdoutLines,
                stderrLines,
            ).also { result ->
                if (result.stderr.isNotEmpty()) {
                    logger.warn { "stderr of `$command`:\t${result.stderr.joinToString("\t")}" }
                }
            }
            executionResult
        }.await()
    }

    private fun getCommandWithArgs(command: String): CommandWithArgs = when (getCurrentOs()) {
        CurrentOs.WINDOWS -> "CMD.EXE" to arrayOf("/C", command)
        else -> "/bin/sh" to arrayOf("-c", command)
    }

    private fun escapeDoubleTicks(command: String): String {
        logger.warn { "Double ticks are not escaped yet" }
        return command
    }
}
