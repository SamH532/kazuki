package com.anaplan.engineering.kazuki.core


@Module
interface CaselessString : Sequence<Char> {

    // TODO -- implement comparable operators if object is comparable
    @ComparableProperty
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
    @ComparableProperty
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

    @ComparableProperty
    val asUtc get() = mk_((h - z.offset) % 24, m, s)

    enum class Zone(val offset: Int) {
        GMT(0),
        CET(1),
        EST(-5)
    }
}

@Module
interface WorkingTime : Time {

    @Invariant
    fun inWorkingHours() = h in 9..17
}

@Module
interface AfternoonTime : Time {

    @Invariant
    fun afternoon() = h >= 12

}

@Module
interface EveningTime : AfternoonTime {

    @Invariant
    fun evening() = h >= 18

}

@Module
interface NearestMinuteTime : Time {

    @ComparableProperty
    val nearestMinute get() = mk_(h - z.offset, m)

}

@Module
interface Flight {
    val departsAt: Time
}


@ComparableTypeLimit
@Module
interface IntRecord3 {
    val f: Int
    val b: String
}

@Module
interface IntRecord4: IntRecord3


@Module
interface IntRecord1 {
    val f: Int
    val b: String
}

@Module
interface IntRecord2 {
    val g: Int
    val c: String
}

@ComparableTypeLimit
@Module
interface IntRecord5: IntRecord4




