package com.saveourtool.processbuilder

import com.saveourtool.processbuilder.utils.CurrentOs
import com.saveourtool.processbuilder.utils.fs
import com.saveourtool.processbuilder.utils.getCurrentOs
import io.kotest.common.runBlocking
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class KommandEngineTest {
    private val processBuilder = ProcessBuilder(KommandEngine, fs)

    @Test
    fun `check stderr`() = runBlocking {
        val actualResult = processBuilder.execute("cd non_existent_dir")
        val (expectedCode, expectedStderr) = when (getCurrentOs()) {
            CurrentOs.WINDOWS -> 1 to listOf("The system cannot find the path specified.")
            else -> return@runBlocking
        }
        assertEquals(expectedCode, actualResult.code)
        assertEquals(emptyList(), actualResult.stdout)
        assertEquals(expectedStderr, actualResult.stderr)
    }

    @Test
    fun `check stderr with stderr wrong redirect`() = runBlocking {
        val actualResult = processBuilder.execute("cd non_existent_dir") {
            stderr = ProcessBuilderConfig.Redirect.File("/dev/null".toPath())
        }
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

    @Test
    fun `check stderr with correct stderr redirect`() = runBlocking {
        val actualResult = processBuilder.execute("cd non_existent_dir") {
            stderr = ProcessBuilderConfig.Redirect.File("NUL".toPath())
        }
        val (expectedCode, expectedStderr) = when (getCurrentOs()) {
            CurrentOs.WINDOWS -> 1 to listOf("The system cannot find the path specified.")
            else -> return@runBlocking
        }
        assertEquals(expectedCode, actualResult.code)
        assertEquals(emptyList(), actualResult.stdout)
        assertEquals(expectedStderr, actualResult.stderr)
        assertFalse { fs.exists("NUL".toPath()) }
    }
}
