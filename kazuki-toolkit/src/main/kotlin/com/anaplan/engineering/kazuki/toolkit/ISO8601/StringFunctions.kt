package com.anaplan.engineering.kazuki.toolkit.ISO8601

import com.anaplan.engineering.kazuki.core.bool
import com.anaplan.engineering.kazuki.core.function
import com.anaplan.engineering.kazuki.toolkit.ISO8601.DTG_Module.mk_DTG
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Time_Module.mk_Time

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
        } else if (s.substring(0, 4).toInt() < FirstYear || s.substring(0, 4).toInt() > LastYear) {
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
        } else if (s.substring(0, 4).toInt() < FirstYear || s.substring(0, 4).toInt() > LastYear) {
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
        mk_Date(s.substring(0, 4).toInt(), s.substring(5, 7).toInt(), s.substring(8, 10).toInt())
    },
    pre = { s -> isDate(s) }
)

val strToDTG: (String) -> DTG = function(
    command = { s: String ->
        val millisecond = if (s.length == 19) 0 else s.substring(20, 23).toInt()
        mk_DTG(
            mk_Date(s.substring(0, 4).toInt(), s.substring(5, 7).toInt(), s.substring(8, 10).toInt()),
            mk_Time(
                s.substring(11, 13).toInt(),
                s.substring(14, 16).toInt(),
                s.substring(17, 19).toInt(),
                millisecond
            )
        )
    },
    pre = { s -> isDTG(s) }
)
