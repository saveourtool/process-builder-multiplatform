package com.saveourtool.processbuilder.exceptions

import kotlin.time.Duration

/**
 * @property timeoutDuration
 */
class ProcessTimeoutException(val timeoutDuration: Duration, message: String) : ProcessExecutionException(message)
