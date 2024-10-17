package com.anaplan.engineering.kazuki.toolkit.ISO8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Duration_Module.mk_Duration

@Module
interface Date {
    val year: Year
    val month: Month
    val day: Day

    @Invariant
    fun isDayValid() = day <= daysInMonth(year, month)

    @ComparableProperty
    val duration_ms: nat get() = functions.toDuration().duration_ms

    @FunctionProvider(DateFunctions::class)
    val functions: DateFunctions

    class DateFunctions(private val date: Date) {

        val toDuration: () -> Duration = function<Duration>(
            command = {
                Duration.durationUpToYear(date.year).functions.add(
                    Duration.durationUpToMonth(date.year, date.month).functions.add(
                        Duration.fromDays(date.day - 1)
                    )
                )
            },
//        post = { result -> result.functions.toDate() == date }
        )

        val format: () -> String = function<String>(
            command = { String.format("%04d-%02d-%02d", date.year, date.month, date.day) }
        )

        val toDayOfWeek: () -> DayOfWeek = function<DayOfWeek>(
            command = { DayOfWeek.entries[(date.functions.toDuration().functions.toDays() - 365) % 7] }
        )
    }
}

@PrimitiveInvariant(name = "Year", base = nat::class)
fun yearNotInRange(year: nat) = year in FirstYear..LastYear

@PrimitiveInvariant(name = "Month", base = nat1::class)
fun monthNotInRange(month: nat1) = month <= MONTHS_PER_YEAR

@PrimitiveInvariant(name = "Day", base = nat1::class)
fun dayNotInRange(day: nat1) = day <= MAX_DAYS_PER_MONTH

val isLeap: (Year) -> bool = function(
    command = { y: Year -> y % 4 == 0 && ((y % 100 == 0) implies { y % 400 == 0 }) }
)

val daysInYear: (Year) -> nat1 = function(
    command = { year: Year -> seq(1..MONTHS_PER_YEAR) { daysInMonth(year, it) }.sum() }
)

val daysInMonth: (Year, Month) -> nat1 = function(
    command = { y: Year, m: Month -> if (isLeap(y)) DAYS_PER_MONTH_LEAP[m] else DAYS_PER_MONTH[m] }
)

val minDate: (Set1<Date>) -> Date = function(
    command = { dates: Set1<Date> -> mk_Duration((set(dates) { it.duration_ms }).min()).functions.toDate() },
    post = { dates, result -> result in dates && forall(dates) { result.duration_ms <= it.duration_ms } }
)
val maxDate: (Set1<Date>) -> Date = function(
    command = { dates: Set1<Date> -> mk_Duration((set(dates) { it.duration_ms }).max()).functions.toDate() },
    post = { dates, result -> result in dates && forall(dates) { result.duration_ms >= it.duration_ms } }
)

val nextDateForYM: (Date) -> Date = function(
    command = { date: Date -> nextDateForDay(date, date.day) }
)
val nextDateForDay: (Date, Day) -> Date = function(
    command = { date: Date, day: Day -> nextYMDForDay(date.year, date.month, date.day, day) },
    pre = { _, day -> day <= MAX_DAYS_PER_MONTH }
)
val nextYMDForDay: (Year, Month, Day, Day) -> Date by lazy {
    function(
        command = { yy: Year, mm: Month, dd: Day, day: Day ->
            val nextM = if (mm == MONTHS_PER_YEAR) 1 else mm + 1
            val nextY = if (mm == MONTHS_PER_YEAR) yy + 1 else yy

            if (dd < day && day <= daysInMonth(yy, mm)) {
                mk_Date(yy, mm, day)
            } else if (day == 1) {
                mk_Date(nextY, nextM, day)
            } else {
                nextYMDForDay(nextY, nextM, 1, day)
            }
        },
        pre = { yy, mm, dd, _ -> dd <= daysInMonth(yy, mm) },
        measure = { yy, mm, _, _ -> ((LastYear + 1) * MONTHS_PER_YEAR) - (yy * MONTHS_PER_YEAR + mm) }
    )
}

val previousDateForYM: (Date) -> Date = function(
    command = { date: Date -> previousDateForDay(date, date.day) }
)
val previousDateForDay: (Date, Day) -> Date = function(
    command = { date: Date, day: Day -> previousYMDForDay(date.year, date.month, date.day, day) },
    pre = { _, day -> day <= MAX_DAYS_PER_MONTH }
)
val previousYMDForDay: (Year, Month, Day, Day) -> Date by lazy {
    function(
        command = { yy: Year, mm: Month, dd: Day, day: Day ->
            val prevM = if (mm > 1) mm - 1 else MONTHS_PER_YEAR
            val prevY = if (mm > 1) yy else yy - 1

            if (day < dd) {
                mk_Date(yy, mm, day)
            } else if (day <= daysInMonth(prevY, prevM)) {
                mk_Date(prevY, prevM, day)
            } else {
                previousYMDForDay(prevY, prevM, 1, day)
            }
        },
        pre = { yy, mm, dd, _ -> dd <= daysInMonth(yy, mm) },
        measure = { yy, mm, _, _ -> yy * MONTHS_PER_YEAR + mm }
    )
}
