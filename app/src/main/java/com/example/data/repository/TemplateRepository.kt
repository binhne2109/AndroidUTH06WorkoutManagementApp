package com.example.data.repository

import com.example.data.model.WorkoutTemplate
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository quản lý Mẫu bài tập (Templates) trên Firestore.
 * Viết theo đúng khuôn mẫu của WorkoutRepository để đồng bộ phong cách code
 * trong dự án, nhưng dùng collection riêng "workout_templates" để không
 * đụng vào dữ liệu "workouts" (nhật ký bài tập) của các thành viên khác.
 */
class TemplateRepository {
    private val db = FirebaseFirestore.getInstance()
    private val templatesCollection = db.collection("workout_templates")

    fun getAllTemplates(userId: String): Flow<List<WorkoutTemplate>> = callbackFlow {
        val subscription = templatesCollection
            .whereEqualTo("userId", userId) // Chỉ lọc, không orderBy để tránh lỗi thiếu Index
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val templates = snapshot.documents.mapNotNull { doc ->
                        val template = doc.toObject(WorkoutTemplate::class.java)
                        template?.apply { id = doc.id }
                    }
                    trySend(templates)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun insert(template: WorkoutTemplate) {
        templatesCollection.add(template).await()
    }

    suspend fun update(template: WorkoutTemplate) {
        templatesCollection.document(template.id).set(template).await()
    }

    suspend fun delete(template: WorkoutTemplate) {
        templatesCollection.document(template.id).delete().await()
    }
}
