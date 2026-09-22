package com.example.ui.calendar

import com.example.data.model.WorkoutEntity

enum class WorkoutStatus {
    UPCOMING,
    COMPLETED,
    MISSED
}

fun WorkoutEntity.getStatus(
    now: Long = System.currentTimeMillis()
): WorkoutStatus {

    return when {

        completed -> {
            WorkoutStatus.COMPLETED
        }

        startTime < now -> {
            WorkoutStatus.MISSED
        }

        else -> {
            WorkoutStatus.UPCOMING
        }
    }
}