/**
 * File containing utils for files
 */

@file:JvmName("FileUtilsJVM")

package com.saveourtool.processbuilder.utils

import io.github.oshai.KotlinLogging
import okio.FileSystem
import okio.Path
import kotlin.jvm.JvmName

expect val fs: FileSystem

internal val fileUtilsLogger = KotlinLogging.logger { }

/**
 * Delete this directory and all other files and directories in it
 *
 * @param path a path to a directory
 */
expect fun FileSystem.myDeleteRecursively(path: Path)

/**
 * Create file in [this] [FileSystem], denoted by [Path] [path]
 *
 * @param path path to a new file
 * @return [path]
 */
fun FileSystem.createFile(path: Path): Path {
    sink(path).close()
    return path
}

/**
 * @param path a path to a file
 * @return list of strings from the file
 */
fun FileSystem.readLines(path: Path): List<String> = this.read(path) {
    generateSequence { readUtf8Line() }.toList()
}
