package com.example.data.model

/**
 * [Thành viên 1: Quản trị Lưu trữ và Trích xuất dữ liệu]
 *
 * Thực thể Mục tiêu cá nhân (Personal Goal) trong hệ thống Firestore.
 *
 * Cho phép người dùng đặt mục tiêu tập luyện theo:
 * - CALORIES: Lượng calo cần đốt cháy (kcal)
 * - DURATION: Tổng thời gian tập luyện (phút)
 * - WORKOUT_COUNT: Số buổi/bài tập cần hoàn thành
 * - DISTANCE: Quãng đường di chuyển (km)
 *
 * Lưu trong Firestore tại collection riêng "personal_goals".
 */
data class PersonalGoal(
    var id: String = "",                         // Firestore tự sinh Document ID
    val userId: String = "",                     // ID người dùng Firebase Auth
    val title: String = "",                      // Tiêu đề mục tiêu (VD: "Đốt 3000 kcal tuần này")
    val targetType: String = "CALORIES",         // CALORIES, DURATION, WORKOUT_COUNT, DISTANCE
    val targetValue: Double = 0.0,               // Giá trị mục tiêu hướng tới
    val currentValue: Double = 0.0,              // Giá trị đã tích lũy hiện tại
    val unit: String = "kcal",                   // Đơn vị đo hiển thị (kcal, phút, bài, km)
    val startDateMillis: Long = System.currentTimeMillis(), // Ngày bắt đầu
    val endDateMillis: Long = System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000, // Hạn chót mục tiêu (mặc định 7 ngày)
    val completed: Boolean = false,              // Trạng thái đã hoàn thành mục tiêu
    val notes: String = "",                      // Ghi chú / Động lực
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    /** Tỷ lệ phần trăm hoàn thành (0% - 100%) */
    val progressPercentage: Float
        get() = if (targetValue > 0) {
            ((currentValue / targetValue) * 100).toFloat().coerceIn(0f, 100f)
        } else 0f

    /** Kiểm tra xem mục tiêu đã hết hạn hay chưa */
    val isExpired: Boolean
        get() = System.currentTimeMillis() > endDateMillis && !completed
}
