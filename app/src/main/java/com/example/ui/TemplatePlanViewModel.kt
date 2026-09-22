package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.PlanItem
import com.example.data.model.WorkoutPlan
import com.example.data.model.WorkoutTemplate
import com.example.data.repository.AuthRepository
import com.example.data.repository.PlanRepository
import com.example.data.repository.TemplateRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 *  ViewModel cho tính năng "Mẫu bài tập" (Reusable Templates)
 * và "Kế hoạch tập" (Workout Plans).
 *
 * Được tách hoàn toàn khỏi WorkoutViewModel để không đụng chạm code quản lý
 * nhật ký bài tập của các thành viên khác. Việc thật sự TẠO một bài tập
 * (WorkoutEntity) từ một Mẫu hoặc từ một Kế hoạch vẫn đi qua
 * WorkoutViewModel.saveWorkout(...) sẵn có, thông qua callback `onCreateWorkout`
 * truyền vào từ màn hình (xem TemplatePlanScreen.kt).
 */
class TemplatePlanViewModel : ViewModel() {

    private val templateRepository = TemplateRepository()
    private val planRepository = PlanRepository()
    private val authRepository = AuthRepository()

    private val currentUserId: String
        get() = authRepository.currentUser?.uid ?: ""

    private var templateJob: Job? = null
    private var planJob: Job? = null

    private val _uiState = MutableStateFlow(TemplatePlanUiState())
    val uiState: StateFlow<TemplatePlanUiState> = _uiState.asStateFlow()

    init {
        loadTemplates()
        loadPlans()
    }

    // ========================= MẪU BÀI TẬP (TEMPLATES) =========================

    fun loadTemplates() {
        if (currentUserId.isBlank()) return
        templateJob?.cancel()
        templateJob = viewModelScope.launch {
            templateRepository.getAllTemplates(currentUserId).collect { list ->
                val sorted = list.sortedByDescending { it.createdAtMillis }
                _uiState.update { it.copy(templates = sorted) }
            }
        }
    }

    fun openAddTemplateDialog() {
        _uiState.update { it.copy(editingTemplate = null, isTemplateSheetOpen = true) }
    }

    fun openEditTemplateDialog(template: WorkoutTemplate) {
        _uiState.update { it.copy(editingTemplate = template, isTemplateSheetOpen = true) }
    }

    fun closeTemplateDialog() {
        _uiState.update { it.copy(isTemplateSheetOpen = false, editingTemplate = null) }
    }

    fun saveTemplate(name: String, category: String, location: String, durationMinutes: Int) {
        if (currentUserId.isBlank() || name.isBlank()) return
        val editing = _uiState.value.editingTemplate

        viewModelScope.launch {
            if (editing != null) {
                val updated = editing.copy(
                    name = name,
                    category = category,
                    location = location,
                    durationMinutes = durationMinutes
                )
                templateRepository.update(updated)
                _uiState.update { it.copy(snackbarMessage = "Đã cập nhật mẫu bài tập") }
            } else {
                val newTemplate = WorkoutTemplate(
                    userId = currentUserId,
                    name = name,
                    category = category,
                    location = location,
                    durationMinutes = durationMinutes
                )
                templateRepository.insert(newTemplate)
                _uiState.update { it.copy(snackbarMessage = "Đã lưu mẫu bài tập mới") }
            }
            closeTemplateDialog()
        }
    }

    fun requestDeleteTemplate(template: WorkoutTemplate) {
        _uiState.update { it.copy(deletingTemplate = template) }
    }

    fun cancelDeleteTemplate() {
        _uiState.update { it.copy(deletingTemplate = null) }
    }

    fun confirmDeleteTemplate() {
        val target = _uiState.value.deletingTemplate ?: return
        viewModelScope.launch {
            templateRepository.delete(target)
            _uiState.update {
                it.copy(deletingTemplate = null, snackbarMessage = "Đã xóa mẫu: ${target.name}")
            }
        }
    }

    // --------- Luồng Auto-fill: chọn một mẫu để khởi tạo bài tập mới ---------

    /** Người dùng bấm "Dùng mẫu này" -> mở biểu mẫu tạo bài tập đã được auto-fill. */
    fun selectTemplateToApply(template: WorkoutTemplate) {
        _uiState.update { it.copy(templateToApply = template) }
    }

    fun clearTemplateToApply() {
        _uiState.update { it.copy(templateToApply = null) }
    }

    // ========================= KẾ HOẠCH TẬP (PLANS) =========================

    fun loadPlans() {
        if (currentUserId.isBlank()) return
        planJob?.cancel()
        planJob = viewModelScope.launch {
            planRepository.getAllPlans(currentUserId).collect { list ->
                val sorted = list.sortedByDescending { it.createdAtMillis }
                _uiState.update { it.copy(plans = sorted) }
            }
        }
    }

