/**
 * File with platform utils
 */

package com.saveourtool.processbuilder.utils

actual fun getCurrentOs() = when (Platform.osFamily) {
    OsFamily.LINUX -> CurrentOs.LINUX
    OsFamily.MACOSX -> CurrentOs.MACOS
    else -> CurrentOs.UNDEFINED
}
