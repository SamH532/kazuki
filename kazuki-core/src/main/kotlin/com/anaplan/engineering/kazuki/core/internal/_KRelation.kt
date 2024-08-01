package com.anaplan.engineering.kazuki.core.internal

import com.anaplan.engineering.kazuki.core.PreconditionFailure
import com.anaplan.engineering.kazuki.core.Relation
import com.anaplan.engineering.kazuki.core.Tuple2
import kotlin.reflect.KClass

interface _KRelation<D, R, T : Relation<D, R>> : Relation<D, R> {
    fun construct(baseSet: Set<Tuple2<D, R>>): T

    val baseSet: Set<Tuple2<D, R>>

    val comparableWith: KClass<*>
}

internal fun <D, R, T : Relation<D, R>> T.transformRelation(fn: (_KRelation<D, R, T>) -> Collection<Tuple2<D, R>>): T {
    val kRelation = this as? _KRelation<D, R, T> ?: throw PreconditionFailure("Relation was implemented outside Kazuki")
    return kRelation.construct(fn(kRelation).toSet())
}

internal class __KRelation<D, R>(override val baseSet: Set<Tuple2<D, R>>) : _KRelation<D, R, __KRelation<D, R>>,
    Set<Tuple2<D, R>> by baseSet {

    override fun construct(baseSet: Set<Tuple2<D, R>>) = __KRelation(baseSet)

    override val comparableWith = Set::class

    override val dom by lazy { com.anaplan.engineering.kazuki.core.set(baseSet) { it._1 } }

    override val rng by lazy { com.anaplan.engineering.kazuki.core.set(baseSet) { it._2 } }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Relation<*, *>) return false
        return baseSet == other
    }

    override fun hashCode(): Int {
        return baseSet.hashCode()
    }

    override fun toString() = "relation$baseSet"
}