    fun openCreatePlanEditor() {
        _uiState.update {
            it.copy(editingPlan = null, planDraftItems = emptyList(), isPlanEditorOpen = true)
        }
    }

    fun openEditPlanEditor(plan: WorkoutPlan) {
        _uiState.update {
            it.copy(editingPlan = plan, planDraftItems = plan.orderedItems, isPlanEditorOpen = true)
        }
    }

    fun closePlanEditor() {
        _uiState.update {
            it.copy(isPlanEditorOpen = false, editingPlan = null, planDraftItems = emptyList())
        }
    }

    /** Thêm một bài tập (tự nhập) vào cuối chuỗi kế hoạch đang soạn. */
    fun addDraftItem(title: String, category: String, location: String, durationMinutes: Int, notes: String = "") {
        if (title.isBlank()) return
        _uiState.update { state ->
            val newItem = PlanItem(
                order = state.planDraftItems.size,
                title = title,
                category = category,
                location = location,
                durationMinutes = durationMinutes,
                notes = notes
            )
            state.copy(planDraftItems = state.planDraftItems + newItem)
        }
    }

    /** Auto-fill: thêm nhanh một Mẫu bài tập có sẵn vào chuỗi kế hoạch. */
    fun addTemplateToDraft(template: WorkoutTemplate) {
        addDraftItem(
            title = template.name,
            category = template.category,
            location = template.location,
            durationMinutes = template.durationMinutes
        )
    }

    fun removeDraftItem(index: Int) {
        _uiState.update { state ->
            val reordered = state.planDraftItems
                .filterIndexed { i, _ -> i != index }
                .mapIndexed { i, item -> item.copy(order = i) }
            state.copy(planDraftItems = reordered)
        }
    }

    /** Di chuyển một bài tập trong chuỗi lên/xuống để sắp lại thứ tự. */
    fun moveDraftItem(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val items = state.planDraftItems.toMutableList()
            if (fromIndex !in items.indices || toIndex !in items.indices) return@update state
            val moved = items.removeAt(fromIndex)
            items.add(toIndex, moved)
            state.copy(planDraftItems = items.mapIndexed { i, item -> item.copy(order = i) })
        }
    }

    fun savePlan(name: String, description: String) {
        if (currentUserId.isBlank() || name.isBlank()) return
        val draftItems = _uiState.value.planDraftItems
        if (draftItems.isEmpty()) return
        val editing = _uiState.value.editingPlan

        viewModelScope.launch {
            if (editing != null) {
                val updated = editing.copy(name = name, description = description, items = draftItems)
                planRepository.update(updated)
                _uiState.update { it.copy(snackbarMessage = "Đã cập nhật kế hoạch tập") }
            } else {
                val newPlan = WorkoutPlan(
                    userId = currentUserId,
                    name = name,
                    description = description,
                    items = draftItems
                )
                planRepository.insert(newPlan)
                _uiState.update { it.copy(snackbarMessage = "Đã tạo kế hoạch tập mới") }
            }
            closePlanEditor()
        }
    }

    fun requestDeletePlan(plan: WorkoutPlan) {
        _uiState.update { it.copy(deletingPlan = plan) }
    }

    fun cancelDeletePlan() {
        _uiState.update { it.copy(deletingPlan = null) }
    }

    fun confirmDeletePlan() {
        val target = _uiState.value.deletingPlan ?: return
        viewModelScope.launch {
            planRepository.delete(target)
            _uiState.update {
                it.copy(deletingPlan = null, snackbarMessage = "Đã xóa kế hoạch: ${target.name}")
            }
        }
    }

    /**
     * Thực hiện kế hoạch: lần lượt tạo từng bài tập trong chuỗi thành một
     * bài tập thật trong nhật ký, thông qua callback [onCreateWorkout]
     * (thường được bind tới WorkoutViewModel.saveWorkout ở màn hình).
     */
    fun startPlan(
        plan: WorkoutPlan,
        onCreateWorkout: (title: String, category: String, duration: Int, calories: Int, intensity: String, notes: String) -> Unit
    ) {
        plan.orderedItems.forEach { item ->
            val notesWithLocation = buildString {
                if (item.location.isNotBlank()) append("Địa điểm: ${item.location}")
                if (item.notes.isNotBlank()) {
                    if (isNotEmpty()) append(" • ")
                    append(item.notes)
                }
            }
            onCreateWorkout(item.title, item.category, item.durationMinutes, 0, "Medium", notesWithLocation)
        }
        _uiState.update {
            it.copy(snackbarMessage = "Đã thêm ${plan.totalExercises} bài tập từ kế hoạch \"${plan.name}\"")
        }
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
