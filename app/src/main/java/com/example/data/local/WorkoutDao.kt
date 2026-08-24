package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWorkout(workout: WorkoutEntity)

    @Update
    fun updateWorkout(workout: WorkoutEntity)

    @Delete
    fun deleteWorkout(workout: WorkoutEntity)

    @Query("SELECT * FROM workouts ORDER BY dateMillis DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>
}