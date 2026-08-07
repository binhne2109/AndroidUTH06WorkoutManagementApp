package com.example.data.repository

import com.example.data.local.WorkoutDao
import com.example.data.model.WorkoutEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WorkoutRepository(private val workoutDao: WorkoutDao) {

    val allWorkouts: Flow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()

    suspend fun insert(workout: WorkoutEntity) {
        withContext(Dispatchers.IO) { // Chuyển sang luồng IO để chạy ngầm
            workoutDao.insertWorkout(workout)
        }
    }

    suspend fun update(workout: WorkoutEntity) {
        withContext(Dispatchers.IO) {
            workoutDao.updateWorkout(workout)
        }
    }

    suspend fun delete(workout: WorkoutEntity) {
        withContext(Dispatchers.IO) {
            workoutDao.deleteWorkout(workout)
        }
    }
}