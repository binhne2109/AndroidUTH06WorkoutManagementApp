package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.WorkoutEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.WorkoutRepository
import com.example.util.AlarmScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private var workoutJob: Job? = null
    private val repository = WorkoutRepository()
    private val authRepository = AuthRepository()
    private val alarmScheduler = AlarmScheduler(application)

    private val currentUserId: String
        get() = authRepository.currentUser?.uid ?: ""

    private val _uiState = MutableStateFlow(WorkoutUiState(workouts = emptyList(), filteredWorkouts = emptyList()))
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    init {
        loadWorkouts()
    }

    fun loadWorkouts() {
        if (currentUserId.isBlank()) return

        workoutJob?.cancel()

        workoutJob = viewModelScope.launch {
            repository.getAllWorkouts(currentUserId).collect { workoutsList ->
                val sortedList = workoutsList.sortedByDescending { it.dateMillis }

                _uiState.update { currentState ->
                    val filtered = filterList(sortedList, currentState.searchQuery, currentState.selectedCategory)
                    currentState.copy(
                        workouts = sortedList,
                        filteredWorkouts = filtered
                    )
                }
            }
        }
    }

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
        notes: String,
        startTimeMillis: Long,
        recurringDays: Int
    ) {
        if (currentUserId.isBlank()) return

        val editing = _uiState.value.editingWorkout

        viewModelScope.launch {
            val workoutToSave = if (editing != null) {
                editing.copy(
                    title = title,
                    category = category,
                    durationMinutes = durationMinutes,
                    caloriesBurned = caloriesBurned,
                    intensity = intensity,
                    notes = notes,
                    startTimeMillis = startTimeMillis,
                    recurringDays = recurringDays
                )
            } else {
                WorkoutEntity(
                    userId = currentUserId,
                    title = title,
                    category = category,
                    durationMinutes = durationMinutes,
                    caloriesBurned = caloriesBurned,
                    intensity = intensity,
                    notes = notes,
                    startTimeMillis = startTimeMillis,
                    recurringDays = recurringDays
                )
            }

            if (editing != null) {
                repository.update(workoutToSave)
                _uiState.update { it.copy(snackbarMessage = "Đã cập nhật bài tập") }
            } else {
                repository.insert(workoutToSave)
                _uiState.update { it.copy(snackbarMessage = "Đã thêm bài tập mới") }
            }

            // Lập lịch báo thức
            if (workoutToSave.startTimeMillis > 0) {
                alarmScheduler.scheduleWorkoutAlarm(workoutToSave)
            } else {
                alarmScheduler.cancelWorkoutAlarm(workoutToSave)
            }

            closeAddEditDialog()
        }
    }

    fun requestDeleteWorkout(workout: WorkoutEntity) {
        _uiState.update { it.copy(deletingWorkout = workout) }
    }

    fun cancelDeleteWorkout() {
        _uiState.update { it.copy(deletingWorkout = null) }
    }

    fun confirmDeleteWorkout() {
        val target = _uiState.value.deletingWorkout ?: return

        viewModelScope.launch {
            repository.delete(target)
            alarmScheduler.cancelWorkoutAlarm(target)
            _uiState.update { it.copy(
                deletingWorkout = null,
                snackbarMessage = "Đã xóa bài tập: ${target.title}"
            )}
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
