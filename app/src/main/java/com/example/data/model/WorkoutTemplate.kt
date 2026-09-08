package com.example.data.model

/**
 *  Mẫu bài tập (Reusable Template).
 *
 * Lưu lại cấu hình TĨNH của một bài tập (Tên, Loại, Địa điểm, Thời lượng)
 * để người dùng có thể tái sử dụng nhiều lần thay vì phải nhập lại từ đầu.
 * Khi người dùng chọn một mẫu, dữ liệu ở đây sẽ được dùng để tự động điền
 * (auto-fill) vào biểu mẫu tạo bài tập mới (xem ApplyTemplateSheet.kt).
 *
 * Được lưu trong Firestore, collection riêng "workout_templates",
 * hoàn toàn tách biệt với collection "workouts" (nhật ký bài tập thật)
 * để không ảnh hưởng tới dữ liệu / code của các thành viên khác.
 */
data class WorkoutTemplate(
    var id: String = "", // Firestore tự gán khi tạo (giống WorkoutEntity)
    val userId: String = "",
    val name: String = "",          // Tên bài tập
    val category: String = "",      // Loại (Strength, Cardio, HIIT, Yoga,...)
    val location: String = "",      // Địa điểm (Phòng gym, Công viên, Ở nhà,...)
    val durationMinutes: Int = 0,   // Thời lượng mặc định (phút)
    val createdAtMillis: Long = System.currentTimeMillis()
)
