package com.example.data.repository

import com.example.data.local.FirestoreManager
import com.example.data.model.WorkoutEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class WorkoutRepository {
    // Kết nối với Firestore đã kích hoạt Offline Persistence
    private val db: FirebaseFirestore = FirestoreManager.getFirestore()
    private val workoutsCollection = db.collection(FirestoreManager.COLLECTION_WORKOUTS)

    // Lắng nghe dữ liệu realtime từ Firestore (hoạt động tốt cả khi Offline nhờ Local Cache)
    fun getAllWorkouts(userId: String): Flow<List<WorkoutEntity>> = callbackFlow {
        val subscription = workoutsCollection
            .whereEqualTo("userId", userId) // CHỈ LỌC. Không orderBy để tránh yêu cầu Composite Index trên Firestore
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

    /**
     * Lấy toàn bộ danh sách bài tập một lần duy nhất (phục vụ trích xuất dữ liệu / Export)
     */
    suspend fun getWorkoutsOnce(userId: String): List<WorkoutEntity> {
        val snapshot = workoutsCollection
            .whereEqualTo("userId", userId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            val workout = doc.toObject(WorkoutEntity::class.java)
            workout?.apply { id = doc.id }
        }
    }

    suspend fun insert(workout: WorkoutEntity) {
        workoutsCollection.add(workout).await() // Đẩy lên mây / lưu cache local khi offline
    }

    suspend fun update(workout: WorkoutEntity) {
        workoutsCollection.document(workout.id).set(workout).await() // Ghi đè theo ID
    }

    suspend fun delete(workout: WorkoutEntity) {
        workoutsCollection.document(workout.id).delete().await() // Xóa theo ID
    }
}