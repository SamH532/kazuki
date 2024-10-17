package com.anaplan.engineering.kazuki.toolkit

import com.anaplan.engineering.kazuki.core.PreconditionFailure
import com.anaplan.engineering.kazuki.core.mk_
import com.anaplan.engineering.kazuki.core.mk_Seq
import com.anaplan.engineering.kazuki.core.mk_Set1
import com.anaplan.engineering.kazuki.toolkit.ISO8601.*
import com.anaplan.engineering.kazuki.toolkit.ISO8601.DTG_Module.mk_DTG
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.ISO8601.DtgInZone_Module.mk_DtgInZone
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Duration_Module.mk_Duration
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Interval_Module.mk_Interval
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Offset_Module.mk_Offset
import com.anaplan.engineering.kazuki.toolkit.ISO8601.TimeInZone_Module.mk_TimeInZone
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Time_Module.mk_Time
import junit.framework.TestCase.assertEquals
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ISO8601Test {

    @Test
    fun isLeapTest() {
        assertEquals(true, isLeap(1992))
        assertEquals(false, isLeap(1993))
    }

    @Test
    fun daysInMonthTest() {
        assertEquals(29, daysInMonth(1992, 2))
        assertEquals(28, daysInMonth(1991, 2))
    }

    @Test
    fun daysInYearTest() {
        assertEquals(366, daysInYear(1992))
        assertEquals(365, daysInYear(1))
        assertEquals(365, daysInYear(1991))
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun dtgInRangeDayTest() {
        assertEquals(
            true,
            mk_DTG(mk_Date(1990, 1, 3), FirstTime).functions.inRange(
                mk_DTG(mk_Date(1990, 1, 1), FirstTime),
                mk_DTG(mk_Date(1990, 1, 6), FirstTime)
            )
        )
        assertEquals(
            false,
            mk_DTG(mk_Date(1990, 1, 7), FirstTime).functions.inRange(
                mk_DTG(mk_Date(1990, 1, 1), FirstTime),
                mk_DTG(mk_Date(1990, 1, 6), FirstTime)
            )
        )
        assertEquals(
            true,
            mk_DTG(mk_Date(1990, 1, 1), FirstTime).functions.inRange(
                mk_DTG(mk_Date(1990, 1, 1), FirstTime),
                mk_DTG(mk_Date(1990, 1, 6), FirstTime)
            )
        )
        assertEquals(
            false,
            mk_DTG(mk_Date(1990, 1, 3), FirstTime).functions.inRange(
                mk_DTG(mk_Date(1990, 1, 1), FirstTime),
                mk_DTG(mk_Date(1990, 1, 3), FirstTime)
            )
        )
    }

    @Test
    fun dtgInRangeTimeTest() {
        assertEquals(
            true,
            mk_DTG(FirstDate, mk_Time(2, 30, 0, 0)).functions.inRange(
                mk_DTG(FirstDate, mk_Time(2, 0, 0, 0)),
                mk_DTG(FirstDate, mk_Time(3, 0, 0, 0))
            )
        )
        assertEquals(
            false,
            mk_DTG(FirstDate, mk_Time(3, 30, 0, 0)).functions.inRange(
                mk_DTG(FirstDate, mk_Time(2, 0, 0, 0)),
                mk_DTG(FirstDate, mk_Time(3, 0, 0, 0))
            )
        )
        assertEquals(
            true,
            mk_DTG(FirstDate, mk_Time(2, 0, 0, 0)).functions.inRange(
                mk_DTG(FirstDate, mk_Time(2, 0, 0, 0)),
                mk_DTG(FirstDate, mk_Time(3, 0, 0, 0))
            )
        )
        assertEquals(
            false,
            mk_DTG(FirstDate, mk_Time(3, 0, 0, 0)).functions.inRange(
                mk_DTG(FirstDate, mk_Time(2, 0, 0, 0)),
                mk_DTG(FirstDate, mk_Time(3, 0, 0, 0))
            )
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun dtgWithinTest() {
        assertEquals(
            true,
            mk_DTG(mk_Date(1989, 1, 3), FirstTime).functions.within(
                Duration.fromDays(3), mk_DTG(mk_Date(1989, 1, 1), FirstTime)
            )
        )
        assertEquals(
            true,
            mk_DTG(mk_Date(1989, 12, 30), FirstTime).functions.within(
                Duration.fromDays(3),
                mk_DTG(mk_Date(1990, 1, 1), FirstTime)
            )
        )
        assertEquals(
            true,
            mk_DTG(mk_Date(1990, 1, 1), FirstTime).functions.within(
                Duration.fromDays(0), mk_DTG(mk_Date(1990, 1, 1), FirstTime)
            )
        )
        assertEquals(
            false,
            mk_DTG(mk_Date(1990, 1, 6), FirstTime).functions.within(
                Duration.fromDays(3), mk_DTG(mk_Date(1990, 1, 1), FirstTime)
            )
        )
        assertEquals(
            false,
            mk_DTG(mk_Date(1989, 12, 27), FirstTime).functions.within(
                Duration.fromDays(3), mk_DTG(mk_Date(1990, 1, 1), FirstTime)
            )
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun inIntervalTest() {
        assertEquals(
            true,
            mk_DTG(mk_Date(1990, 1, 3), FirstTime).functions.inInterval(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FirstTime),
                    mk_DTG(mk_Date(1990, 1, 6), FirstTime)
                )
            )
        )
        assertEquals(
            false,
            mk_DTG(mk_Date(1990, 1, 7), FirstTime).functions.inInterval(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FirstTime),
                    mk_DTG(mk_Date(1990, 1, 6), FirstTime)
                )
            )
        )
        assertEquals(
            true,
            mk_DTG(mk_Date(1990, 1, 1), FirstTime).functions.inInterval(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FirstTime),
                    mk_DTG(mk_Date(1990, 1, 6), FirstTime)
                )
            )

        )
        assertEquals(
            false,
            mk_DTG(mk_Date(1990, 1, 3), FirstTime).functions.inInterval(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FirstTime),
                    mk_DTG(mk_Date(1990, 1, 3), FirstTime)
                )
            )
        )
        assertEquals(
            true,
            mk_DTG(FirstDate, mk_Time(2, 30, 0, 0)).functions.inInterval(
                mk_Interval(
                    mk_DTG(FirstDate, mk_Time(2, 0, 0, 0)),
                    mk_DTG(FirstDate, mk_Time(3, 0, 0, 0))
                )
            )
        )
        assertEquals(
            false,
            mk_DTG(FirstDate, mk_Time(3, 30, 0, 0)).functions.inInterval(
                mk_Interval(
                    mk_DTG(FirstDate, mk_Time(2, 0, 0, 0)),
                    mk_DTG(FirstDate, mk_Time(3, 0, 0, 0))
                )
            )

        )
        assertEquals(
            true,
            mk_DTG(FirstDate, mk_Time(2, 0, 0, 0)).functions.inInterval(
                mk_Interval(
                    mk_DTG(FirstDate, mk_Time(2, 0, 0, 0)),
                    mk_DTG(FirstDate, mk_Time(3, 0, 0, 0))
                )
            )

        )
        assertEquals(
            false,
            mk_DTG(FirstDate, mk_Time(3, 0, 0, 0)).functions.inInterval(
                mk_Interval(
                    mk_DTG(FirstDate, mk_Time(2, 0, 0, 0)),
                    mk_DTG(FirstDate, mk_Time(3, 0, 0, 0))
                )
            )
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun overlapTest() {
        assertEquals(
            true,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 2), FirstTime),
                mk_DTG(mk_Date(1990, 1, 4), FirstTime)
            ).functions.overlap(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FirstTime),
                    mk_DTG(mk_Date(1990, 1, 3), FirstTime)
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), FirstTime),
                mk_DTG(mk_Date(1990, 1, 3), FirstTime)
            ).functions.overlap(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 2), FirstTime),
                    mk_DTG(mk_Date(1990, 1, 4), FirstTime)
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), FirstTime),
                mk_DTG(mk_Date(1990, 1, 6), FirstTime)
            ).functions.overlap(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 2), FirstTime),
                    mk_DTG(mk_Date(1990, 1, 4), FirstTime)
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), FirstTime),
                mk_DTG(mk_Date(1990, 1, 6), FirstTime)
            ).functions.overlap(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FirstTime),
                    mk_DTG(mk_Date(1990, 1, 6), FirstTime)
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), FirstTime),
                mk_DTG(mk_Date(1990, 1, 6), FirstTime)
            ).functions.overlap(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 6), FirstTime),
                    mk_DTG(mk_Date(1990, 1, 8), FirstTime)
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), FirstTime),
                mk_DTG(mk_Date(1990, 1, 6), FirstTime)
            ).functions.overlap(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 8), FirstTime),
                    mk_DTG(mk_Date(1990, 1, 10), FirstTime)
                )
            )
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun withinTest() {
        assertEquals(
            true,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 3), FirstTime),
                mk_DTG(mk_Date(1990, 1, 6), FirstTime)
            ).functions.within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FirstTime),
                    mk_DTG(mk_Date(1990, 1, 10), FirstTime)
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 12), FirstTime),
                mk_DTG(mk_Date(1990, 1, 14), FirstTime)
            ).functions.within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FirstTime),
                    mk_DTG(mk_Date(1990, 1, 10), FirstTime)
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 3), FirstTime),
                mk_DTG(mk_Date(1990, 1, 6), FirstTime)
            ).functions.within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 5), FirstTime),
                    mk_DTG(mk_Date(1990, 1, 10), FirstTime)
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 8), FirstTime),
                mk_DTG(mk_Date(1990, 1, 12), FirstTime)
            ).functions.within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 5), FirstTime),
                    mk_DTG(mk_Date(1990, 1, 10), FirstTime)
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 3), FirstTime),
                mk_DTG(mk_Date(1990, 1, 6), FirstTime)
            ).functions.within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FirstTime),
                    mk_DTG(mk_Date(1990, 1, 3), FirstTime)
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 3), FirstTime),
                mk_DTG(mk_Date(1990, 1, 6), FirstTime)
            ).functions.within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 3), FirstTime),
                    mk_DTG(mk_Date(1990, 1, 6), FirstTime)
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 3), FirstTime),
                mk_DTG(mk_Date(1990, 1, 6), FirstTime)
            ).functions.within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 2), FirstTime),
                    mk_DTG(mk_Date(1990, 1, 6), FirstTime)
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 2), FirstTime),
                mk_DTG(mk_Date(1990, 1, 5), FirstTime)
            ).functions.within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 2), FirstTime),
                    mk_DTG(mk_Date(1990, 1, 6), FirstTime)
                )
            )
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun addTest() {
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 5), FirstTime),
            mk_DTG(mk_Date(1990, 1, 2), FirstTime).functions.add(Duration.fromDays(3))
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 2), mk_Time(5, 20, 0, 0)),
            mk_DTG(mk_Date(1990, 1, 2), mk_Time(2, 0, 0, 0)).functions.add(
                Duration.fromHours(3).functions.add(Duration.fromMinutes(20))
            )
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 5), mk_Time(5, 20, 10, 5)),
            mk_DTG(mk_Date(1990, 1, 1), mk_Time(2, 0, 0, 0)).functions.add(
                Duration.fromDays(4).functions.add(
                    Duration.fromHours(3).functions.add(
                        Duration.fromMinutes(20).functions.add(
                            Duration.fromSeconds(10).functions.add(
                                Duration.fromMillis(5)
                            )
                        )
                    )
                )
            )
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun subtractTest() {
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 2), FirstTime),
            mk_DTG(mk_Date(1990, 1, 5), FirstTime).functions.subtract(
                Duration.fromDays(3)
            )
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 5), mk_Time(3, 20, 0, 0)),
            mk_DTG(mk_Date(1990, 1, 5), mk_Time(6, 40, 0, 0)).functions.subtract(
                Duration.fromHours(3).functions.add(Duration.fromMinutes(20))
            )
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 2), mk_Time(2, 20, 20, 5)),

            mk_DTG(mk_Date(1990, 1, 6), mk_Time(5, 40, 30, 10)).functions.subtract(
                Duration.fromDays(4).functions.add(
                    Duration.fromHours(3).functions.add(
                        Duration.fromMinutes(20).functions.add(
                            Duration.fromSeconds(10).functions.add(
                                Duration.fromMillis(5)
                            )
                        )
                    )
                )
            )
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun diffTest() {
        assertEquals(
            Duration.fromDays(5).functions.add(
                Duration.fromHours(5).functions.add(
                    Duration.fromMinutes(5).functions.add(
                        Duration.fromSeconds(5).functions.add(
                            Duration.fromMillis(5)
                        )
                    )
                )
            ),
            diff(
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 1, 1, 1)),
                mk_DTG(mk_Date(1990, 1, 6), mk_Time(6, 6, 6, 6))
            )
        )
        assertEquals(
            Duration.fromDays(5).functions.add(
                Duration.fromHours(5).functions.add(
                    Duration.fromMinutes(5).functions.add(
                        Duration.fromSeconds(5).functions.add(
                            Duration.fromMillis(5)
                        )
                    )
                )
            ),
            diff(
                mk_DTG(mk_Date(1990, 1, 6), mk_Time(6, 6, 6, 6)),
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 1, 1, 1))
            )
        )
        assertEquals(
            Duration.fromDays(5).functions.add(
                Duration.fromHours(5).functions.add(
                    Duration.fromMinutes(5).functions.add(
                        Duration.fromSeconds(5).functions.add(
                            Duration.fromMillis(5)
                        )
                    )
                )
            ),
            diff(
                mk_DTG(mk_Date(1990, 1, 31), mk_Time(1, 1, 1, 1)),
                mk_DTG(mk_Date(1990, 2, 5), mk_Time(6, 6, 6, 6))
            )
        )
        assertEquals(
            Duration.fromDays(5).functions.add(
                Duration.fromHours(5).functions.add(
                    Duration.fromMinutes(5).functions.add(
                        Duration.fromSeconds(5).functions.add(
                            Duration.fromMillis(5)
                        )
                    )
                )
            ),
            diff(
                mk_DTG(mk_Date(1990, 2, 5), mk_Time(6, 6, 6, 6)),
                mk_DTG(mk_Date(1990, 1, 31), mk_Time(1, 1, 1, 1))
            )
        )
    }

    @Test
    fun durAddTest() {
        assertEquals(
            Duration.fromDays(5),
            Duration.fromDays(2).functions.add(Duration.fromDays(3))
        )
        assertEquals(
            Duration.fromDays(2),
            Duration.fromDays(2).functions.add(Duration.fromDays(0))
        )
    }

    @Test
    fun durSubtractTest() {
        assertEquals(
            Duration.fromDays(5),
            Duration.fromDays(8).functions.subtract(Duration.fromDays(3))
        )
        assertFailsWith<PreconditionFailure> {
            Duration.fromDays(2).functions.subtract(
                Duration.fromDays(
                    3
                )
            )
        }
        assertEquals(
            Duration.fromDays(8),
            Duration.fromDays(8).functions.subtract(Duration.fromDays(0))
        )
        assertEquals(
            Duration.fromDays(0),
            Duration.fromDays(8).functions.subtract(Duration.fromDays(8))
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun durMultiplyTest() {
        assertEquals(Duration.fromDays(10), Duration.fromDays(2).functions.multiply(5))
        assertEquals(
            Duration.fromDays(1).functions.add(Duration.fromHours(1)),
            Duration.fromHours(5).functions.multiply(5)
        )
        assertEquals(
            Duration.fromDays(5000000),
            Duration.fromDays(1000000).functions.multiply(5)
        )
    }

    @Test
    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    fun durDivideTest() {
        assertEquals(Duration.fromDays(2), Duration.fromDays(10).functions.divide(5))
        assertEquals(Duration.fromHours(12), Duration.fromDays(10).functions.divide(20))
        assertEquals(
            Duration.fromDays(1000000),
            Duration.fromDays(5000000).functions.divide(5)
        )
    }

    @Test
    fun durDiffTest() {
        assertEquals(
            Duration.fromDays(5),
            durationDiff(Duration.fromDays(10), Duration.fromDays(5))
        )
        assertEquals(
            Duration.fromDays(5),
            durationDiff(Duration.fromDays(5), Duration.fromDays(10))
        )
        assertEquals(
            Duration.fromDays(0),
            durationDiff(Duration.fromDays(10), Duration.fromDays(10))
        )
        assertEquals(
            Duration.fromDays(1999999995),
            durationDiff(Duration.fromDays(2000000000), Duration.fromDays(5))
        )
        assertEquals(
            Duration.fromMillis(15),
            durationDiff(Duration.fromMillis(20), Duration.fromMillis(5))
        )
    }

    @Test
    fun toMillisTest() {
        assertEquals(12, mk_Duration(12).functions.toMillis())
        assertEquals(0, mk_Duration(0).functions.toMillis())
        assertEquals(2000000000, mk_Duration(2000000000).functions.toMillis())
    }

    @Test
    fun durFromMillisTest() {
        assertEquals(12, Duration.fromMillis(12).duration_ms)
        assertEquals(0, Duration.fromMillis(0).duration_ms)
        assertEquals(2000000000, Duration.fromMillis(2000000000).duration_ms)
    }

    @Test
    fun toSecondsTest() {
        assertEquals(12, mk_Duration(12000).functions.toSeconds())
        assertEquals(0, mk_Duration(0).functions.toSeconds())
        assertEquals(2000000, mk_Duration(2000000000).functions.toSeconds())
    }

    @Test
    fun durFromSecondsTest() {
        assertEquals(60000, Duration.fromSeconds(60).duration_ms)
        assertEquals(0, Duration.fromSeconds(0).duration_ms)
        assertEquals(2000000000, Duration.fromSeconds(2000000).duration_ms)
    }

    @Test
    fun toMinutesTest() {
        assertEquals(10, mk_Duration(600000).functions.toMinutes())
        assertEquals(0, mk_Duration(0).functions.toMinutes())
        assertEquals(2000, mk_Duration(120000000).functions.toMinutes())
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun durFromMinutesTest() {
        assertEquals(3600000, Duration.fromMinutes(60).duration_ms)
        assertEquals(0, Duration.fromMinutes(0).duration_ms)
        assertEquals(120000000000, Duration.fromMinutes(2000000).duration_ms)
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun durModMinutesTest() {
        assertEquals(
            Duration.fromSeconds(10),
            Duration.fromMinutes(5).functions.add(Duration.fromSeconds(10)).functions.modMinutes()
        )
        assertEquals(
            Duration.fromSeconds(10),
            Duration.fromMinutes(0).functions.add(Duration.fromSeconds(10)).functions.modMinutes()
        )
        assertEquals(
            Duration.fromSeconds(10),
            Duration.fromMinutes(2000000).functions.add(Duration.fromSeconds(10)).functions.modMinutes()
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun toHoursTest() {
        assertEquals(10, mk_Duration(36000000).functions.toHours())
        assertEquals(0, mk_Duration(0).functions.toHours())
        assertEquals(200, mk_Duration(720000000).functions.toHours())
        assertEquals(876600, Duration.durationUpToYear(100).functions.toHours())
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun durFromHoursTest() {
        assertEquals(216000000, Duration.fromHours(60).duration_ms)
        assertEquals(0, Duration.fromHours(0).duration_ms)
        assertEquals(7200000000, Duration.fromHours(2000).duration_ms)
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun durModHoursTest() {
        assertEquals(
            Duration.fromSeconds(10),
            Duration.fromHours(5).functions.add(Duration.fromSeconds(10)).functions.modHours()
        )
        assertEquals(
            Duration.fromSeconds(10),
            Duration.fromHours(0).functions.add(Duration.fromSeconds(10)).functions.modHours()
        )
        assertEquals(
            Duration.fromSeconds(10),
            Duration.fromHours(2000000).functions.add(Duration.fromSeconds(10)).functions.modHours()
        )
    }

    @Test
    fun toDaysTest() {
        assertEquals(10, mk_Duration(864000000).functions.toDays())
        assertEquals(9, mk_Duration(863999999).functions.toDays())
        assertEquals(0, mk_Duration(0).functions.toDays())
        assertEquals(20, mk_Duration(1728000000).functions.toDays())
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun durFromDaysTest() {
        assertEquals(864000000, Duration.fromDays(10).duration_ms)
        assertEquals(0, Duration.fromDays(0).duration_ms)
        assertEquals(172800000000, Duration.fromDays(2000).duration_ms)

    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun dateToDayOfWeekTest() {
        assertEquals(
            DayOfWeek.Thursday,
            mk_Date(2009, 8, 13).functions.toDayOfWeek()
        )
        assertEquals(
            DayOfWeek.Saturday,
            mk_Date(2000, 4, 1).functions.toDayOfWeek()
        )
        assertEquals(
            DayOfWeek.Monday,
            mk_Date(1, 1, 1).functions.toDayOfWeek()
        )
        assertEquals(
            DayOfWeek.Wednesday,
            mk_Date(2019, 10, 9).functions.toDayOfWeek()
        )
        assertEquals(
            DayOfWeek.Saturday,
            mk_Date(2017, 10, 28).functions.toDayOfWeek()
        )
        assertEquals(
            DayOfWeek.Wednesday,
            mk_Date(2020, 1, 1).functions.toDayOfWeek()
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun durModDaysTest() {
        assertEquals(
            Duration.fromSeconds(10),
            Duration.fromDays(5).functions.add(Duration.fromSeconds(10)).functions.modDays()
        )
        assertEquals(
            Duration.fromSeconds(10),
            Duration.fromDays(0).functions.add(Duration.fromSeconds(10)).functions.modDays()
        )
        assertEquals(
            Duration.fromSeconds(10),
            Duration.fromDays(200000).functions.add(Duration.fromSeconds(10)).functions.modDays()
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun toMonthTest() {
        assertEquals(0, Duration.fromDays(30).functions.toMonth(1990))
        assertEquals(1, Duration.fromDays(31).functions.toMonth(1990))
        assertEquals(0, Duration.fromDays(0).functions.toMonth(1990))
        assertEquals(11, Duration.fromDays(364).functions.toMonth(1990))
    }

    @Test
    fun durFromMonthTest() {
        assertEquals(Duration.fromDays(31), Duration.fromMonth(1990, 1))
        assertEquals(Duration.fromDays(28), Duration.fromMonth(1990, 2))
        assertEquals(Duration.fromDays(30), Duration.fromMonth(1990, 9))
    }

    @Test
    fun durUpToMonthTest() {
        assertEquals(Duration.fromDays(90), Duration.durationUpToMonth(1990, 4))
        assertEquals(Duration.fromDays(59), Duration.durationUpToMonth(1990, 3))
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun toYearTest() {
        assertEquals(0, Duration.fromDays(0).functions.toYear(1990))
        assertEquals(2, Duration.fromDays(800).functions.toYear(1990))
    }

    @Test
    fun durFromYearTest() {
        assertEquals(Duration.fromDays(365), Duration.fromYear(1990))
        assertEquals(Duration.fromDays(366), Duration.fromYear(2020))
    }

    @Test
    fun durUpToYearTest() {
        assertEquals(Duration.fromDays(366), Duration.durationUpToYear(1))
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun toDTGTest() {
        assertEquals(
            mk_DTG(mk_Date(0, 1, 6), mk_Time(0, 0, 0, 0)),
            Duration.fromDays(5).functions.toDTG()
        )
        assertEquals(
            mk_DTG(mk_Date(0, 1, 1), mk_Time(0, 0, 0, 0)),
            Duration.fromDays(0).functions.toDTG()
        )
        assertEquals(
            mk_DTG(mk_Date(0, 2, 7), mk_Time(0, 0, 0, 0)),
            Duration.fromDays(37).functions.toDTG()
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun dtgToDurTest() {
        assertEquals(
            Duration.fromDays(6),

            mk_DTG(mk_Date(0, 1, 7), mk_Time(0, 0, 0, 0)).functions.toDuration()

        )
        assertEquals(
            Duration.fromDays(0),
            mk_DTG(mk_Date(0, 1, 1), mk_Time(0, 0, 0, 0)).functions.toDuration()
        )

        assertEquals(
            Duration.fromDays(37),
            mk_DTG(mk_Date(0, 2, 7), mk_Time(0, 0, 0, 0)).functions.toDuration()
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun toDateTest() {
        assertEquals(mk_Date(0, 1, 4), Duration.fromDays(3).functions.toDate())
        assertEquals(mk_Date(0, 1, 1), Duration.fromDays(0).functions.toDate())
    }

    @Test
    fun durFromDateTest() {
        assertEquals(Duration.fromDays(3), mk_Date(0, 1, 4).functions.toDuration())
        assertEquals(Duration.fromDays(0), mk_Date(0, 1, 1).functions.toDuration())
    }

    @Test
    fun toTimeTest() {
        assertEquals(mk_Time(3, 0, 0, 0), Duration.fromHours(3).functions.toTime())
        assertEquals(mk_Time(0, 0, 0, 0), Duration.fromHours(0).functions.toTime())
    }

    @Test
    fun durFromTimeTest() {
        assertEquals(Duration.fromHours(4), mk_Time(4, 0, 0, 0).functions.toDuration())
        assertEquals(Duration.fromHours(0), mk_Time(0, 0, 0, 0).functions.toDuration())
    }

    @Test
    fun durFromTimeInZoneTest() {
        assertEquals(
            Duration.fromHours(4),
            mk_TimeInZone(
                mk_Time(3, 0, 0, 0),
                mk_Offset(Duration.fromHours(1), PlusOrMinus.Minus)
            ).functions.toDuration()
        )
        assertEquals(
            Duration.fromHours(2),
            mk_TimeInZone(
                mk_Time(3, 0, 0, 0),
                mk_Offset(Duration.fromHours(1), PlusOrMinus.Plus)
            ).functions.toDuration()
        )
        assertEquals(
            Duration.fromHours(0),
            mk_TimeInZone(
                mk_Time(0, 0, 0, 0),
                mk_Offset(Duration.fromHours(0), PlusOrMinus.None)
            ).functions.toDuration()
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun durFromIntervalTest() {
        assertEquals(
            Duration.fromDays(5),
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), FirstTime),
                mk_DTG(mk_Date(1990, 1, 6), FirstTime)
            ).functions.toDuration()
        )
        assertEquals(
            Duration.fromHours(2),
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(3, 0, 0, 0))
            ).functions.toDuration()
        )
    }

    @Test
    fun finestGranularityTest() {
        assertEquals(
            true,
            mk_DTG(mk_Date(0, 1, 1), mk_Time(10, 0, 0, 0)).functions.finestGranularity(
                Duration.fromHours(1)
            )
        )
        assertEquals(
            false,
            mk_DTG(mk_Date(0, 1, 1), mk_Time(10, 0, 0, 0)).functions.finestGranularity(
                Duration.fromHours(3)
            )
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun finestGranularityITest() {
        assertEquals(
            true,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(3, 0, 0, 0))
            ).functions.finestGranularityI(
                Duration.fromHours(1)

            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(10, 0, 0, 0))
            ).functions.finestGranularityI(
                Duration.fromHours(2)
            )
        )

    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun minDTGTest() {
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
            minDTG(
                mk_Set1(
                    mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
                    mk_DTG(mk_Date(1990, 1, 6), mk_Time(1, 0, 0, 0)),
                    mk_DTG(mk_Date(1990, 2, 1), mk_Time(1, 0, 0, 0)),
                    mk_DTG(mk_Date(1990, 1, 14), mk_Time(1, 0, 0, 0))
                )
            )
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 1), mk_Time(0, 10, 0, 0)),
            minDTG(
                mk_Set1(
                    mk_DTG(mk_Date(1990, 1, 1), mk_Time(0, 10, 0, 0)),
                    mk_DTG(mk_Date(1990, 1, 1), mk_Time(14, 0, 0, 0)),
                    mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 0, 50, 0)),
                    mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 30, 0, 0))
                )
            )
        )
        assertEquals(
            mk_DTG(mk_Date(0, 1, 1), mk_Time(1, 0, 0, 0)),
            minDTG(
                mk_Set1(
                    mk_DTG(mk_Date(0, 1, 1), mk_Time(1, 0, 0, 0)),
                    mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
                    mk_DTG(mk_Date(300, 1, 1), mk_Time(1, 0, 0, 0)),
                    mk_DTG(mk_Date(3000, 1, 1), mk_Time(1, 0, 0, 0))
                )
            )
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun maxDTGTest() {
        assertEquals(
            mk_DTG(mk_Date(1990, 2, 1), mk_Time(1, 0, 0, 0)),
            maxDTG(
                mk_Set1(
                    mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
                    mk_DTG(mk_Date(1990, 1, 6), mk_Time(1, 0, 0, 0)),
                    mk_DTG(mk_Date(1990, 2, 1), mk_Time(1, 0, 0, 0)),
                    mk_DTG(mk_Date(1990, 1, 14), mk_Time(1, 0, 0, 0))
                )
            )
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 1), mk_Time(14, 0, 0, 0)),
            maxDTG(
                mk_Set1(
                    mk_DTG(mk_Date(1990, 1, 1), mk_Time(0, 10, 0, 0)),
                    mk_DTG(mk_Date(1990, 1, 1), mk_Time(14, 0, 0, 0)),
                    mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 0, 50, 0)),
                    mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 30, 0, 0))
                )
            )
        )
        assertEquals(
            mk_DTG(mk_Date(3000, 1, 1), mk_Time(1, 0, 0, 0)),
            maxDTG(
                mk_Set1(
                    mk_DTG(mk_Date(0, 1, 1), mk_Time(1, 0, 0, 0)),
                    mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
                    mk_DTG(mk_Date(300, 1, 1), mk_Time(1, 0, 0, 0)),
                    mk_DTG(mk_Date(3000, 1, 1), mk_Time(1, 0, 0, 0))
                )
            )

        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun minDateTest() {
        assertEquals(
            mk_Date(1990, 1, 4),
            minDate(
                mk_Set1(
                    mk_Date(1990, 4, 1),
                    mk_Date(1990, 1, 4),
                    mk_Date(1990, 1, 31),
                    mk_Date(1990, 12, 1)
                )
            )
        )
        assertEquals(
            mk_Date(0, 1, 1),
            minDate(
                mk_Set1(
                    mk_Date(0, 1, 1),
                    mk_Date(1990, 1, 1),
                    mk_Date(300, 1, 1),
                    mk_Date(9999, 1, 1)
                )
            )
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun maxDateTest() {
        assertEquals(
            mk_Date(1990, 12, 1),
            maxDate(
                mk_Set1(
                    mk_Date(1990, 4, 1),
                    mk_Date(1990, 1, 4),
                    mk_Date(1990, 1, 31),
                    mk_Date(1990, 12, 1)
                )
            )
        )
        assertEquals(
            mk_Date(3000, 1, 1),
            maxDate(
                mk_Set1(
                    mk_Date(0, 1, 1),
                    mk_Date(1990, 1, 1),
                    mk_Date(300, 1, 1),
                    mk_Date(3000, 1, 1)
                )
            )
        )
    }

    @Test
    fun minTimeTest() {
        assertEquals(
            mk_Time(0, 0, 12, 1),
            minTime(
                mk_Set1(
                    mk_Time(1, 0, 4, 1),
                    mk_Time(0, 20, 1, 4),
                    mk_Time(3, 0, 1, 31),
                    mk_Time(0, 0, 12, 1)
                )
            )
        )
        assertEquals(
            mk_Time(0, 0, 0, 0),
            minTime(
                mk_Set1(
                    mk_Time(0, 0, 0, 0),
                    mk_Time(23, 0, 1, 1),
                    mk_Time(0, 59, 1, 1),
                    mk_Time(0, 0, 59, 1)
                )
            )
        )
    }

    @Test
    fun maxTimeTest() {
        assertEquals(
            mk_Time(3, 0, 1, 31),
            maxTime(
                mk_Set1(
                    mk_Time(1, 0, 4, 1),
                    mk_Time(0, 20, 1, 4),
                    mk_Time(3, 0, 1, 31),
                    mk_Time(0, 0, 12, 1)
                )
            )
        )
        assertEquals(
            mk_Time(23, 0, 1, 1),
            maxTime(
                mk_Set1(
                    mk_Time(0, 0, 1, 1),
                    mk_Time(23, 0, 1, 1),
                    mk_Time(0, 59, 1, 1),
                    mk_Time(0, 0, 59, 1)
                )
            )

        )
    }

    @Test
    fun minDurationTest() {
        assertEquals(
            Duration.fromMinutes(40),
            minDuration(
                mk_Set1(
                    Duration.fromHours(3),
                    Duration.fromMinutes(40),
                    Duration.fromDays(2),
                    Duration.fromHours(5)
                )
            )
        )
        assertEquals(
            Duration.fromHours(0),
            minDuration(
                mk_Set1(
                    Duration.fromHours(0),
                    Duration.fromMinutes(14),
                    Duration.fromDays(2),
                    Duration.fromHours(23)
                )
            )
        )
    }

    @Test
    fun maxDurationTest() {
        assertEquals(
            Duration.fromDays(2),
            maxDuration(
                mk_Set1(
                    Duration.fromHours(3),
                    Duration.fromMinutes(40),
                    Duration.fromDays(2),
                    Duration.fromHours(5)
                )
            )
        )
        assertEquals(
            Duration.fromDays(2000000000),
            maxDuration(
                mk_Set1(
                    Duration.fromHours(0),
                    Duration.fromMinutes(14),
                    Duration.fromDays(2000000000),
                    Duration.fromHours(23)
                )
            )
        )
    }

    @Test
    fun sumDurationTest() {
        assertEquals(
            mk_Duration(204000000),
            sumDuration(
                mk_Seq(
                    Duration.fromHours(3),
                    Duration.fromMinutes(40),
                    Duration.fromDays(2),
                    Duration.fromHours(5)
                )
            )
        )
        assertEquals(
            mk_Duration(0),
            sumDuration(
                mk_Seq(
                    Duration.fromHours(0),
                    Duration.fromMinutes(0),
                    Duration.fromDays(0)
                )
            )
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun instantTest() {
        assertEquals(
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 1))
            ),
            mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)).functions.instant()
        )
        assertEquals(
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(23, 59, 59, 999)),
                mk_DTG(mk_Date(1990, 1, 2), mk_Time(0, 0, 0, 0))
            ),
            mk_DTG(mk_Date(1990, 1, 1), mk_Time(23, 59, 59, 999)).functions.instant()
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun nextDateForYMTest() {
        assertEquals(mk_Date(1990, 2, 1), nextDateForYM(mk_Date(1990, 1, 1)))
        assertEquals(mk_Date(1990, 3, 31), nextDateForYM(mk_Date(1990, 1, 31)))
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun nextDateForDayTest() {
        assertEquals(mk_Date(0, 1, 4), nextDateForDay(mk_Date(0, 1, 1), 4))
        assertEquals(mk_Date(1990, 3, 31), nextDateForDay(mk_Date(1990, 1, 31), 31))
        assertEquals(mk_Date(1, 1, 14), nextDateForDay(mk_Date(0, 12, 15), 14))
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun previousDateForYMTest() {
        assertEquals(mk_Date(1990, 1, 3), previousDateForYM(mk_Date(1990, 2, 3)))
        assertEquals(mk_Date(1989, 12, 3), previousDateForYM(mk_Date(1990, 1, 3)))
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun previousDateForDayTest() {
        assertEquals(mk_Date(1990, 1, 12), previousDateForDay(mk_Date(1990, 1, 31), 12))
        assertEquals(mk_Date(1989, 12, 12), previousDateForDay(mk_Date(1990, 1, 4), 12))
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun normaliseTest() {
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 1), mk_Time(3, 0, 0, 0)),
            mk_DtgInZone(
                mk_Date(1990, 1, 1),
                mk_TimeInZone(
                    mk_Time(5, 0, 0, 0),
                    mk_Offset(Duration.fromHours(2), PlusOrMinus.Plus)
                )
            ).functions.normalise()
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 1), mk_Time(7, 0, 0, 0)),
            mk_DtgInZone(
                mk_Date(1990, 1, 1),
                mk_TimeInZone(
                    mk_Time(5, 0, 0, 0),
                    mk_Offset(Duration.fromHours(2), PlusOrMinus.Minus)
                )
            ).functions.normalise()
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 1), mk_Time(5, 0, 0, 0)),
            mk_DtgInZone(
                mk_Date(1990, 1, 1),
                mk_TimeInZone(
                    mk_Time(5, 0, 0, 0),
                    mk_Offset(Duration.fromHours(0), PlusOrMinus.Minus)
                )
            ).functions.normalise()
        )
    }

    @Test
    fun normaliseTimeTest() {
        assertEquals(
            mk_(mk_Time(7, 23, 12, 0), PlusOrMinus.None),
            mk_TimeInZone(
                mk_Time(5, 23, 12, 0),
                mk_Offset(Duration.fromHours(2), PlusOrMinus.Minus)
            ).functions.normalise()
        )
        assertEquals(
            mk_(mk_Time(1, 23, 12, 0), PlusOrMinus.Minus),
            mk_TimeInZone(
                mk_Time(23, 23, 12, 0),
                mk_Offset(Duration.fromHours(2), PlusOrMinus.Minus)
            ).functions.normalise()
        )
        assertEquals(
            mk_(mk_Time(23, 23, 12, 0), PlusOrMinus.None),
            mk_TimeInZone(
                mk_Time(23, 23, 12, 0),
                mk_Offset(Duration.fromHours(0), PlusOrMinus.Minus)
            ).functions.normalise()
        )
        assertEquals(
            mk_(mk_Time(3, 23, 12, 0), PlusOrMinus.None),
            mk_TimeInZone(
                mk_Time(5, 23, 12, 0),
                mk_Offset(Duration.fromHours(2), PlusOrMinus.Plus)
            ).functions.normalise()
        )
        assertEquals(
            mk_(mk_Time(23, 23, 12, 0), PlusOrMinus.Plus),
            mk_TimeInZone(
                mk_Time(1, 23, 12, 0),
                mk_Offset(Duration.fromHours(2), PlusOrMinus.Plus)
            ).functions.normalise()
        )

    }

    @Test
    fun formatDGTTest() {
        assertEquals(
            "1990-01-01T03:00:00",
            mk_DTG(mk_Date(1990, 1, 1), mk_Time(3, 0, 0, 0)).functions.format()
        )
        assertEquals(
            "1990-01-01T03:00:00",
            mk_DTG(mk_Date(1990, 1, 1), mk_Time(3, 0, 0, 0)).functions.format()
        )
        assertEquals(
            "0000-01-01T03:00:00",
            mk_DTG(mk_Date(0, 1, 1), mk_Time(3, 0, 0, 0)).functions.format()
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun formatDtgInZoneTest() {
        assertEquals(
            "1990-01-01T03:00:00+02:00",
            mk_DtgInZone(
                mk_Date(1990, 1, 1),
                mk_TimeInZone(
                    mk_Time(3, 0, 0, 0),
                    mk_Offset(Duration.fromHours(2), PlusOrMinus.Plus)
                )
            ).functions.format()
        )
        assertEquals(
            "1990-01-01T03:00:00-02:00",
            mk_DtgInZone(
                mk_Date(1990, 1, 1),
                mk_TimeInZone(
                    mk_Time(3, 0, 0, 0),
                    mk_Offset(Duration.fromHours(2), PlusOrMinus.Minus)
                )
            ).functions.format()
        )
        assertEquals(
            "1990-01-01T03:00:00Z",
            mk_DtgInZone(
                mk_Date(1990, 1, 1),
                mk_TimeInZone(
                    mk_Time(3, 0, 0, 0),
                    mk_Offset(Duration.fromHours(0), PlusOrMinus.None)
                )
            ).functions.format()
        )
    }

    @Test
    fun formatDateTest() {
        assertEquals("1990-01-01", mk_Date(1990, 1, 1).functions.format())
        assertEquals("0000-01-01", mk_Date(0, 1, 1).functions.format())
    }

    @Test
    fun formatTimeTest() {
        assertEquals("10:07:14", mk_Time(10, 7, 14, 0).functions.format())
        assertEquals("00:00:00", mk_Time(0, 0, 0, 0).functions.format())
        assertEquals("00:00:00.050", mk_Time(0, 0, 0, 50).functions.format())
    }

    @Test
    fun formatTimeInZoneTest() {
        assertEquals(
            "03:00:00+02:00",
            mk_TimeInZone(
                mk_Time(3, 0, 0, 0),
                mk_Offset(Duration.fromHours(2), PlusOrMinus.Plus)
            ).functions.format()
        )
        assertEquals(
            "03:00:00-02:00",
            mk_TimeInZone(
                mk_Time(3, 0, 0, 0),
                mk_Offset(Duration.fromHours(2), PlusOrMinus.Minus)
            ).functions.format()
        )
        assertEquals(
            "03:00:00Z",
            mk_TimeInZone(
                mk_Time(3, 0, 0, 0),
                mk_Offset(Duration.fromHours(0), PlusOrMinus.None)
            ).functions.format()
        )
    }

    @Test
    fun formatIntervalTest() {
        assertEquals(
            "1990-01-01T00:00:00/1990-01-06T00:00:00",
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), FirstTime),
                mk_DTG(mk_Date(1990, 1, 6), FirstTime)
            ).functions.format()
        )
        assertEquals(
            "1990-01-01T00:00:00/1990-01-06T05:00:00",
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(0, 0, 0, 0)),
                mk_DTG(mk_Date(1990, 1, 6), mk_Time(5, 0, 0, 0))
            ).functions.format()
        )
    }

    @Test
    fun formatDurationTest() {
        assertEquals(
            "P2DT6H",
            Duration.fromHours(6).functions.add(Duration.fromDays(2)).functions.format()
        )
        assertEquals("PT0S", Duration.fromHours(0).functions.format())
        assertEquals(
            "PT1.001S",
            Duration.fromSeconds(1).functions.add(Duration.fromMillis(1)).functions.format()
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun addMonthsTest() {
        assertEquals(
            mk_DTG(mk_Date(1990, 3, 31), FirstTime),
            mk_DTG(mk_Date(1990, 1, 31), FirstTime).functions.addMonths(2)
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 3, 28), mk_Time(3, 0, 0, 0)),
            mk_DTG(mk_Date(1990, 1, 31), mk_Time(3, 0, 0, 0)).functions.addMonths(1).functions.addMonths(1)
        )
        assertEquals(
            mk_DTG(mk_Date(1991, 1, 30), FirstTime),
            mk_DTG(mk_Date(1990, 11, 30), FirstTime).functions.addMonths(2)
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun subtractMonthsTest() {
        assertEquals(
            mk_DTG(mk_Date(1990, 2, 2), FirstTime),
            mk_DTG(mk_Date(1990, 4, 2), FirstTime).functions.subtractMonths(2)
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 28), mk_Time(3, 0, 0, 0)),
            (mk_DTG(mk_Date(1990, 3, 31), mk_Time(3, 0, 0, 0))).functions.subtractMonths(1).functions.subtractMonths(1)
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 11, 2), FirstTime),
            mk_DTG(mk_Date(1991, 1, 2), FirstTime).functions.subtractMonths(2)
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun monthsBetweenTest() {
        assertEquals(
            0,
            monthsBetween(
                mk_DTG(mk_Date(1990, 1, 1), FirstTime),
                mk_DTG(mk_Date(1990, 1, 1), FirstTime)
            )
        )
        assertEquals(
            0,
            monthsBetween(
                mk_DTG(mk_Date(1990, 1, 12), FirstTime),
                mk_DTG(mk_Date(1990, 2, 1), FirstTime)
            )
        )
        assertEquals(
            6,
            monthsBetween(
                mk_DTG(mk_Date(1990, 12, 12), FirstTime),
                mk_DTG(mk_Date(1991, 6, 13), FirstTime)
            )
        )
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun yearsBetweenTest() {
        assertEquals(
            0,
            yearsBetween(
                mk_DTG(mk_Date(1990, 1, 1), FirstTime),
                mk_DTG(mk_Date(1990, 1, 1), FirstTime)
            )
        )
        assertEquals(
            0,
            yearsBetween(
                mk_DTG(mk_Date(1990, 1, 12), FirstTime),
                mk_DTG(mk_Date(1990, 12, 1), FirstTime)
            )
        )
        assertEquals(
            2,
            yearsBetween(
                mk_DTG(mk_Date(1990, 1, 12), FirstTime),
                mk_DTG(mk_Date(1992, 3, 13), FirstTime)
            )

        )
    }

    @Test
    fun isDateTest() {
        assertEquals(true, isDate("2018-04-01"))
        assertEquals(false, isDate("2018/04/01"))
    }

    // Fails due to value overflowing because nat is based on int, not long
    @Ignore
    @Test
    fun strToDateTest() {
        assertEquals(mk_Date(2018, 4, 1), strToDate("2018-04-01"))
    }

    @Test
    fun isDTGTest() {
        assertEquals(true, isDTG("1990-01-01T00:00:00"))
        assertEquals(true, isDTG("1990-01-01T00:00:00.000"))
        assertEquals(false, isDTG("1990-01-01!00:00:00"))
        assertEquals(false, isDTG("1990-01-01T00:00:00.FFF"))
    }

    @Test
    fun strToDTGTest() {
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 1), mk_Time(12, 23, 0, 0)),
            strToDTG("1990-01-01T12:23:00")
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 1), mk_Time(12, 23, 0, 1)),
            strToDTG("1990-01-01T12:23:00.001")
        )
    }
}

