package com.anaplan.engineering.kazuki.toolkit

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.ISO8601_Module.mk_DTG
import com.anaplan.engineering.kazuki.toolkit.ISO8601_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.ISO8601_Module.mk_Duration
import com.anaplan.engineering.kazuki.toolkit.ISO8601_Module.mk_Interval
import com.anaplan.engineering.kazuki.toolkit.ISO8601_Module.mk_Offset
import com.anaplan.engineering.kazuki.toolkit.ISO8601_Module.mk_Time
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Module
object ISO8601 {

    // TODO Make the functions methods on types:
    //  e.g. instead of durToMin(), use a function dur.toMin()
    //  Look at kspec for examples of this

    // TODO Use nat instead of longNat
    //  and "@Ignore" all tests that fail due to it being an int
    //  instead of a nat (i.e. overflows)

    // TODO Message Simon to confirm that he wants nanoseconds to be the smallest time period, instead of millisecond

    @PrimitiveInvariant(name = "Year", base = nat::class)
    fun yearNotInRange(y: nat) = y in FIRST_YEAR..LAST_YEAR

    @PrimitiveInvariant(name = "Month", base = nat1::class)
    fun monthNotInRange(m: nat1) = m <= MONTHS_PER_YEAR

    @PrimitiveInvariant(name = "Day", base = nat1::class)
    fun dayNotInRange(d: nat1) = d <= MAX_DAYS_PER_MONTH

    @PrimitiveInvariant(name = "Hour", base = nat::class)
    fun hourNotInRange(h: nat) = h < HOURS_PER_DAY

    @PrimitiveInvariant(name = "Minute", base = nat::class)
    fun minuteNotInRange(m: nat) = m <= MINUTES_PER_HOUR

    @PrimitiveInvariant(name = "Second", base = nat::class)
    fun secondNotInRange(s: nat) = s in 0..SECONDS_PER_MINUTE

    @PrimitiveInvariant(name = "Millisecond", base = nat::class)
    fun millisecondNotInRange(m: nat) = m in 0..MILLIS_PER_SECOND

    interface Date {
        val year: Year
        val month: Month
        val day: Day

        @Invariant
        fun isDayValid() = day <= daysInMonth(year, month)

        @ComparableProperty
        val dur: longNat get() = durFromDate(this).dur
    }

    interface Time {
        val hour: Hour
        val minute: Minute
        val second: Second
        val millisecond: Millisecond

        @ComparableProperty
        val dur: longNat get() = durFromTime(this).dur
    }

    interface TimeInZone {
        val time: Time
        val offset: Offset


        val nTime get() = normaliseTime(this)._1

        @ComparableProperty
        val dur get() = durFromTime(nTime).dur
    }

    interface Offset {
        val delta: Duration
        val pm: PlusOrMinus

        @Invariant
        fun offsetMoreThanDay() = delta.dur < ONE_DAY.dur

        @Invariant
        fun offsetZero() = durModMinutes(delta) == NO_DURATION

        @ComparableProperty
        val comp: longNat
            get() = when (pm) {
                PlusOrMinus.Plus -> delta.dur + 1
                PlusOrMinus.Minus -> delta.dur - 1
                PlusOrMinus.None -> delta.dur
            }
    }

    enum class PlusOrMinus {
        Plus,
        Minus,
        None
    }

    interface DTG {
        val date: Date
        val time: Time

        @ComparableProperty
        val dur: longNat get() = durFromDTG(this).dur
    }

    interface DTGInZone {
        val date: Date
        val time: TimeInZone


        @Invariant
        fun DTGTooEarly() = !(date == FIRST_DATE && normaliseTime(this.time)._2 == PlusOrMinus.Plus)

        @Invariant
        fun DTGTooLate() = !(date == LAST_DATE && normaliseTime(this.time)._2 == PlusOrMinus.Minus)

        @ComparableProperty
        val dur: longNat get() = durFromDTG(normalise(this)).dur

    }

    interface Interval {
        val begins: DTG
        val ends: DTG

        @Invariant
        fun zeroSizeInterval() = begins.dur != ends.dur

        @Invariant
        fun beginAfterEnd() = begins.dur <= ends.dur
    }

