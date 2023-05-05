/**
 * Tests for ProcessBuilder's common part
 */

package com.saveourtool.processbuilder

import com.saveourtool.processbuilder.exceptions.ProcessExecutionException
import com.saveourtool.processbuilder.utils.fs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking

class ProcessBuilderTest {
    private val processBuilder = ProcessBuilder(StubEngine, fs)

    @Test
    fun `empty command`(): Unit = runBlocking {
        try {
            processBuilder.execute(" ")
        } catch (ex: ProcessExecutionException) {
            assertEquals("Execution command in ProcessBuilder couldn't be empty!", ex.message)
        }
    }
}

private object StubEngine : Engine {
    override suspend fun execute(command: String, config: ProcessBuilderConfig) = ExecutionResult(
        code = 1,
        stdout = emptyList(),
        stderr = emptyList(),
    )
}
