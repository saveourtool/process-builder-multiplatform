/**
 * File containing utils for files
 */

package com.saveourtool.processbuilder.utils

import okio.FileSystem
import okio.Path
import java.nio.file.Files

actual val fs: FileSystem = FileSystem.SYSTEM

actual fun FileSystem.myDeleteRecursively(path: Path) {
    path.toFile().walkBottomUp().forEach { file ->
        fileUtilsLogger.trace { "Attempt to delete file $file" }
        Files.delete(file.toPath())
    }
}
