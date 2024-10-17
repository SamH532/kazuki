package com.anaplan.engineering.kazuki.toolkit.ISO8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Duration_Module.mk_Duration
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Offset_Module.mk_Offset

@Module
interface Time {
    val hour: Hour
    val minute: Minute
    val second: Second
    val millisecond: Millisecond

    @ComparableProperty
    val duration_ms: nat get() = functions.toDuration().duration_ms

    @FunctionProvider(TimeFunctions::class)
    val functions: TimeFunctions

    class TimeFunctions(private val time: Time) {
        val toDuration: () -> Duration = function<Duration>(
            command = {
                Duration.fromHours(time.hour).functions.add(
                    Duration.fromMinutes(time.minute).functions.add(
                        Duration.fromSeconds(time.second).functions.add(
                            Duration.fromMillis(time.millisecond)
                        )
                    )
                )
            },
//        post = { result -> result.functions.toTime() == time }
        )

        val format: () -> String = function<String>(
            command = {
                val frac = if (time.millisecond == 0) "" else String.format(".%03d", time.millisecond)
                String.format("%02d:%02d:%02d%s", time.hour, time.minute, time.second, frac)
            }
        )
    }

}

@Module
interface TimeInZone {
    val time: Time
    val offset: Offset

    val nTime: Time get() = functions.normalise()._1

    @ComparableProperty
    val duration_ms: nat get() = nTime.functions.toDuration().duration_ms

    @FunctionProvider(TIZFunctions::class)
    val functions: TIZFunctions

    class TIZFunctions(private val time: TimeInZone) {

        val toDuration: () -> Duration = function(
            command = { time.functions.normalise()._1.functions.toDuration() },
            post = { result -> result.functions.toTime().duration_ms == time.duration_ms }
        )
        val normalise: () -> Tuple2<Time, PlusOrMinus> = function<Tuple2<Time, PlusOrMinus>>(
            command = {
                val utcTimeDuration = time.time.functions.toDuration()
                val offset = time.offset.delta
                when (time.offset) {
                    mk_Offset(offset, PlusOrMinus.Plus) ->
                        if (offset.duration_ms <= utcTimeDuration.duration_ms) {
                            mk_(utcTimeDuration.functions.subtract(offset).functions.toTime(), PlusOrMinus.None)
                        } else {
                            mk_(
                                utcTimeDuration.functions.add(ONE_DAY).functions.subtract(offset).functions.toTime(),
                                PlusOrMinus.Plus
                            )
                        }

                    mk_Offset(offset, PlusOrMinus.Minus) -> {
                        val adjusted = utcTimeDuration.functions.add(offset)
                        if (adjusted.duration_ms < ONE_DAY.duration_ms) {
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
                        if (time.offset.delta.duration_ms == NO_DURATION.duration_ms) {
                            "Z"
                        } else {
                            time.offset.functions.format()
                        }
                        )
            }
        )
    }
}

@Module
interface Offset {
    val delta: Duration
    val pm: PlusOrMinus

    @Invariant
    fun offsetMoreThanDay() = delta.duration_ms < ONE_DAY.duration_ms

    @Invariant
    fun offsetZero() = delta.functions.modMinutes() == NO_DURATION

    @ComparableProperty
    val comp: nat
        get() = when (pm) {
            PlusOrMinus.Plus -> delta.duration_ms + 1
            PlusOrMinus.Minus -> delta.duration_ms - 1
            PlusOrMinus.None -> delta.duration_ms
        }

    @FunctionProvider(OffsetFunctions::class)
    val functions: OffsetFunctions

    class OffsetFunctions(private val offset: Offset) {

        val format: () -> String = function<String>(
            command = {
                val hm = offset.delta.functions.toTime()
                val sign = when (offset.pm) {
                    PlusOrMinus.Plus -> "+"
                    PlusOrMinus.Minus -> "-"
                    else -> ""
                }
                String.format("%s%02d:%02d", sign, hm.hour, hm.minute)
            }
        )
    }
}

@PrimitiveInvariant(name = "Hour", base = nat::class)
fun hourNotInRange(hour: nat) = hour < HOURS_PER_DAY

@PrimitiveInvariant(name = "Minute", base = nat::class)
fun minuteNotInRange(minute: nat) = minute < MINUTES_PER_HOUR

@PrimitiveInvariant(name = "Second", base = nat::class)
fun secondNotInRange(second: nat) = second < SECONDS_PER_MINUTE

@PrimitiveInvariant(name = "Millisecond", base = nat::class)
fun millisecondNotInRange(millisecond: nat) = millisecond < MILLIS_PER_SECOND

val minTime: (Set1<Time>) -> Time = function(
    command = { times: Set1<Time> -> mk_Duration((set(times) { it.duration_ms }).min()).functions.toTime() },
    post = { times, result -> result in times && forall(times) { result.duration_ms <= it.duration_ms } }
)
val maxTime: (Set1<Time>) -> Time = function(
    command = { times: Set1<Time> -> mk_Duration((set(times) { it.duration_ms }).max()).functions.toTime() },
    post = { times, result -> result in times && forall(times) { result.duration_ms >= it.duration_ms } }
)
