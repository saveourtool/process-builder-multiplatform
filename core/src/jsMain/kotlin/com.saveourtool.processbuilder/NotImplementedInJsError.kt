/**
 * File containing wrapper for NotImplementedError
 */

package com.saveourtool.processbuilder

import com.saveourtool.processbuilder.utils.NOT_IMPLEMENTED_IN_JS

object NotImplementedInJsError {
    /**
     * @return Nothing
     * @throws NotImplementedError with [NOT_IMPLEMENTED_IN_JS] error text
     */
    operator fun invoke(): Nothing = throw NotImplementedError(NOT_IMPLEMENTED_IN_JS)
}
