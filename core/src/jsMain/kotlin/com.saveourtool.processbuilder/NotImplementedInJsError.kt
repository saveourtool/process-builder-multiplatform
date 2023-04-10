/**
 * File containing wrapper for NotImplementedError
 */

package com.saveourtool.processbuilder

import com.saveourtool.processbuilder.utils.NOT_IMPLEMENTED_IN_JS

/**
 * @return nothing
 * @throws NotImplementedError with [NOT_IMPLEMENTED_IN_JS] message
 */
@Suppress("FunctionName", "FUNCTION_NAME_INCORRECT_CASE")
fun NotImplementedInJsError(): Nothing = throw NotImplementedError(NOT_IMPLEMENTED_IN_JS)
