package com.example

import android.app.Application
import android.util.Log
import com.example.data.local.FirestoreManager

/**
 * [Thành viên 1: Quản trị Lưu trữ và Trích xuất dữ liệu]
 *
 * Lớp Application tùy biến của ứng dụng, chịu trách nhiệm khởi tạo các dịch vụ toàn cục,
 * đặc biệt là cấu hình Offline Persistence cho Firestore ngay từ thời điểm ứng dụng khởi động.
 */
class WorkoutApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "WorkoutApplication onCreate - Khởi tạo ứng dụng")

        // Kích hoạt tính năng Offline Persistence trên Firebase Firestore
        FirestoreManager.initialize(this)
    }

    companion object {
        private const val TAG = "WorkoutApplication"
    }
}
