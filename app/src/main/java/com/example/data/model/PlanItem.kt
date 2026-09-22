package com.example.data.model

/**
 *  Một bài tập nằm trong chuỗi của Kế hoạch tập (WorkoutPlan).
 *
 * `order` là thứ tự của bài tập này trong kế hoạch (0-based), quyết định
 * trình tự hiển thị và trình tự khi "Bắt đầu kế hoạch" tạo lần lượt các
 * bài tập thật vào nhật ký.
 *
 * Đây là object nhúng (embedded) bên trong WorkoutPlan, không phải là
 * một collection Firestore riêng.
 */
data class PlanItem(
    val order: Int = 0,
    val title: String = "",
    val category: String = "",
    val location: String = "",
    val durationMinutes: Int = 0,
    val notes: String = ""
)
