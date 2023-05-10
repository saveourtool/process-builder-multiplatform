package com.saveourtool.processbuilder

import com.saveourtool.processbuilder.utils.CurrentOs
import com.saveourtool.processbuilder.utils.fs
import com.saveourtool.processbuilder.utils.getCurrentOs
import io.kotest.common.runBlocking
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals

class KommandEngineTest {
    private val processBuilder = ProcessBuilder(KommandEngine, fs)

    @Test
    fun `check stderr`() = runBlocking {
        val actualResult = processBuilder.execute("cd non_existent_dir")
        val (expectedCode, expectedStderr) = when (getCurrentOs()) {
            CurrentOs.LINUX -> 512 to listOf("/bin/sh: 1: cd: can't cd to non_existent_dir")
            CurrentOs.MACOS -> 256 to listOf("/bin/sh: line 0: cd: non_existent_dir: No such file or directory")
            else -> return@runBlocking
        }
        assertEquals(expectedCode, actualResult.code)
        assertEquals(emptyList(), actualResult.stdout)
        assertEquals(expectedStderr, actualResult.stderr)
    }

    @Test
    fun `check stderr redirect to dev null`() = runBlocking {
        val actualResult = processBuilder.execute("cd non_existent_dir") {
            stderr = ProcessBuilderConfig.Redirect.File("/dev/null".toPath())
        }
        val expectedCode = when (getCurrentOs()) {
            CurrentOs.LINUX -> 512
            CurrentOs.MACOS -> 256
            else -> return@runBlocking
        }
        assertEquals(expectedCode, actualResult.code)
        assertEquals(emptyList(), actualResult.stdout)
        assertEquals(emptyList(), actualResult.stderr)
    }

    @Test
    fun `check stderr redirect to Null`() = runBlocking {
        val actualResult = processBuilder.execute("cd non_existent_dir") {
            stderr = ProcessBuilderConfig.Redirect.Null
        }
        val expectedCode = when (getCurrentOs()) {
            CurrentOs.LINUX -> 512
            CurrentOs.MACOS -> 256
            else -> return@runBlocking
        }
        assertEquals(expectedCode, actualResult.code)
        assertEquals(emptyList(), actualResult.stdout)
        assertEquals(emptyList(), actualResult.stderr)
    }
}
