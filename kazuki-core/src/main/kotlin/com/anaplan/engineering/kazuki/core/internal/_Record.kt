package com.anaplan.engineering.kazuki.core.internal

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class _Record(
    vararg val fields: String
)