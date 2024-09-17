package com.anaplan.engineering.kazuki.core.internal

import com.anaplan.engineering.kazuki.core.PreconditionFailure
import com.anaplan.engineering.kazuki.core.Relation
import com.anaplan.engineering.kazuki.core.Tuple2
import kotlin.reflect.KClass

interface _KRelation<D, R, T : Relation<D, R>> : Relation<D, R> {
    fun construct(elements: Set<Tuple2<D, R>>): T

    val elements: Set<Tuple2<D, R>>

    val comparableWith: KClass<*>
}

//internal fun <D, R, T : Relation<D, R>> T.transformRelation(fn: (_KRelation<D, R, T>) -> Collection<Tuple2<D, R>>): T {
//    val kRelation = this as? _KRelation<D, R, T> ?: throw PreconditionFailure("Relation was implemented outside Kazuki")
//    val elements = fn(kRelation).toSet()
//    return kRelation.construct(elements)
//}

internal class __KRelation<D, R>(override val elements: Set<Tuple2<D, R>>) :
    _KRelation<D, R, __KRelation<D, R>>,
    _KSet<Tuple2<D, R>, Relation<D, R>>,
    Set<Tuple2<D, R>> by elements
{

    init {
        assert(elements !is _KazukiObject) {
            "Internal state should not be a Kazuki-generated object"
        }
    }

    override fun construct(elements: Set<Tuple2<D, R>>) = __KRelation(elements)

    override val comparableWith = Set::class

    override val dom by lazy { com.anaplan.engineering.kazuki.core.set(elements) { it._1 } }

    override val rng by lazy { com.anaplan.engineering.kazuki.core.set(elements) { it._2 } }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Relation<*, *>) return false
        return elements == other
    }

    override fun hashCode(): Int {
        return elements.hashCode()
    }

    override fun toString() = "relation$elements"
}