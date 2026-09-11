package com.example.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import java.time.DayOfWeek
import java.time.YearMonth
import com.example.ui.calendar.toLocalDate


@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = viewModel(),
    onBack: () -> Unit
) {

    val workouts by viewModel.workouts.collectAsStateWithLifecycle()

    val currentMonth = remember { YearMonth.now() }

    val calendarState = rememberCalendarState(
        startMonth = currentMonth.minusMonths(12),
        endMonth = currentMonth.plusMonths(12),
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.MONDAY
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Text(
            text = "Lịch tập",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        HorizontalCalendar(
            state = calendarState,

            dayContent = { day ->

                val dayWorkouts =
                    workouts.filter {
                        it.dateMillis.toLocalDate() == day.date
                    }

                CalendarDay(
                    date = day.date,
                    workouts = dayWorkouts
                )
            }
        )
    }
}