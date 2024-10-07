package com.anaplan.engineering.kazuki.toolkit

import com.anaplan.engineering.kazuki.core.PreconditionFailure
import com.anaplan.engineering.kazuki.core.mk_
import com.anaplan.engineering.kazuki.core.mk_Seq
import com.anaplan.engineering.kazuki.core.mk_Set1
import com.anaplan.engineering.kazuki.toolkit.ISO8601.FIRST_DATE
import com.anaplan.engineering.kazuki.toolkit.ISO8601.FIRST_TIME
import com.anaplan.engineering.kazuki.toolkit.ISO8601.add
import com.anaplan.engineering.kazuki.toolkit.ISO8601.daysInMonth
import com.anaplan.engineering.kazuki.toolkit.ISO8601.daysInYear
import com.anaplan.engineering.kazuki.toolkit.ISO8601.diff
import com.anaplan.engineering.kazuki.toolkit.ISO8601.dtgInRange
import com.anaplan.engineering.kazuki.toolkit.ISO8601.dtgWithin
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durAdd
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durDiff
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durDivide
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durFromDTG
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durFromDate
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durFromDays
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durFromHours
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durFromInterval
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durFromMillis
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durFromMinutes
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durFromMonth
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durFromSeconds
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durFromTime
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durFromYear
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durModDays
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durModMinutes
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durMultiply
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durSubtract
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durToDTG
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durToDate
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durToDays
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durToHours
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durToMillis
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durToMinutes
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durToMonth
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durToSeconds
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durToTime
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durToYear
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durUpToMonth
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durUpToYear
import com.anaplan.engineering.kazuki.toolkit.ISO8601.finestGranularity
import com.anaplan.engineering.kazuki.toolkit.ISO8601.finestGranularityI
import com.anaplan.engineering.kazuki.toolkit.ISO8601.formatDTG
import com.anaplan.engineering.kazuki.toolkit.ISO8601.formatDate
import com.anaplan.engineering.kazuki.toolkit.ISO8601.formatDuration
import com.anaplan.engineering.kazuki.toolkit.ISO8601.formatInterval
import com.anaplan.engineering.kazuki.toolkit.ISO8601.formatTime
import com.anaplan.engineering.kazuki.toolkit.ISO8601.formatTimeInZone
import com.anaplan.engineering.kazuki.toolkit.ISO8601.inInterval
import com.anaplan.engineering.kazuki.toolkit.ISO8601.instant
import com.anaplan.engineering.kazuki.toolkit.ISO8601.isLeap
import com.anaplan.engineering.kazuki.toolkit.ISO8601.maxDTG
import com.anaplan.engineering.kazuki.toolkit.ISO8601.maxDate
import com.anaplan.engineering.kazuki.toolkit.ISO8601.maxDuration
import com.anaplan.engineering.kazuki.toolkit.ISO8601.maxTime
import com.anaplan.engineering.kazuki.toolkit.ISO8601.minDTG
import com.anaplan.engineering.kazuki.toolkit.ISO8601.minDate
import com.anaplan.engineering.kazuki.toolkit.ISO8601.minDuration
import com.anaplan.engineering.kazuki.toolkit.ISO8601.minTime
import com.anaplan.engineering.kazuki.toolkit.ISO8601.nextDateForDay
import com.anaplan.engineering.kazuki.toolkit.ISO8601.nextDateForYM
import com.anaplan.engineering.kazuki.toolkit.ISO8601.normalise
import com.anaplan.engineering.kazuki.toolkit.ISO8601.normaliseTime
import com.anaplan.engineering.kazuki.toolkit.ISO8601.overlap
import com.anaplan.engineering.kazuki.toolkit.ISO8601.previousDateForDay
import com.anaplan.engineering.kazuki.toolkit.ISO8601.previousDateForYM
import com.anaplan.engineering.kazuki.toolkit.ISO8601.subtract
import com.anaplan.engineering.kazuki.toolkit.ISO8601.sumDuration
import com.anaplan.engineering.kazuki.toolkit.ISO8601.within
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
            dtgInRange(
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
            )
        )
        assertEquals(
            false, dtgInRange(
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 7), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
            )
        )
        assertEquals(
            true,
            dtgInRange(
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
            )
        )
        assertEquals(
            false,
            dtgInRange(
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME),
                mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME)
            )
        )
    }

    @Test
    fun dtgInRangeTimeTest() {
        assertEquals(
            true, dtgInRange(
                mk_DTG(FIRST_DATE, mk_Time(2, 0, 0, 0)),
                mk_DTG(FIRST_DATE, mk_Time(2, 30, 0, 0)),
                mk_DTG(FIRST_DATE, mk_Time(3, 0, 0, 0))
            )
        )
        assertEquals(
            false, dtgInRange(
                mk_DTG(FIRST_DATE, mk_Time(2, 0, 0, 0)),
                mk_DTG(FIRST_DATE, mk_Time(3, 30, 0, 0)),
                mk_DTG(FIRST_DATE, mk_Time(3, 0, 0, 0))
            )
        )
        assertEquals(
            true, dtgInRange(
                mk_DTG(FIRST_DATE, mk_Time(2, 0, 0, 0)),
                mk_DTG(FIRST_DATE, mk_Time(2, 0, 0, 0)),
                mk_DTG(FIRST_DATE, mk_Time(3, 0, 0, 0))
            )
        )
        assertEquals(
            false, dtgInRange(
                mk_DTG(FIRST_DATE, mk_Time(2, 0, 0, 0)),
                mk_DTG(FIRST_DATE, mk_Time(3, 0, 0, 0)),
                mk_DTG(FIRST_DATE, mk_Time(3, 0, 0, 0))
            )
        )
    }

    @Test
    fun dtgWithinTest() {
        assertEquals(
            true, dtgWithin(
                mk_DTG(mk_Date(1989, 1, 3), FIRST_TIME),
                durFromDays(3),
                mk_DTG(mk_Date(1989, 1, 1), FIRST_TIME)
            )
        )
        assertEquals(
            true, dtgWithin(
                mk_DTG(mk_Date(1989, 12, 30), FIRST_TIME),
                durFromDays(3),
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME)
            )
        )
        assertEquals(
            true, dtgWithin(
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                durFromDays(0),
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME)
            )
        )
        assertEquals(
            false, dtgWithin(
                mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME),
                durFromDays(3),
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME)
            )
        )
        assertEquals(
            false, dtgWithin(
                mk_DTG(mk_Date(1989, 12, 27), FIRST_TIME),
                durFromDays(3),
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME)
            )
        )
    }

    @Test
    fun inIntervalTest() {
        assertEquals(
            true, inInterval(
                mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME),
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                )
            )
        )
        assertEquals(
            false, inInterval(
                mk_DTG(mk_Date(1990, 1, 7), FIRST_TIME),
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                )
            )
        )
        assertEquals(
            true, inInterval(
                mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                )
            )

        )
        assertEquals(
            false, inInterval(
                mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME),
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME)
                )
            )
        )
        assertEquals(
            true, inInterval(
                mk_DTG(FIRST_DATE, mk_Time(2, 30, 0, 0)),
                mk_Interval(
                    mk_DTG(FIRST_DATE, mk_Time(2, 0, 0, 0)),
                    mk_DTG(FIRST_DATE, mk_Time(3, 0, 0, 0))
                )
            )
        )
        assertEquals(
            false, inInterval(
                mk_DTG(FIRST_DATE, mk_Time(3, 30, 0, 0)),
                mk_Interval(
                    mk_DTG(FIRST_DATE, mk_Time(2, 0, 0, 0)),
                    mk_DTG(FIRST_DATE, mk_Time(3, 0, 0, 0))
                )
            )

        )
        assertEquals(
            true, inInterval(
                mk_DTG(FIRST_DATE, mk_Time(2, 0, 0, 0)),
                mk_Interval(
                    mk_DTG(FIRST_DATE, mk_Time(2, 0, 0, 0)),
                    mk_DTG(FIRST_DATE, mk_Time(3, 0, 0, 0))
                )
            )

        )
        assertEquals(
            false, inInterval(
                mk_DTG(FIRST_DATE, mk_Time(3, 0, 0, 0)),
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
            true, overlap(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 2), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 4), FIRST_TIME)
                ),
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME)
                )
            )
        )
        assertEquals(
            true, overlap(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME)
                ),
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 2), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 4), FIRST_TIME)
                )
            )
        )
        assertEquals(
            true, overlap(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                ),
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 2), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 4), FIRST_TIME)
                )
            )
        )
        assertEquals(
            true, overlap(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                ),
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                )
            )
        )
        assertEquals(
            false, overlap(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                ),
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 8), FIRST_TIME)
                )
            )
        )
        assertEquals(
            false, overlap(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                ),
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
            true, within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                ),
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 10), FIRST_TIME)
                )
            )
        )
        assertEquals(
            false,
            within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 12), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 14), FIRST_TIME)
                ),
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 10), FIRST_TIME)
                )
            )
        )
        assertEquals(
            false,
            within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                ),
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 5), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 10), FIRST_TIME)
                )
            )
        )
        assertEquals(
            false,
            within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 8), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 12), FIRST_TIME)
                ),
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 5), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 10), FIRST_TIME)
                )
            )
        )
        assertEquals(
            false,
            within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                ),
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME)
                )
            )
        )
        assertEquals(
            true,
            within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                ),
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                )
            )
        )
        assertEquals(
            true,
            within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 3), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                ),
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 2), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                )
            )
        )
        assertEquals(
            true,
            within(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 2), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 5), FIRST_TIME)
                ),
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
            add(
                mk_DTG(mk_Date(1990, 1, 2), FIRST_TIME),
                durFromDays(3)
            )
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 2), mk_Time(5, 20, 0, 0)),
            add(
                mk_DTG(mk_Date(1990, 1, 2), mk_Time(2, 0, 0, 0)),
                durAdd(durFromHours(3), durFromMinutes(20))
            )
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 5), mk_Time(5, 20, 10, 5)),
            add(
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(2, 0, 0, 0)),
                durAdd(
                    durFromDays(4),
                    durAdd(
                        durFromHours(3),
                        durAdd(
                            durFromMinutes(20),
                            durAdd(durFromSeconds(10), durFromMillis(5))
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
            subtract(
                mk_DTG(mk_Date(1990, 1, 5), FIRST_TIME),
                durFromDays(3)
            )
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 5), mk_Time(3, 20, 0, 0)),
            subtract(
                mk_DTG(mk_Date(1990, 1, 5), mk_Time(6, 40, 0, 0)),
                durAdd(durFromHours(3), durFromMinutes(20))
            )
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 2), mk_Time(2, 20, 20, 5)),
            subtract(
                mk_DTG(mk_Date(1990, 1, 6), mk_Time(5, 40, 30, 10)),
                durAdd(
                    durFromDays(4),
                    durAdd(
                        durFromHours(3),
                        durAdd(
                            durFromMinutes(20),
                            durAdd(durFromSeconds(10), durFromMillis(5))
                        )
                    )
                )
            )
        )
    }

    // TODO See if AddMonths and SubtractMonths should exist

    @Test
    fun diffTest() {
        assertEquals(
            durAdd(
                durFromDays(5),
                durAdd(
                    durFromHours(5),
                    durAdd(
                        durFromMinutes(5),
                        durAdd(durFromSeconds(5), durFromMillis(5))
                    )
                )
            ),
            diff(
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 1, 1, 1)),
                mk_DTG(mk_Date(1990, 1, 6), mk_Time(6, 6, 6, 6))
            )
        )
        assertEquals(
            durAdd(
                durFromDays(5),
                durAdd(
                    durFromHours(5),
                    durAdd(
                        durFromMinutes(5),
                        durAdd(durFromSeconds(5), durFromMillis(5))
                    )
                )
            ),
            diff(
                mk_DTG(mk_Date(1990, 1, 6), mk_Time(6, 6, 6, 6)),
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 1, 1, 1))
            )
        )
        assertEquals(
            durAdd(
                durFromDays(5),
                durAdd(
                    durFromHours(5),
                    durAdd(
                        durFromMinutes(5),
                        durAdd(durFromSeconds(5), durFromMillis(5))
                    )
                )
            ),
            diff(
                mk_DTG(mk_Date(1990, 1, 31), mk_Time(1, 1, 1, 1)),
                mk_DTG(mk_Date(1990, 2, 5), mk_Time(6, 6, 6, 6))
            )
        )
        assertEquals(
            durAdd(
                durFromDays(5),
                durAdd(
                    durFromHours(5),
                    durAdd(
                        durFromMinutes(5),
                        durAdd(durFromSeconds(5), durFromMillis(5))
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
        assertEquals(durFromDays(5), durAdd(durFromDays(2), durFromDays(3)))
        assertEquals(durFromDays(2), durAdd(durFromDays(2), durFromDays(0)))
    }

    @Test
    fun durSubtractTest() {
        assertEquals(durFromDays(5), durSubtract(durFromDays(8), durFromDays(3)))
        assertFailsWith<PreconditionFailure> { durSubtract(durFromDays(2), durFromDays(3)) }
        assertEquals(durFromDays(8), durSubtract(durFromDays(8), durFromDays(0)))
        assertEquals(durFromDays(0), durSubtract(durFromDays(8), durFromDays(8)))
    }

    @Test
    fun durMultiplyTest() {
        assertEquals(durFromDays(10), durMultiply(durFromDays(2), 5))
        assertEquals(durAdd(durFromDays(1), durFromHours(1)), durMultiply(durFromHours(5), 5))
        assertEquals(durFromDays(5000000), durMultiply(durFromDays(1000000), 5))
    }

    @Test
    fun durDivideTest() {
        assertEquals(durFromDays(2), durDivide(durFromDays(10), 5))
        assertEquals(durFromHours(12), durDivide(durFromDays(10), 20))
        assertEquals(durFromDays(1000000), durDivide(durFromDays(5000000), 5))
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
    fun durToMillisTest() {
        assertEquals(12, durToMillis(mk_Duration(12)))
        assertEquals(0, durToMillis(mk_Duration(0)))
        assertEquals(2000000000, durToMillis(mk_Duration(2000000000)))
    }

    @Test
    fun durFromMillisTest() {
        assertEquals(12, durFromMillis(12).dur)
        assertEquals(0, durFromMillis(0).dur)
        assertEquals(2000000000, durFromMillis(2000000000).dur)
    }

    @Test
    fun durToSecondsTest() {
        assertEquals(12, durToSeconds(mk_Duration(12000)))
        assertEquals(0, durToSeconds(mk_Duration(0)))
        assertEquals(2000000, durToSeconds(mk_Duration(2000000000)))
    }

    @Test
    fun durFromSecondsTest() {
        assertEquals(60000, durFromSeconds(60).dur)
        assertEquals(0, durFromSeconds(0).dur)
        assertEquals(2000000000, durFromSeconds(2000000).dur)
    }

    @Test
    fun durToMinutesTest() {
        assertEquals(10, durToMinutes(mk_Duration(600000)))
        assertEquals(0, durToMinutes(mk_Duration(0)))
        assertEquals(2000, durToMinutes(mk_Duration(120000000)))
    }

    @Test
    fun durFromMinutesTest() {
        assertEquals(3600000, durFromMinutes(60).dur)
        assertEquals(0, durFromMinutes(0).dur)
        assertEquals(120000000000, durFromMinutes(2000000).dur)
    }

    @Test
    fun durModMinutesTest() {
        assertEquals(durFromSeconds(10), durModMinutes(durAdd(durFromMinutes(5), durFromSeconds(10))))
        assertEquals(durFromSeconds(10), durModMinutes(durAdd(durFromMinutes(0), durFromSeconds(10))))
        assertEquals(durFromSeconds(10), durModMinutes(durAdd(durFromMinutes(2000000), durFromSeconds(10))))
    }

    @Test
    fun durToHoursTest() {
        assertEquals(10, durToHours(mk_Duration(36000000)))
        assertEquals(0, durToHours(mk_Duration(0)))
        assertEquals(2000, durToHours(mk_Duration(7200000000)))
    }

    @Test
    fun durFromHoursTest() {
        assertEquals(216000000, durFromHours(60).dur)
        assertEquals(0, durFromHours(0).dur)
        assertEquals(7200000000, durFromHours(2000).dur)
    }

    @Test
    fun durModHoursTest() {
        assertEquals(durFromSeconds(10), durModMinutes(durAdd(durFromHours(5), durFromSeconds(10))))
        assertEquals(durFromSeconds(10), durModMinutes(durAdd(durFromHours(0), durFromSeconds(10))))
        assertEquals(durFromSeconds(10), durModMinutes(durAdd(durFromHours(2000000), durFromSeconds(10))))
    }

    @Test
    fun durToDaysTest() {
        assertEquals(10, durToDays(mk_Duration(864000000)))
        assertEquals(9, durToDays(mk_Duration(863999999)))
        assertEquals(0, durToDays(mk_Duration(0)))
        assertEquals(2000, durToDays(mk_Duration(172800000000)))
    }

    @Test
    fun durFromDaysTest() {
        assertEquals(864000000, durFromDays(10).dur)
        assertEquals(0, durFromDays(0).dur)
        assertEquals(172800000000, durFromDays(2000).dur)

    }

    @Test
    fun getDayOfWeekTest() {
        // TODO Verify if this is desired and add it in if so
    }

    @Test
    fun durModDaysTest() {
        assertEquals(durFromSeconds(10), durModDays(durAdd(durFromDays(5), durFromSeconds(10))))
        assertEquals(durFromSeconds(10), durModDays(durAdd(durFromDays(0), durFromSeconds(10))))
        assertEquals(durFromSeconds(10), durModDays(durAdd(durFromDays(200000), durFromSeconds(10))))
    }

    @Test
    fun durToMonthTest() {
        assertEquals(0, durToMonth(durFromDays(30), 1990))
        assertEquals(1, durToMonth(durFromDays(31), 1990))
        assertEquals(0, durToMonth(durFromDays(0), 1990))
        assertEquals(11, durToMonth(durFromDays(364), 1990))
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
    fun durToYearTest() {
        assertEquals(0, durToYear(durFromDays(0), 1990))
        assertEquals(2, durToYear(durFromDays(800), 1990))
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
    fun durToDTGTest() {
        assertEquals(
            mk_DTG(mk_Date(0, 1, 6), mk_Time(0, 0, 0, 0)),
            durToDTG(durFromDays(5))
        )
        assertEquals(
            mk_DTG(mk_Date(0, 1, 1), mk_Time(0, 0, 0, 0)),
            durToDTG(durFromDays(0))
        )
        assertEquals(
            mk_DTG(mk_Date(0, 2, 7), mk_Time(0, 0, 0, 0)),
            durToDTG(durFromDays(37))
        )
    }

    @Test
    fun durFromDTGTest() {
        assertEquals(
            durFromDays(6),
            durFromDTG(
                mk_DTG(mk_Date(0, 1, 7), mk_Time(0, 0, 0, 0))
            )
        )
        assertEquals(
            durFromDays(0),
            durFromDTG(mk_DTG(mk_Date(0, 1, 1), mk_Time(0, 0, 0, 0)))
        )
        assertEquals(
            durFromDays(37), durFromDTG(
                mk_DTG(mk_Date(0, 2, 7), mk_Time(0, 0, 0, 0))
            )
        )
    }

    @Test
    fun durToDateTest() {
        assertEquals(mk_Date(0, 1, 4), durToDate(durFromDays(3)))
        assertEquals(mk_Date(0, 1, 1), durToDate(durFromDays(0)))
    }

    @Test
    fun durFromDateTest() {
        assertEquals(durFromDays(3), durFromDate(mk_Date(0, 1, 4)))
        assertEquals(durFromDays(0), durFromDate(mk_Date(0, 1, 1)))
    }

    @Test
    fun durToTimeTest() {
        assertEquals(mk_Time(3, 0, 0, 0), durToTime(durFromHours(3)))
        assertEquals(mk_Time(0, 0, 0, 0), durToTime(durFromHours(0)))
    }

    @Test
    fun durFromTimeTest() {
        assertEquals(durFromHours(4), durFromTime(mk_Time(4, 0, 0, 0)))
        assertEquals(durFromHours(0), durFromTime(mk_Time(0, 0, 0, 0)))
    }

    @Test
    fun durFromTimeInZoneTest() {
        // TODO This test
    }

    @Test
    fun durFromIntervalTest() {
        assertEquals(
            durFromDays(5), durFromInterval(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                )
            )
        )
        assertEquals(
            durFromHours(2), durFromInterval(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
                    mk_DTG(mk_Date(1990, 1, 1), mk_Time(3, 0, 0, 0))
                )
            )
        )
    }

    @Test
    fun finestGranularityTest() {
        assertEquals(
            true, finestGranularity(
                mk_DTG(mk_Date(0, 1, 1), mk_Time(10, 0, 0, 0)),
                durFromHours(1)
            )
        )
        assertEquals(
            false, finestGranularity(
                mk_DTG(mk_Date(0, 1, 1), mk_Time(10, 0, 0, 0)),
                durFromHours(3)
            )
        )
    }

    @Test
    fun finestGranularityITest() {
        assertEquals(
            true, finestGranularityI(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
                    mk_DTG(mk_Date(1990, 1, 1), mk_Time(3, 0, 0, 0))
                ),
                durFromHours(1)

            )
        )
        assertEquals(
            false, finestGranularityI(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
                    mk_DTG(mk_Date(1990, 1, 1), mk_Time(10, 0, 0, 0))
                ),
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
        // TODO See what I can do about the stack overflow error when year is large
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
            instant(mk_DTG(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)))
        )
        assertEquals(
            mk_Interval(
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(23, 59, 59, 999)),
                mk_DTG(mk_Date(1990, 1, 2), mk_Time(0, 0, 0, 0))
            ),
            instant(
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(23, 59, 59, 999))
            )
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
            normalise(
                mk_DTGInZone(
                    mk_Date(1990, 1, 1),
                    mk_TimeInZone(
                        mk_Time(5, 0, 0, 0),
                        mk_Offset(durFromHours(2), ISO8601.PlusOrMinus.Plus)
                    )
                )
            )
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 1), mk_Time(7, 0, 0, 0)),
            normalise(
                mk_DTGInZone(
                    mk_Date(1990, 1, 1),
                    mk_TimeInZone(
                        mk_Time(5, 0, 0, 0),
                        mk_Offset(durFromHours(2), ISO8601.PlusOrMinus.Minus)
                    )
                )
            )
        )
        assertEquals(
            mk_DTG(mk_Date(1990, 1, 1), mk_Time(5, 0, 0, 0)),
            normalise(
                mk_DTGInZone(
                    mk_Date(1990, 1, 1),
                    mk_TimeInZone(
                        mk_Time(5, 0, 0, 0),
                        mk_Offset(durFromHours(0), ISO8601.PlusOrMinus.Minus)
                    )
                )
            )
        )
    }

    @Test
    fun normaliseTimeTest() {
        assertEquals(
            mk_(mk_Time(7, 23, 12, 0), ISO8601.PlusOrMinus.None),
            normaliseTime(
                mk_TimeInZone(
                    mk_Time(5, 23, 12, 0),
                    mk_Offset(durFromHours(2), ISO8601.PlusOrMinus.Minus)
                )
            )
        )
        assertEquals(
            mk_(mk_Time(1, 23, 12, 0), ISO8601.PlusOrMinus.Minus),
            normaliseTime(
                mk_TimeInZone(
                    mk_Time(23, 23, 12, 0),
                    mk_Offset(durFromHours(2), ISO8601.PlusOrMinus.Minus)
                )
            )
        )
        assertEquals(
            mk_(mk_Time(23, 23, 12, 0), ISO8601.PlusOrMinus.None),
            normaliseTime(
                mk_TimeInZone(
                    mk_Time(23, 23, 12, 0),
                    mk_Offset(durFromHours(0), ISO8601.PlusOrMinus.Minus)
                )
            )
        )
        assertEquals(
            mk_(mk_Time(3, 23, 12, 0), ISO8601.PlusOrMinus.None),
            normaliseTime(
                mk_TimeInZone(
                    mk_Time(5, 23, 12, 0),
                    mk_Offset(durFromHours(2), ISO8601.PlusOrMinus.Plus)
                )
            )
        )
        assertEquals(
            mk_(mk_Time(23, 23, 12, 0), ISO8601.PlusOrMinus.Plus),
            normaliseTime(
                mk_TimeInZone(
                    mk_Time(1, 23, 12, 0),
                    mk_Offset(durFromHours(2), ISO8601.PlusOrMinus.Plus)
                )
            )
        )

    }

    @Test
    fun formatDGTTest() {
        assertEquals(
            "1990-01-01T03:00:00",
            formatDTG(
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(3, 0, 0, 0))
            )
        )
        assertEquals(
            "1990-01-01T03:00:00",
            formatDTG(
                mk_DTG(mk_Date(1990, 1, 1), mk_Time(3, 0, 0, 0))
            )
        )
        assertEquals(
            "0000-01-01T03:00:00",
            formatDTG(
                mk_DTG(mk_Date(0, 1, 1), mk_Time(3, 0, 0, 0))
            )
        )
    }

    // TODO Write tests
    @Test
    fun formatDTGInZoneTest() {
    }

    @Test
    fun formatDateTest() {
        assertEquals("1990-01-01", formatDate(mk_Date(1990, 1, 1)))
        assertEquals("0000-01-01", formatDate(mk_Date(0, 1, 1)))
    }

    @Test
    fun formatTimeTest() {
        assertEquals("10:07:14", formatTime(mk_Time(10, 7, 14, 0)))
        assertEquals("00:00:00", formatTime(mk_Time(0, 0, 0, 0)))
        // TODO Check if it's ok that this differs
        assertEquals("00:00:00.050", formatTime(mk_Time(0, 0, 0, 50)))
    }

    @Test
    fun formatTimeInZoneTest() {
        assertEquals(
            "03:00:00+02:00",
            formatTimeInZone(
                mk_TimeInZone(
                    mk_Time(3, 0, 0, 0),
                    mk_Offset(durFromHours(2), ISO8601.PlusOrMinus.Plus)
                )
            )
        )
        assertEquals(
            "03:00:00-02:00",
            formatTimeInZone(
                mk_TimeInZone(
                    mk_Time(3, 0, 0, 0),
                    mk_Offset(durFromHours(2), ISO8601.PlusOrMinus.Minus)
                )
            )

        )
        assertEquals(
            "03:00:00Z",
            formatTimeInZone(
                mk_TimeInZone(
                    mk_Time(3, 0, 0, 0),
                    mk_Offset(durFromHours(0), ISO8601.PlusOrMinus.None)
                )
            )
        )
    }

    @Test
    fun formatIntervalTest() {
        assertEquals(
            "1990-01-01T00:00:00/1990-01-06T00:00:00",
            formatInterval(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME),
                    mk_DTG(mk_Date(1990, 1, 6), FIRST_TIME)
                )
            )
        )
        assertEquals(
            "1990-01-01T00:00:00/1990-01-06T05:00:00",
            formatInterval(
                mk_Interval(
                    mk_DTG(mk_Date(1990, 1, 1), mk_Time(0, 0, 0, 0)),
                    mk_DTG(mk_Date(1990, 1, 6), mk_Time(5, 0, 0, 0))
                )
            )
        )
    }

    @Test
    fun formatDurationTest() {
        // TODO Resolve disagreement with provided tests here
        assertEquals(
            "P2DT6H",
            formatDuration(
                durAdd(
                    durFromHours(6),
                    durFromDays(2)
                )
            )
        )
        assertEquals("PT0S", formatDuration(durFromHours(0)))
    }

    // TODO Write tests
    @Test
    fun monthsBetweenTest() {
    }

    // TODO Write tests
    @Test
    fun yearsBetweenTest() {
    }

    // TODO Write tests
    @Test
    fun isDateTest() {
    }

    // TODO Write tests
    @Test
    fun toDateTest() {
    }

    // TODO Write tests
    @Test
    fun isDTGTest() {
    }

    // TODO Write tests
    @Test
    fun toDTGTest() {
    }

}

