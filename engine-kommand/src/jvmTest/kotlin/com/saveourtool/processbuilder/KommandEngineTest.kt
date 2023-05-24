package com.saveourtool.processbuilder

import com.saveourtool.processbuilder.utils.CurrentOs
import com.saveourtool.processbuilder.utils.fs
import com.saveourtool.processbuilder.utils.getCurrentOs

import okio.Path.Companion.toPath

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking

class KommandEngineTest {
    private val processBuilder = ProcessBuilder(KommandEngine, fs)

    @Test
    fun `check stderr`() {
        val actualResult = runBlocking { processBuilder.execute("cd non_existent_dir") }
        val (expectedCode, expectedStderr) = when (getCurrentOs()) {
            CurrentOs.LINUX -> 2 to listOf("/bin/sh: 1: cd: can't cd to non_existent_dir")
            CurrentOs.MACOS -> 1 to listOf("/bin/sh: line 0: cd: non_existent_dir: No such file or directory")
            CurrentOs.WINDOWS -> 1 to listOf("The system cannot find the path specified.")
            else -> return
        }
        assertEquals(expectedCode, actualResult.code)
        assertEquals(emptyList(), actualResult.stdout)
        assertEquals(expectedStderr, actualResult.stderr)
    }

    @Test
    fun `check stderr with additional warning`() {
        val actualResult = runBlocking {
            processBuilder.execute("cd non_existent_dir") {
                stderr = ProcessBuilderConfig.Redirect.File("/dev/null".toPath())
            }
        }
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
}
