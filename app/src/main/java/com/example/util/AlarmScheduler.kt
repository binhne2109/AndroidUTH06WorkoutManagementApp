package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.WorkoutEntity
import com.example.receiver.WorkoutAlarmReceiver
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleWorkoutAlarm(workout: WorkoutEntity) {
        if (workout.startTimeMillis == 0L) return

        val intent = Intent(context, WorkoutAlarmReceiver::class.java).apply {
            putExtra("WORKOUT_ID", workout.id)
            putExtra("WORKOUT_TITLE", workout.title)
            putExtra("RECURRING_DAYS", workout.recurringDays)
            putExtra("START_TIME_MILLIS", workout.startTimeMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            workout.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val targetTime = Calendar.getInstance().apply {
            val scheduledCal = Calendar.getInstance().apply { timeInMillis = workout.startTimeMillis }
            
            // Đặt giờ và phút từ lịch trình
            set(Calendar.HOUR_OF_DAY, scheduledCal.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, scheduledCal.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Nếu là lặp lại, tìm ngày gần nhất trong tương lai phù hợp với lịch
            if (workout.recurringDays > 0) {
                // Nếu giờ hôm nay đã qua, bắt đầu tính từ ngày mai
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
                
                // Vòng lặp tối đa 7 ngày để tìm ngày được chọn trong bitmask
                var daysChecked = 0
                while (daysChecked < 7) {
                    val dayOfWeek = get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
                    // Chuyển sang format 0=Mon...6=Sun của chúng ta
                    val myDayIndex = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
                    
                    if ((workout.recurringDays and (1 shl myDayIndex)) != 0) {
                        break // Đã tìm thấy ngày phù hợp
                    }
                    add(Calendar.DAY_OF_YEAR, 1)
                    daysChecked++
                }
            } else if (timeInMillis <= System.currentTimeMillis()) {
                // Nếu không lặp lại và giờ đã qua, mặc định nhắc vào ngày mai
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                targetTime.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                targetTime.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelWorkoutAlarm(workout: WorkoutEntity) {
        val intent = Intent(context, WorkoutAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            workout.id.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
