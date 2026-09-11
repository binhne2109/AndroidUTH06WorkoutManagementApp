package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.model.WorkoutEntity
import com.example.util.AlarmScheduler
import com.example.util.NotificationHelper
import java.util.Calendar

class WorkoutAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val workoutId = intent.getStringExtra("WORKOUT_ID") ?: ""
        val workoutTitle = intent.getStringExtra("WORKOUT_TITLE") ?: "Bài tập mới"
        val recurringDays = intent.getIntExtra("RECURRING_DAYS", 0)
        val startTimeMillis = intent.getLongExtra("START_TIME_MILLIS", 0L)

        // 1. Hiển thị thông báo
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(
            title = "Đến giờ tập luyện rồi!",
            message = "Đã đến lúc bắt đầu bài tập: $workoutTitle"
        )

        // 2. Nếu có lặp lại, lập lịch cho lần tiếp theo
        if (recurringDays > 0 && workoutId.isNotEmpty()) {
            val scheduler = AlarmScheduler(context)
            // Ở đây bạn có thể cập nhật lại thực thể ảo để lập lịch tiếp
            val mockWorkout = WorkoutEntity(
                id = workoutId,
                title = workoutTitle,
                startTimeMillis = startTimeMillis,
                recurringDays = recurringDays
            )
            scheduler.scheduleWorkoutAlarm(mockWorkout)
        }
    }
}
