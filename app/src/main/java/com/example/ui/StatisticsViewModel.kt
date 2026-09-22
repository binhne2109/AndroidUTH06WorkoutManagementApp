package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.data.model.WorkoutEntity
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class StatisticsViewModel : ViewModel() {

    // Khai báo Producer quản lý dữ liệu hiển thị cho Vico Chart
    val chartEntryModelProducer = ChartEntryModelProducer()

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    fun updateDataFromWorkouts(workouts: List<WorkoutEntity>) {
        // 1. Tính tổng số liệu từ danh sách bài tập WorkoutEntity
        val totalMin = workouts.sumOf { it.durationMinutes }
        val totalCal = workouts.sumOf { it.caloriesBurned }
        val totalCount = workouts.size

        _uiState.update { currentState ->
            currentState.copy(
                totalDurationMinutes = totalMin,
                totalCalories = totalCal,
                completedWorkoutsCount = totalCount
            )
        }

        // 2. Cập nhật dữ liệu thời lượng vào 7 cột trên biểu đồ Vico Chart
        val weeklyMinutes = FloatArray(7) { 0f }

        if (workouts.isNotEmpty()) {
            weeklyMinutes[0] = totalMin.toFloat()
        }

        val entries = weeklyMinutes.mapIndexed { index, minutes ->
            entryOf(index.toFloat(), minutes)
        }

        // 3. Đẩy mảng dữ liệu mới vào Vico Chart Producer
        chartEntryModelProducer.setEntries(entries)
    }
}

// Data class chứa đầy đủ tất cả thuộc tính cho màn hình Thống kê & Biểu đồ
data class StatisticsUiState(
    val totalCalories: Int = 0,
    val totalDurationMinutes: Int = 0,
    val completedWorkoutsCount: Int = 0,
    val targetWorkouts: Int = 4,
    val targetDurationMinutes: Int = 180,
    val currentGoal: Int = 4
) {
    // Tiến độ số buổi tập (từ 0.0 đến 1.0)
    val workoutProgress: Float
        get() = if (targetWorkouts > 0) (completedWorkoutsCount.toFloat() / targetWorkouts).coerceAtMost(1f) else 0f

    // Tiến độ thời lượng tập luyện (từ 0.0 đến 1.0)
    val durationProgress: Float
        get() = if (targetDurationMinutes > 0) (totalDurationMinutes.toFloat() / targetDurationMinutes).coerceAtMost(1f) else 0f
}