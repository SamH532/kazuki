package com.anaplan.engineering.kazuki.toolkit

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.ISO8601_Module.mk_DTG
import com.anaplan.engineering.kazuki.toolkit.ISO8601_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.ISO8601_Module.mk_Duration
import com.anaplan.engineering.kazuki.toolkit.ISO8601_Module.mk_Interval
import com.anaplan.engineering.kazuki.toolkit.ISO8601_Module.mk_Time
import kotlin.math.abs

@Module
object ISO8601 {
    @PrimitiveInvariant(name = "Year", base = nat::class)
    fun yearInvariant(y: nat) = y in FIRST_YEAR..LAST_YEAR

    @PrimitiveInvariant(name = "Month", base = nat1::class)
    fun monthInvariant(m: nat1) = m <= MONTHS_PER_YEAR

    @PrimitiveInvariant(name = "Day", base = nat1::class)
    fun dayInvariant(d: nat1) = d <= MAX_DAYS_PER_MONTH

    @PrimitiveInvariant(name = "Hour", base = nat::class)
    fun hourInvariant(h: nat) = h < HOURS_PER_DAY

    @PrimitiveInvariant(name = "Minute", base = nat::class)
    fun minuteInvariant(m: nat) = m <= MINUTES_PER_HOUR

    @PrimitiveInvariant(name = "Second", base = nat::class)
    fun secondInvariant(s: nat) = s in 0..SECONDS_PER_MINUTE

    @PrimitiveInvariant(name = "Millisecond", base = nat::class)
    fun millisecondInvariant(m: nat) = m in 0..MILLIS_PER_SECOND

    interface Date {
        val year: Year
        val month: Month
        val day: Day

        @Invariant
        fun isDayValid() = day <= daysInMonth(year, month)

        @ComparableProperty
        val dur: nat get() = durFromDate(this).dur
    }

    interface Time {
        val hour: Hour
        val minute: Minute
        val second: Second
        val millisecond: Millisecond

        // TODO Fix ordering here
        @ComparableProperty
        val dur: nat get() = durFromTime(this).dur
    }

    interface TimeInZone {
        val time: Time
        val offset: Offset
// TODO Add eq and ordering
    }

    interface Offset {
        val delta: Duration
        val pm: PlusOrMinus

// TODO fix this invariant
//    @Invariant
//  fun os() = delta < ONE_DAY && durModMinutes(delta) == NO_DURATION

// TODO Add in ordering
    }

    enum class PlusOrMinus {
        Plus,
        Minus
    }

    interface DTG {
        val date: Date
        val time: Time

        // TODO Fix ordering here
        @ComparableProperty
        val dur: nat get() = durFromDTG(this).dur
    }

    interface DTGInZone {
        val date: Date
        val time: TimeInZone

// TODO Add in invariant, ordering and equality
    }

    interface Interval {
        val begins: DTG
        val ends: DTG

        // TODO Fix invariant here
        @Invariant
        fun inval() = begins.dur < ends.dur
    }

    interface Duration {
        val dur: nat
    }

    private const val MILLIS_PER_SECOND: nat = 1000
    private const val SECONDS_PER_MINUTE: nat = 60
    private const val MINUTES_PER_HOUR: nat = 60
    private const val HOURS_PER_DAY: nat = 24

    private val DAYS_PER_MONTH: Mapping<Month, Day> = mk_Mapping(
        mk_(1, 31), mk_(2, 28), mk_(3, 31),
        mk_(4, 30), mk_(5, 31), mk_(6, 30),
        mk_(7, 31), mk_(8, 31), mk_(9, 30),
        mk_(10, 31), mk_(11, 30), mk_(12, 31)
    )

    private val DAYS_PER_MONTH_LEAP: Mapping<Month, Day> = DAYS_PER_MONTH * mk_(2, 29)

    private val MONTHS_PER_YEAR: nat1 = DAYS_PER_MONTH.dom.card

    private val MAX_DAYS_PER_MONTH: nat1 = (set(1..MONTHS_PER_YEAR) { DAYS_PER_MONTH[it] }).max()

    val daysInYear: (Year) -> nat1 = function(
        command = { year: Year ->
            seq(1..MONTHS_PER_YEAR) { daysInMonth(year, it) }.sum()
        }
    )

    private const val FIRST_YEAR: nat = 0

