package com.example.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.WorkoutEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.WorkoutRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

// Kế thừa AndroidViewModel để lấy được Context khởi tạo Room Database
class WorkoutViewModel : ViewModel() {
    private var workoutJob: Job? = null
    // 1. Khởi tạo Database và kết nối Repository
    private val repository = WorkoutRepository()
    private val authRepository = AuthRepository()
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
            if (editing != null) {
                // Sửa bài tập
                val updatedWorkout = editing.copy(
                    title = title,
                    category = category,
                    durationMinutes = durationMinutes,
                    caloriesBurned = caloriesBurned,
                    intensity = intensity,
                    notes = notes,
                    dateMillis = dateMillis,
                    startTime = startTime,
                    endTime = endTime
                )

                repository.update(updatedWorkout)
                _uiState.update {
                    it.copy(snackbarMessage = "Đã cập nhật bài tập")
                }
            } else {
                // Thêm bài tập mới
                val start = System.currentTimeMillis()
                val end = start + durationMinutes * 60_000L

                val newWorkout = WorkoutEntity(
                    userId = currentUserId,
                    title = title,
                    category = category,
                    durationMinutes = durationMinutes,
                    caloriesBurned = caloriesBurned,
                    intensity = intensity,
                    notes = notes,
                    dateMillis = start,
                    startTime = start,
                    endTime = end,
                    completed = false
                )

                repository.insert(newWorkout)
                _uiState.update {
                    it.copy(snackbarMessage = "Đã thêm bài tập mới")
                }
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