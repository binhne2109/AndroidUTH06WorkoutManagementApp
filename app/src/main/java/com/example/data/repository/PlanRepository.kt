package com.example.data.repository

import com.example.data.model.WorkoutPlan
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 *  Repository quản lý Kế hoạch tập (Workout Plans) trên Firestore.
 * Collection riêng "workout_plans", mỗi document chứa danh sách PlanItem nhúng bên trong.
 */
class PlanRepository {
    private val db = FirebaseFirestore.getInstance()
    private val plansCollection = db.collection("workout_plans")

    fun getAllPlans(userId: String): Flow<List<WorkoutPlan>> = callbackFlow {
        val subscription = plansCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val plans = snapshot.documents.mapNotNull { doc ->
                        val plan = doc.toObject(WorkoutPlan::class.java)
                        plan?.apply { id = doc.id }
                    }
                    trySend(plans)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun insert(plan: WorkoutPlan) {
        plansCollection.add(plan).await()
    }

    suspend fun update(plan: WorkoutPlan) {
        plansCollection.document(plan.id).set(plan).await()
    }

    suspend fun delete(plan: WorkoutPlan) {
        plansCollection.document(plan.id).delete().await()
    }
}