    private const val LAST_YEAR: nat = 9999

    private val DAYS_PER_YEAR: nat1 by lazy { daysInYear(1) }
    private val DAYS_PER_LEAP_YEAR: nat1 by lazy { daysInYear(4) }

    val FIRST_DATE: Date by lazy { mk_Date(FIRST_YEAR, 1, 1) }

    private val LAST_DATE: Date by lazy { mk_Date(LAST_YEAR, 12, 31) }

    val FIRST_TIME: Time = mk_Time(0, 0, 0, 0)

    private val LAST_TIME: Time by lazy { mk_Time(23, 59, 59, 999) }

    private val FIRST_DTG: DTG by lazy { mk_DTG(FIRST_DATE, FIRST_TIME) }

    private val LAST_DTG: DTG by lazy { mk_DTG(LAST_DATE, LAST_TIME) }

// FUNCTIONS

    val isLeap: (Year) -> bool = function(
        command = { y: Year ->
            y % 4 == 0 && ((y % 100 == 0) implies { y % 400 != 0 })
        }
    )

    val daysInMonth: (Year, Month) -> nat1 = function(
        command = { y: Year, m: Month ->
            if (isLeap(y)) {
                DAYS_PER_MONTH_LEAP[m]
            } else {
                DAYS_PER_MONTH[m]
            }
        }
    )

    val add: (DTG, Duration) -> DTG = function(
        command = { dtg, dur ->
            durToDTG(durAdd(durFromDTG(dtg), dur))
        },
//        post = { dtg, dur, result -> subtract(result, dur) == dtg }
    )

    val subtract: (DTG, Duration) -> DTG = function(
        command = { dtg, dur ->
            durToDTG(durDiff(durFromDTG(dtg), dur))
        },
        pre = { dtg, dur -> dur.dur <= durFromDTG(dtg).dur },
//        post = { dtg, dur, result -> add(result, dur) == dtg }
    )

    val diff: (DTG, DTG) -> Duration = function(
        command = { dtg1: DTG, dtg2: DTG ->
            durDiff(durFromDTG(dtg1), durFromDTG(dtg2))
        }
    )

    // TODO Ask why some tests are commented out of the VDM?
    val durAdd: (Duration, Duration) -> Duration = function(
        command = { dur1: Duration, dur2: Duration ->
            mk_Duration(dur1.dur + dur2.dur)
        },
//        post = { dur1, dur2, result ->
//            durDiff(result, dur1) == dur2 && durDiff(result, dur2) == dur1 &&
//                    durSubtract(result, dur1) == dur2 && durSubtract(result, dur2) == dur1
//        }
    )

    val durSubtract: (Duration, Duration) -> Duration = function(
        command = { dur1: Duration, dur2: Duration ->
            mk_Duration(dur1.dur - dur2.dur)
        },
        pre = { dur1, dur2 -> dur1.dur >= dur2.dur },
//        post = { dur1, dur2, result -> durAdd(result, dur2) == dur1 }
    )

    val durMultiply: (Duration, nat) -> Duration = function(
        command = { dur: Duration, n: nat ->
            mk_Duration(dur.dur * n)
        },
//        post = { dur: Duration, n: nat, result -> durDivide(result, n) == dur }
    )

    val durDivide: (Duration, nat) -> Duration = function(
        command = { dur: Duration, n: nat ->
            mk_Duration(dur.dur / n)
        },
//        post = { dur, n, result ->
//            durMultiply(result, n).dur <= dur.dur && dur.dur < durMultiply(result, n + 1).dur
//        }
    )

    val durDiff: (Duration, Duration) -> Duration = function(
        command = { dur1: Duration, dur2: Duration ->
            mk_Duration(abs(dur1.dur - dur2.dur))
        },
//        post = { dur1, dur2, result ->
//            ((dur1.dur <= dur2.dur) implies (durAdd(dur1, result) == dur2)) &&
//                    ((dur2.dur <= dur1.dur) implies (durAdd(dur2, result) == dur1))
//        }
    )


    val dtgInRange: (DTG, DTG, DTG) -> bool = function(
        command = { dtg1: DTG, dtg2: DTG, dtg3: DTG ->
            dtg1.dur <= dtg2.dur && dtg2.dur < dtg3.dur
        }
    )

