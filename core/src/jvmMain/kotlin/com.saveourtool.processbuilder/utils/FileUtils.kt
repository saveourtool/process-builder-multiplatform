/**
 * File containing utils for files
 */

@file:JvmName("FileUtilsJVM")

package com.saveourtool.processbuilder.utils

import okio.FileSystem

actual val fs: FileSystem = FileSystem.SYSTEM
