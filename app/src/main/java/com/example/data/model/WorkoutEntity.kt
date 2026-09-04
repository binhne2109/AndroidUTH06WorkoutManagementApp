package com.example.data.model
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "workouts")
data class WorkoutEntity(
    var id: String = "", // Đổi thành kiểu String và dùng var để Firestore tự gán ID
    val userId: String = "",
    val title: String = "",
    val category: String = "",
    val durationMinutes: Int = 0,
    val caloriesBurned: Int = 0,
    val intensity: String = "Medium",
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)