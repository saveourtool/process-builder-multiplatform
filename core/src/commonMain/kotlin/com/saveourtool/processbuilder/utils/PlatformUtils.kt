/**
 * File with platform utils
 */

@file:Suppress("FILE_NAME_MATCH_CLASS", "MatchingDeclarationName")

package com.saveourtool.processbuilder.utils

/**
 * Supported platforms
 */
enum class CurrentOs {
    LINUX,
    MACOS,
    UNDEFINED,
    WINDOWS,
    ;
}

/**
 * Get type of current OS
 *
 * @return type of current OS
 */
expect fun getCurrentOs(): CurrentOs

/**
 * Checks if the current OS is windows.
 *
 * @return true if current OS is Windows
 */
fun isCurrentOsWindows(): Boolean = getCurrentOs() == CurrentOs.WINDOWS
