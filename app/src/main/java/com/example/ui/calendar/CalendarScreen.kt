package com.example.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.example.ui.calendar.toLocalDate
import java.time.DayOfWeek
import java.time.YearMonth
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import androidx.compose.runtime.snapshotFlow

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = viewModel(),
    onBack: () -> Unit
) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    val workoutsByDate = remember(workouts) {
        workouts.groupBy { it.dateMillis.toLocalDate() }
    }

    val currentMonth = remember { YearMonth.now() }

    val calendarState = rememberCalendarState(
        startMonth = currentMonth.minusMonths(12),
        endMonth = currentMonth.plusMonths(12),
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.MONDAY
    )

    // Tháng đang hiển thị trên lịch
    var visibleMonth by remember { mutableStateOf(currentMonth) }

    // Cập nhật tiêu đề khi người dùng vuốt sang tháng khác
    LaunchedEffect(calendarState) {
        snapshotFlow { calendarState.firstVisibleMonth.yearMonth }
            .distinctUntilChanged()
            .collect { month ->
                visibleMonth = month
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Nút quay lại màn hình trước / màn hình chính
        Button(
            onClick = onBack,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text("Quay lại")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Tháng ${visibleMonth.monthValue} năm ${visibleMonth.year}, Số workout: ${workouts.size}",

            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalCalendar(
            state = calendarState,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            dayContent = { day ->
                val dayWorkouts = workoutsByDate[day.date].orEmpty() // Hiển thị ô ngày bằng day.date và dayWorkouts
                val dayColor = getWorkoutColor(dayWorkouts)

                CalendarDay(
                    date = day.date,
                    workouts = dayWorkouts,
                    dayColor = dayColor
                )
            }
        )
    }
}