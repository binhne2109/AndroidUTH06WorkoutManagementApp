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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutAddEditSheet(
    editingWorkout: WorkoutEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, duration: Int, calories: Int, intensity: String, notes: String) -> Unit
) {
    var title by remember(editingWorkout) { mutableStateOf(editingWorkout?.title ?: "") }
    var category by remember(editingWorkout) { mutableStateOf(editingWorkout?.category ?: "Strength") }
    var duration by remember(editingWorkout) { mutableStateOf(editingWorkout?.durationMinutes?.toString() ?: "30") }
    var calories by remember(editingWorkout) { mutableStateOf(editingWorkout?.caloriesBurned?.toString() ?: "200") }
    var notes by remember(editingWorkout) { mutableStateOf(editingWorkout?.notes ?: "") }

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
                        if (title.isNotBlank()) {
                            onSave(
                                title.trim(),
                                category,
                                duration.toIntOrNull() ?: 30,
                                calories.toIntOrNull() ?: 200,
                                "Medium",
                                notes.trim()
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