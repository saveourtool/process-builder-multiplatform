/**
 * File containing utils for files
 */

package com.saveourtool.processbuilder.utils

import okio.FileSystem
import okio.Path
import platform.posix.FTW_DEPTH
import platform.posix.nftw
import platform.posix.remove

import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString

actual val fs: FileSystem = FileSystem.SYSTEM

@Suppress("MAGIC_NUMBER", "MagicNumber")
actual fun FileSystem.myDeleteRecursively(path: Path) {
    nftw(path.toString(), staticCFunction { pathName, _, _, _ ->
        val fileName = pathName!!.toKString()
        fileUtilsLogger.trace { "Attempt to delete file $fileName" }
        remove(fileName)
    }, 64, FTW_DEPTH)
}
