package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.WorkoutPlan

/**
 *  Hiển thị một Kế hoạch tập: tên, mô tả, tổng số bài tập/thời
 * lượng, danh sách bài tập theo đúng thứ tự, và nút "Bắt đầu" để tạo hàng
 * loạt bài tập thật vào nhật ký theo đúng trình tự đã lưu.
 */
@Composable
fun PlanCard(
    plan: WorkoutPlan,
    onStartClick: (WorkoutPlan) -> Unit,
    onEditClick: (WorkoutPlan) -> Unit,
    onDeleteClick: (WorkoutPlan) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = { onEditClick(plan) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Sửa kế hoạch", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { onDeleteClick(plan) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa kế hoạch", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (plan.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = plan.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("${plan.totalExercises} bài tập") },
                    leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text("${plan.totalDurationMinutes} phút") },
                    leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                plan.orderedItems.forEachIndexed { index, item ->
                    Text(
                        text = "${index + 1}. ${item.title} • ${item.durationMinutes} phút" +
                                if (item.location.isNotBlank()) " • ${item.location}" else "",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onStartClick(plan) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Bắt đầu kế hoạch")
            }
        }
    }
}
