/**
 * Utilities to run a process and get its result.
 */

package com.saveourtool.processbuilder

import okio.Path

actual class ProcessBuilderInternal actual constructor(
    stdoutFile: Path,
    stderrFile: Path,
    useInternalRedirections: Boolean,
) {
    actual fun prepareCmd(command: String): String = NotImplementedInJsError()
    actual fun exec(cmd: String, timeOutMillis: Long): Int = NotImplementedInJsError()
}
