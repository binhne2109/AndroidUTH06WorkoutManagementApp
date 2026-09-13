package com.example.data.repository

import com.example.data.local.FirestoreManager
import com.example.data.model.PersonalGoal
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * [Thành viên 1: Quản trị Lưu trữ và Trích xuất dữ liệu]
 *
 * Repository quản lý Mục tiêu cá nhân (Personal Goals) trên Firestore.
 * Tương tác với collection "personal_goals", hỗ trợ Offline Persistence
 * giúp người dùng cập nhật và theo dõi tiến độ ngay cả khi không có kết nối mạng.
 */
class GoalRepository {
    private val db: FirebaseFirestore = FirestoreManager.getFirestore()
    private val goalsCollection = db.collection(FirestoreManager.COLLECTION_GOALS)

    /**
     * Lắng nghe danh sách mục tiêu realtime của người dùng từ Firestore.
     */
    fun getAllGoals(userId: String): Flow<List<PersonalGoal>> = callbackFlow {
        val subscription = goalsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val goals = snapshot.documents.mapNotNull { doc ->
                        val goal = doc.toObject(PersonalGoal::class.java)
                        goal?.apply { id = doc.id }
                    }
                    trySend(goals)
                }
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Thêm một mục tiêu cá nhân mới vào Firestore.
     */
    suspend fun insert(goal: PersonalGoal) {
        goalsCollection.add(goal).await()
    }

    /**
     * Cập nhật toàn bộ thông tin mục tiêu.
     */
    suspend fun update(goal: PersonalGoal) {
        goalsCollection.document(goal.id).set(goal).await()
    }

    /**
     * Xóa mục tiêu theo ID.
     */
    suspend fun delete(goal: PersonalGoal) {
        goalsCollection.document(goal.id).delete().await()
    }

    /**
     * Cập nhật tiến độ hiện tại và trạng thái hoàn thành của mục tiêu.
     */
    suspend fun updateProgress(goalId: String, currentProgress: Double, isCompleted: Boolean) {
        goalsCollection.document(goalId).update(
            mapOf(
                "currentValue" to currentProgress,
                "completed" to isCompleted
            )
        ).await()
    }
}
