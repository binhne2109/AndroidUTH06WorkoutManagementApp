package com.example.ui.components

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.WorkoutEntity
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutAddEditSheet(
    editingWorkout: WorkoutEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, duration: Int, calories: Int, intensity: String, notes: String, startTime: Long, recurring: Int) -> Unit,
) {
    val context = LocalContext.current
    var title by remember(editingWorkout) { mutableStateOf(editingWorkout?.title ?: "") }
    var category by remember(editingWorkout) { mutableStateOf(editingWorkout?.category ?: "Strength") }
    var duration by remember(editingWorkout) { mutableStateOf(editingWorkout?.durationMinutes?.toString() ?: "30") }
    var calories by remember(editingWorkout) { mutableStateOf(editingWorkout?.caloriesBurned?.toString() ?: "200") }
    var notes by remember(editingWorkout) { mutableStateOf(editingWorkout?.notes ?: "") }
    
    // Notification & Recurring Logic
    var startTimeMillis by remember(editingWorkout) { mutableStateOf(editingWorkout?.startTimeMillis ?: 0L) }
    var recurringDays by remember(editingWorkout) { mutableStateOf(editingWorkout?.recurringDays ?: 0) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Handle notification permission result
    }

    // Hàm kiểm tra và xin quyền tổng thể
    fun checkAndRequestPermissions() {
        if (startTimeMillis > 0) {
            // 1. Xin quyền thông báo (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            
            // 2. Kiểm tra quyền báo thức chính xác (Android 14+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!alarmManager.canScheduleExactAlarms()) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    context.startActivity(intent)
                }
            }
        }
    }

    val timePickerState = rememberTimePickerState(
        initialHour = if (startTimeMillis > 0) {
            val cal = Calendar.getInstance().apply { timeInMillis = startTimeMillis }
            cal.get(Calendar.HOUR_OF_DAY)
        } else 8,
        initialMinute = if (startTimeMillis > 0) {
            val cal = Calendar.getInstance().apply { timeInMillis = startTimeMillis }
            cal.get(Calendar.MINUTE)
        } else 0
    )

    var showTimePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (editingWorkout == null) "Thêm Bài Tập Mới" else "Chỉnh Sửa Bài Tập",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tên bài tập") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Nhắc nhở & Lịch tập", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (startTimeMillis == 0L) "Chưa đặt giờ nhắc" 
                          else "Nhắc lúc: ${timePickerState.hour}:${String.format("%02d", timePickerState.minute)}"
                )
                Button(onClick = { 
                    showTimePicker = true 
                }) {
                    Text("Đặt giờ")
                }
            }

            if (showTimePicker) {
                TimePickerDialog(
                    onCancel = { showTimePicker = false },
                    onConfirm = {
                        val cal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        startTimeMillis = cal.timeInMillis
                        showTimePicker = false
                    }
                ) {
                    TimePicker(state = timePickerState)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Lặp lại hàng tuần:", style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val days = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                days.forEachIndexed { index, day ->
                    val isSelected = (recurringDays and (1 shl index)) != 0
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            recurringDays = if (isSelected) {
                                recurringDays and (1 shl index).inv()
                            } else {
                                recurringDays or (1 shl index)
                            }
                        },
                        label = { Text(day, fontSize = 10.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Phân loại (Cardio, Strength,...)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it.filter { c -> c.isDigit() } },
                    label = { Text("Phút") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it.filter { c -> c.isDigit() } },
                    label = { Text("Kcal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            if (editingWorkout != null || title.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Ghi chú chi tiết") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Hủy")
                }
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            checkAndRequestPermissions()
                            
                            onSave(
                                title.trim(),
                                category,
                                duration.toIntOrNull() ?: 30,
                                calories.toIntOrNull() ?: 200,
                                "Medium",
                                notes.trim(),
                                startTimeMillis,
                                recurringDays
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (editingWorkout == null) "Thêm" else "Lưu")
                }
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = { TextButton(onClick = onConfirm) { Text("OK") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Hủy") } },
        text = { content() }
    )
}
