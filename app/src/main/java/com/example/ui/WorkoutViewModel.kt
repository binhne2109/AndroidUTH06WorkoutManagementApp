package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.data.model.WorkoutEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WorkoutViewModel : ViewModel() {

    // Mock Dữ liệu ban đầu
    private val initialMockData = listOf(
        WorkoutEntity(
            id = 1,
            title = "Buổi Tập Full Body Strength",
            category = "Strength",
            durationMinutes = 50,
            caloriesBurned = 420,
            intensity = "Nặng",
            notes = "Bench press 4x10, Squat 4x12"
        ),
        WorkoutEntity(
            id = 2,
            title = "Chạy Bộ Buổi Sáng",
            category = "Cardio",
            durationMinutes = 35,
            caloriesBurned = 310,
            intensity = "Trung bình",
            notes = "Chạy công viên 5km, pace 6:30 min/km"
        ),
        WorkoutEntity(
            id = 3,
            title = "HIIT Đốt Mỡ Siêu Cấp",
            category = "HIIT",
            durationMinutes = 25,
            caloriesBurned = 350,
            intensity = "Cực nặng",
            notes = "Burpees, Mountain climbers (45s work / 15s rest)"
        ),
        WorkoutEntity(
            id = 4,
            title = "Yoga Thư Giãn",
            category = "Yoga",
            durationMinutes = 40,
            caloriesBurned = 180,
            intensity = "Dễ",
            notes = "Hatha Yoga tập trung thở sâu"
        )
    )

    private val _uiState = MutableStateFlow(
        WorkoutUiState(
            workouts = initialMockData,
            filteredWorkouts = initialMockData
        )
    )
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _uiState.update { currentState ->
            val filtered = filterList(currentState.workouts, query, currentState.selectedCategory)
            currentState.copy(searchQuery = query, filteredWorkouts = filtered)
        }
    }

    fun onCategorySelected(category: String) {
        _uiState.update { currentState ->
            val filtered = filterList(currentState.workouts, currentState.searchQuery, category)
            currentState.copy(selectedCategory = category, filteredWorkouts = filtered)
        }
    }

    fun openAddWorkoutDialog() {
        _uiState.update { it.copy(editingWorkout = null, isAddEditSheetOpen = true) }
    }

    fun openEditWorkoutDialog(workout: WorkoutEntity) {
        _uiState.update { it.copy(editingWorkout = workout, isAddEditSheetOpen = true) }
    }

    fun closeAddEditDialog() {
        _uiState.update { it.copy(isAddEditSheetOpen = false, editingWorkout = null) }
    }

    fun saveWorkout(
        title: String,
        category: String,
        durationMinutes: Int,
        caloriesBurned: Int,
        intensity: String,
        notes: String
    ) {
        _uiState.update { state ->
            val editing = state.editingWorkout
            val updatedList = if (editing != null) {
                // Sửa bài tập
                state.workouts.map {
                    if (it.id == editing.id) {
                        it.copy(
                            title = title,
                            category = category,
                            durationMinutes = durationMinutes,
                            caloriesBurned = caloriesBurned,
                            intensity = intensity,
                            notes = notes
                        )
                    } else it
                }
            } else {
                // Thêm mới
                val newId = (state.workouts.maxOfOrNull { it.id } ?: 0) + 1
                state.workouts + WorkoutEntity(
                    id = newId,
                    title = title,
                    category = category,
                    durationMinutes = durationMinutes,
                    caloriesBurned = caloriesBurned,
                    intensity = intensity,
                    notes = notes
                )
            }
            val filtered = filterList(updatedList, state.searchQuery, state.selectedCategory)
            state.copy(
                workouts = updatedList,
                filteredWorkouts = filtered,
                isAddEditSheetOpen = false,
                editingWorkout = null,
                snackbarMessage = if (editing != null) "Đã cập nhật bài tập" else "Đã thêm bài tập mới"
            )
        }
    }

    fun requestDeleteWorkout(workout: WorkoutEntity) {
        _uiState.update { it.copy(deletingWorkout = workout) }
    }

    fun cancelDeleteWorkout() {
        _uiState.update { it.copy(deletingWorkout = null) }
    }

    fun confirmDeleteWorkout() {
        _uiState.update { state ->
            val target = state.deletingWorkout ?: return@update state
            val updatedList = state.workouts.filter { it.id != target.id }
            val filtered = filterList(updatedList, state.searchQuery, state.selectedCategory)
            state.copy(
                workouts = updatedList,
                filteredWorkouts = filtered,
                deletingWorkout = null,
                snackbarMessage = "Đã xóa bài tập: ${target.title}"
            )
        }
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun filterList(list: List<WorkoutEntity>, query: String, category: String): List<WorkoutEntity> {
        return list.filter { item ->
            val matchesCategory = (category == "Tất cả") || item.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.notes.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }
}