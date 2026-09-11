package com.example.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import com.example.data.model.WorkoutEntity
import java.time.LocalDate

@Composable
fun CalendarDay(
    date: String,
    workouts: List<WorkoutEntity>
) {

    val missed =
        workouts.any {
            it.getStatus() == WorkoutStatus.MISSED
        }

    val level =
        calculateHeatLevel(workouts)

    val color =
        if (missed) {
            Color(0xFFFFCDD2)
        } else {
            heatMapColor(level)
        }

    Box(
        modifier = Modifier
            .padding(3.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = date
        )
    }
}