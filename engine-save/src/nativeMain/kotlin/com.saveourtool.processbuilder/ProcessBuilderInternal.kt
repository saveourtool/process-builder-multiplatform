/**
 * Utilities to run a process and get its result.
 */

package com.saveourtool.processbuilder

import platform.posix.system

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

@Suppress(
    "MISSING_KDOC_TOP_LEVEL",
    "MISSING_KDOC_CLASS_ELEMENTS",
    "MISSING_KDOC_ON_FUNCTION"
)
actual class ProcessBuilderInternal actual constructor(private val config: ProcessBuilderConfig) {
    actual fun exec(cmd: String): Int {
        val status = runBlocking { withTimeout(config.executionTimeout) { system(cmd) } }

        if (status == -1) {
            error("Couldn't execute $cmd, exit status: $status")
        }
        return status
    }
}
