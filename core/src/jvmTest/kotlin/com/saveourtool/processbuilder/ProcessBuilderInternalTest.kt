package com.saveourtool.processbuilder

import com.saveourtool.processbuilder.utils.CurrentOs
import com.saveourtool.processbuilder.utils.fs
import com.saveourtool.processbuilder.utils.getCurrentOs
import okio.FileSystem
import okio.Path.Companion.toPath
import org.junit.jupiter.api.AfterAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class ProcessBuilderInternalTest {
    private val processBuilder = ProcessBuilder(FileSystem.SYSTEM) {
        defaultExecutionTimeout = 10.seconds
        defaultRedirects = ProcessBuilderConfig.Redirects.All(stdoutPath, stderrPath)
    }

    @Test
    fun `check stderr`() {
        val actualResult = processBuilder.exec("cd non_existent_dir")
        val (expectedCode, expectedStderr) = when (getCurrentOs()) {
            CurrentOs.LINUX -> 2 to listOf("sh: 1: cd: can't cd to non_existent_dir")
            CurrentOs.MACOS -> 1 to listOf("sh: line 0: cd: non_existent_dir: No such file or directory")
            CurrentOs.WINDOWS -> 1 to listOf("The system cannot find the path specified.")
            else -> return
        }
        assertEquals(expectedCode, actualResult.code)
        assertEquals(emptyList(), actualResult.stdout)
        assertEquals(expectedStderr, actualResult.stderr)
    }

    @Test
    fun `check stderr with additional warning`() {
        val actualResult = processBuilder.exec("cd non_existent_dir 2>/dev/null")
        val (expectedCode, expectedStderr) = when (getCurrentOs()) {
            CurrentOs.LINUX -> 2 to emptyList()
            CurrentOs.MACOS -> 1 to emptyList()
            CurrentOs.WINDOWS -> 1 to listOf("The system cannot find the path specified.")
            else -> return
        }
        assertEquals(expectedCode, actualResult.code)
        assertEquals(emptyList(), actualResult.stdout)
        assertEquals(expectedStderr, actualResult.stderr)
    }
    companion object {
        private val stdoutPath = "stdout.txt".toPath()
        private val stderrPath = "stderr.txt".toPath()
        @JvmStatic
        @AfterAll
        fun cleanUp() {
            fs.delete(stdoutPath, false)
            fs.delete(stderrPath, false)
        }
    }
}
