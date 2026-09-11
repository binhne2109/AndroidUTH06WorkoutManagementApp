package com.example.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.WorkoutEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CalendarViewModel : ViewModel() {

    private val repository = WorkoutRepository()
    private val authRepository = AuthRepository()

    private val _workouts = MutableStateFlow<List<WorkoutEntity>>(emptyList())

    val workouts: StateFlow<List<WorkoutEntity>> = _workouts.asStateFlow()

    init {
        loadWorkouts()
    }

    private fun loadWorkouts() {

        val userId = authRepository.currentUser?.uid ?: return

        viewModelScope.launch {

            repository.getAllWorkouts(userId).collect { workoutList -> _workouts.value = workoutList }
        }
    }
}