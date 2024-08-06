package com.anaplan.engineering.kazuki.core

import com.anaplan.engineering.kazuki.core.internal.*

interface Mapping<D, R> : Relation<D, R> {

    operator fun get(d: D): R

}

interface InjectiveMapping<D, R> : Mapping<D, R> {
    val inverse: Mapping<R, D>

    @Invariant
    fun noDuplicatesInRange() = dom.card == rng.card
}

interface InjectiveMapping1<D, R> : InjectiveMapping<D, R>, Mapping1<D, R>

interface Mapping1<D, R> : Mapping<D, R> {
    val card: nat1

    override val dom: Set1<D>

    override val rng: Set1<R>

    @Invariant
    fun atLeastOneElement() = card > 0
}

fun <D, R> as_Mapping(maplets: Iterable<Tuple2<D, R>>): Mapping<D, R> =
    __KMapping(LinkedHashMap<D, R>().apply {
        maplets.forEach { put(it._1, it._2) }
    })

fun <D, R> as_Mapping(maplets: Array<Tuple2<D, R>>): Mapping<D, R> =
    __KMapping(LinkedHashMap<D, R>().apply {
        maplets.forEach { put(it._1, it._2) }
    })

fun <D, R> mk_Mapping(vararg maplets: Tuple2<D, R>): Mapping<D, R> =
    __KMapping(LinkedHashMap<D, R>().apply {
        maplets.forEach { put(it._1, it._2) }
    })

fun <D, R> as_InjectiveMapping(maplets: Iterable<Tuple2<D, R>>): InjectiveMapping<D, R> =
    __KInjectiveMapping(LinkedHashMap<D, R>().apply {
        maplets.forEach { put(it._1, it._2) }
    })

fun <D, R> as_InjectiveMapping1(maplets: Iterable<Tuple2<D, R>>): InjectiveMapping1<D, R> =
    __KInjectiveMapping1(LinkedHashMap<D, R>().apply {
        maplets.forEach { put(it._1, it._2) }
    })

fun <D, R> as_InjectiveMapping(maplets: Array<Tuple2<D, R>>): InjectiveMapping<D, R> =
    __KInjectiveMapping(LinkedHashMap<D, R>().apply {
        maplets.forEach { put(it._1, it._2) }
    })

fun <D, R> as_InjectiveMapping1(maplets: Array<Tuple2<D, R>>): InjectiveMapping1<D, R> =
    __KInjectiveMapping1(LinkedHashMap<D, R>().apply {
        maplets.forEach { put(it._1, it._2) }
    })

fun <D, R> mk_InjectiveMapping(vararg maplets: Tuple2<D, R>): InjectiveMapping<D, R> =
    __KInjectiveMapping(LinkedHashMap<D, R>().apply {
        maplets.forEach { put(it._1, it._2) }
    })

fun <D, R> mk_InjectiveMapping1(vararg maplets: Tuple2<D, R>): InjectiveMapping1<D, R> =
    __KInjectiveMapping1(LinkedHashMap<D, R>().apply {
        maplets.forEach { put(it._1, it._2) }
    })

fun <D, R> is_Mapping(maplets: Iterable<Tuple2<D, R>>) = mk_Relation(maplets).let { it.dom.card == it.card }

fun <D, R> is_Mapping(vararg maplets: Tuple2<D, R>) = mk_Relation(*maplets).let { it.dom.card == it.card }

fun <D, R> as_Mapping1(maplets: Iterable<Tuple2<D, R>>): Mapping1<D, R> =
    __KMapping1(LinkedHashMap<D, R>().apply {
        maplets.forEach { put(it._1, it._2) }
    })

fun <D, R> mk_Mapping1(vararg maplets: Tuple2<D, R>): Mapping1<D, R> =
    __KMapping1(LinkedHashMap<D, R>().apply {
        maplets.forEach { put(it._1, it._2) }
    })

// TODO is there any benefit to defining munion here?
// - helps to see that relation plus is not applicable due to typing issue (otherwise it just
// uses set.plus and you can't see why)
infix fun <D, R, M : Mapping<D, R>> M.munion(r: Relation<D, R>) = this + r

//// TODO is there any benefit to defining plus here?
//infix operator fun <D, R, M : Mapping<D, R>> M.plus(t: Tuple2<D, R>) = transformMapping {
//    if (t._1 in dom && get(t._1) != t._2) throw PreconditionFailure("${t._1} is already present with different mapping")
//    LinkedHashMap<D, R>().apply {
//        put(t._1, t._2)
//        it.forEach { put(it._1, it._2) }
//    }
//}
//
//// TODO is there any benefit to defining plus here?
//infix operator fun <D, R, M : Mapping<D, R>> M.plus(r: Relation<D, R>) = transformMapping {
//    val conflictingIntersection = r.filter { t ->
//        t._1 in dom && get(t._1) != t._2
//    }
//    if (conflictingIntersection.isNotEmpty()) {
//        throw PreconditionFailure("$conflictingIntersection are already present with different mapping")
//    }
//    LinkedHashMap<D, R>().apply {
//        r.forEach {
//            put(it._1, it._2)
//        }
//        it.forEach {
//            put(it._1, it._2)
//        }
//    }
//}

infix operator fun <D, R, M : Mapping<D, R>> M.times(t: Tuple2<D, R>) = transformMapping {
    LinkedHashMap<D, R>().apply {
        put(t._1, t._2)
        it.forEach {
            if (!containsKey(it._1)) {
                put(it._1, it._2)
            }
        }
    }
}

infix operator fun <D, R, M : Mapping<D, R>> M.times(r: Relation<D, R>) = transformMapping {
    if (!is_Mapping(r)) {
        throw PreconditionFailure("Argument to mapping override must be mapping")
    }
    LinkedHashMap<D, R>().apply {
        r.forEach {
            put(it._1, it._2)
        }
        it.forEach {
            if (!containsKey(it._1)) {
                put(it._1, it._2)
            }
        }
    }
}


// TODO - generate for consistency

