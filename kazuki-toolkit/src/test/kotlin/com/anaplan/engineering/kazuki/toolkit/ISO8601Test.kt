package com.anaplan.engineering.kazuki.toolkit

import com.anaplan.engineering.kazuki.core.PreconditionFailure
import com.anaplan.engineering.kazuki.core.mk_
import com.anaplan.engineering.kazuki.core.mk_Seq
import com.anaplan.engineering.kazuki.core.mk_Set1
import com.anaplan.engineering.kazuki.toolkit.ISO8601.FIRST_DATE
import com.anaplan.engineering.kazuki.toolkit.ISO8601.FIRST_TIME
import com.anaplan.engineering.kazuki.toolkit.ISO8601.daysInMonth
import com.anaplan.engineering.kazuki.toolkit.ISO8601.daysInYear
import com.anaplan.engineering.kazuki.toolkit.ISO8601.diff
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durDiff
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durFromDays
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durFromHours
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durFromMillis
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durFromMinutes
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durFromMonth
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durFromSeconds
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durFromYear
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durUpToMonth
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durUpToYear
import com.anaplan.engineering.kazuki.toolkit.ISO8601.isDate
import com.anaplan.engineering.kazuki.toolkit.ISO8601.isLeap
import com.anaplan.engineering.kazuki.toolkit.ISO8601.maxDTG
import com.anaplan.engineering.kazuki.toolkit.ISO8601.maxDate
import com.anaplan.engineering.kazuki.toolkit.ISO8601.maxDuration
import com.anaplan.engineering.kazuki.toolkit.ISO8601.maxTime
import com.anaplan.engineering.kazuki.toolkit.ISO8601.minDTG
import com.anaplan.engineering.kazuki.toolkit.ISO8601.minDate
import com.anaplan.engineering.kazuki.toolkit.ISO8601.minDuration
import com.anaplan.engineering.kazuki.toolkit.ISO8601.minTime
import com.anaplan.engineering.kazuki.toolkit.ISO8601.monthsBetween
import com.anaplan.engineering.kazuki.toolkit.ISO8601.nextDateForDay
import com.anaplan.engineering.kazuki.toolkit.ISO8601.nextDateForYM
import com.anaplan.engineering.kazuki.toolkit.ISO8601.previousDateForDay
import com.anaplan.engineering.kazuki.toolkit.ISO8601.previousDateForYM
import com.anaplan.engineering.kazuki.toolkit.ISO8601.sumDuration
import com.anaplan.engineering.kazuki.toolkit.ISO8601.yearsBetween
import com.anaplan.engineering.kazuki.toolkit.ISO8601_Module.mk_DTG
import com.anaplan.engineering.kazuki.toolkit.ISO8601_Module.mk_DTGInZone
import com.anaplan.engineering.kazuki.toolkit.ISO8601_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.ISO8601_Module.mk_Duration
import com.anaplan.engineering.kazuki.toolkit.ISO8601_Module.mk_Interval
import com.anaplan.engineering.kazuki.toolkit.ISO8601_Module.mk_Offset
import com.anaplan.engineering.kazuki.toolkit.ISO8601_Module.mk_Time
import com.anaplan.engineering.kazuki.toolkit.ISO8601_Module.mk_TimeInZone
import junit.framework.TestCase.assertEquals
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

    @Test
    fun dtgInRangeDayTest() {
        assertEquals(
            true,
            mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME).functions.inRange(
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
            )
        )
        assertEquals(
            false,
            mk_DTG(mk_Date(1990, 1, 7), FIRST_TIME).functions.inRange(
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
            )
        )
        assertEquals(
            true,
            mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME).functions.inRange(
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
            )
        )
        assertEquals(
            false,
            mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME).functions.inRange(
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME)
            )
        )
    }

    @Test
    fun dtgInRangeTimeTest() {
        assertEquals(
            true,
            mk_DTG(FIRST_DATE, mk_Time(2, 30, 0, 0)).functions.inRange(
                mk_DTG(FIRST_DATE, mk_Time(2, 0, 0, 0)),
                mk_DTG(FIRST_DATE, mk_Time(3, 0, 0, 0))
            )
        )
        assertEquals(
            false,
            mk_DTG(FIRST_DATE, mk_Time(3, 30, 0, 0)).functions.inRange(
                mk_DTG(FIRST_DATE, mk_Time(2, 0, 0, 0)),
                mk_DTG(FIRST_DATE, mk_Time(3, 0, 0, 0))
            )
        )
        assertEquals(
            true,
            mk_DTG(FIRST_DATE, mk_Time(2, 0, 0, 0)).functions.inRange(
                mk_DTG(FIRST_DATE, mk_Time(2, 0, 0, 0)),
                mk_DTG(FIRST_DATE, mk_Time(3, 0, 0, 0))
            )
        )
        assertEquals(
            false,
            mk_DTG(FIRST_DATE, mk_Time(3, 0, 0, 0)).functions.inRange(
                mk_DTG(FIRST_DATE, mk_Time(2, 0, 0, 0)),
                mk_DTG(FIRST_DATE, mk_Time(3, 0, 0, 0))
            )
        )
    }


    @Test
    fun dtgWithinTest() {
        assertEquals(
            true,
            mk_DTG(mk_Date(1989, 1, 3), FIRST_TIME).functions.within(
                durFromDays(3), mk_DTG(mk_Date(1989, 1, 1), FIRST_TIME)
            )
        )
        assertEquals(
            true,
            mk_DTG(mk_Date(1989, 12, 30), FIRST_TIME).functions.within(
                durFromDays(3),
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME)
            )
        )
        assertEquals(
            true,
            mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME).functions.within(
                durFromDays(0), mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME)
            )
        )
        assertEquals(
            false,
            mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME).functions.within(
                durFromDays(3), mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME)
            )
        )
        assertEquals(
            false,
            mk_DTG(mk_Date(1989, 12, 27), FIRST_TIME).functions.within(
                durFromDays(3), mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME)
            )
        )
    }

    @Test
    fun inIntervalTest() {
        assertEquals(
            true,
            mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME).functions.inInterval(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                )
            )
        )
        assertEquals(
            false,
            mk_DTG(mk_Date(1990, 1, 7), FIRST_TIME).functions.inInterval(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                )
            )
        )
        assertEquals(
            true,
            mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME).functions.inInterval(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                )
            )

        )
        assertEquals(
            false,
            mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME).functions.inInterval(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME)
                )
            )
        )
        assertEquals(
            true,
            mk_DTG(FIRST_DATE, mk_Time(2, 30, 0, 0)).functions.inInterval(
                mk_Interval(
                    mk_DTG(FIRST_DATE, mk_Time(2, 0, 0, 0)),
                    mk_DTG(FIRST_DATE, mk_Time(3, 0, 0, 0))
                )
            )
        )
        assertEquals(
            false,
            mk_DTG(FIRST_DATE, mk_Time(3, 30, 0, 0)).functions.inInterval(
                mk_Interval(
                    mk_DTG(FIRST_DATE, mk_Time(2, 0, 0, 0)),
                    mk_DTG(FIRST_DATE, mk_Time(3, 0, 0, 0))
                )
            )

        )
        assertEquals(
            true,
            mk_DTG(FIRST_DATE, mk_Time(2, 0, 0, 0)).functions.inInterval(
                mk_Interval(
                    mk_DTG(FIRST_DATE, mk_Time(2, 0, 0, 0)),
                    mk_DTG(FIRST_DATE, mk_Time(3, 0, 0, 0))
                )
            )

        )
        assertEquals(
            false,
            mk_DTG(FIRST_DATE, mk_Time(3, 0, 0, 0)).functions.inInterval(
                mk_Interval(
                    mk_DTG(FIRST_DATE, mk_Time(2, 0, 0, 0)),
                    mk_DTG(FIRST_DATE, mk_Time(3, 0, 0, 0))
                )
            )
        )
    }

    @Test
    fun overlapTest() {
        assertEquals(
            true,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 2), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 4), FIRST_TIME)
            ).functions.overlap(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME)
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME)
            ).functions.overlap(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 2), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 4), FIRST_TIME)
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
            ).functions.overlap(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 2), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 4), FIRST_TIME)
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
            ).functions.overlap(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
            ).functions.overlap(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 8), FIRST_TIME)
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
            ).functions.overlap(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 8), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 10), FIRST_TIME)
                )
            )
        )
    }

    @Test
    fun withinTest() {
        assertEquals(
            true,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
            ).functions.within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 10), FIRST_TIME)
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 12), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 14), FIRST_TIME)
            ).functions.within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 10), FIRST_TIME)
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
            ).functions.within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 5), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 10), FIRST_TIME)
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 8), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 12), FIRST_TIME)
            ).functions.within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 5), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 10), FIRST_TIME)
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
            ).functions.within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME)
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
            ).functions.within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
            ).functions.within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 2), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 2), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 5), FIRST_TIME)
            ).functions.within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 2), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                )
            )
        )
    }

    @Test
    fun addTest() {
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 5), FIRST_TIME),
            mk_DTG(mk_Date(1990, 1, 2), FIRST_TIME).functions.add(durFromDays(3))
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 2), mk_Time(5, 20, 0, 0)),
            mk_DTG(mk_Date(1990, 1, 2), mk_Time(2, 0, 0, 0)).functions.add(
                durFromHours(3).functions.add(durFromMinutes(20))
            )
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 5), mk_Time(5, 20, 10, 5)),
            mk_DTG(mk_Date(1990, 1, 1), mk_Time(2, 0, 0, 0)).functions.add(
                durFromDays(4).functions.add(
                    durFromHours(3).functions.add(
                        durFromMinutes(20).functions.add(
                            durFromSeconds(10).functions.add(
                                durFromMillis(5)
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    fun subtractTest() {
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 2), FIRST_TIME),
            mk_DTG(mk_Date(1990, 1, 5), FIRST_TIME).functions.subtract(
                durFromDays(3)
            )
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 5), mk_Time(3, 20, 0, 0)),
            mk_DTG(mk_Date(1990, 1, 5), mk_Time(6, 40, 0, 0)).functions.subtract(
                durFromHours(3).functions.add(durFromMinutes(20))
            )
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 2), mk_Time(2, 20, 20, 5)),

            mk_DTG(mk_Date(1990, 1, 6), mk_Time(5, 40, 30, 10)).functions.subtract(
                durFromDays(4).functions.add(
                    durFromHours(3).functions.add(
                        durFromMinutes(20).functions.add(
                            durFromSeconds(10).functions.add(
                                durFromMillis(5)
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    fun diffTest() {
        assertEquals(
            durFromDays(5).functions.add(
                durFromHours(5).functions.add(
                    durFromMinutes(5).functions.add(
                        durFromSeconds(5).functions.add(
                            durFromMillis(5)
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
            durFromDays(5).functions.add(
                durFromHours(5).functions.add(
                    durFromMinutes(5).functions.add(
                        durFromSeconds(5).functions.add(
                            durFromMillis(5)
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
            durFromDays(5).functions.add(
                durFromHours(5).functions.add(
                    durFromMinutes(5).functions.add(
                        durFromSeconds(5).functions.add(
                            durFromMillis(5)
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
            durFromDays(5).functions.add(
                durFromHours(5).functions.add(
                    durFromMinutes(5).functions.add(
                        durFromSeconds(5).functions.add(
                            durFromMillis(5)
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
        assertEquals(durFromDays(5), durFromDays(2).functions.add(durFromDays(3)))
        assertEquals(durFromDays(2), durFromDays(2).functions.add(durFromDays(0)))
    }

    @Test
    fun durSubtractTest() {
        assertEquals(durFromDays(5), durFromDays(8).functions.subtract(durFromDays(3)))
        assertFailsWith<PreconditionFailure> { durFromDays(2).functions.subtract(durFromDays(3)) }
        assertEquals(durFromDays(8), durFromDays(8).functions.subtract(durFromDays(0)))
        assertEquals(durFromDays(0), durFromDays(8).functions.subtract(durFromDays(8)))
    }

    @Test
    fun durMultiplyTest() {
        assertEquals(durFromDays(10), durFromDays(2).functions.multiply(5))
        assertEquals(durFromDays(1).functions.add(durFromHours(1)), durFromHours(5).functions.multiply(5))
        assertEquals(durFromDays(5000000), durFromDays(1000000).functions.multiply(5))
    }

    @Test
    fun durDivideTest() {
        assertEquals(durFromDays(2), durFromDays(10).functions.divide(5))
        assertEquals(durFromHours(12), durFromDays(10).functions.divide(20))
        assertEquals(durFromDays(1000000), durFromDays(5000000).functions.divide(5))
    }

    @Test
    fun durDiffTest() {
        assertEquals(durFromDays(5), durDiff(durFromDays(10), durFromDays(5)))
        assertEquals(durFromDays(5), durDiff(durFromDays(5), durFromDays(10)))
        assertEquals(durFromDays(0), durDiff(durFromDays(10), durFromDays(10)))
        assertEquals(durFromDays(1999999995), durDiff(durFromDays(2000000000), durFromDays(5)))
        assertEquals(durFromMillis(15), durDiff(durFromMillis(20), durFromMillis(5)))
    }

    @Test
    fun toMillisTest() {
        assertEquals(12, mk_Duration(12).functions.toMillis())
        assertEquals(0, mk_Duration(0).functions.toMillis())
        assertEquals(2000000000, mk_Duration(2000000000).functions.toMillis())
    }

    @Test
    fun durFromMillisTest() {
        assertEquals(12, durFromMillis(12).dur)
        assertEquals(0, durFromMillis(0).dur)
        assertEquals(2000000000, durFromMillis(2000000000).dur)
    }

    @Test
    fun toSecondsTest() {
        assertEquals(12, mk_Duration(12000).functions.toSeconds())
        assertEquals(0, mk_Duration(0).functions.toSeconds())
        assertEquals(2000000, mk_Duration(2000000000).functions.toSeconds())
    }

    @Test
    fun durFromSecondsTest() {
        assertEquals(60000, durFromSeconds(60).dur)
        assertEquals(0, durFromSeconds(0).dur)
        assertEquals(2000000000, durFromSeconds(2000000).dur)
    }

    @Test
    fun toMinutesTest() {
        assertEquals(10, mk_Duration(600000).functions.toMinutes())
        assertEquals(0, mk_Duration(0).functions.toMinutes())
        assertEquals(2000, mk_Duration(120000000).functions.toMinutes())
    }

    @Test
    fun durFromMinutesTest() {
        assertEquals(3600000, durFromMinutes(60).dur)
        assertEquals(0, durFromMinutes(0).dur)
        assertEquals(120000000000, durFromMinutes(2000000).dur)
    }

    @Test
    fun durModMinutesTest() {
        assertEquals(durFromSeconds(10), durFromMinutes(5).functions.add(durFromSeconds(10)).functions.modMinutes())
        assertEquals(durFromSeconds(10), durFromMinutes(0).functions.add(durFromSeconds(10)).functions.modMinutes())
        assertEquals(
            durFromSeconds(10),
            durFromMinutes(2000000).functions.add(durFromSeconds(10)).functions.modMinutes()
        )
    }

    @Test
    fun toHoursTest() {
        assertEquals(10, mk_Duration(36000000).functions.toHours())
        assertEquals(0, mk_Duration(0).functions.toHours())
        assertEquals(2000, mk_Duration(7200000000).functions.toHours())
    }

    @Test
    fun durFromHoursTest() {
        assertEquals(216000000, durFromHours(60).dur)
        assertEquals(0, durFromHours(0).dur)
        assertEquals(7200000000, durFromHours(2000).dur)
    }

    @Test
    fun durModHoursTest() {
        assertEquals(durFromSeconds(10), durFromHours(5).functions.add(durFromSeconds(10)).functions.modHours())
        assertEquals(durFromSeconds(10), durFromHours(0).functions.add(durFromSeconds(10)).functions.modHours())
        assertEquals(durFromSeconds(10), durFromHours(2000000).functions.add(durFromSeconds(10)).functions.modHours())
    }

    @Test
    fun toDaysTest() {
        assertEquals(10, mk_Duration(864000000).functions.toDays())
        assertEquals(9, mk_Duration(863999999).functions.toDays())
        assertEquals(0, mk_Duration(0).functions.toDays())
        assertEquals(2000, mk_Duration(172800000000).functions.toDays())
    }

    @Test
    fun durFromDaysTest() {
        assertEquals(864000000, durFromDays(10).dur)
        assertEquals(0, durFromDays(0).dur)
        assertEquals(172800000000, durFromDays(2000).dur)

    }

    @Test
    fun dateToDayOfWeekTest() {
        assertEquals(
            ISO8601.DayOfWeek.Thursday,
            mk_Date(2009, 8, 13).functions.toDayOfWeek()
        )
        assertEquals(
            ISO8601.DayOfWeek.Saturday,
            mk_Date(2000, 4, 1).functions.toDayOfWeek()
        )
        assertEquals(
            ISO8601.DayOfWeek.Monday,
            mk_Date(1, 1, 1).functions.toDayOfWeek()
        )
        assertEquals(
            ISO8601.DayOfWeek.Wednesday,
            mk_Date(2019, 10, 9).functions.toDayOfWeek()
        )
        assertEquals(
            ISO8601.DayOfWeek.Saturday,
            mk_Date(2017, 10, 28).functions.toDayOfWeek()
        )
        assertEquals(
            ISO8601.DayOfWeek.Wednesday,
            mk_Date(2020, 1, 1).functions.toDayOfWeek()
        )
    }

    @Test
    fun durModDaysTest() {
        assertEquals(durFromSeconds(10), durFromDays(5).functions.add(durFromSeconds(10)).functions.modDays())
        assertEquals(durFromSeconds(10), durFromDays(0).functions.add(durFromSeconds(10)).functions.modDays())
        assertEquals(durFromSeconds(10), durFromDays(200000).functions.add(durFromSeconds(10)).functions.modDays())
    }

    @Test
    fun toMonthTest() {
        assertEquals(0, durFromDays(30).functions.toMonth(1990))
        assertEquals(1, durFromDays(31).functions.toMonth(1990))
        assertEquals(0, durFromDays(0).functions.toMonth(1990))
        assertEquals(11, durFromDays(364).functions.toMonth(1990))
    }

    @Test
    fun durFromMonthTest() {
        assertEquals(durFromDays(31), durFromMonth(1990, 1))
        assertEquals(durFromDays(28), durFromMonth(1990, 2))
        assertEquals(durFromDays(30), durFromMonth(1990, 9))
    }

    @Test
    fun durUpToMonthTest() {
        assertEquals(durFromDays(90), durUpToMonth(1990, 4))
        assertEquals(durFromDays(59), durUpToMonth(1990, 3))
    }

    @Test
    fun toYearTest() {
        assertEquals(0, durFromDays(0).functions.toYear(1990))
        assertEquals(2, durFromDays(800).functions.toYear(1990))
    }

    @Test
    fun durFromYearTest() {
        assertEquals(durFromDays(365), durFromYear(1990))
        assertEquals(durFromDays(366), durFromYear(2020))
    }

    @Test
    fun durUpToYearTest() {
        assertEquals(durFromDays(366), durUpToYear(1))
    }

    @Test
    fun toDTGTest() {
        assertEquals(
            mk_DTG(mk_Date(0, 1, 6), mk_Time(0, 0, 0, 0)),
            durFromDays(5).functions.toDTG()
        )
        assertEquals(
            mk_DTG(mk_Date(0, 1, 1), mk_Time(0, 0, 0, 0)),
            durFromDays(0).functions.toDTG()
        )
        assertEquals(
            mk_DTG(mk_Date(0, 2, 7), mk_Time(0, 0, 0, 0)),
            durFromDays(37).functions.toDTG()
        )
    }


    @Test
    fun dtgToDurTest() {
        assertEquals(
            durFromDays(6),

            mk_DTG(mk_Date(0, 1, 7), mk_Time(0, 0, 0, 0)).functions.toDur()

        )
        assertEquals(
            durFromDays(0),
            mk_DTG(mk_Date(0, 1, 1), mk_Time(0, 0, 0, 0)).functions.toDur()
        )

        assertEquals(
            durFromDays(37),
            mk_DTG(mk_Date(0, 2, 7), mk_Time(0, 0, 0, 0)).functions.toDur()
        )
    }

    @Test
    fun toDateTest() {
        assertEquals(mk_Date(0, 1, 4), durFromDays(3).functions.toDate())
        assertEquals(mk_Date(0, 1, 1), durFromDays(0).functions.toDate())
    }

    @Test
    fun durFromDateTest() {
        assertEquals(durFromDays(3), mk_Date(0, 1, 4).functions.toDur())
        assertEquals(durFromDays(0), mk_Date(0, 1, 1).functions.toDur())
    }


    @Test
    fun toTimeTest() {
        assertEquals(mk_Time(3, 0, 0, 0), durFromHours(3).functions.toTime())
        assertEquals(mk_Time(0, 0, 0, 0), durFromHours(0).functions.toTime())
    }

    @Test
    fun durFromTimeTest() {
        assertEquals(durFromHours(4), mk_Time(4, 0, 0, 0).functions.toDur())
        assertEquals(durFromHours(0), mk_Time(0, 0, 0, 0).functions.toDur())
    }

    @Test
    fun durFromTimeInZoneTest() {
        assertEquals(
            durFromHours(4),
            mk_TimeInZone(
                mk_Time(3, 0, 0, 0),
                mk_Offset(durFromHours(1), ISO8601.PlusOrMinus.Minus)
            ).functions.toDur()
        )
        assertEquals(
            durFromHours(2),
            mk_TimeInZone(
                mk_Time(3, 0, 0, 0),
                mk_Offset(durFromHours(1), ISO8601.PlusOrMinus.Plus)
            ).functions.toDur()
        )
        assertEquals(
            durFromHours(0),
            mk_TimeInZone(
                mk_Time(0, 0, 0, 0),
                mk_Offset(durFromHours(0), ISO8601.PlusOrMinus.None)
            ).functions.toDur()
        )
    }

    @Test
    fun durFromIntervalTest() {
        assertEquals(
            durFromDays(5),
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
            ).functions.toDur()
        )
        assertEquals(
            durFromHours(2),
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(3, 0, 0, 0))
            ).functions.toDur()
        )
    }

    @Test
    fun finestGranularityTest() {
        assertEquals(
            true,
            mk_DTG(mk_Date(0, 1, 1), mk_Time(10, 0, 0, 0)).functions.finestGranularity(
                durFromHours(1)
            )
        )
        assertEquals(
            false,
            mk_DTG(mk_Date(0, 1, 1), mk_Time(10, 0, 0, 0)).functions.finestGranularity(
                durFromHours(3)
            )
        )
    }

    @Test
    fun finestGranularityITest() {
        assertEquals(
            true,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(3, 0, 0, 0))
            ).functions.finestGranularityI(
                durFromHours(1)

            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(10, 0, 0, 0))
            ).functions.finestGranularityI(
                durFromHours(2)
            )
        )

    }

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
            mk_Date(9999, 1, 1),
            maxDate(
                mk_Set1(
                    mk_Date(0, 1, 1),
                    mk_Date(1990, 1, 1),
                    mk_Date(300, 1, 1),
                    mk_Date(9999, 1, 1)
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
            durFromMinutes(40),
            minDuration(
                mk_Set1(
                    durFromHours(3),
                    durFromMinutes(40),
                    durFromDays(2),
                    durFromHours(5)
                )
            )
        )
        assertEquals(
            durFromHours(0),
            minDuration(
                mk_Set1(
                    durFromHours(0),
                    durFromMinutes(14),
                    durFromDays(2),
                    durFromHours(23)
                )
            )
        )
    }

    @Test
    fun maxDurationTest() {
        assertEquals(
            durFromDays(2),
            maxDuration(
                mk_Set1(
                    durFromHours(3),
                    durFromMinutes(40),
                    durFromDays(2),
                    durFromHours(5)
                )
            )
        )
        assertEquals(
            durFromDays(2000000000),
            maxDuration(
                mk_Set1(
                    durFromHours(0),
                    durFromMinutes(14),
                    durFromDays(2000000000),
                    durFromHours(23)
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
                    durFromHours(3),
                    durFromMinutes(40),
                    durFromDays(2),
                    durFromHours(5)
                )
            )
        )
        assertEquals(
            mk_Duration(0),
            sumDuration(
                mk_Seq(
                    durFromHours(0),
                    durFromMinutes(0),
                    durFromDays(0)
                )
            )
        )
    }

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

    @Test
    fun nextDateForYMTest() {
        assertEquals(mk_Date(1990, 2, 1), nextDateForYM(mk_Date(1990, 1, 1)))
        assertEquals(mk_Date(1990, 3, 31), nextDateForYM(mk_Date(1990, 1, 31)))
    }

    @Test
    fun nextDateForDayTest() {
        assertEquals(mk_Date(0, 1, 4), nextDateForDay(mk_Date(0, 1, 1), 4))
        assertEquals(mk_Date(1990, 3, 31), nextDateForDay(mk_Date(1990, 1, 31), 31))
        assertEquals(mk_Date(1, 1, 14), nextDateForDay(mk_Date(0, 12, 15), 14))
    }

    @Test
    fun previousDateForYMTest() {
        assertEquals(mk_Date(1990, 1, 3), previousDateForYM(mk_Date(1990, 2, 3)))
        assertEquals(mk_Date(1989, 12, 3), previousDateForYM(mk_Date(1990, 1, 3)))
    }

    @Test
    fun previousDateForDayTest() {
        assertEquals(mk_Date(1990, 1, 12), previousDateForDay(mk_Date(1990, 1, 31), 12))
        assertEquals(mk_Date(1989, 12, 12), previousDateForDay(mk_Date(1990, 1, 4), 12))
    }

    @Test
    fun normaliseTest() {
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 1), mk_Time(3, 0, 0, 0)),
            mk_DTGInZone(
                mk_Date(1990, 1, 1),
                mk_TimeInZone(
                    mk_Time(5, 0, 0, 0),
                    mk_Offset(durFromHours(2), ISO8601.PlusOrMinus.Plus)
                )
            ).functions.normalise()
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 1), mk_Time(7, 0, 0, 0)),
            mk_DTGInZone(
                mk_Date(1990, 1, 1),
                mk_TimeInZone(
                    mk_Time(5, 0, 0, 0),
                    mk_Offset(durFromHours(2), ISO8601.PlusOrMinus.Minus)
                )
            ).functions.normalise()
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 1), mk_Time(5, 0, 0, 0)),
            mk_DTGInZone(
                mk_Date(1990, 1, 1),
                mk_TimeInZone(
                    mk_Time(5, 0, 0, 0),
                    mk_Offset(durFromHours(0), ISO8601.PlusOrMinus.Minus)
                )
            ).functions.normalise()
        )
    }

    @Test
    fun normaliseTimeTest() {
        assertEquals(
            mk_(mk_Time(7, 23, 12, 0), ISO8601.PlusOrMinus.None),
            mk_TimeInZone(
                mk_Time(5, 23, 12, 0),
                mk_Offset(durFromHours(2), ISO8601.PlusOrMinus.Minus)
            ).functions.normaliseTime()
        )
        assertEquals(
            mk_(mk_Time(1, 23, 12, 0), ISO8601.PlusOrMinus.Minus),
            mk_TimeInZone(
                mk_Time(23, 23, 12, 0),
                mk_Offset(durFromHours(2), ISO8601.PlusOrMinus.Minus)
            ).functions.normaliseTime()
        )
        assertEquals(
            mk_(mk_Time(23, 23, 12, 0), ISO8601.PlusOrMinus.None),
            mk_TimeInZone(
                mk_Time(23, 23, 12, 0),
                mk_Offset(durFromHours(0), ISO8601.PlusOrMinus.Minus)
            ).functions.normaliseTime()
        )
        assertEquals(
            mk_(mk_Time(3, 23, 12, 0), ISO8601.PlusOrMinus.None),
            mk_TimeInZone(
                mk_Time(5, 23, 12, 0),
                mk_Offset(durFromHours(2), ISO8601.PlusOrMinus.Plus)
            ).functions.normaliseTime()
        )
        assertEquals(
            mk_(mk_Time(23, 23, 12, 0), ISO8601.PlusOrMinus.Plus),
            mk_TimeInZone(
                mk_Time(1, 23, 12, 0),
                mk_Offset(durFromHours(2), ISO8601.PlusOrMinus.Plus)
            ).functions.normaliseTime()
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

    @Test
    fun formatDTGInZoneTest() {
        assertEquals(
            "1990-01-01T03:00:00+02:00",
            mk_DTGInZone(
                mk_Date(1990, 1, 1),
                mk_TimeInZone(
                    mk_Time(3, 0, 0, 0),
                    mk_Offset(durFromHours(2), ISO8601.PlusOrMinus.Plus)
                )
            ).functions.format()
        )
        assertEquals(
            "1990-01-01T03:00:00-02:00",
            mk_DTGInZone(
                mk_Date(1990, 1, 1),
                mk_TimeInZone(
                    mk_Time(3, 0, 0, 0),
                    mk_Offset(durFromHours(2), ISO8601.PlusOrMinus.Minus)
                )
            ).functions.format()
        )
        assertEquals(
            "1990-01-01T03:00:00Z",
            mk_DTGInZone(
                mk_Date(1990, 1, 1),
                mk_TimeInZone(
                    mk_Time(3, 0, 0, 0),
                    mk_Offset(durFromHours(0), ISO8601.PlusOrMinus.None)
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
                mk_Offset(durFromHours(2), ISO8601.PlusOrMinus.Plus)
            ).functions.format()
        )
        assertEquals(
            "03:00:00-02:00",
            mk_TimeInZone(
                mk_Time(3, 0, 0, 0),
                mk_Offset(durFromHours(2), ISO8601.PlusOrMinus.Minus)
            ).functions.format()
        )
        assertEquals(
            "03:00:00Z",
            mk_TimeInZone(
                mk_Time(3, 0, 0, 0),
                mk_Offset(durFromHours(0), ISO8601.PlusOrMinus.None)
            ).functions.format()
        )
    }

    @Test
    fun formatIntervalTest() {
        assertEquals(
            "1990-01-01T00:00:00/1990-01-06T00:00:00",
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
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
            durFromHours(6).functions.add(durFromDays(2)).functions.format()
        )
        assertEquals("PT0S", durFromHours(0).functions.format())
        assertEquals("PT1.001S", durFromSeconds(1).functions.add(durFromMillis(1)).functions.format())
    }

    // TODO Write tests
    @Test
    fun addMonthsTest() {
    }

    //TODO Write tests
    @Test
    fun subtractMonthsTest() {
    }

    @Test
    fun monthsBetweenTest() {
        assertEquals(
            0,
            monthsBetween(
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME)
            )
        )
        assertEquals(
            0,
            monthsBetween(
                mk_DTG(mk_Date(1990, 1, 12), FIRST_TIME),
                mk_DTG(mk_Date(1990, 2, 1), FIRST_TIME)
            )
        )
        assertEquals(
            6,
            monthsBetween(
                mk_DTG(mk_Date(1990, 12, 12), FIRST_TIME),
                mk_DTG(mk_Date(1991, 6, 13), FIRST_TIME)
            )
        )
    }

    @Test
    fun yearsBetweenTest() {
        assertEquals(
            0,
            yearsBetween(
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME)
            )
        )
        assertEquals(
            0,
            yearsBetween(
                mk_DTG(mk_Date(1990, 1, 12), FIRST_TIME),
                mk_DTG(mk_Date(1990, 12, 1), FIRST_TIME)
            )
        )
        assertEquals(
            2,
            yearsBetween(
                mk_DTG(mk_Date(1990, 1, 12), FIRST_TIME),
                mk_DTG(mk_Date(1992, 3, 13), FIRST_TIME)
            )

        )
    }

    // TODO Write tests
    @Test
    fun isDateTest() {
        assertEquals(true, isDate("2018-04-01"))
    }

    // TODO Write tests
    @Test
    fun strToDateTest() {
    }

    // TODO Write tests
    @Test
    fun isDTGTest() {
    }

    // TODO Write tests
    @Test
    fun strToDTGTest() {
    }
}

