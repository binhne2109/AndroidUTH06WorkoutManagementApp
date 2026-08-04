package com.example.ui

import com.example.data.model.WorkoutEntity

data class WorkoutUiState(
    val workouts: List<WorkoutEntity> = emptyList(),
    val filteredWorkouts: List<WorkoutEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "Tất cả",
    val isAddEditSheetOpen: Boolean = false,
    val editingWorkout: WorkoutEntity? = null,
    val deletingWorkout: WorkoutEntity? = null,
    val snackbarMessage: String? = null
) {
    val totalWorkouts: Int get() = workouts.size
    val totalDurationMinutes: Int get() = workouts.sumOf { it.durationMinutes }
    val totalCaloriesBurned: Int get() = workouts.sumOf { it.caloriesBurned }
}