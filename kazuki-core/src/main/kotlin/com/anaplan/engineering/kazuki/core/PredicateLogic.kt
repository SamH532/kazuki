package com.anaplan.engineering.kazuki.core


fun <T> forall(i1: Iterable<T>, condition: (T) -> Boolean) =
    i1.all(condition)

fun <T1, T2> forall(i1: Iterable<T1>, i2: Collection<T2>, condition: (T1, T2) -> Boolean) =
    i1.product(i2).all { condition(it._1, it._2) }

fun <T> `∀`(i1: Iterable<T>, condition: (T) -> Boolean) = forall(i1, condition)

fun <T> iota(i1: Iterable<T>, condition: (T) -> Boolean) =
    i1.singleOrNull(condition) ?: throw IotaDoesNotSelectResult()

class IotaDoesNotSelectResult : SpecificationError()

fun <I1> exists(i1: Iterable<I1>, condition: (I1) -> Boolean) =
    i1.any(condition)

fun <I1> `∃`(i1: Iterable<I1>, condition: (I1) -> Boolean) = exists(i1, condition)

fun <I1> exists1(i1: Iterable<I1>, condition: (I1) -> Boolean) =
    i1.count(condition) == 1

fun <I1> `∃!`(i1: Iterable<I1>, condition: (I1) -> Boolean) = exists1(i1, condition)

infix fun Boolean.`∧`(other: Boolean) = this && other

infix fun Boolean.implies(other: Boolean) = if (this) other else true

/* Enables lazy evaluation of rhs */
infix fun Boolean.implies(other: () -> Boolean) = if (this) other() else true

infix fun Boolean.iff(other: Boolean) = if (this) other else !other



