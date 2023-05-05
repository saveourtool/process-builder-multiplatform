package com.saveourtool.processbuilder

import kotlin.test.Test
import kotlin.test.assertEquals

class PreprocessorTest {
    @Test
    fun `command without echo`() {
        val inputCommand = "cd /some/dir; cat /some/file ; ls"
        assertEquals(inputCommand, Preprocessor.processCommandWithEcho(inputCommand))
    }

    @Test
    fun `simple check`() {
        val inputCommand = "echo something"
        val expectedCommand = "echo | set /p dummyName=\"something\""
        assertEquals(expectedCommand, Preprocessor.processCommandWithEcho(inputCommand))
    }

    @Test
    fun `simple check with redirection`() {
        val inputCommand = "echo something > /dev/null"
        val expectedCommand = "echo | set /p dummyName=\"something\" > /dev/null"
        assertEquals(expectedCommand, Preprocessor.processCommandWithEcho(inputCommand))
    }

    @Test
    fun `simple check with redirection without first whitespace`() {
        val inputCommand = "echo something> /dev/null"
        val expectedCommand = "echo | set /p dummyName=\"something\" > /dev/null"
        assertEquals(expectedCommand, Preprocessor.processCommandWithEcho(inputCommand))
    }

    @Test
    fun `simple check with redirection without whitespaces at all`() {
        val inputCommand = "echo something>/dev/null"
        val expectedCommand = "echo | set /p dummyName=\"something\" >/dev/null"
        assertEquals(expectedCommand, Preprocessor.processCommandWithEcho(inputCommand))
    }

    @Test
    fun `one long echo`() {
        val inputCommand = "echo stub STUB stub foo bar "
        val expectedCommand = "echo | set /p dummyName=\"stub STUB stub foo bar\""
        assertEquals(expectedCommand, Preprocessor.processCommandWithEcho(inputCommand))
    }

    @Test
    fun `change multiple echo commands with redirections`() {
        val inputCommand = "echo a > /dev/null && echo b 2>/dev/null && ls"
        val expectedCommand = "echo | set /p dummyName=\"a\" > /dev/null && echo | set /p dummyName=\"b\" 2>/dev/null && ls"
        assertEquals(expectedCommand, Preprocessor.processCommandWithEcho(inputCommand))
    }

    @Test
    fun `change multiple echo commands with redirections 2`() {
        val inputCommand = "echo a > /dev/null ; echo b 2>/dev/null ; ls"
        val expectedCommand = "echo | set /p dummyName=\"a\" > /dev/null ; echo | set /p dummyName=\"b\" 2>/dev/null ; ls"
        assertEquals(expectedCommand, Preprocessor.processCommandWithEcho(inputCommand))
    }

    @Test
    fun `change multiple echo commands with redirections 3`() {
        val inputCommand = "echo a > /dev/null; echo b 2>/dev/null; ls"
        val expectedCommand = "echo | set /p dummyName=\"a\" > /dev/null ; echo | set /p dummyName=\"b\" 2>/dev/null ; ls"
        assertEquals(expectedCommand, Preprocessor.processCommandWithEcho(inputCommand))
    }

    @Test
    fun `extra whitespaces shouldn't influence to echo`() {
        val inputCommand = "echo foo bar ; echo b; ls"
        val expectedCommand = "echo | set /p dummyName=\"foo bar\"  ; echo | set /p dummyName=\"b\"  ; ls"
        assertEquals(expectedCommand, Preprocessor.processCommandWithEcho(inputCommand))
    }
}
