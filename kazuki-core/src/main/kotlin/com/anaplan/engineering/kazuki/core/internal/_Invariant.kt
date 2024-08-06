package com.anaplan.engineering.kazuki.core.internal

data class _InvariantClause(
    val clauseName: String,
    val clauseFn: () -> Boolean
)

data class _InvariantClauseEvaluation(
    val clause: _InvariantClause,
    val holds: Boolean
)