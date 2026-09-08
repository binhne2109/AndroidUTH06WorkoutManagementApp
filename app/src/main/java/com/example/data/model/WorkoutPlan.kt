package com.example.data.model

/**
 *  Kế hoạch tập (Workout Plan).
 *
 * Cho phép người dùng tạo một chuỗi các bài tập liên tiếp, có tổ chức
 * (ví dụ: "Ngày Chân": Squat 15' -> Lunges 10' -> Plank 5'), lưu lại để
 * dùng nhiều lần thay vì phải tạo từng bài tập rời rạc.
 *
 * Lưu trong Firestore, collection riêng "workout_plans".
 */
data class WorkoutPlan(
    var id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val items: List<PlanItem> = emptyList(),
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    val totalExercises: Int get() = items.size
    val totalDurationMinutes: Int get() = items.sumOf { it.durationMinutes }

    /** Danh sách bài tập trong kế hoạch, luôn được sắp xếp đúng trình tự đã lưu. */
    val orderedItems: List<PlanItem> get() = items.sortedBy { it.order }
}
