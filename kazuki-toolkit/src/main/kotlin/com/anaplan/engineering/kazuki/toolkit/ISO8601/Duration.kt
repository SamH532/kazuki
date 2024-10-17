package com.anaplan.engineering.kazuki.toolkit.ISO8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.ISO8601.DTG_Module.mk_DTG
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Duration_Module.mk_Duration
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Time_Module.mk_Time
import kotlin.math.abs

@Module
interface Duration {

    val duration_ms: nat

    companion object {

        val fromMillis: (nat) -> Duration = function(
            command = { millisecond: nat -> mk_Duration(millisecond) },
//        post = { millisecond, result -> result.functions.toMillis() == millisecond }
        )

        val fromSeconds: (nat) -> Duration = function(
            command = { second: nat -> fromMillis((second * MILLIS_PER_SECOND)) },
//        post = { second, result -> result.functions.toSeconds() == second }
        )

        val fromMinutes: (nat) -> Duration = function(
            command = { minutes: nat -> fromSeconds((minutes * SECONDS_PER_MINUTE)) },
//        post = { minutes, result -> result.functions.toMinutes() == minutes }
        )

        val fromHours: (nat) -> Duration = function(
            command = { hour: nat -> fromMinutes(hour * MINUTES_PER_HOUR) },
//        post = { hour, result -> result.functions.toHours() == hour }
        )

        val fromDays: (nat) -> Duration = function(
            command = { day: nat -> fromHours(day * HOURS_PER_DAY) },
//        post = { day, result -> result.functions.toDays() == day }
        )

        val fromMonth: (Year, Month) -> Duration = function(
            command = { year: Year, month: Month -> fromDays(daysInMonth(year, month)) }
        )

        val durationUpToMonth: (Year, Month) -> Duration = function(
            command = { year: Year, month: Month -> sumDuration(seq(1 until month) { fromMonth(year, it) }) }
        )

        val fromYear: (Year) -> Duration = function(
            command = { year: Year -> fromDays(daysInYear(year)) }
        )

        val durationUpToYear: (Year) -> Duration = function(
            command = { year: Year -> sumDuration(seq(FirstYear until year) { fromYear(it) }) }
        )
    }

    @FunctionProvider(DurationFunctions::class)
    val functions: DurationFunctions

    class DurationFunctions(private val duration: Duration) {
        val toMillis: () -> nat = function(
            command = { duration.duration_ms },
            post = { result -> fromMillis(result) == duration }
        )

        val toSeconds: () -> nat = function(
            command = { duration.functions.toMillis() / MILLIS_PER_SECOND },
            post = { result ->
                fromSeconds(result).duration_ms <= duration.duration_ms && duration.duration_ms < fromSeconds(
                    result + 1
                ).duration_ms
            }
        )

        val toMinutes: () -> nat = function(
            command = { duration.functions.toSeconds() / SECONDS_PER_MINUTE },
            post = { result ->
                fromMinutes(result).duration_ms <= duration.duration_ms && duration.duration_ms < fromMinutes(
                    result + 1
                ).duration_ms
            }
        )

        val toHours: () -> nat = function(
            command = { duration.functions.toMinutes() / MINUTES_PER_HOUR },
            post = { result ->
                fromHours(result).duration_ms <= duration.duration_ms && duration.duration_ms < fromHours(
                    result + 1
                ).duration_ms
            }
        )

        val toDays: () -> nat = function(
            command = { duration.functions.toHours() / HOURS_PER_DAY },
            post = { result ->
                fromDays(result).duration_ms <= duration.duration_ms && duration.duration_ms < fromDays(
                    result + 1
                ).duration_ms
            }
        )

        val toMonth: (Year) -> nat = function(
            command = { year: Year ->
                (set(
                    1..MONTHS_PER_YEAR,
                    filter = { durationUpToMonth(year, it).duration_ms <= duration.duration_ms })
                { it }).max() - 1
            },
            pre = { year -> duration.duration_ms < fromYear(year).duration_ms }
        )

        val toYear: (Year) -> nat by lazy {
            function(
                command = { year: Year ->
                    if (duration.duration_ms < fromYear(year).duration_ms) {
                        0
                    } else {
                        1 + durationDiff(duration, fromYear(year)).functions.toYear(year + 1)
                    }
                },
//                    This post condition is slow and toYear() is recursive, so it repeats many times
//                    post = { year, result ->
//                        durUpToYear(year + result).functions.subtract(durUpToYear(year)).duration_ms <= d.duration_ms &&
//                                durUpToYear(year + result + 1).functions.subtract(durUpToYear(year)).duration_ms > d.dur
//                    },
                measure = { year -> LastYear - year },
            )
        }

        val toDTG: () -> DTG = function(
            command = {
                val days = fromDays(duration.functions.toDays())
                mk_DTG(days.functions.toDate(), durationDiff(duration, days).functions.toTime())
            },
            post = { result -> result.functions.toDuration() == duration }
        )

        val toDate: () -> Date = function(
            command = {
                val year = duration.functions.toYear(FirstYear)
                val yearDur = durationDiff(duration, durationUpToYear(year))
                val month = yearDur.functions.toMonth(year) + 1
                val day = durationDiff(yearDur, durationUpToMonth(year, month)).functions.toDays() + 1
                mk_Date(year, month, day)
            },
            post = { result ->
                result.functions.toDuration().duration_ms <= duration.duration_ms &&
                        duration.duration_ms < result.functions.toDuration().functions.add(ONE_DAY).duration_ms
            }
        )

