package com.example.data.local

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

/**
 * [Thành viên 1: Quản trị Lưu trữ và Trích xuất dữ liệu]
 *
 * Quản lý kết nối Firestore tập trung và cấu hình tính năng Offline Persistence.
 * Giúp ứng dụng hoạt động mượt mà khi không có mạng (Offline First),
 * tự động lưu trữ cục bộ vào Persistent Cache không giới hạn và đồng bộ khi có kết nối trở lại.
 */
object FirestoreManager {
    private const val TAG = "FirestoreManager"

    // Định nghĩa tên các Collection chuẩn trong hệ thống NoSQL Firestore
    const val COLLECTION_WORKOUTS = "workouts"
    const val COLLECTION_TEMPLATES = "workout_templates"
    const val COLLECTION_GOALS = "personal_goals"
    const val COLLECTION_PLANS = "workout_plans"

    @Volatile
    private var isInitialized = false

    /**
     * Khởi tạo cấu hình Firestore với tính năng Offline Persistence.
     * Được gọi trong [WorkoutApplication.onCreate] khi ứng dụng khởi chạy.
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        synchronized(this) {
            if (isInitialized) return
            try {
                val db = FirebaseFirestore.getInstance()
                val settings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(
                        PersistentCacheSettings.newBuilder()
                            .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                            .build()
                    )
                    .build()
                db.firestoreSettings = settings
                isInitialized = true
                Log.i(TAG, "Firestore Offline Persistence initialized successfully with UNLIMITED cache.")
            } catch (e: Exception) {
                Log.w(TAG, "Firestore settings notice (might already be initialized): ${e.message}")
                isInitialized = true
            }
        }
    }

    /**
     * Lấy instance FirebaseFirestore đã được kích hoạt tính năng lưu trữ cục bộ.
     */
    fun getFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}
