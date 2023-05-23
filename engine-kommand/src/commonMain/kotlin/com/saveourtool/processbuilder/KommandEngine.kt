package com.saveourtool.processbuilder

import com.saveourtool.processbuilder.utils.CurrentOs
import com.saveourtool.processbuilder.utils.cwd
import com.saveourtool.processbuilder.utils.getCurrentOs
import com.saveourtool.processbuilder.utils.logger
import com.saveourtool.processbuilder.utils.readStderr
import com.saveourtool.processbuilder.utils.readStdout
import com.saveourtool.processbuilder.utils.stderr
import com.saveourtool.processbuilder.utils.stdout

import com.kgit2.process.Command

import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.withTimeout

private typealias CommandWithArgs = Pair<String, Array<String>>

object KommandEngine : Engine {
    override suspend fun execute(
        command: String,
        config: ProcessBuilderConfig,
    ): ExecutionResult {
        val (cmd, args) = getCommandWithArgs(command).also { (command, args) ->
            logger.debug { "Executing $command [${args.joinToString(", ")}]" }
        }
        return withTimeout(config.executionTimeout) {
            val stdout = config.stdout
            val stderr = config.stderr
            val child = Command(cmd)
                .args(*args)
                .cwd(config.workingDirectory)
                .stdout(stdout)
                .stderr(stderr)
                .spawn()

            val stdoutChannel = child.readStdout()
            val stderrChannel = child.readStderr()

            val childExitStatus = child.wait()

            val executionResult = ExecutionResult(
                childExitStatus.code,
                stdoutChannel.toList().let(stdout::printToFileIfNeeded),
                stderrChannel.toList().let(stderr::printToFileIfNeeded),
            ).also { result ->
                if (result.stderr.isNotEmpty()) {
                    logger.warn { "stderr of `$command`:\t${result.stderr.joinToString("\t")}" }
                }
            }
            executionResult
        }
    }

    private fun getCommandWithArgs(command: String): CommandWithArgs = when (getCurrentOs()) {
        CurrentOs.WINDOWS -> "CMD.EXE" to arrayOf("/C", command)
        else -> "/bin/sh" to arrayOf("-c", command)
    }
}
