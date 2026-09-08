package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.WorkoutTemplate

/**
 *  Luồng AUTO-FILL: khi người dùng chọn một Mẫu bài tập,
 * sheet này mở ra với dữ liệu đã được tự động khởi tạo sẵn từ mẫu
 * (Tên, Loại, Thời lượng, Địa điểm -> đưa vào Ghi chú), người dùng chỉ
 * cần xem lại / chỉnh nếu cần rồi xác nhận để tạo bài tập thật.
 *
 * `onConfirm` có cùng chữ ký với WorkoutViewModel.saveWorkout(...) nên màn
 * hình chỉ cần bind thẳng vào đó, không cần sửa WorkoutViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyTemplateSheet(
    template: WorkoutTemplate,
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, duration: Int, calories: Int, intensity: String, notes: String) -> Unit
) {
    // Auto-fill: khởi tạo toàn bộ state từ dữ liệu của mẫu ngay khi sheet mở ra
    var title by remember(template) { mutableStateOf(template.name) }
    var category by remember(template) { mutableStateOf(template.category) }
    var duration by remember(template) { mutableStateOf(template.durationMinutes.toString()) }
    var calories by remember(template) { mutableStateOf("200") }
    var notes by remember(template) {
        mutableStateOf(if (template.location.isNotBlank()) "Địa điểm: ${template.location}" else "")
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tạo từ mẫu \"${template.name}\"",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Dữ liệu đã được tự động điền từ mẫu, bạn có thể chỉnh lại trước khi lưu.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                label = { Text("Phân loại") },
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
                label = { Text("Ghi chú (địa điểm, chi tiết...)") },
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
                            onConfirm(
                                title.trim(),
                                category.trim(),
                                duration.toIntOrNull() ?: template.durationMinutes,
                                calories.toIntOrNull() ?: 200,
                                "Medium",
                                notes.trim()
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Thêm bài tập")
                }
            }
        }
    }
}