    val dtgWithin: (DTG, Duration, DTG) -> bool = function(
        command = { dtg: DTG, dur: Duration, target: DTG ->
            inInterval(dtg, mk_Interval(subtract(target, dur), add(target, dur)))
        }
    )

    val inInterval: (DTG, Interval) -> bool = function(
        command = { dtg: DTG, ival: Interval ->
            dtgInRange(ival.begins, dtg, ival.ends)
        }
    )

    val durToMillis: (Duration) -> Millisecond = function(
        command = { d: Duration ->
            d.dur
        },
//        post = { d, result -> durFromMillis(result) == d }
    )
    val durFromMillis: (Millisecond) -> Duration = function(
        command = { ms: Millisecond ->
            mk_Duration(ms)
        },
//        post = { ms, result -> durToMillis(result) == ms }
    )

    val durToSeconds: (Duration) -> Second = function(
        command = { d: Duration ->
            durToMillis(d) / MILLIS_PER_SECOND
        },
//        post = { d, result -> durFromSeconds(result).dur <= d.dur && d.dur < durFromSeconds(result + 1).dur }
    )
    val durFromSeconds: (Second) -> Duration = function(
        command = { sc: Second ->
            durFromMillis(sc * MILLIS_PER_SECOND)
        },
//        post = { sc, result -> durToSeconds(result) == sc }
    )

    val durToMinutes: (Duration) -> Minute = function(
        command = { d: Duration ->
            durToSeconds(d) / SECONDS_PER_MINUTE
        },
//        post = { d, result -> durFromMinutes(result).dur <= d.dur && d.dur < durFromMinutes(result + 1).dur }
    )
    val durFromMinutes: (Minute) -> Duration = function(
        command = { mn: Minute ->
            durFromSeconds(mn * SECONDS_PER_MINUTE)
        },
//        post = { mn, result -> durToMinutes(result) == mn }
    )

    // TODO Add in Exists condition
    val durModMinutes: (Duration) -> Duration = function(
        command = { d: Duration ->
            mk_Duration(d.dur % ONE_MINUTE.dur)
        },
        post = { _, result -> result.dur < ONE_MINUTE.dur }
    )

    val durToHours: (Duration) -> Hour = function(
        command = { d: Duration ->
            durToMinutes(d) / MINUTES_PER_HOUR
        },
//        post = { d, result -> durFromHours(result).dur <= d.dur && d.dur < durFromHours(result + 1).dur }
    )
    val durFromHours: (Hour) -> Duration = function(
        command = { hr: Hour ->
            durFromMinutes(hr * MINUTES_PER_HOUR)
        },
//        post = { hr, result -> durToHours(result) == hr }
    )

    //TODO Add in Exists condition
    val durModHours: (Duration) -> Duration = function(
        command = { d: Duration ->
            mk_Duration(d.dur % ONE_HOUR.dur)
        },
        post = { _, result -> result.dur < ONE_HOUR.dur }
    )

    val durToDays: (Duration) -> Day = function(
        command = { d: Duration ->
            durToHours(d) / HOURS_PER_DAY
        },
//        post = { d, result -> durFromDays(result).dur <= d.dur && d.dur < durFromDays(result + 1).dur }
    )
    val durFromDays: (Day) -> Duration = function(
        command = { dy: Day ->
            durFromHours(dy * HOURS_PER_DAY)
        },
//        post = { dy, result -> durToDays(result) == dy }
    )

    // TODO Add in Exists condition
    val durModDays: (Duration) -> Duration = function(
        command = { d: Duration ->
            mk_Duration(d.dur % ONE_DAY.dur)
        },
        post = { _, result -> result.dur < ONE_DAY.dur }
    )

    val durToMonth: (Duration, Year) -> Month = function(
        command = { d: Duration, yr: Year ->
            ((set(1..MONTHS_PER_YEAR, filter = { durUpToMonth(yr, it).dur <= d.dur }) { it }).max()) - 1
        },
        pre = { d, yr -> d.dur < durFromYear(yr).dur }
    )
    val durFromMonth: (Year, Month) -> Duration = function(
        command = { yr: Year, mn: Month ->
            durFromDays(daysInMonth(yr, mn))
        }
    )
    val durUpToMonth: (Year, Month) -> Duration = function(
        command = { yr: Year, mn: Month ->
            sumDuration(seq(1..mn - 1) { durFromMonth(yr, mn) })
        }
    )

