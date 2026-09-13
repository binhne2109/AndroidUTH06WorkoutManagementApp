package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.WorkoutEntity
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutAddEditSheet(
    editingWorkout: WorkoutEntity?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        category: String,
        duration: Int,
        calories: Int,
        intensity: String,
        notes: String,
        dateMillis: Long,
        startTime: Long,
        endTime: Long
    ) -> Unit
) {
    var title by remember(editingWorkout) { mutableStateOf(editingWorkout?.title ?: "") }
    var category by remember(editingWorkout) { mutableStateOf(editingWorkout?.category ?: "Strength") }
    var duration by remember(editingWorkout) { mutableStateOf(editingWorkout?.durationMinutes?.toString() ?: "30") }
    var calories by remember(editingWorkout) { mutableStateOf(editingWorkout?.caloriesBurned?.toString() ?: "200") }
    var notes by remember(editingWorkout) { mutableStateOf(editingWorkout?.notes ?: "") }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedStartTime by remember { mutableStateOf(LocalTime.of(18, 0)) }
    var selectedEndTime by remember { mutableStateOf(LocalTime.of(19, 0)) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var scheduleError by remember { mutableStateOf<String?>(null) }
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Phân loại (Strength, Cardio, HIIT, Yoga,...)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it.filter { c -> c.isDigit() } },
                    label = { Text("Thời lượng (phút)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it.filter { c -> c.isDigit() } },
                    label = { Text("Calo (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = "Ngày tập",
                modifier = Modifier.padding(top = 8.dp)
            )

            OutlinedButton(
                onClick = {
                    showDatePicker = true
                    scheduleError = null
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedDate.format(dateFormatter))
            }

            Text(
                text = "Giờ bắt đầu",
                modifier = Modifier.padding(top = 8.dp)
            )

            OutlinedButton(
                onClick = {
                    showStartTimePicker = true
                    scheduleError = null
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedStartTime.format(timeFormatter))
            }

            Text(
                text = "Giờ kết thúc",
                modifier = Modifier.padding(top = 8.dp)
            )

            OutlinedButton(
                onClick = {
                    showEndTimePicker = true
                    scheduleError = null
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedEndTime.format(timeFormatter))
            }

            if (scheduleError != null) {
                Text(
                    text = scheduleError!!,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Ghi chú chi tiết") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Hủy")
                }
                Button(
                    onClick = {
                        val startMillis = selectedDate
                            .atTime(selectedStartTime)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()

                        val endMillis = selectedDate
                            .atTime(selectedEndTime)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()

                        if (title.isBlank()) {
                            scheduleError = "Vui lòng nhập tên bài tập"
                        } else if (endMillis <= startMillis) {
                            scheduleError = "Giờ kết thúc phải sau giờ bắt đầu"
                        } else {
                            scheduleError = null

                            val dateMillis = selectedDate
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli()

                            onSave(
                                title.trim(),
                                category,
                                duration.toIntOrNull() ?: 30,
                                calories.toIntOrNull() ?: 200,
                                "Medium",
                                notes.trim(),
                                dateMillis,
                                startMillis,
                                endMillis
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (editingWorkout == null) "Thêm" else "Lưu")
                }
            }
            if (showDatePicker) { // hop chon ngay
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = selectedDate
                        .atStartOfDay(ZoneId.of("UTC"))
                        .toInstant()
                        .toEpochMilli()
                )

                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val selectedMillis = datePickerState.selectedDateMillis

                                if (selectedMillis != null) {
                                    selectedDate = Instant
                                        .ofEpochMilli(selectedMillis)
                                        .atZone(ZoneId.of("UTC"))
                                        .toLocalDate()
                                }

                                showDatePicker = false
                            }
                        ) {
                            Text("Xác nhận")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDatePicker = false
                            }
                        ) {
                            Text("Hủy")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
            if (showStartTimePicker) { // hop chon gio bat dau
                val timePickerState = rememberTimePickerState(
                    initialHour = selectedStartTime.hour,
                    initialMinute = selectedStartTime.minute,
                    is24Hour = true
                )

                AlertDialog(
                    onDismissRequest = { showStartTimePicker = false },
                    title = { Text("Chọn giờ bắt đầu") },
                    text = { TimePicker(state = timePickerState) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                selectedStartTime = LocalTime.of(
                                    timePickerState.hour,
                                    timePickerState.minute
                                )
                                showStartTimePicker = false
                                scheduleError = null
                            }
                        ) {
                            Text("Xác nhận")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showStartTimePicker = false
                            }
                        ) {
                            Text("Hủy")
                        }
                    }

                )
            }
            if (showEndTimePicker) { // hop chon gio ket thuc
                val timePickerState = rememberTimePickerState(
                    initialHour = selectedEndTime.hour,
                    initialMinute = selectedEndTime.minute,
                    is24Hour = true
                )

                AlertDialog(
                    onDismissRequest = {
                        showEndTimePicker = false
                    },
                    title = {
                        Text("Chọn giờ kết thúc")
                    },
                    text = {
                        TimePicker(state = timePickerState)
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                selectedEndTime = LocalTime.of(
                                    timePickerState.hour,
                                    timePickerState.minute
                                )
                                showEndTimePicker = false
                                scheduleError = null
                            }
                        ) {
                            Text("Xác nhận")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showEndTimePicker = false
                            }
                        ) {
                            Text("Hủy")
                        }
                    }
                )
            }
        }
    }
}