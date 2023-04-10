/**
 * File containing utils for files
 */

package com.saveourtool.processbuilder.utils

import com.saveourtool.processbuilder.NotImplementedInJsError
import okio.FileSystem
import okio.Path

actual val fs: FileSystem = throw NotImplementedInJsError()

/**
 * Delete this directory and all other files and directories in it
 *
 * @param path a path to a directory
 */
actual fun FileSystem.myDeleteRecursively(path: Path) {
    NotImplementedInJsError()
}