    val durToYear: (Duration, Year) -> Year by lazy {
        function(
            command = { d: Duration, yr: Year ->
                if (d.dur < durFromYear(yr).dur) {
                    0
                } else {
                    1 + durToYear(durDiff(d, durFromYear(yr)), yr + 1)
                }
            },
            // TODO Fix this post condition
//            post = { _, _, _ -> true },
            measure = { d, _ -> d.dur }
        )
    }

    val durFromYear: (Year) -> Duration = function(
        command = { yr: Year ->
            durFromDays(daysInYear(yr))
        }
    )
    val durUpToYear: (Year) -> Duration = function(
        command = { yr: Year ->
            sumDuration(seq(FIRST_YEAR..yr - 1) { durFromYear(it) })
        }
    )

    val durToDTG: (Duration) -> DTG = function(
        command = { d: Duration ->
            val dy = durFromDays(durToDays(d))
            mk_DTG(durToDate(dy), durToTime(durDiff(d, dy)))
        },
//        post = { d, result -> durFromDTG(result) == d }
    )
    val durFromDTG: (DTG) -> Duration = function(
        command = { dtg: DTG ->
            durAdd(durFromDate(dtg.date), durFromTime(dtg.time))
        },
//        post = { dtg, result -> durToDTG(result) == dtg }
    )

    val durToDate: (Duration) -> Date = function(
        command = { d: Duration ->
            val yr = durToYear(d, FIRST_YEAR)
            val ydur = durDiff(d, durUpToYear(yr))
            val mn = durToMonth(ydur, yr) + 1
            val dy = durToDays(durDiff(ydur, durUpToMonth(yr, mn))) + 1
            mk_Date(yr, mn, dy)
        },
//        post = { d, result -> durFromDate(result).dur <= d.dur && d.dur < durAdd(durFromDate(result), ONE_DAY).dur }
    )
    val durFromDate: (Date) -> Duration = function(
        command = { date: Date ->
            durAdd(durUpToYear(date.year), durAdd(durUpToMonth(date.year, date.month), durFromDays(date.day - 1)))
        },
//        post = { date, result -> durToDate(result) == date }
    )

    val durToTime: (Duration) -> Time = function(
        command = { d: Duration ->
            val hr = durToHours(d)
            val mn = durToMinutes(durDiff(d, durFromHours(hr)))
            val hmd = durAdd(durFromHours(hr), durFromMinutes(mn))
            val sc = durToSeconds(durDiff(d, hmd))
            val ml = durToMillis(durDiff(d, durAdd(hmd, durFromSeconds(sc))))
            mk_Time(hr, mn, sc, ml)
        },
//        post = { d, result -> durFromTime(result) == d }
    )
    val durFromTime: (Time) -> Duration = function(
        command = { time: Time ->
            durAdd(
                durFromHours(time.hour),
                durAdd(
                    durFromMinutes(time.minute),
                    durAdd(durFromSeconds(time.second), durFromMillis(time.millisecond))
                )
            )
        },
//        post = { time, result -> durToTime(result) == time }
    )

    val durFromInterval: (Interval) -> Duration = function(
        command = { i: Interval ->
            diff(i.begins, i.ends)
        },
        post = { i, result -> add(i.begins, result) == i.ends }
    )

    val sumDuration: (Sequence<Duration>) -> Duration = function(
        command = { sd: Sequence<Duration> ->
            mk_Duration((seq(sd) { it.dur }).sum())
        }
    )

    /*
        val normaliseTime: (TimeInZone) -> Time = function(
            command = { time: TimeInZone ->

            }
        )
    */
    private val NO_DURATION: Duration = durFromMillis(0)

    private val ONE_MILLISECOND: Duration = durFromMillis(1)

    private val ONE_SECOND: Duration = durFromSeconds(1)

    private val ONE_MINUTE: Duration = durFromMinutes(1)

    private val ONE_HOUR: Duration = durFromHours(1)

    private val ONE_DAY: Duration = durFromDays(1)

    private val ONE_YEAR: Duration = durFromDays(DAYS_PER_YEAR)

    private val ONE_LEAP_YEAR: Duration = durFromDays(DAYS_PER_LEAP_YEAR)

}