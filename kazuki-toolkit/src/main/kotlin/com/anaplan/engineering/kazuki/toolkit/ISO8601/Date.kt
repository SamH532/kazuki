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
    command = { year: Year -> year % 4 == 0 && ((year % 100 == 0) implies { year % 400 == 0 }) }
)

val daysInYear: (Year) -> nat1 = function(
    command = { year: Year -> seq(1..MONTHS_PER_YEAR) { daysInMonth(year, it) }.sum() }
)

val daysInMonth: (Year, Month) -> nat1 = function(
    command = { year: Year, month: Month -> if (isLeap(year)) DAYS_PER_MONTH_LEAP[month] else DAYS_PER_MONTH[month] }
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
        command = { dateYear: Year, dateMonth: Month, dateDay: Day, day: Day ->
            val nextMonth = if (dateMonth == MONTHS_PER_YEAR) 1 else dateMonth + 1
            val nextYear = if (dateMonth == MONTHS_PER_YEAR) dateYear + 1 else dateYear

            if (dateDay < day && day <= daysInMonth(dateYear, dateMonth)) {
                mk_Date(dateYear, dateMonth, day)
            } else if (day == 1) {
                mk_Date(nextYear, nextMonth, day)
            } else {
                nextYMDForDay(nextYear, nextMonth, 1, day)
            }
        },
        pre = { dateYear, dateMonth, dateDay, _ -> dateDay <= daysInMonth(dateYear, dateMonth) },
        measure = { dateYear, dateMonth, _, _ -> ((LastYear + 1) * MONTHS_PER_YEAR) - (dateYear * MONTHS_PER_YEAR + dateMonth) }
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
        command = { dateYear: Year, dateMonth: Month, dateDay: Day, day: Day ->
            val prevMonth = if (dateMonth > 1) dateMonth - 1 else MONTHS_PER_YEAR
            val prevYear = if (dateMonth > 1) dateYear else dateYear - 1

            if (day < dateDay) {
                mk_Date(dateYear, dateMonth, day)
            } else if (day <= daysInMonth(prevYear, prevMonth)) {
                mk_Date(prevYear, prevMonth, day)
            } else {
                previousYMDForDay(prevYear, prevMonth, 1, day)
            }
        },
        pre = { dateYear, dateMonth, dd, _ -> dd <= daysInMonth(dateYear, dateMonth) },
        measure = { dateYear, dateMonth, _, _ -> dateYear * MONTHS_PER_YEAR + dateMonth }
    )
}
