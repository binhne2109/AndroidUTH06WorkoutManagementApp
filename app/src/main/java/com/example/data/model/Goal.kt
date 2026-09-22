package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Định nghĩa loại mục tiêu: Theo Tuần hoặc Theo Tháng
enum class GoalType {
    WEEKLY,
    MONTHLY
}

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey
    val id: String = "default_goal", // Mã mục tiêu
    val type: GoalType = GoalType.WEEKLY, // Loại mục tiêu (mặc định là theo tuần)
    val targetWorkouts: Int = 4,         // Mục tiêu số buổi tập (mặc định: 4 buổi/tuần)
    val targetDurationMinutes: Int = 180, // Mục tiêu tổng thời gian tập (mặc định: 180 phút/tuần)
    val startDate: Long = System.currentTimeMillis(), // Ngày bắt đầu áp dụng mục tiêu
    val endDate: Long = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L) // Ngày kết thúc
)