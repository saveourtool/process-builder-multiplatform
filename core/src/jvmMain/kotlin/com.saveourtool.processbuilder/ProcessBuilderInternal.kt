/**
 * Utilities to run a process and get its result.
 */

package com.saveourtool.processbuilder

import com.saveourtool.processbuilder.exceptions.ProcessTimeoutException
import com.saveourtool.processbuilder.utils.fs
import com.saveourtool.processbuilder.utils.isCurrentOsWindows

import okio.Path

import java.io.BufferedReader
import java.io.InputStreamReader

import kotlin.time.Duration
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking

actual class ProcessBuilderInternal actual constructor(
    private val redirects: ProcessBuilderConfig.Redirects,
    private val childProcessUserName: String?,
) {
    actual fun appendShellCommand(command: String): String = buildList {
        if (isCurrentOsWindows()) {
            add("CMD")
            add("/C")
        } else {
            add("sh")
            add("-c")
        }
        add(command)
    }.joinToString()

    actual fun runCommandByNameOfAnotherUser(command: String): String = when {
        childProcessUserName == null -> ""
        isCurrentOsWindows() -> "runas /user:$childProcessUserName "
        else -> "sudo -u $childProcessUserName "
    }.plus(command)

    @OptIn(DelicateCoroutinesApi::class)
    @Suppress("UnsafeCallOnNullableType")
    actual fun execute(
        cmd: String,
        timeoutDuration: Duration,
    ): ExecutionResult {
        var status = -1
        var stdout: List<String> = emptyList()
        var stderr: List<String> = emptyList()
        runBlocking {
            val processContext = newFixedThreadPoolContext(2, "TimeOutWatcher")

            val runTime = Runtime.getRuntime()
            var process: Process? = null
            val job = launch(processContext) {
                val timeOut = launch {
                    delay(timeoutDuration)
                    process?.destroy()
                    throw ProcessTimeoutException(timeoutDuration, "Timeout is reached: $timeoutDuration")
                }
                launch {
                    process = runTime.exec(cmd.split(", ").toTypedArray())
                    stdout = writeDataFromBufferToFile(process!!, "stdout", redirects.redirectStdoutTo)
                    stderr = writeDataFromBufferToFile(process!!, "stderr", redirects.redirectStdoutTo)
                    status = process!!.waitFor()
                    timeOut.cancel()
                }
            }
            job.join()
        }
        return ExecutionResult(status, stdout, stderr)
    }

    private fun writeDataFromBufferToFile(
        process: Process,
        stream: String,
        file: Path?,
    ): List<String> {
        val br = BufferedReader(
            InputStreamReader(
                if (stream == "stdout") {
                    process.inputStream
                } else {
                    process.errorStream
                }
            )
        )
        val data = br.readLines()
        file?.let {
            fs.write(file) {
                write(data.joinToString("\n").encodeToByteArray())
            }
        }
        return data
    }
}
