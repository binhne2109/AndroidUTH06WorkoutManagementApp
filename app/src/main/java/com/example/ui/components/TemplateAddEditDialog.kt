package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.WorkoutTemplate

/**
 *  Biểu mẫu để tạo mới hoặc chỉnh sửa một Mẫu bài tập (Template).
 * Lưu 4 trường tĩnh: Tên, Loại, Địa điểm, Thời lượng — đúng theo mô tả nhiệm vụ.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateAddEditSheet(
    editingTemplate: WorkoutTemplate?,
    onDismiss: () -> Unit,
    onSave: (name: String, category: String, location: String, durationMinutes: Int) -> Unit
) {
    var name by remember(editingTemplate) { mutableStateOf(editingTemplate?.name ?: "") }
    var category by remember(editingTemplate) { mutableStateOf(editingTemplate?.category ?: "Strength") }
    var location by remember(editingTemplate) { mutableStateOf(editingTemplate?.location ?: "") }
    var duration by remember(editingTemplate) { mutableStateOf(editingTemplate?.durationMinutes?.toString() ?: "30") }

    val locationSuggestions = listOf("Phòng gym", "Ở nhà", "Công viên", "Hồ bơi")

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (editingTemplate == null) "Tạo Mẫu Bài Tập" else "Chỉnh Sửa Mẫu Bài Tập",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Lưu cấu hình để tái sử dụng, giúp tạo bài tập mới nhanh hơn.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên bài tập") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Loại (Strength, Cardio, HIIT, Yoga,...)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Địa điểm") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                locationSuggestions.forEach { suggestion ->
                    SuggestionChip(
                        onClick = { location = suggestion },
                        label = { Text(suggestion) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it.filter { c -> c.isDigit() } },
                label = { Text("Thời lượng (phút)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Hủy")
                }
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(
                                name.trim(),
                                category.trim(),
                                location.trim(),
                                duration.toIntOrNull() ?: 30
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (editingTemplate == null) "Lưu mẫu" else "Cập nhật")
                }
            }
        }
    }
}
