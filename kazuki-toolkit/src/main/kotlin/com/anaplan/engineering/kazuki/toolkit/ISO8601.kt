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

    // TODO Use nat instead of longNat
    //  and "@Ignore" all tests that fail due to it being an int
    //  instead of a nat (i.e. overflows)

    interface Duration {

        val dur: longNat

        companion object {

            val fromMillis: (longNat) -> Duration = function(
                command = { ms: longNat ->
                    mk_Duration(ms)
                },
//        post = { ms, result -> result.functions.toMillis() == ms }
            )

            val fromSeconds: (longNat) -> Duration = function(
                command = { sc: longNat ->
                    fromMillis((sc * MILLIS_PER_SECOND))
                },
//        post = { sc, result -> result.functions.toSeconds() == sc }
            )

            val fromMinutes: (longNat) -> Duration = function(
                command = { mn: longNat ->
                    fromSeconds((mn * SECONDS_PER_MINUTE))
                },
//        post = { mn, result -> result.functions.toMinutes() == mn }
            )

            val fromHours: (longNat) -> Duration = function(
                command = { hr: longNat ->
                    fromMinutes(hr * MINUTES_PER_HOUR)
                },
//        post = { hr, result -> result.functions.toHours() == hr }
            )

            val fromDays: (longNat) -> Duration = function(
                command = { dy: longNat ->
                    fromHours(dy * HOURS_PER_DAY)
                },
//        post = { dy, result -> result.functions.toDays() == dy }
            )

            val fromMonth: (Year, Month) -> Duration = function(
                command = { yr: Year, mn: Month ->
                    fromDays(daysInMonth(yr, mn).toLong())
                }
            )

            val durUpToMonth: (Year, Month) -> Duration = function(
                command = { yr: Year, mn: Month ->
                    sumDuration(seq(1..mn - 1) { fromMonth(yr, it) })
                }
            )

            val fromYear: (Year) -> Duration = function(
                command = { yr: Year ->
                    fromDays(daysInYear(yr).toLong())
                }
            )

            val durUpToYear: (Year) -> Duration = function(
                command = { yr: Year ->
                    sumDuration(seq(FIRST_YEAR..yr - 1) { fromYear(it) })
                }
            )
        }

        @FunctionProvider(DurationFunctions::class)
        val functions: DurationFunctions

        class DurationFunctions(private val d: Duration) {
            val toMillis: () -> longNat = function(
                command = { d.dur },
                post = { result -> fromMillis(result) == d }
            )

            val toSeconds: () -> longNat = function(
                command = { d.functions.toMillis() / MILLIS_PER_SECOND },
                post = { result -> fromSeconds(result).dur <= d.dur && d.dur < fromSeconds(result + 1).dur }
            )

            val toMinutes: () -> longNat = function(
                command = { d.functions.toSeconds() / SECONDS_PER_MINUTE },
                post = { result -> fromMinutes(result).dur <= d.dur && d.dur < fromMinutes(result + 1).dur }
            )

            val toHours: () -> longNat = function(
                command = { d.functions.toMinutes() / MINUTES_PER_HOUR },
                post = { result -> fromHours(result).dur <= d.dur && d.dur < fromHours(result + 1).dur }
            )

            val toDays: () -> longNat = function(
                command = { d.functions.toHours() / HOURS_PER_DAY },
                post = { result -> fromDays(result).dur <= d.dur && d.dur < fromDays(result + 1).dur }
            )

            val toMonth: (Year) -> nat = function(
                command = { yr: Year ->
                    ((set(1..MONTHS_PER_YEAR,
                        filter = { durUpToMonth(yr, it).dur <= d.dur }) { it }).max()) - 1
                },
                pre = { yr -> d.dur < fromYear(yr).dur }
            )

            val toYear: (Year) -> nat by lazy {
                function(
                    command = { yr: Year ->
                        if (d.dur < fromYear(yr).dur) {
                            0
                        } else {
                            1 + durDiff(d, fromYear(yr)).functions.toYear(yr + 1)
                        }
                    },
//                    This post condition is slow and toYear() is recursive, so it repeats many times
//                    post = { yr, result ->
//                        durUpToYear(yr + result).functions.subtract(durUpToYear(yr)).dur <= d.dur &&
//                                durUpToYear(yr + result + 1).functions.subtract(durUpToYear(yr)).dur > d.dur
//                    },
                    measure = { yr -> LAST_YEAR - yr },
                )
            }

            val toDTG: () -> DTG = function(
                command = {
                    val dy = fromDays(d.functions.toDays())
                    mk_DTG(dy.functions.toDate(), durDiff(d, dy).functions.toTime())
                },
                post = { result -> result.functions.toDur() == d }
            )

            val toDate: () -> Date = function(
                command = {
                    val yr = d.functions.toYear(FIRST_YEAR)
                    val ydur = durDiff(d, durUpToYear(yr))
                    val mn = ydur.functions.toMonth(yr) + 1
                    val dy = durDiff(ydur, durUpToMonth(yr, mn)).functions.toDays().toInt() + 1
                    mk_Date(yr, mn, dy)
                },
                post = { result ->
                    result.functions.toDur().dur <= d.dur && d.dur <
                            result.functions.toDur().functions.add(ONE_DAY).dur
                }
            )

            val toTime: () -> Time = function(
                command = {
                    val hr = d.functions.toHours()
                    val mn = durDiff(d, fromHours(hr)).functions.toMinutes()
                    val hmd = fromHours(hr).functions.add(fromMinutes(mn))
                    val sc = durDiff(d, hmd).functions.toSeconds()
                    val ml = durDiff(d, hmd.functions.add(fromSeconds(sc))).functions.toMillis()
                    mk_Time(hr.toInt(), mn.toInt(), sc.toInt(), ml.toInt())
                },
                post = { result -> result.functions.toDur() == d }
            )

            val add: (Duration) -> Duration = function(
                command = { dur: Duration ->
                    mk_Duration(d.dur + dur.dur)
                },
                post = { dur, result ->
//            durDiff(result, d) == dur && durDiff(result, dur) == d &&
                    result.functions.subtract(d) == dur && result.functions.subtract(dur) == d
                }
            )

            val subtract: (Duration) -> Duration = function(
                command = { dur: Duration ->
                    mk_Duration(d.dur - dur.dur)
                },
                pre = { dur -> d.dur >= dur.dur },
//        post = { dur, result -> result.functions.add(dur) == d }
            )

            val multiply: (nat) -> Duration = function(
                command = { n: nat ->
                    mk_Duration(d.dur * n)
                },
                post = { n: nat, result -> result.functions.divide(n) == d }
            )

            val divide: (nat) -> Duration = function(
                command = { n: nat ->
                    mk_Duration(d.dur / n)
                },
//        post = { n, result ->
//            result.functions.multiply(n).dur <= d.dur && d.dur < result.functions.multiply(n+1).dur
//          }
            )

            val modMinutes: () -> Duration = function(
                command = { mk_Duration(d.dur % ONE_MINUTE.dur) },
                post = { result ->
                    result.dur < ONE_MINUTE.dur
//         && exists(0..mk_DTG(LAST_DATE, LAST_TIME).functions.toDur().functions.toMinutes()) { fromMinutes(it).functions.add(result) == d }
                }
            )
            val modHours: () -> Duration = function(
                command = { mk_Duration(d.dur % ONE_HOUR.dur) },
                post = { result ->
                    result.dur < ONE_HOUR.dur
//         && exists(0..mk_DTG(LAST_DATE, LAST_TIME).functions.toDur().functions.toHours()) { fromHours(it).functions.add(result) == d }
                }
            )
            val modDays: () -> Duration = function(
                command = { mk_Duration(d.dur % ONE_DAY.dur) },
                post = { result ->
                    result.dur < ONE_DAY.dur
//         && exists(0..mk_DTG(LAST_DATE, LAST_TIME).functions.toDur().functions.toDays()) { fromDays(it).functions.add(result) == d }
                }
            )

            val format: () -> String = function<String>(
                command = {
                    val numDays = d.functions.toDays()
                    val timeOfDay = d.functions.modDays().functions.toTime()
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
                    val time = item(timeOfDay.hour, 'H') + item(timeOfDay.minute, 'M') +
                            if (timeOfDay.millisecond == 0) {
                                item(timeOfDay.second, 'S')
                            } else {
                                itemSec(timeOfDay.second, timeOfDay.millisecond)
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

        }

    }

    @PrimitiveInvariant(name = "Year", base = nat::class)
    fun yearNotInRange(y: nat) = y in FIRST_YEAR..LAST_YEAR

    @PrimitiveInvariant(name = "Month", base = nat1::class)
    fun monthNotInRange(m: nat1) = m <= MONTHS_PER_YEAR

    @PrimitiveInvariant(name = "Day", base = nat1::class)
    fun dayNotInRange(d: nat1) = d <= MAX_DAYS_PER_MONTH

    @PrimitiveInvariant(name = "Hour", base = nat::class)
    fun hourNotInRange(h: nat) = h < HOURS_PER_DAY

    @PrimitiveInvariant(name = "Minute", base = nat::class)
    fun minuteNotInRange(m: nat) = m < MINUTES_PER_HOUR

    @PrimitiveInvariant(name = "Second", base = nat::class)
    fun secondNotInRange(s: nat) = s < SECONDS_PER_MINUTE

    @PrimitiveInvariant(name = "Millisecond", base = nat::class)
    fun millisecondNotInRange(m: nat) = m < MILLIS_PER_SECOND

    interface Date {
        val year: Year
        val month: Month
        val day: Day

        @Invariant
        fun isDayValid() = day <= daysInMonth(year, month)

        @ComparableProperty
        val dur: longNat get() = this.functions.toDur().dur

        @FunctionProvider(DateFunctions::class)
        val functions: DateFunctions

        open class DateFunctions(private val date: Date) {

            val toDur: () -> Duration = function<Duration>(
                command = {
                    Duration.durUpToYear(date.year).functions.add(
                        Duration.durUpToMonth(date.year, date.month).functions.add(
                            Duration.fromDays(date.day - 1.toLong())
                        )
                    )
                },
//        post = { result -> result.functions.toDate() == date }
            )

            val format: () -> String = function<String>(
                command = { String.format("%04d-%02d-%02d", date.year, date.month, date.day) }
            )

            val toDayOfWeek: () -> DayOfWeek = function<DayOfWeek>(
                command = {
                    val d = (date.functions.toDur().functions.toDays().toInt() - 365) % 7
                    DayOfWeek.entries[d]
                }
            )
        }
    }

    interface Time {
        val hour: Hour
        val minute: Minute
        val second: Second
        val millisecond: Millisecond

        @ComparableProperty
        val dur: longNat get() = this.functions.toDur().dur


        @FunctionProvider(TimeFunctions::class)
        val functions: TimeFunctions

        class TimeFunctions(private val time: Time) {
            val toDur: () -> Duration = function<Duration>(
                command = {
                    Duration.fromHours(time.hour.toLong()).functions.add(
                        Duration.fromMinutes(time.minute.toLong()).functions.add(
                            Duration.fromSeconds(time.second.toLong()).functions.add(
                                Duration.fromMillis(time.millisecond.toLong())
                            )
                        )
                    )
                },
//        post = { result -> result.functions.toTime() == time }
            )

            val format: () -> String = function<String>(
                command = {
                    val frac = if (time.millisecond == 0) {
                        ""
                    } else {
                        String.format(".%03d", time.millisecond)
                    }
                    String.format("%02d:%02d:%02d%s", time.hour, time.minute, time.second, frac)
                }
            )
        }

    }

    interface TimeInZone {
        val time: Time
        val offset: Offset

        val nTime: Time get() = this.functions.normalise()._1

        @ComparableProperty
        val dur: longNat get() = nTime.functions.toDur().dur

        @FunctionProvider(TIZFunctions::class)
        val functions: TIZFunctions

        class TIZFunctions(private val time: TimeInZone) {

            val toDur: () -> Duration = function(
                command = { time.functions.normalise()._1.functions.toDur() },
                post = { result -> result.functions.toTime().dur == time.dur }
            )
            val normalise: () -> Tuple2<Time, PlusOrMinus> = function<Tuple2<Time, PlusOrMinus>>(
                command = {
                    val utcTimeDur = time.time.functions.toDur()
                    val offset = time.offset.delta
                    when (time.offset) {
                        mk_Offset(offset, PlusOrMinus.Plus) ->
                            if (offset.dur <= utcTimeDur.dur) {
                                mk_(utcTimeDur.functions.subtract(offset).functions.toTime(), PlusOrMinus.None)
                            } else {
                                mk_(
                                    utcTimeDur.functions.add(ONE_DAY).functions.subtract(offset).functions.toTime(),
                                    PlusOrMinus.Plus
                                )
                            }

                        mk_Offset(offset, PlusOrMinus.Minus) -> {
                            val adjusted = utcTimeDur.functions.add(offset)
                            if (adjusted.dur < ONE_DAY.dur) {
                                mk_(adjusted.functions.toTime(), PlusOrMinus.None)
                            } else {
                                mk_(adjusted.functions.subtract(ONE_DAY).functions.toTime(), PlusOrMinus.Minus)
                            }
                        }

                        else -> mk_(time.time, PlusOrMinus.None)
                    }
                }
            )
            val format: () -> String = function<String>(
                command = {
                    time.time.functions.format() + (
                            if (time.offset.delta.dur == NO_DURATION.dur) {
                                "Z"
                            } else {
                                time.offset.functions.format()
                            }
                            )
                }
            )
        }
    }

    interface Offset {
        val delta: Duration
        val pm: PlusOrMinus

        @Invariant
        fun offsetMoreThanDay() = delta.dur < ONE_DAY.dur

        @Invariant
        fun offsetZero() = delta.functions.modMinutes() == NO_DURATION

        @ComparableProperty
        val comp: longNat
            get() = when (pm) {
                PlusOrMinus.Plus -> delta.dur + 1
                PlusOrMinus.Minus -> delta.dur - 1
                PlusOrMinus.None -> delta.dur
            }

        @FunctionProvider(OffsetFunctions::class)
        val functions: OffsetFunctions

        class OffsetFunctions(private val offset: Offset) {

            val format: () -> String = function<String>(
                command = {
                    val hm = offset.delta.functions.toTime()
                    val sign = when (offset.pm) {
                        PlusOrMinus.Plus -> {
                            "+"
                        }

                        PlusOrMinus.Minus -> {
                            "-"
                        }

                        else -> {
                            ""
                        }
                    }
                    String.format("%s%02d:%02d", sign, hm.hour, hm.minute)
                }
            )
        }
    }

    interface DTG {
        val date: Date
        val time: Time

        @ComparableProperty
        val dur: longNat get() = this.functions.toDur().dur

        @FunctionProvider(DTGFunctions::class)
        val functions: DTGFunctions

        class DTGFunctions(private val dtg: DTG) {

            val add: (Duration) -> DTG = function(
                command = { dur: Duration ->
                    dtg.functions.toDur().functions.add(dur).functions.toDTG()
                },
                post = { dur, result -> result.functions.subtract(dur) == dtg }
            )

            val subtract: (Duration) -> DTG = function(
                command = { dur ->
                    durDiff(dtg.functions.toDur(), dur).functions.toDTG()
                },
                pre = { dur -> dur.dur <= dtg.functions.toDur().dur },
//        post = { dur, result -> result.functions.add(dur) == dtg }
            )

            val toDur: () -> Duration = function<Duration>(
                command = { dtg.date.functions.toDur().functions.add(dtg.time.functions.toDur()) },
            )

            val within: (Duration, DTG) -> bool = function(
                command = { dur: Duration, target: DTG ->
                    if (dur.dur.toInt() == 0) {
                        dtg.dur == target.dur
                    } else {
                        dtg.functions.inInterval(mk_Interval(target.functions.subtract(dur), target.functions.add(dur)))
                    }
                },
                post = { dur, target, result -> (dur.dur == 0.toLong()) implies ((dtg == target) == result) }
            )

            val inInterval: (Interval) -> bool = function(
                command = { ival: Interval ->
                    dtg.functions.inRange(ival.begins, ival.ends)
                }
            )

            val inRange: (DTG, DTG) -> bool = function(
                command = { lowerDTG: DTG, upperDTG: DTG ->
                    lowerDTG.dur <= dtg.dur && dtg.dur < upperDTG.dur
                }
            )

            val finestGranularity: (Duration) -> bool = function(
                command = { d: Duration ->
                    dtg.functions.toDur().dur % d.dur == 0.toLong()
                },
                pre = { d -> d.dur != NO_DURATION.dur }
            )

            val instant: () -> Interval = function(
                command = { mk_Interval(dtg, dtg.functions.add(ONE_MILLISECOND)) },
                post = { result -> dtg.functions.inInterval(result) }
            )

            val format: () -> String = function<String>(
                command = { dtg.date.functions.format() + "T" + dtg.time.functions.format() }
            )

        }
    }

    interface DTGInZone {
        val date: Date
        val time: TimeInZone

        @Invariant
        fun dtgTooEarly() = !(date == FIRST_DATE && this.time.functions.normalise()._2 == PlusOrMinus.Plus)

        @Invariant
        fun dtgTooLate() = !(date == LAST_DATE && this.time.functions.normalise()._2 == PlusOrMinus.Minus)

        @ComparableProperty
        val dur: longNat get() = this.functions.normalise().functions.toDur().dur


        @FunctionProvider(DTGIZFunctions::class)
        val functions: DTGIZFunctions

        class DTGIZFunctions(private val dtgIZ: DTGInZone) {

            val normalise: () -> DTG = function<DTG>(
                command = {
                    val ntime = dtgIZ.time.functions.normalise()
                    val baseDTG = mk_DTG(dtgIZ.date, ntime._1)
                    when (ntime._2) {
                        PlusOrMinus.Plus -> baseDTG.functions.subtract(ONE_DAY)
                        PlusOrMinus.Minus -> baseDTG.functions.add(ONE_DAY)
                        PlusOrMinus.None -> baseDTG
                    }
                }
            )

            val format: () -> String = function<String>(
                command = { dtgIZ.date.functions.format() + "T" + dtgIZ.time.functions.format() }
            )


        }
    }

    interface Interval {
        val begins: DTG
        val ends: DTG

        @Invariant
        fun zeroSizeInterval() = begins.dur != ends.dur

        @Invariant
        fun beginAfterEnd() = begins.dur <= ends.dur

        @FunctionProvider(IntervalFunctions::class)
        val functions: IntervalFunctions

        class IntervalFunctions(private val interval: Interval) {
            val within: (Interval) -> bool = function(
                command = { containerInterval: Interval ->
                    containerInterval.begins.dur <= interval.begins.dur && interval.ends.dur <= containerInterval.ends.dur
                },
                post = { containerInterval, result ->
                    result == (containerInterval.begins.dur <= interval.begins.dur
                            && interval.ends.dur <= containerInterval.ends.dur)
                }
            )
            val overlap: (Interval) -> bool = function(
                command = { other: Interval ->
                    other.begins.dur < interval.ends.dur && interval.begins.dur < other.ends.dur
                },
                post = { other, result ->
                    result == exists(
                        max(interval.begins.dur, other.begins.dur)..min(
                            interval.ends.dur,
                            other.ends.dur
                        )
                    ) {
                        mk_Duration(it).functions.toDTG().functions.inInterval(interval)
                                && mk_Duration(it).functions.toDTG().functions.inInterval(other)
                    }
                }
            )
            val toDur: () -> Duration = function(
                command = { diff(interval.begins, interval.ends) },
                post = { result -> interval.begins.functions.add(result) == interval.ends }
            )
            val finestGranularityI: (Duration) -> bool = function(
                command = { d: Duration ->
                    interval.begins.functions.finestGranularity(d) && interval.ends.functions.finestGranularity(d)
                }
            )

            val format: () -> String = function<String>(
                command = { interval.begins.functions.format() + "/" + interval.ends.functions.format() }
            )
        }
    }

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
    private val DAYS_PER_YEAR: nat1 by lazy { daysInYear(1) } // arbitrary non-leap year
    private val DAYS_PER_LEAP_YEAR: nat1 by lazy { daysInYear(4) } // arbitrary leap year

    private const val FIRST_YEAR: nat = 0
    private const val LAST_YEAR: nat = 9999

    val FIRST_DATE: Date by lazy { mk_Date(FIRST_YEAR, 1, 1) }

    private val LAST_DATE: Date by lazy { mk_Date(LAST_YEAR, 12, 31) }

    val FIRST_TIME: Time = mk_Time(0, 0, 0, 0)

    private val LAST_TIME: Time by lazy { mk_Time(23, 59, 59, 999) }

    private val FIRST_DTG: DTG by lazy { mk_DTG(FIRST_DATE, FIRST_TIME) }

    private val LAST_DTG: DTG by lazy { mk_DTG(LAST_DATE, LAST_TIME) }

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

    val diff: (DTG, DTG) -> Duration = function(
        command = { dtg1: DTG, dtg2: DTG ->
            durDiff(dtg1.functions.toDur(), dtg2.functions.toDur())
        }
    )

    val durDiff: (Duration, Duration) -> Duration = function(
        command = { dur1: Duration, dur2: Duration ->
            mk_Duration(abs(dur1.dur - dur2.dur))
        },
        post = { dur1, dur2, result ->
            ((dur1.dur <= dur2.dur) implies (dur1.functions.add(result) == dur2)) &&
                    ((dur2.dur <= dur1.dur) implies (dur2.functions.add(result) == dur1))
        }
    )

    val minDTG: (Set1<DTG>) -> DTG = function(
        command = { dtgs: Set1<DTG> ->
            mk_Duration((set(dtgs) { it.dur }).min()).functions.toDTG()
        },
        post = { dtgs, result ->
            result in dtgs && forall(dtgs) { result.dur <= it.dur }
        }
    )
    val maxDTG: (Set1<DTG>) -> DTG = function(
        command = { dtgs: Set1<DTG> ->
            mk_Duration((set(dtgs) { it.dur }).max()).functions.toDTG()
        },
        post = { dtgs, result ->
            result in dtgs && forall(dtgs) { result.dur >= it.dur }
        }
    )

    val minDate: (Set1<Date>) -> Date = function(
        command = { dates: Set1<Date> ->
            mk_Duration((set(dates) { it.dur }).min()).functions.toDate()
        },
        post = { dates, result ->
            result in dates && forall(dates) { result.dur <= it.dur }
        }
    )
    val maxDate: (Set1<Date>) -> Date = function(
        command = { dates: Set1<Date> ->
            mk_Duration((set(dates) { it.dur }).max()).functions.toDate()
        },
        post = { dates, result ->
            result in dates && forall(dates) { result.dur >= it.dur }
        }
    )

    val minTime: (Set1<Time>) -> Time = function(
        command = { times: Set1<Time> ->
            mk_Duration((set(times) { it.dur }).min()).functions.toTime()
        },
        post = { times, result ->
            result in times && forall(times) { result.dur <= it.dur }
        }
    )
    val maxTime: (Set1<Time>) -> Time = function(
        command = { times: Set1<Time> ->
            mk_Duration((set(times) { it.dur }).max()).functions.toTime()
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

    val isDate: (String) -> bool = function(
        command = { s: String ->
            if (s.length != 10) {
                false
            } else if (s.elementAt(4) != '-' || s.elementAt(7) != '-') {
                false
            } else if (s.substring(0, 4).toIntOrNull() == null ||
                s.substring(5, 7).toIntOrNull() == null ||
                s.substring(8, 10).toIntOrNull() == null
            ) {
                false
            } else if (s.substring(0, 4).toInt() < FIRST_YEAR || s.substring(0, 4).toInt() > LAST_YEAR) {
                false
            } else if (s.substring(5, 7).toInt() < 1 || s.substring(5, 7).toInt() > MONTHS_PER_YEAR) {
                false
            } else if (s.substring(8, 10).toInt() < 1 || s.substring(8, 10).toInt() > daysInMonth(
                    s.substring(0, 4).toInt(), s.substring(5, 7).toInt()
                )
            ) {
                false
            } else {
                true
            }
        }
    )

    val isDTG: (String) -> bool = function(
        command = { s: String ->
            if (s.length != 19 && s.length != 23) {
                false
            } else if (s.elementAt(4) != '-' || s.elementAt(7) != '-' ||
                s.elementAt(10) != 'T' || s.elementAt(13) != ':' || s.elementAt(16) != ':'
            ) {
                false
            } else if (s.substring(0, 4).toIntOrNull() == null ||
                s.substring(5, 7).toIntOrNull() == null ||
                s.substring(8, 10).toIntOrNull() == null ||
                s.substring(11, 13).toIntOrNull() == null ||
                s.substring(14, 16).toIntOrNull() == null ||
                s.substring(17, 19).toIntOrNull() == null
            ) {
                false
            } else if (s.substring(0, 4).toInt() < FIRST_YEAR || s.substring(0, 4).toInt() > LAST_YEAR) {
                false
            } else if (s.substring(5, 7).toInt() < 1 || s.substring(5, 7).toInt() > MONTHS_PER_YEAR) {
                false
            } else if (s.substring(8, 10).toInt() < 1 || s.substring(8, 10).toInt() > daysInMonth(
                    s.substring(0, 4).toInt(), s.substring(5, 7).toInt()
                )
            ) {
                false
            } else if (s.substring(11, 13).toInt() < 0 || s.substring(11, 13).toInt() > HOURS_PER_DAY) {
                false
            } else if (s.substring(14, 16).toInt() < 0 || s.substring(14, 16).toInt() > MINUTES_PER_HOUR) {
                false
            } else if (s.substring(17, 19).toInt() < 0 || s.substring(17, 19).toInt() > SECONDS_PER_MINUTE) {
                false
            } else if (s.length == 23) {
                if (s.elementAt(19) != '.') {
                    false
                } else if (s.substring(20, 23).toIntOrNull() == null) {
                    false
                } else if (s.substring(20, 23).toInt() < 0 || s.substring(20, 23).toInt() > MILLIS_PER_SECOND) {
                    false
                } else {
                    true
                }
            } else {
                true
            }
        }
    )

    val strToDate: (String) -> Date = function(
        command = { s: String ->
            mk_Date(
                s.substring(0, 4).toInt(),
                s.substring(5, 7).toInt(),
                s.substring(8, 10).toInt()
            )
        },
        pre = { s -> isDate(s) }
    )

    val strToDTG: (String) -> DTG = function(
        command = { s: String ->
            val ms = if (s.length == 19) {
                0
            } else {
                s.substring(20, 23).toInt()
            }
            mk_DTG(
                mk_Date(
                    s.substring(0, 4).toInt(),
                    s.substring(5, 7).toInt(),
                    s.substring(8, 10).toInt()
                ), mk_Time(
                    s.substring(11, 13).toInt(),
                    s.substring(14, 16).toInt(),
                    s.substring(17, 19).toInt(),
                    ms
                )
            )
        },
        pre = { s -> isDTG(s) }
    )

    val addMonths: (DTG, nat) -> DTG = function(
        command = { dtg: DTG, n: nat ->
            val nextM = ((dtg.date.month + n - 1) % MONTHS_PER_YEAR) + 1
            val nextY = dtg.date.year + (dtg.date.month + n - 1) / MONTHS_PER_YEAR
            if (dtg.date.day > daysInMonth(nextY, nextM)) {
                mk_DTG(mk_Date(nextY, nextM, daysInMonth(nextY, nextM)), dtg.time)
            } else {
                mk_DTG(mk_Date(nextY, nextM, dtg.date.day), dtg.time)
            }
        },
    )

    val subtractMonths: (DTG, nat) -> DTG = function(
        command = { dtg: DTG, n: nat ->
            val nextM = (dtg.date.month - n - 1).mod(MONTHS_PER_YEAR) + 1
            val nextY = dtg.date.year + (dtg.date.month - n - 12) / MONTHS_PER_YEAR
            if (dtg.date.day > daysInMonth(nextY, nextM)) {
                mk_DTG(mk_Date(nextY, nextM, daysInMonth(nextY, nextM)), dtg.time)
            } else {
                mk_DTG(mk_Date(nextY, nextM, dtg.date.day), dtg.time)
            }
        }
    )

    val monthsBetween: (DTG, DTG) -> nat = function(
        command = { starts: DTG, ends: DTG ->
            var n = 0
            while (addMonths(starts, n).dur <= ends.dur) {
                n++
            }
            n - 1
        },
        pre = { starts, ends -> starts.dur <= ends.dur }
    )

    val yearsBetween: (DTG, DTG) -> nat = function(
        command = { starts: DTG, ends: DTG ->
            if (Duration.durUpToMonth(
                    starts.date.year,
                    starts.date.month
                ).dur + Duration.fromDays(starts.date.day.toLong()).dur <=
                Duration.durUpToMonth(
                    ends.date.year,
                    ends.date.month
                ).dur + Duration.fromDays(ends.date.day.toLong()).dur
            ) {
                ends.date.year - starts.date.year
            } else {
                ends.date.year - starts.date.year - 1
            }
        },
        pre = { starts, ends -> starts.dur <= ends.dur }
    )

    private val NO_DURATION: Duration = Duration.fromMillis(0)

    private val ONE_MILLISECOND: Duration = Duration.fromMillis(1)

    private val ONE_SECOND: Duration = Duration.fromSeconds(1)

    private val ONE_MINUTE: Duration = Duration.fromMinutes(1)

    private val ONE_HOUR: Duration = Duration.fromHours(1)

    private val ONE_DAY: Duration = Duration.fromDays(1)

    private val ONE_YEAR: Duration = Duration.fromDays(DAYS_PER_YEAR.toLong())

    private val ONE_LEAP_YEAR: Duration = Duration.fromDays(DAYS_PER_LEAP_YEAR.toLong())

}

