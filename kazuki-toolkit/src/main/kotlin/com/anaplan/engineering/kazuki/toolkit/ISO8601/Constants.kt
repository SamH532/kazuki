package com.anaplan.engineering.kazuki.toolkit.ISO8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Time_Module.mk_Time

enum class PlusOrMinus {
    Plus,
    Minus,
    None
}

enum class DayOfWeek {
    Sunday,
    Monday,
    Tuesday,
    Wednesday,
    Thursday,
    Friday,
    Saturday
}

const val MILLIS_PER_SECOND: nat = 1000
const val SECONDS_PER_MINUTE: nat = 60
const val MINUTES_PER_HOUR: nat = 60
const val HOURS_PER_DAY: nat = 24

val DAYS_PER_MONTH: Mapping<Month, Day> = mk_Mapping(
    mk_(1, 31), mk_(2, 28), mk_(3, 31),
    mk_(4, 30), mk_(5, 31), mk_(6, 30),
    mk_(7, 31), mk_(8, 31), mk_(9, 30),
    mk_(10, 31), mk_(11, 30), mk_(12, 31)
)
val DAYS_PER_MONTH_LEAP: Mapping<Month, Day> = DAYS_PER_MONTH * mk_(2, 29)

val MONTHS_PER_YEAR: nat1 = DAYS_PER_MONTH.dom.card
val MAX_DAYS_PER_MONTH: nat1 = (set(1..MONTHS_PER_YEAR) { DAYS_PER_MONTH[it] }).max()

const val FirstYear: nat = 0
const val LastYear: nat = 9999

val FirstDate: Date = mk_Date(FirstYear, 1, 1)

val LastDate: Date = mk_Date(LastYear, 12, 31)

val FirstTime: Time = mk_Time(0, 0, 0, 0)

val NO_DURATION: Duration = Duration.fromMillis(0)

val ONE_MILLISECOND: Duration = Duration.fromMillis(1)

val ONE_MINUTE: Duration = Duration.fromMinutes(1)

val ONE_HOUR: Duration = Duration.fromHours(1)

val ONE_DAY: Duration = Duration.fromDays(1)
