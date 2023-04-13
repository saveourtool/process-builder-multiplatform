/**
 * Utilities to run a process and get its result.
 */

package com.saveourtool.processbuilder

import com.saveourtool.processbuilder.exceptions.ProcessTimeoutException
import com.saveourtool.processbuilder.utils.fs
import com.saveourtool.processbuilder.utils.isCurrentOsWindows
import com.saveourtool.processbuilder.utils.readLines

import platform.posix.system

import kotlin.time.Duration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking

@Suppress(
    "MISSING_KDOC_TOP_LEVEL",
    "MISSING_KDOC_CLASS_ELEMENTS",
    "MISSING_KDOC_ON_FUNCTION"
)
actual class ProcessBuilderInternal actual constructor(
    private val redirects: ProcessBuilderConfig.Redirects,
    private val childProcessUserName: String?,
) {
    actual fun appendShellCommand(command: String) = when (redirects) {
        is ProcessBuilderConfig.Redirects.None -> command
        is ProcessBuilderConfig.Redirects.Stdout -> "($command) >${redirects.redirectStdoutTo}"
        is ProcessBuilderConfig.Redirects.Stderr -> "($command) 2>${redirects.redirectStderrTo}"
        is ProcessBuilderConfig.Redirects.All -> "($command) >${redirects.redirectStdoutTo} 2>${redirects.redirectStderrTo}"
        else -> throw IllegalStateException()
    }

    actual fun runCommandByNameOfAnotherUser(command: String): String = childProcessUserName?.let {
        "sudo -u $childProcessUserName "
    }
        .orEmpty()
        .plus(command)

    @OptIn(ExperimentalCoroutinesApi::class)
    actual fun execute(
        cmd: String,
        timeoutDuration: Duration,
    ): ExecutionResult {
        var status = -1
        /*
         * fixme: this should be implemented with posix api like fork, pipe and exec
         */
        runBlocking {
            val timeOut = async(newSingleThreadContext("timeOut")) {
                delay(timeoutDuration)
                destroy(cmd)
                throw ProcessTimeoutException(timeoutDuration, "Timeout is reached: $timeoutDuration")
            }

            val command = async {
                status = system(cmd)
                timeOut.cancel()
            }
            joinAll(timeOut, command)
        }

        if (status == -1) {
            error("Couldn't execute $cmd, exit status: $status")
        }

        val stdout = redirects.redirectStdoutTo?.let { fs.readLines(it) }.orEmpty()
        val stderr = redirects.redirectStderrTo?.let { fs.readLines(it) }.orEmpty()

        return ExecutionResult(status, stdout, stderr)
    }

    private fun destroy(cmd: String) {
        val killCmd = if (isCurrentOsWindows()) {
            "taskkill /im \"$cmd\" /f"
        } else {
            "pkill \"$cmd\""
        }
        system(killCmd)
        logger.trace { "Executed kill command: $killCmd" }
    }
}