    interface Duration {
        val dur: longNat
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
            y % 4 == 0 && ((y % 100 == 0) implies { y % 400 == 0 })
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
        post = { dtg, dur, result -> subtract(result, dur) == dtg }
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

    val durAdd: (Duration, Duration) -> Duration = function(
        command = { dur1: Duration, dur2: Duration ->
            mk_Duration(dur1.dur + dur2.dur)
        },
        post = { dur1, dur2, result ->
//            durDiff(result, dur1) == dur2 && durDiff(result, dur2) == dur1 &&
            durSubtract(result, dur1) == dur2 && durSubtract(result, dur2) == dur1
        }
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
        post = { dur: Duration, n: nat, result -> durDivide(result, n) == dur }
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
        post = { dur1, dur2, result ->
            ((dur1.dur <= dur2.dur) implies (durAdd(dur1, result) == dur2)) &&
                    ((dur2.dur <= dur1.dur) implies (durAdd(dur2, result) == dur1))
        }
    )


    val dtgInRange: (DTG, DTG, DTG) -> bool = function(
        command = { dtg1: DTG, dtg2: DTG, dtg3: DTG ->
            dtg1.dur <= dtg2.dur && dtg2.dur < dtg3.dur
        }
    )

    val dtgWithin: (DTG, Duration, DTG) -> bool = function(
        command = { dtg: DTG, dur: Duration, target: DTG ->
            if (dur.dur.toInt() == 0) {
                dtg.dur == target.dur
            } else {
                inInterval(dtg, mk_Interval(subtract(target, dur), add(target, dur)))
            }
        },
        post = { dtg, dur, target, result -> (dur.dur == 0.toLong()) implies ((dtg == target) == result) }
    )

    val inInterval: (DTG, Interval) -> bool = function(
        command = { dtg: DTG, ival: Interval ->
            dtgInRange(ival.begins, dtg, ival.ends)
        }
    )

    val overlap: (Interval, Interval) -> bool = function(
        command = { i1: Interval, i2: Interval ->
            i2.begins.dur < i1.ends.dur && i1.begins.dur < i2.ends.dur
        },
        post = { i1, i2, result ->
            result == exists(max(i1.begins.dur, i2.begins.dur)..min(i1.ends.dur, i2.ends.dur)) {
                inInterval(durToDTG(mk_Duration(it)), i1) && inInterval(durToDTG(mk_Duration(it)), i2)
            }
        }
    )

    val within: (Interval, Interval) -> bool = function(
        command = { i1: Interval, i2: Interval ->
            i2.begins.dur <= i1.begins.dur && i1.ends.dur <= i2.ends.dur
        },
        post = { i1, i2, result ->
            result == (i2.begins.dur <= i1.begins.dur && i1.ends.dur <= i2.ends.dur)
        }
    )

    val durToMillis: (Duration) -> longNat = function(
        command = { d: Duration ->
            d.dur
        },
        post = { d, result -> durFromMillis(result) == d }
    )
    val durFromMillis: (longNat) -> Duration = function(
        command = { ms: longNat ->
            mk_Duration(ms)
        },
//        post = { ms, result -> durToMillis(result) == ms }
    )

    val durToSeconds: (Duration) -> longNat = function(
        command = { d: Duration ->
            durToMillis(d) / MILLIS_PER_SECOND
        },
        post = { d, result -> durFromSeconds(result).dur <= d.dur && d.dur < durFromSeconds(result + 1).dur }
    )
    val durFromSeconds: (longNat) -> Duration = function(
        command = { sc: longNat ->
            durFromMillis((sc * MILLIS_PER_SECOND))
        },
//        post = { sc, result -> durToSeconds(result) == sc }
    )

    val durToMinutes: (Duration) -> longNat = function(
        command = { d: Duration ->
            durToSeconds(d) / SECONDS_PER_MINUTE
        },
        post = { d, result -> durFromMinutes(result).dur <= d.dur && d.dur < durFromMinutes(result + 1).dur }
    )
    val durFromMinutes: (longNat) -> Duration = function(
        command = { mn: longNat ->
            durFromSeconds((mn * SECONDS_PER_MINUTE).toLong())
        },
//        post = { mn, result -> durToMinutes(result) == mn }
    )

