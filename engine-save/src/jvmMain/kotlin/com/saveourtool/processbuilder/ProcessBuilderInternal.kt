/**
 * Utilities to run a process and get its result.
 */

package com.saveourtool.processbuilder

import com.saveourtool.processbuilder.utils.isCurrentOsWindows
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

actual class ProcessBuilderInternal actual constructor(private val config: ProcessBuilderConfig) {
    @Suppress("UnsafeCallOnNullableType")
    actual fun exec(cmd: String): Int {
        val command = wrapCommandWithInterpreterCall(cmd)
        return runBlocking {
            val runTime = Runtime.getRuntime()
            withTimeout(config.executionTimeout) {
                runTime.exec(command).waitFor()
            }
        }
    }

    private fun wrapCommandWithInterpreterCall(command: String) = if (isCurrentOsWindows()) {
        arrayOf("CMD.EXE", "/C", command)
    } else {
        arrayOf("sh", "-c", command)
    }
}
