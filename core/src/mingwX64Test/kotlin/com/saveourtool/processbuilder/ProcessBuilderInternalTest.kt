package com.saveourtool.processbuilder

import com.saveourtool.processbuilder.utils.fs
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.test.AfterClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class ProcessBuilderInternalTest {
    private val stdoutPath = "stdout.txt".toPath()
    private val stderrPath = "stderr.txt".toPath()
    private val processBuilder = ProcessBuilder(FileSystem.SYSTEM) {
        defaultExecutionTimeout = 10.seconds
        defaultRedirects = ProcessBuilderConfig.Redirects.All(stdoutPath, stderrPath)
    }

    @AfterClass
    fun cleanUp() {
        fs.delete(stdoutPath, false)
        fs.delete(stderrPath, false)
    }

    @Test
    fun `check stderr`() {
        val actualResult = processBuilder.exec("cd non_existent_dir")
        assertEquals(1, actualResult.code)
        assertEquals(emptyList(), actualResult.stdout)
        assertEquals(listOf("The system cannot find the path specified."), actualResult.stderr)
    }

    @Test
    fun `check stderr with additional warning`() {
        val actualResult = processBuilder.exec("cd non_existent_dir 2>/dev/null")
        assertEquals(1, actualResult.code)
        assertEquals(emptyList(), actualResult.stdout)
        assertEquals(listOf("The system cannot find the path specified."), actualResult.stderr)
    }
}
