package com.saveourtool.processbuilder.exceptions

/**
 * @property timeoutMillis
 */
class ProcessTimeoutException(val timeoutMillis: Long, message: String) : ProcessExecutionException(message)
