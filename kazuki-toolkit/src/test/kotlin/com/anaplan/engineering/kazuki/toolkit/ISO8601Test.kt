package com.anaplan.engineering.kazuki.toolkit

import com.anaplan.engineering.kazuki.toolkit.ISO8601.FIRST_DATE
import com.anaplan.engineering.kazuki.toolkit.ISO8601.FIRST_TIME
import com.anaplan.engineering.kazuki.toolkit.ISO8601.daysInMonth
import com.anaplan.engineering.kazuki.toolkit.ISO8601.daysInYear
import com.anaplan.engineering.kazuki.toolkit.ISO8601.dtgInRange
import com.anaplan.engineering.kazuki.toolkit.ISO8601.dtgWithin
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durFromDTG
import com.anaplan.engineering.kazuki.toolkit.ISO8601.durFromDays
import com.anaplan.engineering.kazuki.toolkit.ISO8601.isLeap
import com.anaplan.engineering.kazuki.toolkit.ISO8601_Module.mk_DTG
import com.anaplan.engineering.kazuki.toolkit.ISO8601_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.ISO8601_Module.mk_Time
import junit.framework.TestCase.assertEquals
import kotlin.test.Test


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
        // This test will fail forever. The one below it will fail until the Int vs Long issues are resolved
        assertEquals(true, durFromDTG(mk_DTG(mk_Date(1990, 1, 1), FIRST_TIME)).dur)
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

}

