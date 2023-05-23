/**
 * File containing utils for Command class of kommand lib
 */

package com.saveourtool.processbuilder.utils

import com.saveourtool.processbuilder.ProcessBuilderConfig

import com.kgit2.io.Reader
import com.kgit2.process.Child
import com.kgit2.process.Command
import com.kgit2.process.Stdio
import okio.Path

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield

/**
 * @param redirect [ProcessBuilderConfig.Redirect] to set stdout to
 */
fun Command.stdout(redirect: ProcessBuilderConfig.Redirect): Command = stdout(redirect.toStdio())

/**
 * @param redirect [ProcessBuilderConfig.Redirect] to set stderr to
 */
fun Command.stderr(redirect: ProcessBuilderConfig.Redirect): Command = stderr(redirect.toStdio())

/**
 * @param workingDirectory working dir to set
 */
fun Command.cwd(workingDirectory: Path?): Command = workingDirectory?.let {
    cwd(it.toString())
} ?: this

/**
 * @return stdout as [Channel] of [String]s
 */
suspend fun Child.readStdout() = getReceiveChannelFor(getChildStdout())

/**
 * @return stderr as [Channel] of [String]s
 */
suspend fun Child.readStderr() = getReceiveChannelFor(getChildStderr())

private fun ProcessBuilderConfig.Redirect.toStdio() = when (this) {
    is ProcessBuilderConfig.Redirect.Inherit -> Stdio.Inherit
    is ProcessBuilderConfig.Redirect.Pipe -> Stdio.Pipe
    is ProcessBuilderConfig.Redirect.File -> Stdio.Pipe
    is ProcessBuilderConfig.Redirect.Null -> Stdio.Pipe
}

@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun getReceiveChannelFor(reader: Reader?) = coroutineScope {
    produce(context = Dispatchers.IO, capacity = Channel.UNLIMITED) {
        reader ?: return@produce
        reader.currentReadLine = true
        while (!reader.endOfInput) {
            reader.readLine()?.let { send(it) }
            yield()
        }
        reader.currentReadLine = false
    }
}
