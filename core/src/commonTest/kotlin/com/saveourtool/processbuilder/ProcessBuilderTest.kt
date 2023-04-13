package com.saveourtool.processbuilder

import com.saveourtool.processbuilder.ProcessBuilder.Companion.processCommandWithEcho
import com.saveourtool.processbuilder.exceptions.ProcessExecutionException
import com.saveourtool.processbuilder.utils.fs
import com.saveourtool.processbuilder.utils.isCurrentOsWindows
import okio.Path.Companion.toPath
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class ProcessBuilderTest {
    private val stdoutPath = "stdout.txt".toPath()
    private val stderrPath = "stderr.txt".toPath()
    private val processBuilder = ProcessBuilder(fs) {
        defaultExecutionTimeout = 10.seconds
        defaultRedirects = ProcessBuilderConfig.Redirects.All(stdoutPath, stderrPath)
    }

    @AfterTest
    fun cleanUp() {
        fs.delete(stdoutPath, false)
        fs.delete(stderrPath, false)
    }

    @Test
    fun `empty command`() {
        try {
            processBuilder.exec(" ")
        } catch (ex: ProcessExecutionException) {
            assertEquals("Execution command in ProcessBuilder couldn't be empty!", ex.message)
        }
    }

    @Test
    fun `check stdout`() {
        val actualResult = processBuilder.exec("echo something")
        val expectedCode = 0
        val expectedStdout = listOf("something")
        assertEquals(expectedCode, actualResult.code)
        assertEquals(expectedStdout, actualResult.stdout)
        assertEquals(emptyList(), actualResult.stderr)
    }

    @Test
    fun `check stdout with redirection`() {
        val actualResult = processBuilder.exec("echo something >/dev/null")
        val (expectedCode, expectedStderr) = when {
            isCurrentOsWindows() -> 1 to listOf("The system cannot find the path specified.")
            else -> 0 to emptyList()
        }
        assertEquals(expectedCode, actualResult.code)
        assertEquals(emptyList(), actualResult.stdout)
        assertEquals(expectedStderr, actualResult.stderr)
    }

    @Test
    fun `command without echo`() {
        val inputCommand = "cd /some/dir; cat /some/file ; ls"
        assertEquals(inputCommand, processCommandWithEcho(inputCommand))
    }

    @Test
    fun `simple check`() {
        val inputCommand = "echo something"
        val expectedCommand = "echo | set /p dummyName=\"something\""
        assertEquals(expectedCommand, processCommandWithEcho(inputCommand))
    }

    @Test
    fun `simple check with redirection`() {
        val inputCommand = "echo something > /dev/null"
        val expectedCommand = "echo | set /p dummyName=\"something\" > /dev/null"
        assertEquals(expectedCommand, processCommandWithEcho(inputCommand))
    }

    @Test
    fun `simple check with redirection without first whitespace`() {
        val inputCommand = "echo something> /dev/null"
        val expectedCommand = "echo | set /p dummyName=\"something\" > /dev/null"
        assertEquals(expectedCommand, processCommandWithEcho(inputCommand))
    }

    @Test
    fun `simple check with redirection without whitespaces at all`() {
        val inputCommand = "echo something>/dev/null"
        val expectedCommand = "echo | set /p dummyName=\"something\" >/dev/null"
        assertEquals(expectedCommand, processCommandWithEcho(inputCommand))
    }

    @Test
    fun `one long echo`() {
        val inputCommand = "echo stub STUB stub foo bar "
        val expectedCommand = "echo | set /p dummyName=\"stub STUB stub foo bar\""
        assertEquals(expectedCommand, processCommandWithEcho(inputCommand))
    }

    @Test
    fun `change multiple echo commands with redirections`() {
        val inputCommand = "echo a > /dev/null && echo b 2>/dev/null && ls"
        val expectedCommand = "echo | set /p dummyName=\"a\" > /dev/null && echo | set /p dummyName=\"b\" 2>/dev/null && ls"
        assertEquals(expectedCommand, processCommandWithEcho(inputCommand))
    }

    @Test
    fun `change multiple echo commands with redirections 2`() {
        val inputCommand = "echo a > /dev/null ; echo b 2>/dev/null ; ls"
        val expectedCommand = "echo | set /p dummyName=\"a\" > /dev/null ; echo | set /p dummyName=\"b\" 2>/dev/null ; ls"
        assertEquals(expectedCommand, processCommandWithEcho(inputCommand))
    }

    @Test
    fun `change multiple echo commands with redirections 3`() {
        val inputCommand = "echo a > /dev/null; echo b 2>/dev/null; ls"
        val expectedCommand = "echo | set /p dummyName=\"a\" > /dev/null ; echo | set /p dummyName=\"b\" 2>/dev/null ; ls"
        assertEquals(expectedCommand, processCommandWithEcho(inputCommand))
    }

    @Test
    fun `extra whitespaces shouldn't influence to echo`() {
        val inputCommand = "echo foo bar ; echo b; ls"
        val expectedCommand = "echo | set /p dummyName=\"foo bar\"  ; echo | set /p dummyName=\"b\"  ; ls"
        assertEquals(expectedCommand, processCommandWithEcho(inputCommand))
    }
}
