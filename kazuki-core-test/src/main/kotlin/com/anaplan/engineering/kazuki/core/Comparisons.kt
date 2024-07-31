package com.anaplan.engineering.kazuki.core


@Module
interface CaselessString : Sequence<Char> {

    // TODO -- implement comparable operators if object is comparable
    @Comparable
    val uppercase get() = seq(this) { it.uppercase() }

}

@Module
interface NonEmptyCaselessString : CaselessString {

    @Invariant
    fun notEmpty() = isNotEmpty()
}

@Module
interface Id1 : NonEmptyCaselessString {

    @Invariant
    fun firstCharNotUnderscore() = first() != '_'
}

@Module
interface Id2 : NonEmptyCaselessString {

    @Invariant
    fun firstCharNotDollar() = first() != '$'
}

@Module
interface Id3 : NonEmptyCaselessString {

    @Invariant
    fun firstCharNotDollar() = first() != '$'

    // Id3 is incomparable with Id1 or Id2 as it defines a new relation
    @Comparable
    val uppercaseIgnoreSpace get() = seq(this, filter = { !it.isWhitespace() }) { it.uppercase() }
}

@Module
interface CaselessStringRecord {
    val a: CaselessString
}

// TODO - implement for sets + add meaningful example?
// TODO - implement for mapping + add meaningful example?

@Module
interface Time {
    val h: Int
    val m: Int
    val s: Int
    val z: Zone

    @Invariant
    fun validHour() = h in 0..23

    @Invariant
    fun validMinute() = m in 0..59

    @Invariant
    fun validSecond() = s in 0..59

    @Comparable
    val asUtc get() = mk_(h - z.offset, m, s)

    enum class Zone(val offset: Int) {
        GMT(0),
        CET(1),
        EST(-5)
    }
}

@Module
interface Flight {
    val departsAt: Time
}





