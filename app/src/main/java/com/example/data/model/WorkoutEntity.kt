package com.example.data.model
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // "Strength", "Cardio", "HIIT", "Yoga", "Pilates"
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val intensity: String = "Medium", // "Dễ", "Trung bình", "Nặng", "Cực nặng"
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)