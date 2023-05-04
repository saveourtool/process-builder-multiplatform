package com.saveourtool.processbuilder

import com.saveourtool.processbuilder.utils.CurrentOs
import com.saveourtool.processbuilder.utils.fs
import com.saveourtool.processbuilder.utils.getCurrentOs
import io.kotest.common.runBlocking
import okio.Path.Companion.toPath
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ProcessBuilderInternalTest {
    private val stdoutPath = "stdout.txt".toPath()
    private val stderrPath = "stderr.txt".toPath()
    private val processBuilder = ProcessBuilder(SaveEngine, fs)

    @AfterTest
    fun cleanUp() {
        fs.delete(stdoutPath, false)
        fs.delete(stderrPath, false)
    }

    @Test
    fun `check stderr`() = runBlocking {
        val actualResult = processBuilder.execute("cd non_existent_dir")
        val (expectedCode, expectedStderr) = when (getCurrentOs()) {
            CurrentOs.LINUX -> 512 to listOf("sh: 1: cd: can't cd to non_existent_dir")
            CurrentOs.MACOS -> 256 to listOf("sh: line 0: cd: non_existent_dir: No such file or directory")
            CurrentOs.WINDOWS -> 1 to listOf("The system cannot find the path specified.")
            else -> return@runBlocking
        }
        assertEquals(expectedCode, actualResult.code)
        assertEquals(emptyList(), actualResult.stdout)
        assertEquals(expectedStderr, actualResult.stderr)
    }

    @Test
    fun `check stderr with additional warning`() = runBlocking {
        val actualResult = processBuilder.execute("cd non_existent_dir 2>/dev/null")
        val (expectedCode, expectedStderr) = when (getCurrentOs()) {
            CurrentOs.LINUX -> 512 to emptyList()
            CurrentOs.MACOS -> 256 to emptyList()
            CurrentOs.WINDOWS -> 1 to listOf("The system cannot find the path specified.")
            else -> return@runBlocking
        }
        assertEquals(expectedCode, actualResult.code)
        assertEquals(emptyList(), actualResult.stdout)
        assertEquals(expectedStderr, actualResult.stderr)
    }
}
