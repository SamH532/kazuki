package com.anaplan.engineering.kazuki.core.internal

import com.anaplan.engineering.kazuki.core.InvariantFailure
import com.anaplan.engineering.kazuki.core.PreconditionFailure
import com.anaplan.engineering.kazuki.core.Set1
import kotlin.reflect.KClass

interface _KSet<T, S : Set<T>> : Set<T>, _KazukiObject {
    fun construct(elements: Set<T>): S

    val elements: Set<T>

    val comparableWith: KClass<*>
}

// TODO - generate impls
internal class __KSet<T>(override val elements: Set<T>) : Set<T> by elements, _KSet<T, Set<T>> {

    init {
        assert(elements !is _KazukiObject) {
            "Internal state should not be a Kazuki-generated object"
        }
    }

    override fun construct(elements: Set<T>) = __KSet(elements)

    override val comparableWith = Set::class

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Set<*>) return false
        return elements == other
    }

    override fun hashCode(): Int {
        return elements.hashCode()
    }

    override fun toString() = "set$elements"
}

internal class __KSet1<T>(override val elements: Set<T>) :
    _KSet<T, Set<T>>,
    Set1<T>,
    Set<T> by elements
{

    init {
        assert(elements !is _KazukiObject) {
            "Internal state should not be a Kazuki-generated object"
        }
        if (!isValid()) {
            throw InvariantFailure()
        }
    }

    override val comparableWith = Set::class

    override fun construct(elements: Set<T>) = __KSet1(elements)

    protected fun isValid(): Boolean = atLeastOneElement()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Set<*>) return false
        return elements == other
    }

    override fun hashCode(): Int {
        return elements.hashCode()
    }

    override fun toString() = "set1$elements"
}

internal fun <T, S : Set<T>> S.transformSet(fn: (_KSet<T, S>) -> Collection<T>): S {
    val kSet = this as? _KSet<T, S> ?: throw PreconditionFailure("Set ${this::class} was implemented outside Kazuki")
    val elements = fn(kSet).toSet()
    if (kSet is Set1<*> && elements.isEmpty()) {
        throw PreconditionFailure("Cannot create set1 without elements")
    }
    return kSet.construct(elements)
}