    val durModMinutes: (Duration) -> Duration = function(
        command = { d: Duration ->
            mk_Duration(d.dur % ONE_MINUTE.dur)
        },
        post = { d, result ->
            result.dur < ONE_MINUTE.dur
//         && exists(0..durToMinutes(durFromDTG(mk_DTG(LAST_DATE, LAST_TIME)))) { durAdd(durFromMinutes(it),result) == d }
        }
    )

    val durToHours: (Duration) -> longNat = function(
        command = { d: Duration ->
            durToMinutes(d) / MINUTES_PER_HOUR
        },
        post = { d, result -> durFromHours(result).dur <= d.dur && d.dur < durFromHours(result + 1).dur }
    )
    val durFromHours: (longNat) -> Duration = function(
        command = { hr: longNat ->
            durFromMinutes(hr * MINUTES_PER_HOUR)
        },
//        post = { hr, result -> durToHours(result) == hr }
    )

    val durModHours: (Duration) -> Duration = function(
        command = { d: Duration ->
            mk_Duration(d.dur % ONE_HOUR.dur)
        },
        post = { d, result ->
            result.dur < ONE_HOUR.dur
//         && exists(0..durToHours(durFromDTG(mk_DTG(LAST_DATE, LAST_TIME)))) { durAdd(durFromHours(it),result) == d }
        }
    )

    val durToDays: (Duration) -> longNat = function(
        command = { d: Duration ->
            durToHours(d) / HOURS_PER_DAY
        },
        post = { d, result -> durFromDays(result).dur <= d.dur && d.dur < durFromDays(result + 1).dur }
    )
    val durFromDays: (longNat) -> Duration = function(
        command = { dy: longNat ->
            durFromHours(dy * HOURS_PER_DAY)
        },
//        post = { dy, result -> durToDays(result) == dy }
    )

    val durModDays: (Duration) -> Duration = function(
        command = { d: Duration ->
            mk_Duration(d.dur % ONE_DAY.dur)
        },
        post = { d, result ->
            result.dur < ONE_DAY.dur
//         && exists(0..durToDays(durFromDTG(mk_DTG(LAST_DATE, LAST_TIME)))) { durAdd(durFromDays(it),result) == d }
        }
    )

    val durToMonth: (Duration, Year) -> longNat = function(
        command = { d: Duration, yr: Year ->
            ((set(1..MONTHS_PER_YEAR,
                filter = { durUpToMonth(yr, it).dur <= d.dur }) { it }).max()).toLong() - 1.toLong()
        },
        pre = { d, yr -> d.dur < durFromYear(yr).dur }
    )
    val durFromMonth: (Year, Month) -> Duration = function(
        command = { yr: Year, mn: Month ->
            durFromDays(daysInMonth(yr, mn).toLong())
        }
    )
    val durUpToMonth: (Year, Month) -> Duration = function(
        command = { yr: Year, mn: Month ->
            sumDuration(seq(1..mn - 1) { durFromMonth(yr, it) })
        }
    )

    val durToYear: (Duration, Year) -> longNat by lazy {
        function(
            // TODO Use a general JAVA function here
            command = { d: Duration, yr: Year ->
                if (d.dur < durFromYear(yr).dur) {
                    0
                } else {
                    1 + durToYear(durDiff(d, durFromYear(yr)), yr + 1)
                }
            },
            // TODO Confirm why this was commented out in VDM
            // Test year above and below instead of like this
//            post = { d, yr, result ->
//                (set(FIRST_YEAR..LAST_YEAR,
//                    filter = { durUpToYear(yr + it).dur <= d.dur }) { it }).max().toLong() == result
//            },
            measure = { _, yr -> -yr }
        )
    }

