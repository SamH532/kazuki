package com.anaplan.engineering.kazuki.core.internal

import com.anaplan.engineering.kazuki.core.Invariant
import org.slf4j.LoggerFactory


data class _InvariantClause(
    val moduleName: String,
    val clauseName: String,
    private val clauseFn: () -> Boolean
) {
    val holds: Boolean by lazy {
        val result = clauseFn()
        if (!result) {
            Log.debug("Invariant clause {}.{} does not hold", moduleName, clauseName)
        }
        result
    }
}

private val Log = LoggerFactory.getLogger(Invariant::class.java)
