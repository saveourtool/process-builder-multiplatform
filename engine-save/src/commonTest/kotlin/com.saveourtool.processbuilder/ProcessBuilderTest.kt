package com.saveourtool.processbuilder

import com.saveourtool.processbuilder.utils.isCurrentOsWindows

import okio.FileSystem
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking

val fs: FileSystem = FakeFileSystem()

class ProcessBuilderTest {
    private val processBuilder = ProcessBuilder(SaveEngine, fs)

    @Test
    fun `check stdout`() = runBlocking {
        val actualResult = processBuilder.execute("echo something")
        val expectedCode = 0
        val expectedStdout = listOf("something")
        assertEquals(expectedCode, actualResult.code)
        assertEquals(expectedStdout, actualResult.stdout)
        assertEquals(emptyList(), actualResult.stderr)
    }

    @Test
    fun `check stdout with redirection`() = runBlocking {
        val actualResult = processBuilder.execute("echo something") {
            stdout = ProcessBuilderConfig.Redirect.File("/dev/null".toPath())
        }
        val (expectedCode, expectedStderr) = when {
            isCurrentOsWindows() -> 1 to listOf("The system cannot find the path specified.")
            else -> 0 to emptyList()
        }
        assertEquals(expectedCode, actualResult.code)
        assertEquals(emptyList(), actualResult.stdout)
        assertEquals(expectedStderr, actualResult.stderr)
    }
}
