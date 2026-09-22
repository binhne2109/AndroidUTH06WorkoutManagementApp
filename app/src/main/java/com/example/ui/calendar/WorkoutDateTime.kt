package com.example.ui.calendar

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

fun workoutTimestamp(date: LocalDate, time: LocalTime): Long {
    return date.atTime(time)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

fun LocalDate.toStartOfDayMillis(): Long {
    return atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}