        val toTime: () -> Time = function(
            command = {
                val hour = duration.functions.toHours()
                val minute = durationDiff(duration, fromHours(hour)).functions.toMinutes()
                val hmd = fromHours(hour).functions.add(fromMinutes(minute))
                val second = durationDiff(duration, hmd).functions.toSeconds()
                val millisecond =
                    durationDiff(duration, hmd.functions.add(fromSeconds(second))).functions.toMillis()
                mk_Time(hour, minute, second, millisecond)
            },
            post = { result -> result.functions.toDuration() == duration }
        )

        val add: (Duration) -> Duration = function(
            command = { plusDuration: Duration -> mk_Duration(duration.duration_ms + plusDuration.duration_ms) },
            post = { plusDuration, result ->
//            durDiff(result, d) == duration && durDiff(result, duration) == d &&
                result.functions.subtract(plusDuration) == duration && result.functions.subtract(duration) == plusDuration
            }
        )

        val subtract: (Duration) -> Duration = function(
            command = { subtractDuration: Duration -> mk_Duration(duration.duration_ms - subtractDuration.duration_ms) },
            pre = { subtractDuration -> duration.duration_ms >= subtractDuration.duration_ms },
//        post = { subtractDuration, result -> result.functions.add(dur) == d }
        )

        val multiply: (nat) -> Duration = function(
            command = { n: nat -> mk_Duration(duration.duration_ms * n) },
            post = { n: nat, result -> result.functions.divide(n) == duration }
        )

        val divide: (nat) -> Duration = function(
            command = { n: nat -> mk_Duration(duration.duration_ms / n) },
//        post = { n, result ->
//            result.functions.multiply(n).duration_ms <= d.duration_ms && d.duration_ms < result.functions.multiply(n+1).dur
//          }
        )

        val modMinutes: () -> Duration = function(
            command = { mk_Duration(duration.duration_ms % ONE_MINUTE.duration_ms) },
            post = { result ->
                result.duration_ms < ONE_MINUTE.duration_ms
//         && exists(0..mk_DTG(LAST_DATE, LAST_TIME).functions.toDur().functions.toMinutes()) { fromMinutes(it).functions.add(result) == d }
            }
        )
        val modHours: () -> Duration = function(
            command = { mk_Duration(duration.duration_ms % ONE_HOUR.duration_ms) },
            post = { result ->
                result.duration_ms < ONE_HOUR.duration_ms
//         && exists(0..mk_DTG(LAST_DATE, LAST_TIME).functions.toDur().functions.toHours()) { fromHours(it).functions.add(result) == d }
            }
        )
        val modDays: () -> Duration = function(
            command = { mk_Duration(duration.duration_ms % ONE_DAY.duration_ms) },
            post = { result ->
                result.duration_ms < ONE_DAY.duration_ms
//         && exists(0..mk_DTG(LAST_DATE, LAST_TIME).functions.toDur().functions.toDays()) { fromDays(it).functions.add(result) == d }
            }
        )

        private val formatItem: (nat, Char) -> String = function(
            command = { n: nat, c: Char -> if (n == 0) "" else String.format("%d%s", n, c) }
        )

        private val formatItemSec: (nat, nat) -> String = function(
            command = { seconds: nat, milliseconds: nat -> String.format("%d.%03dS", seconds, milliseconds) }
        )

        val format: () -> String = function<String>(
            command = {
                val numDays = duration.functions.toDays()
                val timeOfDay = duration.functions.modDays().functions.toTime()
                val date = formatItem(numDays, 'D')
                val time = formatItem(timeOfDay.hour, 'H') + formatItem(timeOfDay.minute, 'M') +
                        if (timeOfDay.millisecond == 0) {
                            formatItem(timeOfDay.second, 'S')
                        } else {
                            formatItemSec(timeOfDay.second, timeOfDay.millisecond)
                        }
                if (date == "" && time == "") "PT0S" else "P$date${if (time == "") "" else "T$time"}"
            }
        )
    }
}

val minDuration: (Set1<Duration>) -> Duration = function(
    command = { durationSet1: Set1<Duration> -> mk_Duration((set(durationSet1) { it.duration_ms }).min()) },
    post = { durationSet1, result -> result in durationSet1 && forall(durationSet1) { result.duration_ms <= it.duration_ms } }
)
val maxDuration: (Set1<Duration>) -> Duration = function(
    command = { durationSet1: Set1<Duration> -> mk_Duration((set(durationSet1) { it.duration_ms }).max()) },
    post = { durationSet1, result -> result in durationSet1 && forall(durationSet1) { result.duration_ms >= it.duration_ms } }
)
val sumDuration: (Sequence<Duration>) -> Duration = function(
    command = { durationSequence: Sequence<Duration> -> mk_Duration((seq(durationSequence) { it.duration_ms }).sum()) }
)

val durationDiff: (Duration, Duration) -> Duration = function(
    command = { dur1: Duration, dur2: Duration -> mk_Duration(abs(dur1.duration_ms - dur2.duration_ms)) },
    post = { dur1, dur2, result ->
        ((dur1.duration_ms <= dur2.duration_ms) implies (dur1.functions.add(result) == dur2)) &&
                ((dur2.duration_ms <= dur1.duration_ms) implies (dur2.functions.add(result) == dur1))
    }
)
