/**
 * File with platform utils
 */

@file:JvmName("PlatformUtilsJVM")

package com.saveourtool.processbuilder.utils

actual fun getCurrentOs(): CurrentOs = when {
    System.getProperty("os.name").startsWith("Linux", ignoreCase = true) -> CurrentOs.LINUX
    System.getProperty("os.name").startsWith("Mac", ignoreCase = true) -> CurrentOs.MACOS
    System.getProperty("os.name").startsWith("Windows", ignoreCase = true) -> CurrentOs.WINDOWS
    else -> CurrentOs.UNDEFINED
}
