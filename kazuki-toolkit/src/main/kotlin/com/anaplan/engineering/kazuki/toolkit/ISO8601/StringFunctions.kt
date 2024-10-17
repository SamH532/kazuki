package com.anaplan.engineering.kazuki.toolkit.ISO8601

import com.anaplan.engineering.kazuki.core.bool
import com.anaplan.engineering.kazuki.core.function
import com.anaplan.engineering.kazuki.toolkit.ISO8601.DTG_Module.mk_DTG
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Time_Module.mk_Time

val isDate: (String) -> bool = function(
    command = { string: String ->
        if (string.length != 10) {
            false
        } else if (string.elementAt(4) != '-' || string.elementAt(7) != '-') {
            false
        } else if (string.substring(0, 4).toIntOrNull() == null ||
            string.substring(5, 7).toIntOrNull() == null ||
            string.substring(8, 10).toIntOrNull() == null
        ) {
            false
        } else if (string.substring(0, 4).toInt() < FirstYear || string.substring(0, 4).toInt() > LastYear) {
            false
        } else if (string.substring(5, 7).toInt() < 1 || string.substring(5, 7).toInt() > MONTHS_PER_YEAR) {
            false
        } else if (string.substring(8, 10).toInt() < 1 || string.substring(8, 10).toInt() > daysInMonth(
                string.substring(0, 4).toInt(), string.substring(5, 7).toInt()
            )
        ) {
            false
        } else {
            true
        }
    }
)

val isDTG: (String) -> bool = function(
    command = { string: String ->
        if (string.length != 19 && string.length != 23) {
            false
        } else if (string.elementAt(4) != '-' || string.elementAt(7) != '-' ||
            string.elementAt(10) != 'T' || string.elementAt(13) != ':' || string.elementAt(16) != ':'
        ) {
            false
        } else if (string.substring(0, 4).toIntOrNull() == null ||
            string.substring(5, 7).toIntOrNull() == null ||
            string.substring(8, 10).toIntOrNull() == null ||
            string.substring(11, 13).toIntOrNull() == null ||
            string.substring(14, 16).toIntOrNull() == null ||
            string.substring(17, 19).toIntOrNull() == null
        ) {
            false
        } else if (string.substring(0, 4).toInt() < FirstYear || string.substring(0, 4).toInt() > LastYear) {
            false
        } else if (string.substring(5, 7).toInt() < 1 || string.substring(5, 7).toInt() > MONTHS_PER_YEAR) {
            false
        } else if (string.substring(8, 10).toInt() < 1 || string.substring(8, 10).toInt() > daysInMonth(
                string.substring(0, 4).toInt(), string.substring(5, 7).toInt()
            )
        ) {
            false
        } else if (string.substring(11, 13).toInt() < 0 || string.substring(11, 13).toInt() > HOURS_PER_DAY) {
            false
        } else if (string.substring(14, 16).toInt() < 0 || string.substring(14, 16).toInt() > MINUTES_PER_HOUR) {
            false
        } else if (string.substring(17, 19).toInt() < 0 || string.substring(17, 19).toInt() > SECONDS_PER_MINUTE) {
            false
        } else if (string.length == 23) {
            if (string.elementAt(19) != '.') {
                false
            } else if (string.substring(20, 23).toIntOrNull() == null) {
                false
            } else if (string.substring(20, 23).toInt() < 0 || string.substring(20, 23).toInt() > MILLIS_PER_SECOND) {
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
    command = { string: String ->
        mk_Date(string.substring(0, 4).toInt(), string.substring(5, 7).toInt(), string.substring(8, 10).toInt())
    },
    pre = { string -> isDate(string) }
)

val strToDTG: (String) -> DTG = function(
    command = { string: String ->
        val millisecond = if (string.length == 19) 0 else string.substring(20, 23).toInt()
        mk_DTG(
            mk_Date(string.substring(0, 4).toInt(), string.substring(5, 7).toInt(), string.substring(8, 10).toInt()),
            mk_Time(
                string.substring(11, 13).toInt(),
                string.substring(14, 16).toInt(),
                string.substring(17, 19).toInt(),
                millisecond
            )
        )
    },
    pre = { string -> isDTG(string) }
)
