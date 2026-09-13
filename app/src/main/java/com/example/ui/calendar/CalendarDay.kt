package com.example.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.WorkoutEntity
import java.time.LocalDate

@Composable
fun CalendarDay(
    date: LocalDate,
    workouts: List<WorkoutEntity>,
    dayColor: Color
) {
    Box(
        modifier = Modifier
            .padding(3.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(dayColor),
        contentAlignment = Alignment.Center
    ) {
        Text(text = date.dayOfMonth.toString())
    }
}