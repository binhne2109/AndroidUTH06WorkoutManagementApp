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
import java.time.LocalDate
import java.time.LocalTime

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private var workoutJob: Job? = null
    private val repository = WorkoutRepository()
    private val authRepository = AuthRepository()
    private val alarmScheduler = AlarmScheduler(application)
    // 2. Khởi tạo AuthRepository để lấy mã ID Firebase
    val currentUserId: String
        get() = authRepository.currentUser?.uid ?: ""

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    // Bắt đầu với State rỗng (không dùng Mock Data nữa)
    private val _uiState = MutableStateFlow(WorkoutUiState(workouts = emptyList(), filteredWorkouts = emptyList()))
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    init {
        // Vừa vào app là tải dữ liệu từ CSDL của đúng người dùng đó lên ngay
        loadWorkouts()
    }

    fun loadWorkouts() {
        if (currentUserId.isBlank()) return // Chưa đăng nhập thì bỏ qua

        workoutJob?.cancel() // Hủy kết nối cũ

        workoutJob = viewModelScope.launch {
            // Lắng nghe dữ liệu từ Firestore
            repository.getAllWorkouts(currentUserId).collect { workoutsList ->

                // THÊM DÒNG NÀY: Tự động sắp xếp thời gian giảm dần (mới nhất lên trên)
                val sortedList = workoutsList.sortedByDescending { it.dateMillis }

                _uiState.update { currentState ->
                    val filtered = filterList(sortedList, currentState.searchQuery, currentState.selectedCategory)
                    currentState.copy(
                        workouts = sortedList, // Dùng danh sách đã sắp xếp
                        filteredWorkouts = filtered // Dùng danh sách đã sắp xếp
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
        location: String,
        dateMillis: Long = System.currentTimeMillis(),
        startTime: Long = dateMillis,
        endTime: Long = startTime + durationMinutes * 60_000L
    ) {
        if (currentUserId.isBlank()) return
        if (title.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "Lỗi: Tên bài tập không được để trống!") }
            return
        }
        if (durationMinutes <= 0) {
            _uiState.update { it.copy(snackbarMessage = "Lỗi: Thời lượng tập phải lớn hơn 0 phút!") }
            return
        }
        val editing = _uiState.value.editingWorkout

        viewModelScope.launch {
            val workoutToSave: WorkoutEntity
            if (editing != null) {
                // Sửa bài tập
                workoutToSave = editing.copy(
                    title = title,
                    category = category,
                    durationMinutes = durationMinutes,
                    caloriesBurned = caloriesBurned,
                    intensity = intensity,
                    notes = notes,
                    location = location,
                    dateMillis = dateMillis,
                    startTime = startTime,
                    endTime = endTime,
                    startTimeMillis = startTime
                )

                repository.update(workoutToSave)
                _uiState.update {
                    it.copy(snackbarMessage = "Đã cập nhật bài tập")
                }
            } else {
                // Thêm bài tập mới (Dùng thời gian do người dùng chọn trên giao diện)
                workoutToSave = WorkoutEntity(
                    userId = currentUserId,
                    title = title,
                    category = category,
                    durationMinutes = durationMinutes,
                    caloriesBurned = caloriesBurned,
                    intensity = intensity,
                    notes = notes,
                    location = location,
                    dateMillis = dateMillis,
                    startTime = startTime,
                    endTime = endTime,
                    startTimeMillis = startTime,
                    completed = false
                )

                repository.insert(workoutToSave)
                _uiState.update {
                    it.copy(snackbarMessage = "Đã thêm bài tập mới")
                }
            }

            // Tự động lên lịch báo thức thông báo dựa trên startTimeMillis
            alarmScheduler.scheduleWorkoutAlarm(workoutToSave)

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