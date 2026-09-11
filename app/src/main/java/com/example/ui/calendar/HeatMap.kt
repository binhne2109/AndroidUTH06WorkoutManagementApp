package com.example.ui.calendar

import androidx.compose.ui.graphics.Color
import com.example.data.model.WorkoutEntity

fun calculateHeatLevel(
    workouts: List<WorkoutEntity>
): Int {

    val totalMinutes =
        workouts
            .filter {
                it.getStatus() == WorkoutStatus.COMPLETED
            }
            .sumOf {
                it.durationMinutes
            }

    return when {
        totalMinutes == 0 -> 0
        totalMinutes <= 30 -> 1
        totalMinutes <= 60 -> 2
        totalMinutes <= 90 -> 3
        else -> 4
    }
}
fun heatMapColor(level: Int): Color {

    return when (level) {
        0 -> Color(0xFFE8F5E9)
        1 -> Color(0xFFC8E6C9)
        2 -> Color(0xFF81C784)
        3 -> Color(0xFF4CAF50)
        else -> Color(0xFF1B5E20)
    }
}