    val durFromYear: (Year) -> Duration = function(
        command = { yr: Year ->
            durFromDays(daysInYear(yr).toLong())
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
        post = { d, result -> durFromDTG(result) == d }
    )
    val durFromDTG: (DTG) -> Duration = function(
        command = { dtg: DTG ->
            durAdd(durFromDate(dtg.date), durFromTime(dtg.time))
        },
//        post = { dtg, result -> durToDTG(result) == dtg }
    )

    val durToDate: (Duration) -> Date = function(
        command = { d: Duration ->
            val yr = durToYear(d, FIRST_YEAR).toInt()
            val ydur = durDiff(d, durUpToYear(yr))
            val mn = durToMonth(ydur, yr).toInt() + 1
            val dy = durToDays(durDiff(ydur, durUpToMonth(yr, mn))).toInt() + 1
            mk_Date(yr, mn, dy)
        },
        post = { d, result -> durFromDate(result).dur <= d.dur && d.dur < durAdd(durFromDate(result), ONE_DAY).dur }
    )
    val durFromDate: (Date) -> Duration = function(
        command = { date: Date ->
            durAdd(
                durUpToYear(date.year),
                durAdd(durUpToMonth(date.year, date.month), durFromDays(date.day - 1.toLong()))
            )
        },
//        post = { date, result -> durToDate(result) == date }
    )

    val durToTime: (Duration) -> Time = function(
        command = { d: Duration ->
            val hr = durToHours(d)
            val mn = durToMinutes(durDiff(d, durFromHours(hr)))
            val hmd = durAdd(durFromHours(hr), durFromMinutes(mn))
            val sc = durToSeconds(durDiff(d, hmd))
            val ml = durToMillis(durDiff(d, durAdd(hmd, durFromSeconds(sc.toLong()))))
            mk_Time(hr.toInt(), mn.toInt(), sc.toInt(), ml.toInt())
        },
        post = { d, result -> durFromTime(result) == d }
    )
    val durFromTime: (Time) -> Duration = function(
        command = { time: Time ->
            durAdd(
                durFromHours(time.hour.toLong()),
                durAdd(
                    durFromMinutes(time.minute.toLong()),
                    durAdd(durFromSeconds(time.second.toLong()), durFromMillis(time.millisecond.toLong()))
                )
            )
        },
//        post = { time, result -> durToTime(result) == time }
    )

    val durFromTimeInZone: (TimeInZone) -> Duration = function(
        command = { time: TimeInZone ->
            durFromTime(normaliseTime(time)._1)
        },
        post = { time, result -> durToTime(result).dur == time.dur }
    )

    val durFromInterval: (Interval) -> Duration = function(
        command = { i: Interval ->
            diff(i.begins, i.ends)
        },
        post = { i, result -> add(i.begins, result) == i.ends }
    )

    val finestGranularity: (DTG, Duration) -> bool = function(
        command = { dtg: DTG, d: Duration ->
            durFromDTG(dtg).dur % d.dur == 0.toLong()
        },
        pre = { _, d -> d.dur != NO_DURATION.dur }
    )

    val finestGranularityI: (Interval, Duration) -> bool = function(
        command = { i: Interval, d: Duration ->
            finestGranularity(i.begins, d) && finestGranularity(i.ends, d)
        }
    )

    val minDTG: (Set1<DTG>) -> DTG = function(
        command = { dtgs: Set1<DTG> ->
            durToDTG(mk_Duration((set(dtgs) { it.dur }).min()))
        },
        post = { dtgs, result ->
            result in dtgs && forall(dtgs) { result.dur <= it.dur }
        }
    )
    val maxDTG: (Set1<DTG>) -> DTG = function(
        command = { dtgs: Set1<DTG> ->
            durToDTG(mk_Duration((set(dtgs) { it.dur }).max()))
        },
        post = { dtgs, result ->
            result in dtgs && forall(dtgs) { result.dur >= it.dur }
        }
    )

    val minDate: (Set1<Date>) -> Date = function(
        command = { dates: Set1<Date> ->
            durToDate(mk_Duration((set(dates) { it.dur }).min()))
        },
        post = { dates, result ->
            result in dates && forall(dates) { result.dur <= it.dur }
        }
    )
    val maxDate: (Set1<Date>) -> Date = function(
        command = { dates: Set1<Date> ->
            durToDate(mk_Duration((set(dates) { it.dur }).max()))
        },
        post = { dates, result ->
            result in dates && forall(dates) { result.dur >= it.dur }
        }
    )

    val minTime: (Set1<Time>) -> Time = function(
        command = { times: Set1<Time> ->
            durToTime(mk_Duration((set(times) { it.dur }).min()))
        },
        post = { times, result ->
            result in times && forall(times) { result.dur <= it.dur }
        }
    )
    val maxTime: (Set1<Time>) -> Time = function(
        command = { times: Set1<Time> ->
            durToTime(mk_Duration((set(times) { it.dur }).max()))
        },
        post = { times, result ->
            result in times && forall(times) { result.dur >= it.dur }
        }
    )

    val minDuration: (Set1<Duration>) -> Duration = function(
        command = { durs: Set1<Duration> ->
            mk_Duration((set(durs) { it.dur }).min())
        },
        post = { durs, result ->
            result in durs && forall(durs) { result.dur <= it.dur }
        }
    )
    val maxDuration: (Set1<Duration>) -> Duration = function(
        command = { durs: Set1<Duration> ->
            mk_Duration((set(durs) { it.dur }).max())
        },
        post = { durs, result ->
            result in durs && forall(durs) { result.dur >= it.dur }
        }
    )

    val sumDuration: (Sequence<Duration>) -> Duration = function(
        command = { sd: Sequence<Duration> ->
            mk_Duration((seq(sd) { it.dur }).sum())
        }
    )

    val instant: (DTG) -> Interval = function(
        command = { dtg: DTG ->
            mk_Interval(dtg, add(dtg, ONE_MILLISECOND))
        },
        post = { dtg, result -> inInterval(dtg, result) }
    )

    val nextDateForYM: (Date) -> Date = function(
        command = { date: Date ->
            nextDateForDay(date, date.day)
        }
    )
    val nextDateForDay: (Date, Day) -> Date = function(
        command = { date: Date, day: Day ->
            nextYMDForDay(date.year, date.month, date.day, day)
        },
        pre = { _, day -> day <= MAX_DAYS_PER_MONTH }
    )
    val nextYMDForDay: (Year, Month, Day, Day) -> Date by lazy {
        function(
            command = { yy: Year, mm: Month, dd: Day, day: Day ->
                val nextM = if (mm == MONTHS_PER_YEAR) {
                    1
                } else {
                    mm + 1
                }
                val nextY = if (mm == MONTHS_PER_YEAR) {
                    yy + 1
                } else {
                    yy
                }
                if (dd < day && day <= daysInMonth(yy, mm)) {
                    mk_Date(yy, mm, day)
                } else if (day == 1) {
                    mk_Date(nextY, nextM, day)
                } else {
                    nextYMDForDay(nextY, nextM, 1, day)
                }
            },
            pre = { yy, mm, dd, _ -> dd <= daysInMonth(yy, mm) },
            measure = { yy, mm, _, _ -> ((LAST_YEAR + 1) * MONTHS_PER_YEAR) - (yy * MONTHS_PER_YEAR + mm) }
        )
    }

    val previousDateForYM: (Date) -> Date = function(
        command = { date: Date ->
            previousDateForDay(date, date.day)
        }
    )
    val previousDateForDay: (Date, Day) -> Date = function(
        command = { date: Date, day: Day ->
            previousYMDForDay(date.year, date.month, date.day, day)
        },
        pre = { _, day -> day <= MAX_DAYS_PER_MONTH }
    )
    val previousYMDForDay: (Year, Month, Day, Day) -> Date by lazy {
        function(
            command = { yy: Year, mm: Month, dd: Day, day: Day ->
                val prevM = if (mm > 1) {
                    mm - 1
                } else {
                    MONTHS_PER_YEAR
                }
                val prevY = if (mm > 1) {
                    yy
                } else {
                    yy - 1
                }
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

    val normalise: (DTGInZone) -> DTG = function(
        command = { dtgIZ: DTGInZone ->
            val ntime = normaliseTime(dtgIZ.time)
            val baseDTG = mk_DTG(dtgIZ.date, ntime._1)
            when (ntime._2) {
                PlusOrMinus.Plus -> subtract(baseDTG, ONE_DAY)
                PlusOrMinus.Minus -> add(baseDTG, ONE_DAY)
                PlusOrMinus.None -> baseDTG
            }
        }
    )

    val normaliseTime: (TimeInZone) -> Tuple2<Time, PlusOrMinus> = function(
        command = { time: TimeInZone ->
            val utcTimeDur = durFromTime(time.time)
            val offset = time.offset.delta
            when (time.offset) {
                mk_Offset(offset, PlusOrMinus.Plus) ->
                    if (offset.dur <= utcTimeDur.dur) {
                        mk_(durToTime(durSubtract(utcTimeDur, offset)), PlusOrMinus.None)
                    } else {
                        mk_(durToTime(durSubtract(durAdd(utcTimeDur, ONE_DAY), offset)), PlusOrMinus.Plus)
                    }

                mk_Offset(offset, PlusOrMinus.Minus) -> {
                    val adjusted = durAdd(utcTimeDur, offset)
                    if (adjusted.dur < ONE_DAY.dur) {
                        mk_(durToTime(adjusted), PlusOrMinus.None)
                    } else {
                        mk_(durToTime(durSubtract(adjusted, ONE_DAY)), PlusOrMinus.Minus)
                    }
                }

                else -> mk_(time.time, PlusOrMinus.None)
            }
        }
    )

    val formatDTG: (DTG) -> String = function(
        command = { dtg: DTG ->
            formatDate(dtg.date) + "T" + formatTime(dtg.time)
        }
    )

    val formatDTGInZone: (DTGInZone) -> String = function(
        command = { dtg: DTGInZone ->
            formatDate(dtg.date) + "T" + formatTimeInZone(dtg.time)
        }
    )

    val formatDate: (Date) -> String = function(
        command = { date: Date ->
            String.format("%04d-%02d-%02d", date.year, date.month, date.day)
        }
    )

    val formatTime: (Time) -> String = function(
        command = { time: Time ->
            val frac = if (time.millisecond == 0) {
                ""
            } else {
                String.format(".%03d", time.millisecond)
            }
            String.format("%02d:%02d:%02d%s", time.hour, time.minute, time.second, frac)
        }
    )

    val formatTimeInZone: (TimeInZone) -> String = function(
        command = { time: TimeInZone ->
            formatTime(time.time) + (
                    if (time.offset.delta.dur == NO_DURATION.dur) {
                        "Z"
                    } else {
                        formatOffset(time.offset)
                    }
                    )
        }
    )

    val formatOffset: (Offset) -> String = function(
        command = { offset: Offset ->
            val hm = durToTime(offset.delta)
            val sign = if (offset.pm == PlusOrMinus.Plus) {
                "+"
            } else if (offset.pm == PlusOrMinus.Minus) {
                "-"
            } else {
                ""
            }
            String.format("%s%02d:%02d", sign, hm.hour, hm.minute)
        }
    )

    val formatInterval: (Interval) -> String = function(
        command = { interval: Interval ->
            formatDTG(interval.begins) + "/" + formatDTG(interval.ends)
        }
    )

    val formatDuration: (Duration) -> String = function(
        command = { d: Duration ->
            val numDays = durToDays(d)
            val inputTime = durToTime(durModDays(d))
            val item: (nat, Char) -> String = function(
                command = { n: nat, c: Char ->
                    if (n == 0) {
                        ""
                    } else {
                        String.format("%d%s", n, c)
                    }
                }
            )
            val itemSec: (nat, nat) -> String = function(
                command = { x: nat, y: nat ->
                    String.format("%d.%03dS", x, y)
                }
            )

            val date = item(numDays.toInt(), 'D')
            val time = item(inputTime.hour, 'H') + item(inputTime.minute, 'M') + if (inputTime.millisecond == 0) {
                item(inputTime.second, 'S')
            } else {
                itemSec(inputTime.second, inputTime.millisecond)
            }
            if (date == "" && time == "") {
                "PT0S"
            } else {
                "P" + date + if (time == "") {
                    ""
                } else {
                    "T" + time
                }
            }
        }
    )

    private val NO_DURATION: Duration = durFromMillis(0)

    private val ONE_MILLISECOND: Duration = durFromMillis(1)

    private val ONE_SECOND: Duration = durFromSeconds(1)

    private val ONE_MINUTE: Duration = durFromMinutes(1)

    private val ONE_HOUR: Duration = durFromHours(1)

    private val ONE_DAY: Duration = durFromDays(1)

    private val ONE_YEAR: Duration = durFromDays(DAYS_PER_YEAR.toLong())

    private val ONE_LEAP_YEAR: Duration = durFromDays(DAYS_PER_LEAP_YEAR.toLong())

}