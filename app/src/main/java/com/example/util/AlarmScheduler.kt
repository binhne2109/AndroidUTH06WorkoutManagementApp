package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.WorkoutEntity
import com.example.receiver.WorkoutAlarmReceiver
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleWorkoutAlarm(workout: WorkoutEntity) {
        val alarmTime = if (workout.startTimeMillis > 0) workout.startTimeMillis else workout.startTime
        if (alarmTime == 0L) return

        val intent = Intent(context, WorkoutAlarmReceiver::class.java).apply {
            putExtra("WORKOUT_ID", workout.id)
            putExtra("WORKOUT_TITLE", workout.title)
            putExtra("RECURRING_DAYS", workout.recurringDays)
            putExtra("START_TIME_MILLIS", alarmTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            workout.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val targetTime = Calendar.getInstance().apply {
            timeInMillis = alarmTime
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Nếu giờ đã qua trong quá khứ
            if (timeInMillis <= System.currentTimeMillis()) {
                if (workout.recurringDays > 0) {
                    add(Calendar.DAY_OF_YEAR, 1)
                    var daysChecked = 0
                    while (daysChecked < 7) {
                        val dayOfWeek = get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
                        val myDayIndex = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
                        if ((workout.recurringDays and (1 shl myDayIndex)) != 0) {
                            break
                        }
                        add(Calendar.DAY_OF_YEAR, 1)
                        daysChecked++
                    }
                } else if (System.currentTimeMillis() - timeInMillis > 60_000L) {
                    // Nếu quá 1 phút so với mốc hẹn hôm nay, đẩy sang ngày mai
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }

        try {
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
            Log.d("AlarmScheduler", "Đã lên lịch báo thức thành công lúc: ${targetTime.time}")
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "Thiếu quyền SCHEDULE_EXACT_ALARM: ${e.message}")
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
