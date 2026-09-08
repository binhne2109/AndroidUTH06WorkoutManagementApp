package com.example.ui

import com.example.data.model.PlanItem
import com.example.data.model.WorkoutPlan
import com.example.data.model.WorkoutTemplate

/**
 * Trạng thái màn hình cho Mẫu bài tập & Kế hoạch tập.
 * Tách riêng khỏi WorkoutUiState để không thay đổi state của tính năng
 * nhật ký bài tập đã có sẵn.
 */
data class TemplatePlanUiState(
    // --- Mẫu bài tập (Templates) ---
    val templates: List<WorkoutTemplate> = emptyList(),
    val isTemplateSheetOpen: Boolean = false,
    val editingTemplate: WorkoutTemplate? = null,
    val deletingTemplate: WorkoutTemplate? = null,

    // --- Luồng Auto-fill: mẫu đang được chọn để tạo bài tập mới ---
    val templateToApply: WorkoutTemplate? = null,

    // --- Kế hoạch tập (Plans) ---
    val plans: List<WorkoutPlan> = emptyList(),
    val isPlanEditorOpen: Boolean = false,
    val editingPlan: WorkoutPlan? = null,
    val deletingPlan: WorkoutPlan? = null,
    val planDraftItems: List<PlanItem> = emptyList(),

    val snackbarMessage: String? = null
)
