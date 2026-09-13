package com.example.data.repository

import com.example.data.model.WorkoutEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class WorkoutRepository {
    // Kết nối với Firestore
    private val db = FirebaseFirestore.getInstance()
    private val workoutsCollection = db.collection("workouts")

    // Lắng nghe dữ liệu realtime từ Firestore
    fun getAllWorkouts(userId: String): Flow<List<WorkoutEntity>> = callbackFlow {
        val subscription = workoutsCollection
            .whereEqualTo("userId", userId) // CHỈ LỌC. Đã xóa dòng orderBy để tránh lỗi Index!
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val workouts = snapshot.documents.mapNotNull { doc ->
                        val workout = doc.toObject(WorkoutEntity::class.java)
                        workout?.apply { id = doc.id }
                    }
                    trySend(workouts)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun insert(workout: WorkoutEntity) {
        workoutsCollection.add(workout).await() // Đẩy lên mây, Firebase tự tạo ID
    }

    suspend fun update(workout: WorkoutEntity) {
        workoutsCollection.document(workout.id).set(workout).await() // Ghi đè theo ID
    }

    suspend fun delete(workout: WorkoutEntity) {
        workoutsCollection.document(workout.id).delete().await() // Xóa theo ID
    }
}