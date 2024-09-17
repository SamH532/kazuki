package com.anaplan.engineering.kazuki.core

import com.anaplan.engineering.kazuki.core.internal.__KSet
import com.anaplan.engineering.kazuki.core.internal.__KSet1
import com.anaplan.engineering.kazuki.core.internal.transformSet


interface Set1<T> : Set<T> {

    @Invariant
    fun atLeastOneElement() = card > 0

}


fun <T> mk_Set(vararg elems: T): Set<T> = __KSet(elems.toSet())

fun <T> as_Set(elems: Iterable<T>): Set<T> = __KSet(LinkedHashSet<T>(elems.count()).apply { addAll(elems) })

fun <T> as_Set(elems: Array<T>): Set<T> = __KSet(elems.toSet())

fun <T> mk_Set1(vararg elems: T): Set1<T> =
    if (elems.isEmpty()) {
        throw PreconditionFailure("Cannot create set1 without elements")
    } else {
        __KSet1(elems.toSet())
    }

fun <T> as_Set1(elems: Iterable<T>): Set1<T> =
    if (elems.count() == 0) {
        throw PreconditionFailure("Cannot convert to set1 without elements")
    } else {
        __KSet1(LinkedHashSet<T>(elems.count()).apply { addAll(elems) })
    }

fun <T> as_Set1(elems: Array<T>): Set1<T> =
    if (elems.isEmpty()) {
        throw PreconditionFailure("Cannot convert to set1 without elements")
    } else {
        __KSet1(elems.toSet())
    }


infix fun <T> Set<T>.subset(other: Set<T>) = other.containsAll(this)

infix fun <T, U> Iterable<T>.x(other: Iterable<U>) = as_Set(flatMap { t -> other.map { u -> mk_(t, u) } })

infix fun <T, S : Set<T>> S.inter(other: Set<T>) = transformSet { it.elements.filter { it in other } }

infix fun <T, S : Set<T>> S.union(other: Set<T>) = transformSet { it.elements.toMutableSet().apply { addAll(other) } }

val <T> Set<T>.card get() = size

fun <T> Set<T>.arbitrary() =
    if (isEmpty()) throw PreconditionFailure("Cannot get arbitrary member of emptyset") else first()

fun <T> Set<T>.single(): T {
    if (card != 1) {
        throw PreconditionFailure("Cannot get single item for set with cardinality $card")
    }
    return first()
}

fun <T> dunion(sets: Set<Set<T>>) = as_Set(sets.flatten())

fun <T> dunion(vararg sets: Set<T>) = dunion(sets.toSet())

infix operator fun <T, S : Set<T>> S.plus(s: Set<T>) = transformSet { it.elements.toMutableSet().apply { addAll(s) } }

infix operator fun <T, S : Set<T>> S.plus(t: T) = transformSet { it.elements.toMutableSet().apply { add(t) } }

infix operator fun <T, S : Set<T>> S.minus(s: Set<T>) =
    transformSet { it.elements.toMutableSet().apply { removeAll(s) } }

infix operator fun <T, S : Set<T>> S.minus(t: T) = transformSet { it.elements.toMutableSet().apply { remove(t) } }