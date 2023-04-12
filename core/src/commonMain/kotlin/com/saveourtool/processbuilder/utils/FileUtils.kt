/**
 * File containing utils for files
 */

@file:JvmName("FileUtilsJVM")

package com.saveourtool.processbuilder.utils

import okio.FileSystem
import okio.Path
import kotlin.jvm.JvmName

expect val fs: FileSystem

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
