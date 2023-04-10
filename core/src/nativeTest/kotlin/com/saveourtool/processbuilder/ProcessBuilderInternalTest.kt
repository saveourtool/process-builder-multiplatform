package com.saveourtool.processbuilder

import com.saveourtool.processbuilder.utils.CurrentOs
import com.saveourtool.processbuilder.utils.getCurrentOs
import okio.FileSystem
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("LOCAL_VARIABLE_EARLY_DECLARATION",
    "SAY_NO_TO_VAR",
    "MISSING_KDOC_TOP_LEVEL",
    "MISSING_KDOC_CLASS_ELEMENTS",
    "MISSING_KDOC_ON_FUNCTION"
)
class ProcessBuilderInternalTest {
    private val processBuilder = ProcessBuilder(useInternalRedirections = true, FileSystem.SYSTEM)

    @Test
    fun `check stderr`() {
        val actualResult = processBuilder.exec("cd non_existent_dir", "", null, 10_000L)
        val expectedStdout: List<String> = emptyList()
        val (expectedCode, expectedStderr) = when (getCurrentOs()) {
            CurrentOs.LINUX -> 512 to listOf("sh: 1: cd: can't cd to non_existent_dir")
            CurrentOs.MACOS -> 256 to listOf("sh: line 0: cd: non_existent_dir: No such file or directory")
            CurrentOs.WINDOWS -> 1 to listOf("The system cannot find the path specified.")
            else -> return
        }
        assertEquals(expectedCode, actualResult.code)
        assertEquals(expectedStdout, actualResult.stdout)
        assertEquals(expectedStderr, actualResult.stderr)
    }

    @Test
    fun `check stderr with additional warning`() {
        val actualResult = processBuilder.exec("cd non_existent_dir 2>/dev/null", "", null, 10_000L)
        val expectedStdout: List<String> = emptyList()
        val (expectedCode, expectedStderr) = when (getCurrentOs()) {
            CurrentOs.LINUX -> 512 to emptyList()
            CurrentOs.MACOS -> 256 to emptyList()
            CurrentOs.WINDOWS -> 1 to listOf("The system cannot find the path specified.")
            else -> return
        }
        assertEquals(expectedCode, actualResult.code)
        assertEquals(expectedStdout, actualResult.stdout)
        assertEquals(expectedStderr, actualResult.stderr)
    }
}
