package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.data.model.WorkoutEntity
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Data class chứa thông tin thống kê theo nhóm bài tập
data class CategoryStat(
    val categoryName: String,
    val totalMinutes: Int,
    val totalCalories: Int
)

data class StatisticsUiState(
    val totalCalories: Int = 0,
    val totalDurationMinutes: Int = 0,
    val completedWorkoutsCount: Int = 0,
    val targetWorkouts: Int = 4,
    val targetDurationMinutes: Int = 180,
    val categoryStats: List<CategoryStat> = emptyList()
) {
    val workoutProgress: Float
        get() = if (targetWorkouts > 0) (completedWorkoutsCount.toFloat() / targetWorkouts).coerceAtMost(1f) else 0f

    val durationProgress: Float
        get() = if (targetDurationMinutes > 0) (totalDurationMinutes.toFloat() / targetDurationMinutes).coerceAtMost(1f) else 0f
}

class StatisticsViewModel : ViewModel() {

    val chartEntryModelProducer = ChartEntryModelProducer()

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    // 1. Cập nhật dữ liệu bài tập & Thống kê theo từng nhóm bài tập
    fun updateDataFromWorkouts(workouts: List<WorkoutEntity>) {
        val totalMin = workouts.sumOf { it.durationMinutes }
        val totalCal = workouts.sumOf { it.caloriesBurned }
        val totalCount = workouts.size

        // Gom nhóm thống kê theo loại/nhóm bài tập (category)
        val statsByCategory = workouts.groupBy { it.category }
            .map { (category, list) ->
                CategoryStat(
                    categoryName = category.ifEmpty { "Khác" },
                    totalMinutes = list.sumOf { it.durationMinutes },
                    totalCalories = list.sumOf { it.caloriesBurned }
                )
            }

        _uiState.update { currentState ->
            currentState.copy(
                totalDurationMinutes = totalMin,
                totalCalories = totalCal,
                completedWorkoutsCount = totalCount,
                categoryStats = statsByCategory
            )
        }

        // Cập nhật dữ liệu biểu đồ Vico
        val weeklyMinutes = FloatArray(7) { 0f }
        if (workouts.isNotEmpty()) {
            weeklyMinutes[0] = totalMin.toFloat()
        }

        val entries = weeklyMinutes.mapIndexed { index, minutes ->
            entryOf(index.toFloat(), minutes)
        }

        chartEntryModelProducer.setEntries(entries)
    }

    // 2. Tính năng Thiết lập Mục tiêu (Goals): Cho phép người dùng tùy chỉnh chỉ tiêu
    fun updateGoals(newTargetWorkouts: Int, newTargetDurationMinutes: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                targetWorkouts = newTargetWorkouts,
                targetDurationMinutes = newTargetDurationMinutes
            )
        }
    }
}