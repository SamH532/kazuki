package com.anaplan.engineering.kazuki.core

import kotlin.reflect.KClass


annotation class RecordType

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Module(
    /** If true, concrete implementation is generated with necessary constructors and utilities. If false, no concrete
     *  implementation is generated, but fields, invariants etc. are incorporated into descendant classes.
     */
    val makeable: Boolean = true
)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Invariant


/**
 * Annotates a derived property that should be used for all available comparisons (including equality)
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ComparableProperty(
    val useForOutput: Boolean = false
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ComparableTypeLimit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PrimitiveInvariant(
    val name: String,
    val base: KClass<*>
)

/**
 * Note that, the provider must:
 *  - have inferable generic types (only tested where same as the module thus far)
 *  - have constructor that takes a single param, which is an instance of the module
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
// TODO - better name!
annotation class FunctionProvider(
    val provider: KClass<*>
)

// TODO - create stdlib
typealias nat1 = Int

typealias nat = Int

typealias int = Int

typealias bool = Boolean

object InbuiltPrimitiveInvariant {

    val invariants = mapOf(
        nat1::class to ::isNat1Valid,
        nat::class to ::isNatValid,
    )

    fun isNat1Valid(value: nat1) = value > 0

    fun isNatValid(value: nat1) = value >= 0

}
