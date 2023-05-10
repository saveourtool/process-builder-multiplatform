/**
 * File containing utils for Command class of kommand lib
 */

package com.saveourtool.processbuilder.utils

import com.saveourtool.processbuilder.ProcessBuilderConfig

import com.kgit2.process.Command
import com.kgit2.process.Stdio
import okio.Path

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

private fun ProcessBuilderConfig.Redirect.toStdio() = when (this) {
    is ProcessBuilderConfig.Redirect.Inherit -> Stdio.Inherit
    is ProcessBuilderConfig.Redirect.Pipe -> Stdio.Pipe
    is ProcessBuilderConfig.Redirect.File -> Stdio.Pipe
    is ProcessBuilderConfig.Redirect.Null -> Stdio.Pipe
    else -> throw IllegalStateException("Redirect should be Null, Inherit, Pipe or File!")
}
