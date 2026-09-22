package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.PlanItem
import com.example.data.model.WorkoutPlan
import com.example.data.model.WorkoutTemplate

/**
 * Biểu mẫu tạo/sửa Kế hoạch tập: đặt tên, mô tả, và xây dựng
 * chuỗi bài tập có thứ tự (thêm mới, thêm nhanh từ Mẫu bài tập, sắp xếp lại,
 * xóa từng bài) trước khi lưu thành một WorkoutPlan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanEditorSheet(
    editingPlan: WorkoutPlan?,
    draftItems: List<PlanItem>,
    availableTemplates: List<WorkoutTemplate>,
    onDismiss: () -> Unit,
    onAddItem: (title: String, category: String, location: String, durationMinutes: Int) -> Unit,
    onAddFromTemplate: (WorkoutTemplate) -> Unit,
    onRemoveItem: (index: Int) -> Unit,
    onMoveItem: (fromIndex: Int, toIndex: Int) -> Unit,
    onSave: (name: String, description: String) -> Unit
) {
    var name by remember(editingPlan) { mutableStateOf(editingPlan?.name ?: "") }
    var description by remember(editingPlan) { mutableStateOf(editingPlan?.description ?: "") }

    var itemTitle by remember { mutableStateOf("") }
    var itemCategory by remember { mutableStateOf("Strength") }
    var itemLocation by remember { mutableStateOf("") }
    var itemDuration by remember { mutableStateOf("15") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (editingPlan == null) "Tạo Kế Hoạch Tập" else "Chỉnh Sửa Kế Hoạch Tập",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Sắp xếp một chuỗi bài tập liên tiếp, có tổ chức.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên kế hoạch (VD: Ngày Chân)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả (không bắt buộc)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Chuỗi bài tập (${draftItems.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (draftItems.isEmpty()) {
                Text(
                    text = "Chưa có bài tập nào. Thêm mới hoặc dùng mẫu bên dưới.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            draftItems.forEachIndexed { index, item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${index + 1}. ${item.title}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = buildString {
                                    if (item.category.isNotBlank()) append(item.category)
                                    append(" • ${item.durationMinutes} phút")
                                    if (item.location.isNotBlank()) append(" • ${item.location}")
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { if (index > 0) onMoveItem(index, index - 1) },
                            enabled = index > 0
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Di chuyển lên")
                        }
                        IconButton(
                            onClick = { if (index < draftItems.lastIndex) onMoveItem(index, index + 1) },
                            enabled = index < draftItems.lastIndex
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Di chuyển xuống")
                        }
                        IconButton(onClick = { onRemoveItem(index) }) {
                            Icon(Icons.Default.Close, contentDescription = "Xóa khỏi kế hoạch", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            if (availableTemplates.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Thêm nhanh từ mẫu có sẵn",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRowSimple {
                    availableTemplates.forEach { template ->
                        SuggestionChip(
                            onClick = { onAddFromTemplate(template) },
                            label = { Text(template.name) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Hoặc thêm bài tập tùy chỉnh",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = itemTitle,
                onValueChange = { itemTitle = it },
                label = { Text("Tên bài tập") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = itemCategory,
                onValueChange = { itemCategory = it },
                label = { Text("Loại") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = itemLocation,
                    onValueChange = { itemLocation = it },
                    label = { Text("Địa điểm") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = itemDuration,
                    onValueChange = { itemDuration = it.filter { c -> c.isDigit() } },
                    label = { Text("Phút") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    if (itemTitle.isNotBlank()) {
                        onAddItem(itemTitle.trim(), itemCategory.trim(), itemLocation.trim(), itemDuration.toIntOrNull() ?: 15)
                        itemTitle = ""
                        itemLocation = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Thêm vào chuỗi")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Hủy")
                }
                Button(
                    onClick = { if (name.isNotBlank() && draftItems.isNotEmpty()) onSave(name.trim(), description.trim()) },
                    modifier = Modifier.weight(1f),
                    enabled = name.isNotBlank() && draftItems.isNotEmpty()
                ) {
                    Text(if (editingPlan == null) "Lưu kế hoạch" else "Cập nhật")
                }
            }
        }
    }
}

/** Bố cục chip cuộn ngang đơn giản, tránh phụ thuộc thêm thư viện FlowRow. */
@Composable
private fun FlowRowSimple(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}
