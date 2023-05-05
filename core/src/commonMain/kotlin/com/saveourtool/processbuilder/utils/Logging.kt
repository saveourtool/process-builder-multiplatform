/**
 * Logging utils
 */

package com.saveourtool.processbuilder.utils

import com.saveourtool.processbuilder.exceptions.ProcessExecutionException
import io.github.oshai.KotlinLogging

val logger = KotlinLogging.logger("ProcessBuilder")

/**
 * Log error message and throw exception
 *
 * @param errMsg error message
 * @return [Nothing]
 * @throws ProcessExecutionException
 */
fun logErrorAndThrowProcessBuilderException(errMsg: String): Nothing {
    logger.error { errMsg }
    throw ProcessExecutionException(errMsg)
}
